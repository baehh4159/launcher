# AGENTS.md — 책상·공책 작업 규칙 (v2)

이 파일은 이 저장소에서 코드를 작성하는 모든 AI 에이전트(Claude Code, Codex 등)와 사람이 따르는 규칙이다.
제품 요구사항은 [`PRD.md`](./PRD.md)(v2), 시각·모션은 [`docs/DESIGN_LANGUAGE.md`](./docs/DESIGN_LANGUAGE.md)가 기준이다. 이 파일과 두 문서가 충돌하면 **작업을 멈추고 사용자에게 묻는다.**

---

## 0. 프로젝트 한 줄 요약
안드로이드(Galaxy S24+, Android 14/15) 기본 홈 런처 겸 오프라인 공책. 홈 = 책상(이어서 쓰기·바로 적기), 왼쪽 서류철을 오른쪽에서 엄지로 훑어 공책을 꺼내 책상 위 창으로 펼친다. 덮개로 업무 내용을 가린다.
Kotlin · Jetpack Compose · Material 3(토큰은 자체) · Room · DataStore · Hilt.

---

## 1. 절대 규칙 (MUST)

### 규칙 1. 한 번에 한 기능만 수정한다
- **작업 단위 = PRD §16의 기능 ID 1개** (예: `STRIP-02`, `PAPER-01`). 사용자가 여러 ID를 묶어 요청한 경우만 예외.
- 수정 범위는 그 기능의 **모듈 1개**. 꼭 필요한 공유 모듈 변경(`core:database`의 Entity/DAO/Migration, `core:designsystem` 토큰, `app` 조립)만 허용하고 **계획에 미리 명시**한다.
- 금지: 무관한 리팩터링·이름 변경·일괄 포맷, 다른 feature 모듈 수정, 한 커밋에 여러 기능.
- 다른 기능의 문제를 발견하면 고치지 말고 보고의 "발견 사항"에 적는다. 범위를 넘는 변경이 불가피하면 **먼저 묻는다.**

### 규칙 2. 임의 라이브러리 추가 금지
- §3 허용 목록만 사용. 목록 밖 라이브러리·Gradle 플러그인·저장소 추가, 허용 라이브러리의 **버전 변경**은 사용자 승인 후에만.
- 버전은 `gradle/libs.versions.toml` 한 곳. `build.gradle.kts`에 버전 하드코딩 금지.
- 외부 라이브러리·디자인 참고 사이트(beUI, Beautiful UI) **코드 복사 금지** — 원칙과 수치만 옮긴다.
- 표준 SDK·Kotlin·`java.*`로 되는 것은 직접 구현(계산기 수식 파서, 초성 분해, Markdown 쓰기 등).

### 규칙 3. Compose Preview를 반드시 포함한다
- 새로 만들거나 수정한 **모든 화면과 재사용 컴포넌트**에 `@Preview`.
- 화면은 `XxxRoute`(ViewModel 연결, Preview 대상 아님) + `XxxScreen(state, onAction, modifier)`(상태 없음, Preview 대상)로 나눈다.
- Preview는 Hilt·ViewModel·Room·Context 없이 동작. 가짜 데이터는 같은 모듈 `ui/preview/`.
- 화면 Preview 최소: 기본 + 빈 상태 + (해당 시) 로딩/오류, 각각 라이트·다크 → `@DeskPreviews`.
- 디자인시스템 컴포넌트는 `@DeskFontScalePreviews`(1.0/1.5/2.0)도.
- 덮개 상태가 있는 화면은 **덮개 Preview**도 둔다.
- 제스처처럼 Preview로 보기 어려운 것은 정지 스냅샷(예: `focus = 2`, 돌출 상태)으로.
- Preview 함수는 `private`, 이름 `XxxScreenPreview`, `XxxScreenEmptyPreview`, `XxxScreenCoveredPreview`.

---

## 2. 작업 절차
1. **확인**: 기능 ID와 PRD 해당 절, DESIGN_LANGUAGE 해당 원형을 읽는다. 모호하면 질문.
2. **계획**: 수정 모듈·파일, Entity/Migration 여부, 추가 Preview를 짧게 먼저.
3. **구현**: §4·§5.
4. **검증**: §7 명령. 실행 못 하면 그 사실과 이유를 보고(통과로 추정 금지).
5. **보고**:
   ```
   기능 ID: STRIP-02
   변경 파일: …
   공유 모듈 변경: (없음 | core:database — SheetEntity 추가, v1→v2)
   추가된 Preview: …
   검증: assembleDebug ✅ / testDebugUnitTest ✅ / lintDebug ✅
   실기기 확인 필요: (햅틱, 제스처, 덮개 첫 프레임 등)
   발견 사항: …
   ```
6. **커밋**: Conventional Commits, scope = 모듈명, 한 커밋 = 한 기능 ID. 예: `feat(desk): STRIP-02 부모 관찰형 훑기 제스처`.

---

## 3. 의존성 허용 목록

| 그룹 | 라이브러리 | 용도 |
|---|---|---|
| Kotlin | kotlin-stdlib, kotlinx-coroutines-android | 기본, 비동기 |
| Kotlin | kotlinx-serialization-json (+ 플러그인) | 내보내기·설정 직렬화 |
| Compose | compose-bom, ui, ui-graphics, foundation, animation, material3, ui-tooling-preview, ui-tooling(debug), material-icons-extended | UI |
| AndroidX | core-ktx, activity-compose, lifecycle-runtime-compose, lifecycle-viewmodel-compose | 기본 |
| AndroidX | room-runtime, room-ktx, room-compiler(KSP), room-testing(test) | 로컬 DB |
| AndroidX | datastore-preferences | 설정·덮개 상태 |
| DI | hilt-android, hilt-compiler(KSP), hilt-lifecycle-viewmodel-compose | 주입 |
| 빌드 | AGP, Kotlin Gradle Plugin, Compose Compiler, KSP, Hilt, Room 플러그인 | |
| 테스트 | junit4, kotlinx-coroutines-test, androidx-test-ext-junit, androidx-test-runner, compose-ui-test-junit4, compose-ui-test-manifest(debug) | |

- 승인 기록: v1 승인(2026-11-26)을 v2에서 그대로 유지. v2에서 **ML Kit 스캐너·Navigation Compose는 사용하지 않는다**(MVP 비목표·창 기반 구조). 다시 쓰려면 승인.
- 명시적 금지(승인 전): 이미지 로더, 네트워크, 다른 DI, RxJava, Accompanist, 분석·광고·크래시 SDK, ktlint/detekt, Turbine·MockK, `androidx.biometric`, `androidx.core:core-splashscreen`.

---

## 4. 아키텍처 규칙

### 4.1 모듈
```
app                     Application, MainActivity(HOME), 창 조립(어느 창을 띄울지), Hilt 진입점
core/common             Dispatcher 주입, 시계(Clock), 결과 타입
core/designsystem       DeskTheme(색·글자·형태), DeskMotion, DeskHaptics, 원형 컴포넌트, @DeskPreviews
core/database           Room DeskNotebookDatabase — notebook, sheet, block, desk_item, edit_session
core/datastore          설정·덮개 상태(DataStore)
core/testing            Fake, MainDispatcherRule
feature/desk            책상 화면, 서류철(STRIP), 덮개(HOME-03), 캘린더 줄(HOME-04)
feature/apps            앱 서랍
feature/paper           임시 종이·정리
feature/notebook        공책 목록·페이지 편집기·검색
feature/tools           문구(계산기)·클립
feature/settings        설정·온보딩·내보내기
```
- 방향: `app → feature:* → core:*`. **feature → feature 의존 금지.** 여러 feature를 잇는 일(서류철에서 공책 창 열기 등)은 `app`이 콜백으로 조립한다.
- 다른 feature의 데이터가 필요하면 `core:database` DAO를 읽기 전용으로 쓰고 자기 도메인 모델로 매핑.

### 4.2 feature 모듈 내부 계층
```
domain/  model, repository(인터페이스), usecase   ← 순수 Kotlin (android.*, androidx.*, Room 금지)
data/    repository 구현, mapper                    ← core:database / core:datastore
ui/      XxxRoute, XxxScreen, XxxViewModel, XxxUiState, XxxAction, component/, preview/
di/      Hilt 모듈
```
- 제스처·좌표 계산(서류철 칸 기하, 탐색 판정, 머묾, 쪽지 묶음 판정, 정리 흐름)은 **순수 함수/클래스**로 분리해 단위 테스트한다.

### 4.3 상태 관리 (UDF)
- ViewModel: `val uiState: StateFlow<XxxUiState>` + `fun onAction(action: XxxAction)`. UiState 불변, Action `sealed interface`.
- 일회성 이벤트는 `Channel` 하나로 통일(모듈 안 한 방식).
- `collectAsStateWithLifecycle()`. 프로세스 종료 복원이 필요한 입력은 `SavedStateHandle` 또는 `edit_session`.

### 4.4 Room
- Entity·DAO·Migration은 `core:database`. 스키마 변경 시 버전 +1, 마이그레이션, `schemas/` JSON 커밋, **마이그레이션 테스트**.
- `fallbackToDestructiveMigration` 금지.
- DAO 조회 `Flow`, 쓰기 `suspend`. 구조 변경(꽂기·옮기기·할 일로·되돌리기)은 **`@Transaction` DAO 메서드 하나**로(PRD §13 불변식).
- 시간은 epoch millis(Long, UTC), ID는 UUID 문자열, 열거형은 이름 문자열.

### 4.5 창(Window) 규칙
- 공책·앱 서랍·정리·전체·설정은 **책상 위 창**(`DeskWindow`)으로 연다. 전체 화면 이동(Navigation) 없음.
- 창 스택은 `app`이 상태로 관리. 뒤로가기는 맨 위 창(또는 창 안의 도구·클립)부터 닫는다.
- 창이 열린 동안 책상 훑기 영역은 비활성.

---

## 5. 코딩 규칙
- **문자열**: `strings.xml`(한국어). Preview 가짜 데이터는 예외.
- **색·치수·글자·모서리**: `DeskTheme` 토큰만. feature에서 `Color(0x…)`·임의 dp 상수 금지(레이아웃 간격은 `DeskSpacing`).
- **모션**: `DeskMotion`의 스프링·이징·길이만. 새 모션은 DESIGN_LANGUAGE D1(이유를 말할 수 있는가)을 먼저 적용. **움직임 줄이기 상태**(시스템 애니메이션 배율 0)를 모든 모션에 구현.
- **햅틱**: `DeskHaptics`만(`performHapticFeedback` 기반). feature에서 `Vibrator`·`HapticFeedbackConstants` 직접 사용 금지.
- **Composable**: 첫 선택 파라미터 `modifier: Modifier = Modifier`. 상태 끌어올리기.
- **성능**: 드래그·애니메이션 값은 람다형 Modifier(`offset { }`, `graphicsLayer { }`, `drawBehind { }`)에서 읽는다. 리스트 `key` 지정.
- **제스처**: 겹친 투명 오버레이로 아래 요소 클릭을 흉내 내지 않는다. 부모가 관찰하다 조건 충족 시 가져간다(PRD §4.2).
- **코루틴**: `GlobalScope`·메인 `runBlocking` 금지(덮개 첫 프레임은 `OnPreDrawListener` 보류로 해결). Dispatcher 주입.
- **Kotlin**: `!!` 금지, `lateinit`은 DI·테스트 외 지양, 공개 API 명시적 타입.
- **런처 안정성**: 런처가 죽으면 홈이 사라진다. 창 하나의 예외가 앱 전체를 죽이지 않게 경계에서 오류 상태로.
- **권한**: PRD §15 표 밖 권한 금지. `INTERNET`(앱 기능), `QUERY_ALL_PACKAGES`, `MANAGE_EXTERNAL_STORAGE` 금지.
- **한글 입력**: 텍스트 구조 변경은 조합 중 텍스트를 포함한 현재 입력 상태에서 처리. 저장된 문자열로 입력칸을 재생성하지 않는다.
- **주석**: 코드로 드러나지 않는 "왜"만 한국어로. 터치·물리 수치에는 근거(PRD·DESIGN_LANGUAGE 절).
- **테스트 이름**: ASCII `subject_expectedResult_condition`, 한국어 설명은 위 주석.

---

## 6. 문구(소품) 추가 체크리스트 (TOOL-*)
1. [ ] PRD에 기능 ID 추가(사용자 승인)
2. [ ] `feature/tools`에 소품 구현: 트레이 UI + 결과 문자열(`본문에 넣기`·`보며 쓰기`로 넘길 값)
3. [ ] 키보드 자리 트레이 높이·커서 보존 동작 확인
4. [ ] Preview(기본·빈 입력·오류), 계산 로직 단위 테스트

---

## 7. 빌드·검증

### 7.1 환경
- JDK 17+ (검증: JDK 21), Gradle Wrapper 9.9.x.
- Android SDK `platforms;android-37.2`, `build-tools;36.1.0`. `local.properties`의 `sdk.dir` 또는 `ANDROID_HOME`(커밋 금지).
- 컨벤션 플러그인(`build-logic`):

| 플러그인 ID | 용도 |
|---|---|
| `desk.android.application` | `app` |
| `desk.android.library` | 모든 라이브러리 모듈 (+ JUnit, coroutines-test) |
| `desk.android.compose` | Compose 모듈 (+ BOM, tooling) |
| `desk.android.feature` | feature = library + compose + hilt + 공통 core 의존 |
| `desk.hilt` | Hilt + KSP |
| `desk.room` | Room + KSP + 스키마 내보내기 |

- AGP 9 내장 Kotlin을 쓰므로 `org.jetbrains.kotlin.android` 플러그인을 적용하지 않는다.

### 7.2 명령
```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew :feature:desk:testDebugUnitTest
```
- 테스트 필수: UseCase, ViewModel 상태 전이, Room 마이그레이션·트랜잭션 불변식, 서류철 기하·탐색 판정, 계산기 파서, 쪽지 묶음 판정, 블록 분할(할 일로·본문으로).
- 햅틱·제스처 충돌·덮개 첫 프레임·한글 IME는 실기기 → 보고서 "실기기 확인 필요".
- `lintDebug`의 `OldTargetApi`(targetSdk 36) 경고는 승인된 결정이므로 무시.
- CI: `.github/workflows/build-debug-apk.yml`이 푸시마다 APK를 Releases `latest-debug`에 올린다.

---

## 8. 하지 말 것
- 범위 밖 수정, 여러 기능 한 커밋
- 허용 목록 밖 라이브러리, 버전 임의 변경, 참고 사이트 코드 복사
- Preview 없는 화면·컴포넌트
- feature 간 의존, domain의 Android 의존
- 파괴적 마이그레이션, 마이그레이션 없는 스키마 변경
- 이유 없는 모션, 움직임 줄이기 미지원, 그림자·배지로 위계 만들기
- 검증 안 한 것을 "통과"로 보고
