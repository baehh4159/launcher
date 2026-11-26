# AGENTS.md — 책상 런처 작업 규칙

이 파일은 이 저장소에서 코드를 작성하는 모든 AI 에이전트(Claude Code, Codex 등)와 사람이 따르는 규칙이다.
제품 요구사항은 [`PRD.md`](./PRD.md)가 기준이며, 이 파일과 PRD가 충돌하면 **작업을 멈추고 사용자에게 묻는다.**

---

## 0. 프로젝트 한 줄 요약
안드로이드(Galaxy S24+, Android 14/15) 기본 런처 겸 생산성 앱. 우측 서류철 인덱스 탭을 엄지로 스크럽해 책상 도구(할일·캘린더·메모·일기·서류정리함·계산기·낙서장·알람·내파일)를 연다.
Kotlin · Jetpack Compose · Material 3 · Room · ML Kit Document Scanner · Clean Architecture + 기능별 모듈.

---

## 1. 절대 규칙 (MUST)

### 규칙 1. 한 번에 한 기능만 수정한다
- **작업 단위 = PRD §5의 기능 ID 1개** (예: `TODO-01`, `SHELL-03`). 사용자가 여러 ID를 명시적으로 묶어 요청한 경우만 예외.
- 수정 범위는 해당 기능이 속한 **모듈 1개**로 한정한다 (예: `TODO-01` → `feature/todo`).
- 그 기능에 꼭 필요한 공유 모듈 변경(예: `core:database`에 Entity/DAO/Migration 추가, `app`에 모듈 등록)만 허용하며, **작업 시작 전 계획에 미리 명시**한다.
- 금지:
  - 요청과 무관한 리팩터링, 이름 변경, 포맷팅 일괄 변경, "김에" 고치기
  - 다른 feature 모듈 수정
  - 한 커밋에 여러 기능 섞기
- 작업 중 다른 기능의 버그·개선점을 발견하면 **고치지 말고** 보고 내용의 "발견 사항"에 적는다.
- 범위를 넘는 변경이 불가피하다고 판단되면 **진행하지 말고 사용자에게 먼저 묻는다.**

### 규칙 2. 임의 라이브러리 추가 금지
- 의존성은 아래 **§3 허용 목록**에 있는 것만 사용한다.
- 목록에 없는 라이브러리, Gradle 플러그인, 저장소(maven repo) 추가는 **사용자의 명시적 승인 후에만** 한다. 승인되면 §3 표에 사유와 함께 추가한다.
- 허용 목록에 있는 라이브러리라도 **버전 변경**(업/다운그레이드)은 별도 승인 대상이다.
- 모든 버전은 `gradle/libs.versions.toml`(버전 카탈로그) 한 곳에서만 관리한다. `build.gradle.kts`에 버전 문자열 하드코딩 금지.
- 외부 라이브러리 소스 코드를 복사해 붙여넣는 방식의 우회도 금지.
- 표준 Android SDK / Kotlin 표준 라이브러리 / `java.*`로 해결 가능한 것은 직접 구현한다 (예: 수식 파서, 한글 초성 분해, PDF 보기=`PdfRenderer`, 이미지 디코딩=`BitmapFactory`/`ImageDecoder`).

### 규칙 3. Compose Preview를 반드시 포함한다
- 새로 만들거나 수정한 **모든 화면(Screen)과 재사용 컴포넌트**에는 `@Preview`가 있어야 한다.
- 화면은 두 단계로 나눈다:
  - `XxxRoute(...)` — ViewModel·내비게이션 연결 (Preview 대상 아님)
  - `XxxScreen(state: XxxUiState, onAction: (XxxAction) -> Unit, modifier: Modifier = Modifier)` — 상태 없음, **Preview 대상**
- Preview는 Hilt·ViewModel·Room·Context 의존 없이 동작해야 한다. 가짜 데이터는 같은 모듈의 `ui/preview/` 패키지(`XxxPreviewData`, `PreviewParameterProvider`)에 둔다.
- 화면 Preview 최소 구성: **기본 상태 + 빈 상태 + (해당 시) 로딩/오류 상태**, 각각 라이트·다크.
- `core:designsystem`이 제공하는 멀티 프리뷰 애너테이션을 사용한다:
  - `@DeskPreviews` — S24+ 기준 기기(`spec:width=384dp,height=832dp,dpi=450`) × 라이트/다크
  - `@DeskFontScalePreviews` — 글꼴 배율 1.0 / 1.5 / 2.0 (디자인시스템 컴포넌트 필수)
- Preview 함수는 `private`, 이름은 `XxxScreenPreview`, `XxxScreenEmptyPreview` 형식.
- 인터랙션(스트립 스크럽 등)처럼 Preview로 확인하기 어려운 동작은, 정지 상태 스냅샷(예: `focus = 2.4f`)을 Preview로 만든다.

---

## 2. 작업 절차

1. **확인**: 요청된 기능 ID와 PRD 해당 절을 읽는다. 모호하면 구현 전에 질문한다.
2. **계획 공유**: 수정할 모듈·파일 목록, 추가할 Entity/Migration 여부, 추가할 Preview를 짧게 먼저 제시한다.
3. **구현**: §4~§5 아키텍처·코딩 규칙을 따른다.
4. **검증**: §7 명령을 실행한다. 실행할 수 없으면(환경 미구성 등) 그 사실과 이유를 보고한다 — 통과했다고 추정하지 않는다.
5. **보고**: 아래 형식으로 마무리한다.
   ```
   기능 ID: TODO-01
   변경 파일: …
   공유 모듈 변경: (없음 | core:database — TodoEntity 추가, Migration 3→4)
   추가된 Preview: TodoScreenPreview, TodoScreenEmptyPreview, …
   검증: assembleDebug ✅ / testDebugUnitTest ✅ / lint ⚠️(사유)
   실기기 확인 필요: (햅틱, 제스처 등 에뮬레이터로 확인 불가한 항목)
   발견 사항: (범위 밖이라 손대지 않은 문제)
   ```
6. **커밋**: Conventional Commits, scope = 모듈명. 한 커밋 = 한 기능 ID.
   - 예: `feat(todo): TODO-01 할일 추가·수정·삭제`, `fix(desk): SHELL-04 경계 왕복 시 햅틱 중복 수정`

---

## 3. 의존성 허용 목록

| 그룹 | 라이브러리 | 용도 |
|---|---|---|
| Kotlin | kotlin-stdlib, kotlinx-coroutines-android | 기본, 비동기 |
| Kotlin | kotlinx-serialization-json (+ serialization 플러그인) | 타입 안전 내비게이션 라우트, 낙서 획 데이터 |
| Compose | compose-bom, ui, ui-graphics, foundation, animation, material3, ui-tooling-preview, ui-tooling(debug), material-icons-extended | UI |
| AndroidX | core-ktx, activity-compose, lifecycle-runtime-compose, lifecycle-viewmodel-compose | 기본 |
| AndroidX | navigation-compose | 화면 이동 |
| AndroidX | room-runtime, room-ktx, room-compiler(KSP), room-testing(test) | 로컬 DB |
| AndroidX | datastore-preferences | 앱 설정 |
| DI | hilt-android, hilt-compiler(KSP), hilt-navigation-compose | 의존성 주입, 도구 멀티바인딩 |
| Google | play-services-mlkit-document-scanner | 문서 스캐너 |
| 빌드 | Android Gradle Plugin, Kotlin Gradle Plugin, Compose Compiler 플러그인, KSP, Hilt 플러그인, Room 플러그인 | |
| 테스트 | junit4, kotlinx-coroutines-test, androidx-test-ext-junit, androidx-test-runner, compose-ui-test-junit4, compose-ui-test-manifest(debug) | 테스트 |

**명시적으로 금지(승인 전)**: 이미지 로더(Coil/Glide), 네트워크(Retrofit/OkHttp/Ktor), 다른 DI(Koin), RxJava, Accompanist, 분석·광고·크래시 SDK, 포맷터/정적분석 플러그인(ktlint/detekt), Turbine·MockK 등 추가 테스트 라이브러리, `androidx.biometric`(DIARY-03 착수 시 승인 요청).

---

## 4. 아키텍처 규칙

### 4.1 모듈 의존
```
app ──▶ feature:* ──▶ core:*
```
- **feature → feature 의존 금지.** 다른 도구의 데이터는 `core:database` DAO를 읽기 전용으로 쓰고 자기 도메인 모델로 매핑한다.
- `core` 모듈 간 순환 의존 금지. `core:tool-api`는 구현 코드 없이 계약만 둔다.
- 셸(`feature:desk`)은 특정 도구를 import하지 않는다. `Set<DeskTool>`만 사용한다.

### 4.2 feature 모듈 내부 계층
```
domain/  model, repository(인터페이스), usecase   ← 순수 Kotlin. android.*, androidx.*, Room 타입 import 금지
data/    repository 구현, mapper(Entity ↔ Domain) ← core:database 사용
ui/      XxxRoute, XxxScreen, XxxViewModel, XxxUiState, XxxAction, component/, preview/
di/      Hilt 모듈
XxxTool.kt  DeskTool 구현
```
- 방향: `ui → domain ← data`. ViewModel은 UseCase(또는 Repository 인터페이스)만 의존.
- Room Entity를 `ui`나 `domain`으로 내보내지 않는다. 반드시 mapper를 거친다.
- UseCase는 단일 public `operator fun invoke`. 단순 위임뿐이면 UseCase 없이 Repository 인터페이스 사용 허용.

### 4.3 상태 관리 (UDF)
- ViewModel: `val uiState: StateFlow<XxxUiState>` + `fun onAction(action: XxxAction)`.
- `XxxUiState`는 불변 `data class`(또는 `sealed interface`), `XxxAction`은 `sealed interface`.
- 일회성 이벤트(스낵바, 화면 이동)는 상태에 포함하고 소비 후 초기화하거나 `Channel` 기반 이벤트 흐름 하나로 통일 — 모듈 안에서 한 방식만 사용.
- Composable은 `collectAsStateWithLifecycle()`로 수집.
- 프로세스 종료 복원이 필요한 입력값은 `SavedStateHandle`.

### 4.4 Room
- Entity·DAO·Migration은 모두 `core:database`에 둔다 (단일 `DeskDatabase`).
- 스키마 변경 시: DB 버전 +1, `Migration(n, n+1)` 작성, `core/database/schemas/` 스키마 JSON 커밋, **마이그레이션 테스트 추가**.
- `fallbackToDestructiveMigration` 사용 금지 (디버그 포함 — 데이터 보존 습관화).
- DAO 조회는 `Flow`, 쓰기는 `suspend`. 메인 스레드 쿼리 금지.
- 시간·ID·열거형 저장 형식은 PRD §8.1을 따른다.

### 4.5 플러그인(도구) 규칙
- `DeskTool.id`는 출시 후 **절대 변경 금지** (DB `tool_config` 키).
- 도구는 자기 화면을 `tool/{id}` 하위 중첩 그래프로만 등록한다.
- 도구를 꺼도 데이터는 삭제하지 않는다.

---

## 5. 코딩 규칙

- **문자열**: 하드코딩 금지, `strings.xml`(한국어 기본). Preview 가짜 데이터는 예외.
- **색·치수·타이포**: `core:designsystem` 토큰만 사용. feature 모듈에서 `Color(0xFF…)` 직접 사용 금지.
- **모션·햅틱**: 애니메이션 상수는 `DeskMotion`, 햅틱 호출은 `DeskHaptics`로만. feature 모듈에서 `Vibrator` 직접 사용 금지.
- **Composable**: 첫 번째 선택 파라미터는 `modifier: Modifier = Modifier`. 상태 끌어올리기(state hoisting) 원칙.
- **성능**: 스크롤·드래그·애니메이션 값은 람다형 Modifier(`Modifier.offset { }`, `graphicsLayer { }`, `drawBehind { }`)에서 읽어 재구성을 피한다. 리스트는 `key` 지정.
- **코루틴**: `GlobalScope`, 메인 스레드 `runBlocking` 금지. Dispatcher는 주입받는다(`core:common`).
- **Kotlin**: `!!` 금지, `lateinit`은 DI·테스트 외 지양. 공개 API는 명시적 타입.
- **런처 안정성**: 도구 화면 하나의 예외가 앱 전체를 죽이지 않도록 도구 경계에서 오류 상태 화면으로 처리.
- **권한/매니페스트**: PRD §9 표에 없는 권한 추가 금지. 특히 `QUERY_ALL_PACKAGES`, `MANAGE_EXTERNAL_STORAGE`, `USE_EXACT_ALARM`, `INTERNET`(앱 기능용) 금지.
- **파일**: 앱 내부 저장소 경로는 `core:storage` 규약을 사용, 외부 공유는 `FileProvider`만.
- **주석**: 코드로 드러나지 않는 "왜"만 한국어로 짧게. 터치 수치·물리 상수에는 근거를 남긴다.

---

## 6. 새 도구(플러그인) 추가 체크리스트
1. [ ] PRD §5에 도구 섹션과 기능 ID 추가 (사용자 승인)
2. [ ] `feature/<name>` 모듈 생성 — 기존 도구 모듈의 패키지 구조(§4.2) 그대로
3. [ ] `<Name>Tool : DeskTool` 구현, 불변 `id` 결정
4. [ ] `di/<Name>ToolModule` — `@Binds @IntoSet`
5. [ ] (필요 시) `core:database`에 Entity/DAO + Migration + 마이그레이션 테스트
6. [ ] `settings.gradle.kts` include, `app` 의존성 추가
7. [ ] `DeskPreview()` 및 모든 화면 Preview 작성
8. [ ] 도구함에 노출·켜기/끄기·순서 변경 동작 확인

---

## 7. 빌드·검증 명령
> 프로젝트 골격(M0) 생성 전에는 아래 명령이 존재하지 않는다. 생성 후 실제 태스크명과 다르면 이 절을 갱신한다.

```bash
./gradlew assembleDebug            # 빌드
./gradlew testDebugUnitTest        # 단위 테스트 (domain·ViewModel·파서·마이그레이션)
./gradlew lintDebug                # Android Lint
./gradlew :feature:todo:testDebugUnitTest   # 수정한 모듈만 빠르게
```
- 테스트 필수 대상: UseCase, ViewModel 상태 전이, Room 마이그레이션, 계산기 파서, 알람 다음 울림 시각 계산, 스트립의 `focus → 인덱스/햅틱 경계` 계산(순수 함수로 분리).
- 햅틱·제스처 충돌·알람 잠금화면 표시는 에뮬레이터로 검증 불가 → 보고서의 "실기기 확인 필요"에 기록.

---

## 8. 하지 말 것 (요약)
- 요청 범위 밖 파일 수정 / 여러 기능을 한 번에 수정
- 허용 목록 밖 라이브러리·플러그인 추가, 버전 임의 변경
- Preview 없는 화면·컴포넌트 커밋
- feature 간 직접 의존, domain 계층의 Android 의존
- 파괴적 DB 마이그레이션, 마이그레이션 없는 스키마 변경
- 검증하지 않은 내용을 "통과"로 보고
