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

The Android `.kt` check must return no files. Compose/Kotlin hits in Android production or test source fail acceptance. Backend seed hits in production code fail acceptance.

## Device Gate

- If the real device is unavailable, skip all `adb install`, `adb shell am instrument`, screenshot pull, and contact-sheet generation.
- In that state, record real-device UI parity as `not-covered`; do not claim Open Design pages passed visual verification.
- Resume device work only after the user explicitly says the device is available again.
- When resumed, run retained visual smoke coverage and pull screenshots to a dated verification directory.

## UI Acceptance

Each retained Open Design page must be mapped to an Android Java/XML screen:

- `community-home-a.html`
- `commerce-home-a.html`
- `messages-a.html`
- `profile-a.html`
- `search-a.html`
- `post-compose-a.html`
- `post-detail-comment-toolbar-a.html`
- `message-conversation-a.html`
- `notification-detail-a.html`
- `profile-edit-a.html`
- `profile-posts-a.html`
- `profile-following-a.html`
- `profile-followers-a.html`
- `settings-home-a.html`
- `settings-account-security-a.html`
- `settings-privacy-permissions-a.html`
- `settings-notifications-a.html`
- `login-a.html`
- `register-a.html`

For each retained screen, record screenshot evidence or write `not covered` with the reason. Do not reuse Kotlin/Compose screenshots as Java/XML evidence.

## Empty Data Acceptance

After runtime seed/demo removal:

- Community empty feed shows empty state.
- Commerce empty product list shows empty state.
- Messages empty state is visible.
- Profile asset empty states are visible.
- HTTP 401 on protected surfaces shows a login boundary, not raw technical error text.
- Detail pages without a valid ID show designed empty or boundary states.

No screen may use local fake data to avoid looking empty.

## Removed Feature Acceptance

Current documentation, Open Design, diagrams and verification evidence must not present these as current features:

- AI page, AI flow, AI Provider, visual model or large model generation.
- Payment page, payment boundary, WeChat/Alipay SDK/API, refund, reconciliation, production callback.
- Map API, location provider, map picker or automatic location fill.
- Real SMS Provider, production rate limiting, production risk control.
- Full compliance documents such as privacy policy, user agreement, SDK list, filing or complaint mechanism.

Address management is explicitly retained and must not be removed by the map/location cleanup.
OSS is explicitly retained and must not be removed or weakened by this cleanup.

## Documentation Acceptance

Development docs must describe Java/XML as the current Android architecture and must not describe Kotlin/Compose as the active implementation target.
