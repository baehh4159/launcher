# PRD — 책상 런처 (Desk Launcher)

> 휴대폰 홈 화면을 '내 책상'처럼 쓰는 안드로이드 기본 런처 겸 올인원 생산성 앱

| 항목 | 내용 |
|---|---|
| 문서 상태 | v0.1 초안 (구현 전) |
| 기준 기기 | Samsung Galaxy S24+ (6.7", 기본 FHD+ 1080×2340 ≈ 384×832dp, 120Hz) |
| 기준 OS | Android 14 (One UI 6.x) / Android 15 (One UI 7) — Android 16 호환 확인 |
| 플랫폼 | Android 전용, 폰 세로 모드 전용 (MVP) |
| 규칙 파일 | [`AGENTS.md`](./AGENTS.md) — 코드 작업 시 반드시 함께 따른다 |

---

## 1. 제품 개요

### 1.1 문제
- 홈 화면(런처)과 생산성 도구(할일·메모·캘린더·스캐너 등)가 서로 다른 앱으로 흩어져 있어, 잠깐 기록하려 해도 "앱 찾기 → 실행 → 화면 이동"을 반복해야 한다.
- 6.7" 대화면에서 화면 상단·좌측 요소는 한 손(오른손 엄지)으로 닿기 어렵다.

### 1.2 해결 방향
- **런처 자체가 책상**이 된다. 홈 버튼을 누르면 곧바로 내 책상이 나온다.
- 화면 **좌측에 서류철(인덱스 탭)이 세로로 꽂혀** 있고, **오른손 엄지로 화면 오른쪽을** 위아래로 훑으면 그 높이의 서류철이 왼쪽에서 '툭' 튀어나오며 서류철이 **'드르륵' 넘어가며**(물리 애니메이션 + 촉각 햅틱) 선택된다. 손을 떼면 해당 서류철(도구)이 펼쳐진다.
- 책상 위 도구(할일, 캘린더, 메모 등)는 **플러그인 레지스트리**로 관리되어 '내 상점(도구함)'에서 켜고 끌 수 있고, 새 도구를 모듈 하나로 추가할 수 있다.

### 1.3 목표 / 비목표
| 목표 (MVP) | 비목표 (MVP 제외) |
|---|---|
| 기본 런처(HOME)로 매일 사용 가능한 안정성 | 클라우드 동기화, 계정/로그인 |
| 한 손 서류철 스크럽 인터랙션의 완성도 | 홈 화면 위젯(AppWidgetHost) |
| 9개 내장 도구의 핵심 기능 | 원격 다운로드형 플러그인, 결제/유료 상점 |
| | 데이터 백업/내보내기(JSON/ZIP) — P2, §5.11 |
| 완전 오프라인 동작 (앱 기능에 네트워크 미사용) | 태블릿·폴더블·가로 모드 최적화 |
| 도구 켜기/끄기·순서 변경 | 아이콘 팩, 알림 배지(NotificationListener) |

### 1.4 성공 기준 (MVP 완료 판단)
1. 기본 런처로 지정 후 **2주간 일상 사용 시 크래시 0건**, 홈 버튼 → 책상 표시가 체감 지연 없음.
2. 서류철 스크럽 중 **120Hz에서 프레임 드랍이 체감되지 않음** (GPU 렌더링 프로파일 기준 대부분 프레임 8.3ms 이내).
3. 스크럽 시 탭 경계마다 햅틱이 **누락·중복 없이** 1회씩 발생.
4. 9개 내장 도구 각각의 MVP 기능(§5)이 동작하고, 앱 재설치 전까지 데이터 유실 0건(DB 마이그레이션 포함).

---

## 2. 기술 스택 (고정)

| 구분 | 선택 | 비고 |
|---|---|---|
| 언어 | Kotlin 2.x | Compose Compiler Gradle 플러그인 사용 |
| UI | Jetpack Compose + Material 3 | Compose BOM으로 버전 일괄 관리 |
| 로컬 DB | Room (KSP) | 오프라인 퍼스트, 단일 DB, `exportSchema = true` |
| 문서 스캐너 | Google ML Kit Document Scanner API | `play-services-mlkit-document-scanner`, 카메라 권한 불필요 |
| 아키텍처 | Clean Architecture + 기능별 모듈 | §7 참고 |
| DI / 내비게이션 / 설정 저장 | Hilt / Navigation Compose / DataStore | 허용 목록은 `AGENTS.md` §3 |
| SDK | minSdk 34, targetSdk 36, compileSdk 37.2 | Android 14 미만 미지원 (S24+ 기준). targetSdk는 Play 정책 요구 수준에 맞춰 조정. compileSdk는 최신 AndroidX가 37.1 이상을 요구해 37.2 (컴파일용 API 수준일 뿐, 런타임 동작은 targetSdk가 결정 — §13) |
| 빌드 | AGP 9.5 (내장 Kotlin), Gradle 9.9, JDK 17+ | 공통 설정은 `build-logic/` 컨벤션 플러그인, 버전은 `gradle/libs.versions.toml` |

> 위 목록 외 라이브러리는 `AGENTS.md`의 **임의 라이브러리 추가 금지** 규칙에 따라 사용자 승인 후에만 추가한다.

---

## 3. 핵심 인터랙션: 서류철 인덱스 탭 (Index Tab Strip)

앱의 정체성이 걸린 기능이므로 가장 먼저, 가장 꼼꼼하게 만든다.

### 3.1 레이아웃
- 화면 좌측 가장자리에 도구별 **인덱스 탭**이 세로로 꽂혀 있다. 엄지는 **화면 오른쪽의 보이지 않는 훑는 영역**(가장자리 32dp 안쪽, 폭 64dp, 표시 없음)에서 위아래로 움직이고, 같은 높이의 탭이 화면 안쪽(오른쪽)으로 '툭' 튀어나온다. 탭을 직접 눌러도 열린다 (실제 서류철처럼 위 탭이 아래 탭을 살짝 덮음).
- 기본 배치 영역: 화면 높이의 **하단 약 2/3 (엄지 도달 영역)**. 설정에서 그립 위치(상·중·하)를 바꿀 수 있다.
- 탭 하나: 기본 두께 28~36dp(겹침), 포커스 시 56dp 이상으로 펼쳐짐. 탭에는 도구 색상 + 세로 라벨(또는 아이콘).
- 도구 수가 많아 영역을 넘으면 탭 간격을 압축한다(최소 간격 이하로는 줄이지 않고, 포커스 주변만 펼침 — 어안(fisheye) 효과).

### 3.2 상태 머신
```
Idle ──(탭 영역 터치 다운)──▶ Scrubbing ──(손 뗌, 속도 낮음)──▶ Settling ──▶ Opening ──▶ ToolOpen
  ▲                              │  ▲                                            │
  │                              │  └──(다시 터치)── Flinging ◀─(손 뗌, 속도 높음)─┘(X)
  │                              └──(영역 밖으로 크게 이탈 / 취소)──▶ Idle
  └──────────────────(뒤로가기 / 홈 버튼)──────────────────────────────────── ToolOpen
```
| 상태 | 동작 |
|---|---|
| Idle | 탭이 가장자리에 살짝만 보임. 책상 위에는 시계/날짜 + (P1) 오늘 요약 |
| Scrubbing | 손가락 Y좌표 → 연속값 `focus: Float`(탭 인덱스)로 매핑. 포커스 탭이 왼쪽으로 튀어나오고 주변 탭은 거리에 따라 점점 덜 튀어나옴. 책상 위에 포커스 도구의 미리보기(DeskPreview) 표시 |
| Flinging | 손을 빠르게 튕기고 떼면 `focus`가 감쇠(decay) 애니메이션으로 계속 넘어가며 경계마다 햅틱 |
| Settling | 가장 가까운 정수 인덱스로 스프링 스냅 |
| Opening | 선택된 서류철이 왼쪽에서 책상 위로 '꺼내져' **팝업 창**으로 펼쳐지는 전환(슬라이드+스케일, 뒤 책상은 어두운 막) |
| ToolOpen | 팝업 창 안에 도구 표시(창 둘레로 책상이 보임). 막 누르기·창 빈 곳 오른쪽 밀기(30% 이상 또는 빠른 튕김, 끄는 동안 창이 손가락을 따라 이동·축소)·뒤로가기(도구 첫 화면)·홈 버튼 시 창을 왼쪽으로 다시 꽂는 역방향 전환 |

- **탭 단순 터치(Tap)**: 스크럽 없이 탭을 누르면 해당 도구를 바로 연다.
- **길게 누르기**: (P1) 빠른 메뉴 — "빠른 추가", "도구함에서 편집".

### 3.3 물리 애니메이션
- 포커스 이동: 손가락 추적 중에는 1:1 추적(지연 없음), 손을 떼면 Compose `Animatable` + 감쇠(`splineBasedDecay`) → 스프링 스냅.
- 탭 튀어나옴 오프셋: `pop(i) = maxPop × exp(-(i - focus)² / (2σ²))` 형태의 연속 함수 (σ, maxPop은 튜닝 상수).
- 포커스 위쪽 탭은 살짝 뒤로 젖혀지고(회전/스케일 축소) 아래쪽 탭은 앞으로 기우는 느낌 → "넘겨지는" 감각.
- 첫/마지막 탭을 넘어서 당기면 고무줄 저항(overscroll) 후 되돌아옴.
- 모든 튜닝 상수(스프링 감쇠비·강성, σ, maxPop, 탭 두께)는 `core:designsystem`의 `DeskMotion` 한 곳에 모은다.
- 시스템 설정 "애니메이션 제거"(animator duration scale = 0)가 켜져 있으면 전환 애니메이션을 생략하고 즉시 상태 전환.

### 3.4 햅틱 (촉각 피드백)
| 이벤트 | 1순위 | 대체(fallback) |
|---|---|---|
| 탭 경계 통과 | `PRIMITIVE_THUD` 0.55 (빠른 연속 0.45) — 짧고 둔탁하게 | `PRIMITIVE_LOW_TICK` 0.8 → 단발 10ms |
| 첫/마지막 탭 도달(벽) | `PRIMITIVE_THUD` 0.7 | `PRIMITIVE_LOW_TICK` 1.0 → 단발 10ms |
| 서류철 열기 확정 | `PRIMITIVE_THUD` 0.92 — 책을 툭 내려놓는 묵직함 | `PRIMITIVE_LOW_TICK` 1.0 → 단발 24ms |

- 2026-11-27 리디자인: 가볍고 날카로운 틱 대신 S24+ 리니어 모터의 THUD로 '책' 질감. 모든 진동은 `USAGE_TOUCH`(시스템 터치 진동 세기 반영), 설정 '약하게'는 ×0.6.
- 잔향 방지: 넘김 최소 간격 42ms(THUD 꼬리보다 길게), 90ms 안에 연속이면 약한 넘김, 확정 뒤 160ms는 넘김 진동 없음. 값은 `DeskMotion`, 선택 로직은 `DeskHapticSpec`(순수·테스트).
- 정수 인덱스 경계를 **넘는 순간에만** 1회 발생 (같은 경계를 떨림으로 왕복할 때 중복 방지를 위해 히스테리시스 적용).
- 고속 플링 시 최소 발생 간격(예: 15~20ms) 제한 — 진동이 뭉개지지 않게.
- Composition primitive는 기기마다 지원이 다르므로 `Vibrator.areAllPrimitivesSupported()` 확인 후 사용.
- 햅틱 강도: 설정에서 끔 / 약 / 기본. 시스템 "터치 피드백" 설정을 존중한다.
- 햅틱 로직은 `core:designsystem`의 `DeskHaptics` 래퍼로만 호출 (도구 모듈에서 Vibrator 직접 사용 금지).

### 3.5 제스처 충돌 대응 (Galaxy 특이사항 — 반드시 실기기 검증)
- **제스처 내비게이션(삼성 '스와이프 제스처')의 양쪽 가장자리 '뒤로 가기' 스와이프**와 겹친다.
  - 탭의 *시각적* 위치는 가장자리지만, *터치 시작 영역*은 가장자리에서 약 16~24dp 안쪽부터 받도록 설계.
  - 그립 영역에 `Modifier.systemGestureExclusion()` 적용. 단, 시스템이 한쪽 가장자리당 **최대 200dp 높이**만 허용하므로 그립 영역(엄지 시작 지점)에만 적용하고, 스크럽이 시작된 뒤에는 영역 밖으로 이동해도 계속 추적.
- **One UI 엣지 패널 핸들**(기본 우측)과 겹칠 수 있음 → 온보딩에서 안내, 그립 위치 설정으로 회피.
- 버튼 내비게이션 사용자는 충돌 없음.

### 3.6 접근성
- TalkBack: 탭 스트립은 "도구 목록"으로 노출, 각 탭은 선택 가능한 항목(이름·순서·상태 낭독). 스크럽 대신 탭/더블탭으로 열기.
- 사용자 지정 접근성 동작: "다음 서류철", "이전 서류철".
- 글꼴 크기 200%에서도 라벨이 잘리지 않도록 세로 라벨 → 아이콘 전환.

---

## 4. 화면 구조

### 4.1 내비게이션 맵
```
MainActivity (HOME, singleTask, edge-to-edge)
└─ NavHost
   ├─ onboarding           최초 실행: 기본 런처 지정 / 권한 안내 / 제스처 충돌 안내
   ├─ desk  ★start         책상 메인 (인덱스 탭 스트립 + 책상 위 영역)
   │   ├─ (위로 스와이프) → apps
   │   └─ (탭 선택)       → tool/{toolId}/…
   ├─ apps                 앱 서랍(서랍장)
   ├─ store                내 상점(도구함)
   ├─ settings             설정
   └─ tool/{toolId}/…      각 DeskTool이 등록한 중첩 그래프
       ├─ todo / calendar / memo / diary / cabinet
       └─ calculator / doodle / alarm / files

AlarmRingActivity (별도, 잠금화면 위 표시 · 화면 켜기)  ← feature:alarm 소유
```
- 단일 Activity. 예외는 잠금화면 위에 떠야 하는 알람 울림 화면만.
- **홈 버튼 동작**: 어떤 화면에 있든 HOME 인텐트(`onNewIntent`) 수신 시 열린 도구를 닫고 `desk`로 복귀 (이미 `desk`면 스트립을 Idle로 리셋).
- 프로세스 종료 후 복원: `SavedStateHandle`로 현재 화면/입력 중 텍스트 보존.

### 4.2 책상 메인 (desk)
```
┌───────────────────────────────────┐
│ (상태바 - edge-to-edge)            │
│                                   │
│   10:24                           │
│   11월 26일 목요일                 │
│                                   │
│  ┌─────────────────────┐   ┌──┐  │
│  │ 책상 위               │   │할│  │
│  │ - Idle: 오늘 요약(P1) │   │캘│  │
│  │ - Scrub: 포커스 도구  │  ◀┤메├  │ ← 포커스 탭 (튀어나옴)
│  │   미리보기(DeskPreview)│   │일│  │
│  └─────────────────────┘   │서│  │
│                            │계│  │
│                            │낙│  │
│                            │알│  │
│                            │파│  │
│      ︿ 위로 밀어 앱 서랍     └──┘  │
└───────────────────────────────────┘
```
- 배경: 나무/종이 질감의 책상 표면 (Material 3 색 체계 위에 질감 레이어, 다크 모드 대응).
- 빈 상태: 켜진 도구가 0개이면 "도구함에서 책상 도구를 꺼내 보세요" + 도구함 이동 버튼.

### 4.3 앱 서랍 (apps)
- 여는 법: 서류철 스트립 **맨 끝의 '앱 서랍' 서류철**(도구함에서 끌 수 없음, 도구가 하나도 없어도 꽂혀 있음) 또는 책상 위로 밀기. 도구처럼 **팝업 창**으로 펼쳐진다 (2026-11-27 실기기 피드백).
- 설치 앱 목록(가나다/ABC 순) + 상단 검색창(자동 포커스 옵션).
- 우측에 **초성/알파벳 인덱스 스트립** — 서류철 스트립과 같은 스크럽 컴포넌트·햅틱을 재사용 (ㄱ ㄴ ㄷ … A B C … #).
  - 글자·터치 영역은 가장자리에서 32dp 안쪽부터 시작한다. 가장자리 띠는 삼성 '스와이프 제스처' 뒤로가기 몫으로 비워 둔다 (2026-11-27 실기기 피드백).
- 앱 길게 누르기: 앱 정보, 숨기기, 즐겨찾기(상단 고정), 제거.
- 데이터: `LauncherApps` API로 조회, `LauncherApps.Callback`으로 설치/삭제 실시간 반영.

### 4.4 내 상점 / 도구함 (store)
- 레지스트리에 등록된 모든 도구 목록: 아이콘, 이름, 설명, 켜기/끄기 스위치.
- 드래그로 순서 변경(= 인덱스 탭 순서), 탭 색상 변경(팔레트에서 선택).
- "상점"은 결제가 아닌 **책상 도구 진열장** 개념. 원격 다운로드는 없음(MVP).

### 4.5 설정 (settings)
- 기본 런처 지정(`RoleManager.ROLE_HOME` 요청) / 현재 상태 표시
- 햅틱 강도, 그립 위치, (P2) 왼손 모드, (P2) 넘김 효과음
- 테마: 시스템 / 라이트 / 다크
- 권한 상태: 알림, 정확한 알람, 전체 화면 알림
- 앱 정보·오픈소스 라이선스

### 4.6 도구 화면 공통 프레임
- 상단: 서류철 헤더(도구 색상의 탭 모양 상단 바) + 제목 + 도구별 액션.
- 하단 우측: 주 동작 FAB (예: 할일 추가, 서류 스캔) — 엄지 영역 우선.
- 뒤로가기: 도구 내부 스택 → 마지막이면 서류철 닫힘 전환 후 `desk`.

---

## 5. 기능 명세 (MVP 범위)

> 우선순위: **P0** = MVP 필수 / **P1** = MVP 내 여유 시 / **P2** = MVP 이후.
> 각 기능 ID는 `AGENTS.md`의 "한 번에 한 기능만 수정" 규칙에서 작업 단위로 사용한다.

### 5.1 셸 (런처 기본 기능) — `feature:desk`, `feature:apps`, `feature:store`, `feature:settings`
| ID | 기능 | 우선 |
|---|---|---|
| SHELL-01 | HOME 인텐트 필터 등록, 기본 런처 지정 플로우(RoleManager) | P0 |
| SHELL-02 | 책상 메인 화면 + 시계/날짜 | P0 |
| SHELL-03 | 인덱스 탭 스트립: 레이아웃·스크럽·플링·스냅 (§3) | P0 |
| SHELL-04 | 스트립 햅틱 (§3.4) | P0 |
| SHELL-05 | 서류철 열기/닫기: 도구를 **책상 위 팝업 창**으로 펼침(왼쪽 서류철에서 꺼내지는 전환, 어두운 막, 막 누르기·창 빈 곳을 왼쪽→오른쪽으로 밀기·뒤로·홈으로 닫기), 창 안 예측형 뒤로가기 | P0 |
| SHELL-06 | 홈 버튼 시 책상 복귀 | P0 |
| SHELL-07 | 앱 서랍: 목록·검색·실행·초성 스트립. 서류철 맨 끝 '앱 서랍' 서류철(또는 책상 위로 밀기)로 열고, 도구와 같은 팝업 창에 뜬다 | P0 |
| SHELL-08 | 앱 숨기기/즐겨찾기/앱 정보/제거 | P1 |
| SHELL-09 | 도구 레지스트리 + 도구함(켜기/끄기·순서·색상) | P0 |
| SHELL-10 | 온보딩(기본 런처 지정·권한·제스처 충돌 안내) | P0 |
| SHELL-11 | 설정 화면 | P0 |
| SHELL-12 | 책상 위 '오늘 요약' (DeskSummary: 오늘 할일, 다음 일정, 다음 알람) | P1 |
| SHELL-13 | 스트립 좌/우 배치 설정(기본 좌측, SHELL-03), 넘김 효과음 | P2 |
| SHELL-14 | 플러그인 SDK: `plugins/` 폴더에 모듈을 넣으면 자동 등록(Gradle 자동 include·app 의존), `SimpleDeskTool`(화면 1개 플러그인), `DeskToolHost`(탭 색·닫기·전용 저장소), `plugin_data` 키-값 저장소, 잘못된 플러그인(id 형식·SDK 버전·중복)은 앱을 죽이지 않고 제외 (§6.5) | P0 |

### 5.2 할일정리 — `feature:todo` (toolId: `todo`)
| ID | 기능 | 우선 |
|---|---|---|
| TODO-01 | 할일 추가(빠른 입력 바)·수정·삭제(실행 취소 스낵바) | P0 |
| TODO-02 | 완료 체크(스와이프/체크박스), 완료 목록 분리 | P0 |
| TODO-03 | 필터: 오늘 / 예정 / 전체 / 완료 | P0 |
| TODO-04 | 마감일·시간, 우선순위(없음/낮음/보통/높음), 프로젝트 연결 | P0 |
| TODO-05 | 드래그 정렬 | P1 |
| TODO-06 | 마감 알림(알림 채널) | P2 |

### 5.3 캘린더 — `feature:calendar` (toolId: `calendar`)
| ID | 기능 | 우선 |
|---|---|---|
| CAL-01 | 월간 보기 + 선택일 일정 목록(아젠다) | P0 |
| CAL-02 | 일정 추가/수정/삭제 (제목·시작/종료·종일·장소·메모·색상) | P0 |
| CAL-03 | 마감일 있는 할일을 해당 날짜에 함께 표시(읽기 전용) | P0 |
| CAL-04 | 주간 보기 | P1 |
| CAL-05 | 반복 일정, 일정 알림 | P2 |
| CAL-06 | 기기 캘린더(CalendarContract) 읽기 연동 | P2 |

### 5.4 프로젝트 메모 — `feature:memo` (toolId: `memo`)
| ID | 기능 | 우선 |
|---|---|---|
| MEMO-01 | 프로젝트 목록(이름·색상) 생성/수정/보관/삭제 | P0 |
| MEMO-02 | 프로젝트별 메모 목록, 메모 작성/편집(자동 저장) | P0 |
| MEMO-03 | 메모 고정(핀), 제목·본문 검색 | P1 |
| MEMO-04 | 프로젝트 상세에 연결된 할일 표시 | P1 |

### 5.5 개인일기 — `feature:diary` (toolId: `diary`)
| ID | 기능 | 우선 |
|---|---|---|
| DIARY-01 | 날짜별 일기 1건 작성/편집(자동 저장), 기분(5단계) | P0 |
| DIARY-02 | 일기 목록(최신순) + 달력에서 작성일 표시 | P0 |
| DIARY-03 | 생체 인증 잠금 (androidx.biometric 승인 필요) | P2 |

### 5.6 서류정리함 + 문서 스캐너 — `feature:scanner` (toolId: `cabinet`)
| ID | 기능 | 우선 |
|---|---|---|
| DOC-01 | **+ 버튼 → ML Kit Document Scanner 실행** (전체 모드, 갤러리 가져오기 허용, 결과: PDF + 페이지 JPEG) | P0 |
| DOC-02 | 스캔 결과를 앱 내부 저장소에 PDF로 저장 + 첫 페이지 썸네일 생성 | P0 |
| DOC-03 | 폴더(1단계) 생성/이름 변경/삭제, 문서 이동·이름 변경·삭제 | P0 |
| DOC-04 | 문서 보기(`PdfRenderer` 페이지 뷰어), 공유(FileProvider) | P0 |
| DOC-05 | 외부 PDF 가져오기(SAF) | P1 |
| DOC-06 | 중첩 폴더, 태그 | P2 |

> ML Kit 스캐너는 Google Play 서비스가 제공하는 UI를 띄우므로 앱에 CAMERA 권한이 필요 없다. 모듈 미설치/다운로드 중 등의 실패 상태를 사용자에게 안내한다.

### 5.7 계산기 — `feature:calculator` (toolId: `calculator`)
| ID | 기능 | 우선 |
|---|---|---|
| CALC-01 | 사칙연산, %, 괄호, 부호 전환, 소수점, 실시간 미리보기 결과 | P0 |
| CALC-02 | 수식 파서는 직접 구현(외부 라이브러리 금지), `BigDecimal` 기반 정밀도 | P0 |
| CALC-03 | 계산 기록(최근 100건), 기록 탭하여 재사용 | P1 |

### 5.8 낙서장 — `feature:doodle` (toolId: `doodle`)
| ID | 기능 | 우선 |
|---|---|---|
| DOODLE-01 | Compose Canvas 자유 그리기: 펜/지우개, 색상 5종, 굵기 3단계 | P0 |
| DOODLE-02 | 실행 취소/다시 실행, 전체 지우기 | P0 |
| DOODLE-03 | 저장(획 데이터 + PNG 썸네일), 낙서 목록 | P0 |
| DOODLE-04 | PNG 내보내기/공유 | P1 |

### 5.9 알람 — `feature:alarm` (toolId: `alarm`)
| ID | 기능 | 우선 |
|---|---|---|
| ALARM-01 | 알람 목록, 추가/수정/삭제, 켜기/끄기 | P0 |
| ALARM-02 | 시각(M3 TimePicker), 반복 요일, 라벨, 진동, 다시 울림(분) | P0 |
| ALARM-03 | `AlarmManager.setAlarmClock()`으로 예약, 정확한 알람 권한 확인/요청 | P0 |
| ALARM-04 | 울림 화면(잠금화면 위, 화면 켜기): 끄기 / 다시 울림 | P0 |
| ALARM-05 | 재부팅·시간대 변경·앱 업데이트 후 재예약 | P0 |
| ALARM-06 | 벨소리 선택(시스템 RingtoneManager) | P1 |

### 5.10 내파일 — `feature:files` (toolId: `files`)
| ID | 기능 | 우선 |
|---|---|---|
| FILES-01 | 앱이 만든 파일 카테고리별 보기 (스캔 문서 / 낙서 이미지) | P0 |
| FILES-02 | 외부 파일 즐겨찾기: SAF(`ACTION_OPEN_DOCUMENT`) + 영구 URI 권한 | P0 |
| FILES-03 | 열기(외부 앱 인텐트), 공유, 목록에서 제거 | P0 |
| FILES-04 | 정렬(이름/날짜/크기), 검색 | P1 |

> 전체 저장소 접근(`MANAGE_EXTERNAL_STORAGE`)은 사용하지 않는다. 기기 전체 파일 탐색기가 아니라 '책상 위에 꺼내 둔 파일'을 모아 보는 개념.

### 5.11 백업/내보내기 — `feature:settings` (MVP 이후)
| ID | 기능 | 우선 |
|---|---|---|
| BACKUP-01 | 로컬 백업/내보내기: DB 데이터(JSON) + 내부 파일(PDF·이미지·획 데이터)을 ZIP 하나로 묶어 SAF로 저장, 같은 ZIP에서 복원 | P2 |

> 시스템 자동 백업은 사용하지 않는다(`allowBackup="false"`, §13). 사용자가 직접 만든 백업 파일만이 공식 백업 수단이다.

---

## 6. 플러그인 레지스트리 (도구 확장 구조)

### 6.1 개념
- 모든 책상 도구는 `core:tool-api`의 **`DeskTool` 계약**을 구현한 독립 feature 모듈이다.
- 각 모듈은 Hilt 멀티바인딩(`@IntoSet`)으로 자신을 등록한다. `app`은 `Set<DeskTool>`을 주입받아 조립만 한다.
- 셸(`feature:desk`)은 개별 도구를 **전혀 모르고** `DeskTool` 인터페이스만 안다.
- 컴파일 타임 레지스트리다. (원격 다운로드형 동적 플러그인은 P2 이후 검토)
- 사용자 플러그인은 `plugins/<id>/` 모듈로 만들며 Gradle이 자동 포함한다 (§6.5, `docs/PLUGIN_GUIDE.md`).

### 6.2 `DeskTool` 계약 (`core/tool-api/.../DeskTool.kt`가 기준)
| 멤버 | 설명 |
|---|---|
| `id: String` | 영구 불변 키 (`todo`, `calendar` …), 형식 `[a-z][a-z0-9_]{1,31}`. DB의 `tool_config.toolId`와 매칭. **한번 출시하면 변경 금지** |
| `apiVersion` (기본 = `DeskToolApi.VERSION`) | 플러그인이 맞춘 SDK 버전. 앱보다 새 버전이면 싣지 않음 |
| `title`, `description` | 문자열 리소스 ID |
| `icon` | 탭/도구함 아이콘 |
| `defaultTabColor` | 기본 탭 색상 (디자인시스템 팔레트 토큰) |
| `defaultEnabled`, `defaultOrder` | 최초 설치 시 기본값 |
| `startDestination` | 도구를 열 때 이동할 `@Serializable` 라우트 객체 (보통 도구의 중첩 그래프 라우트) |
| `NavGraphBuilder.toolGraph(navController)` | 자기 화면들을 중첩 그래프로 등록 (타입 안전 내비게이션). 다른 도구의 라우트는 등록하지 않음 |
| `DeskPreview(tabColor, modifier)` (선택) | 스크럽 중 책상 위에 보일 미리보기 Composable. 기본 구현 = `ToolPreviewCard`(아이콘 + 제목) |
| `DeskSummary()` (선택, P1) | Idle 상태 '오늘 요약' 카드 |
| `quickAction` (선택, P1) | 탭 길게 누르기 메뉴의 "빠른 추가" 동작 |

### 6.3 레지스트리 동작
- `ToolRegistry`(계약: `core:tool-api`, 구현: `core:tool-registry`)가 `Set<DeskTool>` × `tool_config` 테이블을 합쳐 `Flow<List<ActiveTool>>` 제공.
- 코드에 있는데 DB에 없는 도구 → 기본값으로 행 추가.
- DB에 있는데 코드에 없는 도구(삭제된 모듈) → 무시(행은 보존).
- 도구를 꺼도 그 도구의 데이터는 삭제하지 않는다.
- 검증: id 형식 위반·지원하지 않는 `apiVersion`·id 중복인 도구는 **크래시 없이 제외**하고 로그만 남긴다 (중복이면 클래스 이름순으로 먼저 온 것만). 런처가 죽으면 홈 화면이 사라지기 때문.

### 6.4 새 도구 추가 절차 (요약 — 상세 체크리스트는 `AGENTS.md` §6)
1. `feature/<name>` 모듈 생성 (기존 도구 모듈 구조 복제)
2. `DeskTool` 구현 + Hilt `@IntoSet` 바인딩
3. 필요 시 `core:database`에 Entity/DAO 추가 + **Room 마이그레이션 작성**
4. `settings.gradle.kts` 및 `app` 의존성에 모듈 추가
5. 도구함에서 자동 노출 확인

### 6.5 플러그인 SDK (SHELL-14)
| 구성 | 위치 | 설명 |
|---|---|---|
| `SimpleDeskTool` | `core:tool-api` | 화면 1개 플러그인 기반 클래스. `@Composable Content(host, modifier)`만 구현. 라우트는 앱의 공용 `SimpleToolDestination(toolId)` 하나가 모두 띄움 |
| `DeskToolHost` | `core:tool-api` | 앱이 플러그인에 빌려주는 것: `toolId`, `tabColor`, `storage`, `close()`. 멤버 추가는 기본 구현과 함께(하위 호환) |
| `PluginStorage` / `PluginStorageProvider` | 계약 `core:tool-api`, 구현 `core:tool-registry` | 플러그인별로 격리된 문자열 키-값. `plugin_data` 표(§8.3) |
| `DeskToolScaffold` | `core:tool-api` | 탭 색 헤더 + 책상으로 + 본문 |
| `PreviewDeskToolHost`, `InMemoryPluginStorage` | `core:tool-api` | Preview·단위 테스트용 |
| `desk.plugin` 컨벤션 | `build-logic` | feature와 같은 의존. 앱 내부 core 모듈(`database` 등)은 주지 않음 |
| 자동 포함 | `settings.gradle.kts`, `app/build.gradle.kts` | `plugins/<id>/build.gradle.kts`가 있으면 include + app 의존 |
| `scripts/new-plugin.sh` | | 뼈대 생성, id 형식·중복 검사 |
| 예시 | `plugins/water` | 물 마시기 기록 (기본 꺼짐) |

---

## 7. 아키텍처 / 모듈 구조

### 7.1 모듈
```
app/                          Application, MainActivity(HOME), NavHost 조립, Hilt 진입점
core/
  common/                     공용 유틸, Dispatcher 주입, 결과 타입, 시간 유틸
  designsystem/               테마(책상·종이 질감), 색/타이포 토큰, 공용 컴포넌트,
                              IndexScrubStrip(스크럽 컴포넌트), DeskMotion, DeskHaptics, @DeskPreviews
  tool-api/                   DeskTool 계약, ActiveTool 모델, ToolRegistry 인터페이스 (구현 없음)
  tool-registry/              ToolRegistry 구현 (Set<DeskTool> × tool_config). 셸과 도구함이 함께 쓰므로 core에 둔다
  database/                   Room DB, 모든 Entity·DAO·TypeConverter·Migration
  datastore/                  앱 설정(DataStore Preferences)
  storage/                    앱 내부 파일 저장소 경로·FileProvider 규약
  testing/                    테스트 공용 Fake, 테스트 Dispatcher 규칙
feature/
  desk/                       책상 메인, 인덱스 탭 스트립 화면
  apps/                       앱 서랍
  store/                      내 상점(도구함)
  settings/                   설정, 온보딩
  todo/  calendar/  memo/  diary/  scanner/(서류정리함)
  calculator/  doodle/  alarm/  files/
plugins/                      사용자 플러그인 (자동 포함, §6.5) — 예: water/
```

### 7.2 의존 규칙
```
app ──▶ feature:* ──▶ core:tool-api, core:designsystem, core:database, core:datastore, core:storage, core:common
                         (core 모듈끼리는 단방향, 순환 금지)
```
> M0 시점에 생성된 모듈: `app`, `core:{common, designsystem, tool-api, tool-registry, database}`, `feature:desk`. 나머지는 해당 기능 작업을 시작할 때 만든다 (`AGENTS.md` §6).

- **feature → feature 의존 금지.** 다른 도구의 데이터가 필요하면 `core:database`의 DAO를 *읽기 전용*으로 사용하고 자기 도메인 모델로 매핑한다 (예: 캘린더가 `TodoDao`로 마감일 할일 조회).
- `core:tool-api`는 Compose·Navigation 외 의존 없음.

### 7.3 feature 모듈 내부 계층 (Clean Architecture)
```
feature/todo/src/main/kotlin/…/todo/
  domain/   model/  repository/(인터페이스)  usecase/      ← 순수 Kotlin, Android 의존 금지
  data/     repository/(구현)  mapper/(Entity ↔ Domain)    ← core:database 사용
  ui/       TodoRoute.kt  TodoScreen.kt  TodoViewModel.kt  TodoUiState.kt  TodoAction.kt  component/
  di/       TodoModule.kt (Repository 바인딩), TodoToolModule.kt (DeskTool @IntoSet)
  TodoTool.kt                                                ← DeskTool 구현
```
- 방향: `ui → domain ← data`. `ui`는 `data`를 직접 참조하지 않는다.
- 상태 관리: 단방향 데이터 흐름(UDF). ViewModel이 `StateFlow<UiState>` 노출, UI는 `Action`을 전달.
- 화면 = 상태를 가진 `XxxRoute`(ViewModel 연결) + 상태 없는 `XxxScreen(state, onAction)` (Preview 대상).

---

## 8. 데이터 모델 (Room)

### 8.1 공통 규칙
- DB: 단일 `DeskDatabase` (`core:database`), `exportSchema = true`, 스키마 JSON은 `core/database/schemas/`에 커밋.
- **릴리스 빌드에서 `fallbackToDestructiveMigration` 금지** — 버전 업마다 마이그레이션 + 마이그레이션 테스트.
- PK: 사용자 콘텐츠는 `id: String` (UUID) — 향후 동기화 대비. 시스템 테이블은 자연키.
- 시간 저장: 시점(Instant) = `Long` epoch millis(UTC), 날짜(LocalDate) = `Long` epochDay, 시각(LocalTime) = `Int` 자정 기준 분(minuteOfDay). 표시할 때만 시스템 시간대로 변환.
- 열거형은 `String` 이름으로 저장 (순서 변경에 안전).
- 파일 본문(PDF, 이미지, 획 데이터)은 DB가 아니라 앱 내부 저장소에 두고 **상대 경로**만 저장.
- 모든 사용자 콘텐츠 테이블에 `createdAt`, `updatedAt`.

### 8.2 ER 개요
```
project 1 ── * memo            (CASCADE: 프로젝트 삭제 시 메모 삭제 — 확인 대화상자 필수)
project 1 ── * todo            (SET NULL)
document_folder 1 ── * document (SET NULL → 루트로 이동)
tool_config / app_pref / alarm / calendar_event / diary_entry / doodle / calc_history / file_bookmark : 독립
```

### 8.3 테이블 정의

**tool_config** — 도구함 상태
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| toolId | String | PK | `DeskTool.id` |
| enabled | Boolean | | 켜기/끄기 |
| sortOrder | Int | | 탭 순서 |
| tabColor | String? | | 사용자 지정 색상 토큰 (null = 기본) |

**app_pref** — 앱 서랍 개인화
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| componentKey | String | PK | `패키지명/액티비티명` (+ 사용자 프로필, P2) |
| hidden | Boolean | | 숨김 |
| favorite | Boolean | | 상단 고정 |
| launchCount | Int | | 실행 횟수 |
| lastLaunchedAt | Long? | | |

**todo**
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | String | PK | UUID |
| title | String | NOT NULL | |
| note | String | 기본 "" | |
| isDone | Boolean | INDEX(isDone, dueDate) | |
| priority | String | | NONE / LOW / MEDIUM / HIGH |
| dueDate | Long? | | epochDay |
| dueTime | Int? | | minuteOfDay (dueDate 있을 때만) |
| projectId | String? | FK → project.id SET NULL, INDEX | |
| sortOrder | Int | | |
| completedAt | Long? | | |
| createdAt / updatedAt | Long | | |

**project**
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | String | PK | |
| name | String | NOT NULL | |
| color | String | | 팔레트 토큰 |
| archived | Boolean | | 보관 |
| sortOrder | Int | | |
| createdAt / updatedAt | Long | | |

**memo**
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | String | PK | |
| projectId | String | FK → project.id CASCADE, INDEX | |
| title | String | | |
| body | String | | 일반 텍스트 |
| pinned | Boolean | | |
| createdAt / updatedAt | Long | INDEX(updatedAt) | |

**diary_entry**
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | String | PK | |
| date | Long | UNIQUE | epochDay (하루 1건) |
| mood | String? | | VERY_BAD … VERY_GOOD |
| body | String | | |
| createdAt / updatedAt | Long | | |

**calendar_event**
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | String | PK | |
| title | String | NOT NULL | |
| startAt / endAt | Long | INDEX(startAt) | epoch millis |
| allDay | Boolean | | true면 시작/종료를 로컬 자정 기준으로 해석 |
| location | String? | | |
| note | String | | |
| color | String? | | |
| recurrenceRule | String? | | **예약 컬럼** (P2, MVP에서는 항상 null) |
| createdAt / updatedAt | Long | | |

**document_folder**
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | String | PK | |
| name | String | NOT NULL | |
| parentId | String? | | **예약 컬럼** (MVP는 1단계, 항상 null) |
| sortOrder | Int | | |
| createdAt / updatedAt | Long | | |

**document**
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | String | PK | |
| folderId | String? | FK → document_folder.id SET NULL, INDEX | null = 루트 |
| title | String | | 기본값 "스캔 yyyy-MM-dd HH:mm" |
| pdfPath | String | | `documents/{id}.pdf` (내부 저장소 상대 경로) |
| thumbnailPath | String? | | `documents/thumbs/{id}.jpg` |
| pageCount | Int | | |
| sizeBytes | Long | | |
| source | String | | SCAN / IMPORT |
| createdAt / updatedAt | Long | | |

**doodle**
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | String | PK | |
| title | String | | |
| strokesPath | String | | `doodles/{id}.json` (획 데이터, kotlinx.serialization) |
| thumbnailPath | String | | `doodles/{id}.png` |
| canvasWidth / canvasHeight | Int | | 저장 시점 캔버스 크기(재현용) |
| createdAt / updatedAt | Long | | |

**alarm**
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | String | PK | |
| hour / minute | Int | | |
| repeatDays | Int | | 요일 비트마스크 (월=1 … 일=64, 0 = 1회) |
| label | String | | |
| enabled | Boolean | INDEX | |
| vibrate | Boolean | | |
| ringtoneUri | String? | | null = 기본 알람음 |
| snoozeMinutes | Int | | 기본 5 |
| nextTriggerAt | Long? | | 다음 예약 시각 캐시 (재예약 계산용) |
| createdAt / updatedAt | Long | | |

**calc_history**
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | Long | PK autoGenerate | |
| expression | String | | |
| result | String | | BigDecimal 문자열 |
| createdAt | Long | INDEX | 100건 초과 시 오래된 것부터 삭제 |

**file_bookmark** — 내파일의 외부 파일 즐겨찾기
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | String | PK | |
| uri | String | UNIQUE | SAF content URI (영구 권한 취득) |
| displayName | String | | |
| mimeType | String? | | |
| sizeBytes | Long? | | |
| addedAt | Long | | |

> "내파일"의 앱 생성 파일(스캔·낙서)은 별도 테이블 없이 `document`, `doodle` 테이블을 조회해 보여준다.

**plugin_data** — 플러그인 공용 키-값 저장소 (SHELL-14, v11)
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| pluginId | String | PK(pluginId, key) | `DeskTool.id` |
| key | String | PK | 플러그인이 정하는 키 |
| value | String | | 문자열 (구조가 있으면 JSON) |
| updatedAt | Long | | epoch millis |

> 단순 플러그인은 자기 Entity·마이그레이션 없이 이 표만 쓴다. 검색·정렬이 필요한 큰 데이터가 생기면 그때 `core:database`에 전용 Entity를 추가한다. 도구를 꺼도 행은 지우지 않는다.

### 8.4 DataStore (Preferences) — 앱 설정
`hapticLevel`(OFF/LIGHT/DEFAULT), `gripPosition`(TOP/MIDDLE/BOTTOM), `themeMode`(SYSTEM/LIGHT/DARK), `onboardingCompleted`, `appDrawerAutoFocusSearch`

### 8.5 내부 저장소 레이아웃 (`filesDir/`)
```
documents/{id}.pdf
documents/thumbs/{id}.jpg
doodles/{id}.json
doodles/{id}.png
```
- 외부 공유는 `FileProvider`를 통해서만. 경로 규약은 `core:storage`가 소유.
- DB 행 삭제와 파일 삭제는 함께 처리(파일 삭제 실패 시 로그 + 다음 실행 때 고아 파일 정리).

---

## 9. 권한 및 매니페스트

| 항목 | 용도 | 비고 |
|---|---|---|
| `intent-filter` MAIN + HOME + DEFAULT | 기본 런처 | `launchMode="singleTask"`, `RoleManager.ROLE_HOME`으로 지정 요청 |
| `<queries>` (MAIN/LAUNCHER 인텐트) | 앱 서랍 목록 | **`QUERY_ALL_PACKAGES` 사용 금지** |
| `POST_NOTIFICATIONS` | 알람 알림 | 런타임 권한, 첫 알람 생성 시 요청 |
| `SCHEDULE_EXACT_ALARM` | 정확한 알람 | Android 14+ 신규 설치 시 기본 거부 → `canScheduleExactAlarms()` 확인 후 설정 화면 안내. (`USE_EXACT_ALARM`은 Play 정책상 알람·캘린더가 핵심 기능인 앱 한정이라 사용하지 않음) |
| `USE_FULL_SCREEN_INTENT` | 잠금화면 알람 울림 | Android 14+ 제한 → `canUseFullScreenIntent()` 확인, 불가 시 헤드업 알림으로 대체 + 설정 안내 |
| `RECEIVE_BOOT_COMPLETED` | 재부팅 후 알람 재예약 | `TIME_SET`, `TIMEZONE_CHANGED`, `MY_PACKAGE_REPLACED`도 수신 |
| `VIBRATE` | 알람 진동, Vibrator 기반 햅틱 | |
| `REQUEST_DELETE_PACKAGES` | 앱 서랍 길게 누르기 → 제거 (SHELL-08) | 시스템 제거 확인 대화상자만 띄움 (일반 권한) |
| CAMERA / 저장소 권한 | **불필요** | 스캐너는 ML Kit(Play 서비스) UI, 파일은 SAF |
| `INTERNET` | **앱 기능에서 사용 안 함** | 의존 라이브러리가 병합하는 권한은 merged manifest로 확인 후 이 표에 기록. M0 기준 병합 권한: `{applicationId}.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`(androidx.core 내부용)뿐. M4: ML Kit가 병합하는 `INTERNET`·`ACCESS_NETWORK_STATE`는 `tools:node="remove"`로 제거 (스캐너 UI·모듈 다운로드는 Play 서비스 담당 — **실기기에서 스캔 동작 확인 필요**) |
| `android:allowBackup="false"` + `dataExtractionRules` | 자동 백업·기기 간 이전 비활성화 | 개인일기 등 개인 데이터 보호 (§13). targetSdk 31+에서는 allowBackup만으로 기기 간 이전이 막히지 않아 `res/xml/data_extraction_rules.xml`에서 모든 도메인을 제외. 백업은 BACKUP-01(P2)로 별도 제공 |

---

## 10. 비기능 요구사항
- **성능**: 홈 복귀 시 책상 즉시 표시(초기화 작업은 비동기·지연 로딩). 스트립 스크럽은 재구성(recomposition) 최소화 — 위치·스케일 계산은 레이아웃/그리기 단계(`Modifier.offset { }`, `graphicsLayer { }`)에서 수행.
- **안정성**: 런처는 죽으면 안 된다. 메인 스레드 DB/파일 I/O 금지, 예외는 도구 경계에서 잡아 해당 도구만 오류 화면 표시.
- **오프라인 퍼스트**: 모든 기능이 네트워크 없이 동작. 단일 진실 공급원 = Room.
- **개인정보**: 모든 데이터는 기기 내부에만 저장. 분석/광고 SDK 없음.
- **화면**: edge-to-edge(Android 15+ 강제), 상태바/내비게이션바 인셋 처리, 다크 모드, 글꼴 크기 200%.
- **언어**: 한국어 기본(`values/strings.xml`). 영어(`values-en`)는 P2.

## 11. 마일스톤
| 단계 | 범위 | 완료 조건 |
|---|---|---|
| M0 ✅ | 프로젝트 골격: 버전 카탈로그, 모듈 생성, 디자인시스템 테마, HOME 인텐트, 빈 책상 | 빌드·단위 테스트 통과 (2026-11-26) |
| M1 🔶 | SHELL-02~06 (구현 완료, 실기기 검증 대기): 책상·시계, 서류철 스트립(스크럽·플링·스냅), 햅틱, 열기/닫기 전환, 홈 복귀, One UI 제스처 충돌 회피 | debug 샘플 도구로 스크럽→열기→닫기 전체 흐름 S24+ 실기기 검증 |
| M2 🔶 | SHELL-01·07~11 (구현 완료, 실기기 검증 대기): 기본 런처 지정, 앱 서랍(초성 인덱스), 도구함(켜기/끄기·순서·색상), 온보딩, 설정(DataStore) | 도구함 변경이 책상 탭에 즉시 반영 |
| M3 🔶 | 기록 도구 (P0 구현 완료, 실기기 검증 대기): 할일정리, 프로젝트 메모, 캘린더, 개인일기 | 각 P0 완료, Room 마이그레이션 테스트 |
| M4 🔶 | 서류정리함 + ML Kit 문서 스캐너 (P0 구현 완료, 실기기 검증 대기) | DOC P0 완료, 스캔→저장→보기→공유 실기기 검증 |
| M5 🔶 | 유틸리티 도구 (P0 구현 완료, 실기기 검증 대기): 계산기, 낙서장, 알람, 내파일 | 각 P0 완료, 알람 재부팅 재예약 검증 |
| M6 | 다듬기: 성능·접근성·P1 (SHELL-12 등) | §1.4 성공 기준 충족 |

## 12. 리스크 및 열린 질문
| 구분 | 내용 | 대응 |
|---|---|---|
| 리스크 | 우측 가장자리 뒤로가기 제스처·엣지 패널과 스트립 충돌 | §3.5, M1에서 실기기 최우선 검증 |
| 리스크 | Galaxy 기기의 Composition 햅틱 primitive 지원 편차 | 지원 여부 확인 + `HapticFeedbackConstants` 대체 |
| 리스크 | 화면 꺼짐 상태에서 알람 울림 재생 유지(포그라운드 서비스 필요 여부) | M2에서 실기기 검증 후 방식 확정 |
| 리스크 | ML Kit 스캐너 모듈 미설치/Play 서비스 없는 환경 | 오류 상태 안내 UI |
| 질문 | 업무 프로필(Work profile) 앱 표시 | 현재 P2 |

## 13. 결정 기록
| 날짜 | 결정 | 내용 |
|---|---|---|
| 2026-11-26 | 앱 서랍 포함 | SHELL-07 앱 서랍(ㄱㄴㄷ 초성/알파벳 인덱스 스트립) MVP P0로 승인 |
| 2026-11-26 | 허용 라이브러리 | Hilt, Navigation Compose, DataStore, kotlinx-serialization 승인 (`AGENTS.md` §3) |
| 2026-11-26 | SDK 버전 | minSdk 34, targetSdk 36 승인 |
| 2026-11-26 | compileSdk 37.2 (M0 중 변경, 확인 요청) | 최신 AndroidX(Compose 1.13, core 1.19, navigation 2.10, lifecycle 2.11, hilt-navigation-compose 1.4)가 compileSdk 37.1 이상을 요구 → compileSdk만 37.2로 올림. minSdk·targetSdk는 승인값 유지. 대안: AndroidX를 compileSdk 36 호환 구버전으로 낮추기 |
| 2026-11-26 | 서류정리함 모듈 | 서류정리함 UI와 ML Kit 스캐너를 `feature/scanner` 단일 모듈로 통합 (toolId `cabinet`) |
| 2026-11-26 | 자동 백업 비활성화 | 개인일기 포함 **전체 비활성화**: `android:allowBackup="false"` + `dataExtractionRules` 전 도메인 제외 (Auto Backup·기기 간 이전 모두 제외). 백업은 추후 로컬 백업/내보내기 기능(JSON/ZIP)으로 별도 처리 → BACKUP-01 |
| 2026-11-26 | 데이터 내보내기 | **P2**(MVP 이후)로 분류 → BACKUP-01 |
| 2026-11-27 | 플러그인 확장 구조 | 사용자 요청: "각 서류철 = 플러그인, 내가 만든 플러그인을 쉽게 붙이게" → SHELL-14 플러그인 SDK(`plugins/` 자동 포함, `SimpleDeskTool`, `DeskToolHost`, `plugin_data` v11). 동적(원격) 로딩은 여전히 P2 |
| 2026-11-27 | 서류철 위치·훑는 위치 | 실기기(S24+) 피드백: 서류철은 **화면 좌측**에 꽂혀 오른쪽으로 튀어나오고, 훑기(드래그)는 **화면 우측**(가장자리 32dp 안쪽)에서 한다 (SHELL-03). `IndexTabStrip(side, scrubSide)`로 분리, 설정 UI는 SHELL-13 |
| 2026-11-27 | 앱 서랍 서류철 | 실기기 피드백: 앱 서랍을 서류철 하나로 넣는다 (SHELL-07). 셸 기능이라 도구 레지스트리가 아닌 책상이 맨 끝에 고정으로 꽂고, 팝업 창으로 연다 |
| 2026-11-27 | 도구 열기 방식 | 실기기 피드백: 도구는 전체 화면 이동이 아니라 **책상 위 팝업 창**으로 연다 (SHELL-05). 창마다 전용 NavHost에 도구 그래프를 붙여 도구 모듈은 그대로 |
| 2026-11-27 | 서류철 스크럽 모션 | 사용자 요청: 글자 파도 대신 **왼쪽 서류철이 칸마다 툭툭 튀어나오는** 이산 스프링 모션 (SHELL-03) |
