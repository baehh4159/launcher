# 책상 플러그인 만들기 (SHELL-14)

오른쪽 서류철 하나 = 플러그인 하나. 플러그인은 `plugins/<id>/` 폴더 하나로 끝난다.
`settings.gradle.kts`와 `app`이 `plugins/*`를 **자동으로** 포함하므로, 폴더를 넣으면 붙고 빼면 떨어진다.

## 1. 30초 시작
```bash
scripts/new-plugin.sh habit "습관 체크"   # plugins/habit/ 생성
./gradlew assembleDebug                  # 설치 후 도구함에서 '습관 체크' 켜기
```
만들어지는 파일:
```
plugins/habit/
  build.gradle.kts                 alias(libs.plugins.desk.plugin) + namespace
  src/main/kotlin/…/plugin/habit/
    HabitTool.kt                   SimpleDeskTool 구현 (id·이름·아이콘·탭 색·기본 순서)
    di/HabitToolModule.kt          @Binds @IntoSet — 이것이 "등록"
    ui/HabitScreen.kt              HabitRoute / HabitScreen / @DeskPreviews
  src/main/res/values/strings.xml
```
완성된 예시는 [`plugins/water`](../plugins/water) (오늘 마신 물 잔 수 — 저장소·ViewModel·테스트·Preview 포함).

## 2. 플러그인이 받는 것: `DeskToolHost`
`SimpleDeskTool.Content(host, modifier)`로 앱이 건넨다. 플러그인은 앱 내부(DB, DataStore, 다른 도구)를 몰라도 된다.

| 멤버 | 용도 |
|---|---|
| `toolId` | 내 도구 id |
| `tabColor` | 사용자가 도구함에서 고른 탭 색 (헤더·강조색에 사용) |
| `storage: PluginStorage` | **나만의 키-값 저장소** (`observe`/`observeAll`/`get`/`put`/`clear`). Room `plugin_data` 표에 저장, 다른 플러그인과 격리. 구조가 있는 값은 kotlinx.serialization JSON 문자열로 |
| `close()` | 책상으로 돌아가기 |

화면 틀은 `DeskToolScaffold(host, title) { padding -> … }` — 모든 도구와 같은 서류철 헤더·뒤로 버튼.

상태는 ViewModel로 둔다. Hilt 없이 호스트 저장소로 바로 만든다:
```kotlin
val viewModel: HabitViewModel = viewModel(key = host.toolId) { HabitViewModel(HabitStore(host.storage)) }
```
Preview·테스트는 `PreviewDeskToolHost()` / `InMemoryPluginStorage()`를 쓴다.

## 3. 규칙
- **id**: `[a-z][a-z0-9_]{1,31}`. DB 키라서 **출시 후 변경 금지**. 같은 id가 이미 있으면 스크립트가 거부한다.
- **안전장치**: id 형식이 틀리거나, 앱보다 새 SDK(`DeskTool.apiVersion > DeskToolApi.VERSION`)이거나, id가 중복된 도구는 앱을 죽이지 않고 **그 도구만 빠진다** (Logcat 태그 `ToolRegistry`).
- `desk.plugin` 컨벤션은 `core:common`, `core:designsystem`, `core:tool-api`와 Compose·Navigation·Lifecycle·Hilt만 준다. `core:database` 등 앱 내부 모듈에 의존하지 않는다 — 앱 내부가 바뀌어도 플러그인이 깨지지 않게 하기 위함.
- 라이브러리 추가 금지·Preview 필수·문자열은 strings.xml·색은 designsystem 토큰 — `AGENTS.md` 규칙은 플러그인에도 똑같이 적용된다.
- 도구를 꺼도 `plugin_data`는 지우지 않는다. 플러그인 폴더를 빼도 데이터는 남아, 다시 넣으면 이어서 쓴다.

## 4. 더 큰 플러그인
| 필요 | 방법 |
|---|---|
| 화면 여러 개 | `SimpleDeskTool` 대신 `DeskTool`을 직접 구현하고 `toolGraph()`에 `navigation<XxxGraph>{ … }` 중첩 그래프 등록 (feature 도구들과 같은 방식). 저장소는 `PluginStorageProvider`를 Hilt로 주입받아 `forTool(id)` |
| 검색·정렬이 필요한 큰 데이터 | 키-값 대신 `core:database`에 Entity·DAO·마이그레이션 추가 (AGENTS.md §4.4) — 이때는 `feature/<id>` 모듈로 승격 |
| 책상 위 미리보기 | `DeskPreview(tabColor, modifier)` 재정의 (기본 = 아이콘 + 제목 카드) |

## 5. SDK 버전
`DeskToolApi.VERSION = 1`. 계약(`DeskTool`, `DeskToolHost`, `PluginStorage`)에 멤버를 더할 때는 기본 구현을 함께 넣어 기존 플러그인이 그대로 컴파일되게 한다. 호환이 깨지는 변경만 버전을 올린다.
