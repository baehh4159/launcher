# 디자인 언어 — 책상·공책

> 사용자 지정 참고: [beUI](https://beui.dev/) (모션 원칙·토큰), [Beautiful UI](https://www.beautifului.dev/) (컴포넌트 패턴).
> 두 사이트는 웹(React·Framer Motion·Tailwind) 라이브러리라 코드를 가져오지 않는다. **원칙과 수치만** Compose로 옮긴다(AGENTS.md 규칙 2: 외부 코드 복사 금지).
> 모든 값은 `core:designsystem`의 `DeskTheme`·`DeskMotion`·`DeskHaptics` 토큰으로만 쓴다. feature 모듈에 숫자를 직접 쓰지 않는다.

---

## 1. 원칙

| # | 원칙 | 출처 | 이 앱에서 |
|---|---|---|---|
| D1 | **"Motion that explains, not distracts."** 움직임의 이유를 말할 수 없으면 움직임을 없앤다 | beUI 모션 가이드 ("If you cannot explain why it moves, remove the movement.") | 서류철 돌출 = "이 칸이 선택됨", 창 펼침 = "이 공책이 그 칸에서 나옴". 장식 애니메이션 없음 |
| D2 | **300ms 이하가 기본** | beUI ("Under 300ms is the default for interface motion.") | 누름 100–160ms, 팝오버 125–200ms, 창·서랍만 200–500ms |
| D3 | **움직임 줄이기는 설계된 상태** | beUI ("Reduced motion is a designed state", "Keep meaning. Remove travel.") | 시스템 애니메이션 배율 0이면 이동·크기 변화 없이 150ms 페이드만. 의미(선택·열림)는 그대로 |
| D4 | **같은 표면을 유지한 채 모양을 바꾼다** | beUI 레이아웃 연속성(layoutId morph) | 서류철 칸 → 창, 바로 적기 줄 → 종이, 정리 카드 → 다음 카드가 같은 자리에서 |
| D5 | **그림자 대신 얇은 선** | beUI(카드·패널은 `border` 중심, 시트만 그림자) | 카드·칸·창은 0.5–1dp 선. 그림자는 창(떠 있는 것)에만 아주 옅게 |
| D6 | **의미 토큰만 쓴다** | beUI(`background`·`card`·`foreground`·`muted-foreground`·`border`) | `DeskTheme.colors.paper/onPaper/muted/line…` |
| D7 | **작고 반복 가능한 원형(primitive)** | Beautiful UI ("Crafted primitives") | 화면마다 새 위젯을 만들지 않고 §5의 원형을 조합 |

---

## 2. 색

종이 톤 중립색 + 낮은 채도 인덱스 색. 강조는 색이 아니라 **선 두께·글자 굵기**로 먼저 한다.

| 토큰 | 밝은 | 어두운 | 용도 |
|---|---|---|---|
| `desk` (background) | `#F7F6F2` | `#151514` | 책상 면 |
| `paper` (card) | `#FCFBF8` | `#1E1E1C` | 창·카드·종이 |
| `onPaper` (foreground) | `#1C1C1E` | `#ECEAE4` | 본문 |
| `muted` (muted-foreground) | `#6E6B64` | `#9C988F` | 보조 글, 정리 개수 |
| `line` (border) | `#E2DFD7` | `#2E2D2A` | 0.5dp 구분선 |
| `lineStrong` | `#CFCBC1` | `#3D3B37` | 1dp 테두리, 칸 경계 |
| `scrim` | 검정 32% | 검정 48% | 창 뒤 책상 |
| `ink` (primary) | `#2F3A4A` | `#C9D3E0` | 체크 완료·커서·선택 강조 |

**인덱스 색(공책 책등)**: 테라코타 `#C98B76`, 살구 `#D9A27C`, 겨자 `#D4B66A`, 세이지 `#A3B59C`, 청록 `#93B2AE`, 하늘 `#9DB1C6`, 남색 `#A2A8C3`, 라벤더 `#B5A9C2`, 장미 `#D1A9AC`, 갈색 `#B59F8A`, 회색 `#A8ABAF`. 글자는 `onPaper` 잉크색(대비 4.5:1 이상 확인).
덮개 상태 책등: 모두 `lineStrong` 단색(이름·색으로 공책을 구별할 수 없게).

---

## 3. 형태·간격·글자

| 토큰 | 값 | 근거 |
|---|---|---|
| 모서리 — 창 | 24dp | beUI 패널·시트 `rounded-2xl`(24px) |
| 모서리 — 카드·종이 | 16dp | |
| 모서리 — 컨트롤 | 12dp | |
| 모서리 — 알약(바로 적기, 칩) | 완전 원형 | beUI `rounded-full`, Beautiful UI Prompt Bar "Pill" |
| 책등 | 안쪽 모서리만 10dp | 실물 서류철 탭 |
| 간격 | 4·8·12·16·24·32dp | 4dp 격자 |
| 컨트롤 높이 | 48dp | beUI 44px(`h-11`) → Android 최소 터치 48dp |
| 선 | 0.5dp(hairline) / 1dp(thin) | D5 |

글자: 시스템 글꼴(One UI 한글). 굵기로 위계를 만들고 크기 단계를 적게.
| 역할 | 크기/줄높이 | 굵기 | 참고 |
|---|---|---|---|
| 벽 시계 | 28/34sp | Light | |
| 페이지 제목 | 20/28sp | Medium | |
| 카드 제목 | 16/22sp | Medium | beUI `text-base font-medium` |
| 본문(공책) | 16/26sp | Regular | 한글 긴 문단 가독성 위해 줄높이 1.6 |
| 라벨·버튼 | 14/20sp | Medium | beUI `text-sm font-medium` |
| 보조 | 13/18sp | Regular, `muted` | beUI `text-sm text-muted-foreground` |
| 책등 별칭 | 12/14sp 고정(글씨 배율 미적용) | Medium | PRD §4.1 |

---

## 4. 모션 토큰

beUI의 Motion 스프링(stiffness / damping / mass)을 Compose `spring(dampingRatio, stiffness)`로 바꿀 때:
`dampingRatio = damping / (2·√(stiffness·mass))`, `stiffness(Compose) = stiffness / mass`.

| 토큰 | beUI 원값 | Compose | 쓰는 곳 |
|---|---|---|---|
| `Press` | 500 / 30 / 0.6 | ratio 0.87, stiffness 833 | 누름 축소 0.97 |
| `Swap` | 460 / 30 / 0.55 | 0.94, 836 | 아이콘·라벨 교체 |
| `Panel` | 420 / 40 / 0.5 | 1.0(임계), 840 | 팝오버·트레이 등장 |
| `Layout` | 360 / 32 / 0.6 | 1.0(임계), 600 | 카드 재배치·접기 |
| `Glide` | 700 / 50 / 0.5 | 1.0(임계), 1400 | 선택 강조가 칸 사이를 미끄러짐(Beautiful UI "gliding hover states") |
| `Folder` | 300 / 32 / 0.9 (beUI Bloom menu 폴더 열림) | 0.97, 333 | 서류철 칸 → 창 펼침 |
| `TabPop` | (자체) | 0.75, 900 | 서류철 돌출 — 살짝 튀고 멈춤 |

* 계산상 1을 넘는 감쇠비는 Compose에서 과감쇠와 같아 1.0으로 둔다.

| 이징 | cubic-bezier | 쓰는 곳 |
|---|---|---|
| `EaseOut` | (0.16, 1, 0.3, 1) | 등장·퇴장 기본 |
| `EaseInOut` | (0.77, 0, 0.175, 1) | 자리 이동 |
| `EaseDrawer` | (0.32, 0.72, 0, 1) | 창·서랍 열고 닫기(튀지 않음) |

| 패턴 | 값 | 출처 |
|---|---|---|
| 누름 | scale 0.97, `Press` 스프링 | beUI `whileTap` |
| 내용 등장 | 아래 8dp → 0 + 투명 → 불투명, 220ms `EaseOut` | beUI content reveal (blur는 성능 때문에 생략) |
| 내용 교체 | 나감 120ms 위 4dp / 들어옴 180ms 아래 4dp | beUI content swap |
| 창 열기·닫기 | 칸 위치에서 펼침 `Folder` 스프링 / 닫힘 280ms `EaseDrawer`, 막은 같은 곡선으로 페이드 | beUI Bloom menu, Bottom sheet |
| 창 밀어 닫기 | 120dp 넘게 끌거나 600dp/s 넘게 튕기면 닫힘, 아니면 제자리 | beUI Bottom sheet 해제 규칙 |
| 서류철 돌출 | 칸 진입 시 `TabPop`, 이웃 칸 30%·8% | PRD §4.2 |
| 움직임 줄이기 | 이동·크기·스프링 없이 150ms 페이드 | D3 |

---

## 5. 원형(primitive) — 화면은 이것들의 조합

| 원형 | 참고 | 행동 |
|---|---|---|
| **알약 입력(바로 적기)** | Beautiful UI *Prompt Bar*(Pill 변형) | 누르면 같은 자리에서 종이로 펼쳐짐(D4). 키보드 위 `다음 종이` |
| **창(Window)** | beUI *Bottom Sheet*·*Bloom Menu* | 칸에서 펼쳐지는 종이 창, 둘레 10dp, 24dp 모서리, 막 위 |
| **정리 카드** | Beautiful UI *Approval Card*("1 / 3", 건너뛰기/계속) | 원문 + 공책 선택 칩, 카드가 같은 자리에서 다음 장으로 교체 |
| **선택 도구줄** | Beautiful UI *Selection Actions* | 본문 선택 시 `할 일로`·`책상에 두기`·`보며 쓰기`·`복사` |
| **체크 행** | Beautiful UI *Task Rows* | 체크·마감 칩, 완료 시 잉크색 체크 + 글자 muted (줄 긋기 없음 — 문맥에서 다시 읽기) |
| **칩** | Beautiful UI *Tool Chips* | 공책 선택, 마감, 클립 |
| **되돌리기 토스트** | beUI *Toast Stack* | 구조 변경 후 4초, 밀어서 닫기, 하나만 |
| **검색** | Beautiful UI *Search*(실시간 거르기 + 빈 상태) | 전체 창 위 검색 |
| **선택 강조 미끄러짐** | Beautiful UI *Sidebar Nav* gliding | 서류철·앱 서랍 초성의 현재 칸 표시 |

---

## 6. 촉각 (참고 사이트에는 없음 — PRD §5)
- 칸 이동·머묾: `SEGMENT_FREQUENT_TICK`, 열기·꽂기 확정: `CONFIRM`, 모두 `View.performHapticFeedback`.
- 모션과 같은 박자: 칸이 튀어나오는 프레임에 탐색 진동, 창이 펼쳐지기 시작하는 프레임에 확정 진동.

---

## 7. 하지 말 것
- 이유 없는 반복 애니메이션(시머·펄스·빙글 로더). 로딩은 대부분 오프라인이라 즉시 — 필요하면 정지 상태 + 텍스트.
- 그림자로 위계 만들기, 그라데이션, 유리 흐림(blur)을 장식으로 쓰기.
- 빨간 배지·숫자 강조.
- 300ms 넘는 모션(창·서랍 제외).
