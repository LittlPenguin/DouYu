# Testing And Acceptance

## Required Verification

Android:

```powershell
rg --files DouYu/app/src | rg "\.kt$"
rg -n "compose|Composable|Navigation Compose|kotlinx|MockData|coil.compose|paging.compose" DouYu/app
cd DouYu
.\gradlew.bat :app:testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
.\gradlew.bat :app:lintDebug --console=plain
```

Backend:

```powershell
rg -n "DataInitializer|static/seed|/seed/|post_seed|prod_|seedTopic|seedProduct|seedPost" doyu-server/src/main doyu-server/src/test
cd doyu-server
mvn test
```

The Android `.kt` check must return no files. Compose/Kotlin hits in Android
production or test source fail acceptance. Backend seed hits in production code
fail acceptance.

Repair-stage Android checks:

```powershell
cd DouYu
.\gradlew.bat :app:assembleDebugAndroidTest --console=plain
```

The repair stage must include unit coverage for:

- HTTP 401 mapping to a login or unauthenticated state, not raw `HTTP 401` UI.
- Open Design layout mapping for all 18 HTML references.
- Critical UI copy not containing mojibake fragments such as `鍥`, `绉`,
  `閫`, `璇`, or replacement glyphs.

Repair-stage device gate:

- If the user says the real device is unavailable, skip all `adb install`,
  `adb shell am instrument`, screenshot pull, and contact-sheet generation.
- In that state, record real-device UI parity as `not-covered`; do not claim the
  18 Open Design pages passed repair-stage visual verification.
- Resume device work only after the user explicitly says the device is
  available again.
- When resumed, run both `VisualSmokeInstrumentedTest` and
  `RealBackendSmokeInstrumentedTest`, pull screenshots to the repair-stage
  directories, and update
  `doc/development/verification/2026-06-07-repair-stage-verification.md`.

Repair-stage backend checks:

```powershell
cd doyu-server
mvn test
```

The backend test suite must prove that default runtime startup does not insert
seed/demo content and that an isolated clean backend can return empty list
responses for feed, products, topics, and sticker packs.

Real device:

```powershell
cd DouYu
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
adb -s 10.64.241.158:38407 install -r app\build\outputs\apk\debug\app-debug.apk
adb -s 10.64.241.158:38407 install -r app\build\outputs\apk\androidTest\debug\app-debug-androidTest.apk
adb -s 10.64.241.158:38407 shell am instrument -w -r -e class cn.edu.app.douyu.VisualSmokeInstrumentedTest cn.edu.app.douyu.test/androidx.test.runner.AndroidJUnitRunner
adb -s 10.64.241.158:38407 pull /storage/emulated/0/Android/data/cn.edu.app.douyu/files/visual-smoke doc\development\verification\android-java-xml-screenshots\real-device
```

The device serial may change. Record the actual serial, model, Android version,
screen size, and screenshot directory for each evidence run.

Repair-stage real backend:

- Run a read-only `dev` profile diagnostic and record any persistent historical
  data without deleting it.
- Run an isolated clean backend profile or Compose project that does not reuse
  the user's dev database volume.
- Confirm these endpoints return 200 with empty lists on the clean backend:
  `/api/v1/posts/feed`, `/api/v1/products`, `/api/v1/topics`, and
  `/api/v1/sticker-packs`.
- Run the visual smoke suite on the real device and pull screenshots into
  `doc/development/verification/android-java-xml-screenshots/repair-stage/`.
- Stop Spring Boot, PostgreSQL, and Redis after verification and record that
  `8081` no longer listens.

## UI Acceptance

Each Open Design page must be mapped to an Android Java/XML screen:

- `community-home-a.html`
- `commerce-home-a.html`
- `ai-home-a.html`
- `messages-a.html`
- `profile-a.html`
- `search-a.html`
- `post-compose-a.html`
- `post-detail-comment-toolbar-a.html`
- `message-conversation-a.html`
- `notification-detail-a.html`
- `profile-edit-a.html`
- `settings-home-a.html`
- `settings-account-security-a.html`
- `settings-privacy-permissions-a.html`
- `settings-notifications-a.html`
- `settings-about-compliance-a.html`
- `future-capability-ui-a.html`
- `doyu-design-directions.html`

For each screen, record screenshot evidence or write `not covered` with the
reason. Do not reuse Kotlin/Compose screenshots as Java/XML evidence.

Current evidence:

- Real-device Java/XML smoke screenshots exist under
  `doc/development/verification/android-java-xml-screenshots/real-device/visual-smoke`.
- Repair-stage real-device screenshots exist under
  `doc/development/verification/android-java-xml-screenshots/repair-stage/visual-smoke`
  and
  `doc/development/verification/android-java-xml-screenshots/repair-stage/real-backend-smoke`.
- Repair-stage contact sheets exist at
  `doc/development/verification/android-java-xml-screenshots/repair-stage/visual-smoke-contact-sheet.png`
  and
  `doc/development/verification/android-java-xml-screenshots/repair-stage/real-backend-smoke-contact-sheet.png`.
- The smoke suite covers the five main tabs, quick menu, key secondary flows,
  Settings sections, CameraX, and future capability boundary.
- `RealBackendSmokeInstrumentedTest` covers supported real API-backed detail
  flows for post detail, product detail, AI job/detail, conversation, and
  notification detail.
- `doc/development/verification/2026-06-07-open-design-parity-matrix.md`
  records the 18 Open Design references, Java/XML screens, real-device
  screenshot evidence, accepted device/mock-frame differences, and final
  screen-level parity conclusion.

## Empty Data Acceptance

After runtime seed/demo removal:

- Community empty feed shows empty state.
- Commerce empty product list shows empty state.
- AI history empty state is visible.
- Messages empty state is visible.
- Profile asset empty states are visible.
- HTTP 401 on AI, Messages, Profile, and Settings account surfaces shows a
  login boundary, not raw technical error text.
- Detail pages without a valid ID show designed empty or boundary states.

No screen may use local fake data to avoid looking empty.

## Repair Stage Acceptance

`RB-001` through `RB-005` can be closed only when
`doc/development/verification/2026-06-07-repair-stage-verification.md` records
fresh command output summaries, screenshot paths, and a page-by-page comparison
against the Open Design HTML files. Any remaining deviation must stay open in
the repair log instead of being counted as passed.

The repair-stage execution plan itself is recorded in
`doc/development/verification/2026-06-07-repair-stage-plan.md`. That plan
requires detail evidence to be labeled `real-api`, `boundary`, or
`not-covered` so that UI smoke does not get mistaken for full backend flow
coverage.

## Documentation Acceptance

Development docs must describe Java/XML as the target architecture and must not
describe Kotlin/Compose as the active implementation target.
