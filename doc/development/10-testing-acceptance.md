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

## Retained UI Acceptance

Each retained Open Design page must be mapped to an Android Java/XML screen:

- `community-home-a.html`
- `commerce-home-a.html`
- `messages-a.html`
- `profile-a.html`
- `search-a.html`
- `post-compose-a.html`
- `post-detail-comment-toolbar-a.html`
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

- Community empty feed shows empty state.
- Commerce empty product list shows empty state.
- Notifications empty state is visible.
- Profile asset empty states are visible.
- HTTP 401 on protected surfaces shows a login boundary.
- Detail pages without a valid ID show designed empty or boundary states.

No screen may use local fake data to avoid looking empty.

## Settings Acceptance

- Account and Security loads `GET /api/v1/users/me`, shows real email/account status/session state, and only supports local logout.
- Local logout clears `SessionStore` and does not call refresh, logout or account-cancel APIs.
- Privacy and Permissions reads real Android camera and notification permission state, can request camera and Android 13+ notification runtime permissions, and can open the OS app settings page.
- Privacy switches save through `PATCH /api/v1/users/me/settings` and roll back or show retry on failure.
- Notification switches save through `PATCH /api/v1/users/me/settings`; they must not imply message history deletion, push-provider completion, or private-message support.
- Help and About shows real build/API/session diagnostics and only exposes executable actions such as copy diagnostics or open system app settings.

## Search Acceptance

- `SearchActivity` must not contain UI-only/fake-result copy.
- Search input, clear, cancel, retry, and filters must have executable behavior.
- `GET /api/v1/search` supports `all`, `posts`, `products`, `users`, and `topics`.
- Empty keyword renders an input/empty state, not local hot lists.
- Post results open post detail; product results open product detail; user and topic results expose real returned data without fake navigation.

## Commerce Acceptance

- Product detail quantity is limited by backend SKU stock.
- Add-to-cart and order creation only show success after backend API success.
- Product detail and cart checkout use user-entered address snapshots.
- Android and backend runtime must not introduce payment endpoints, payment pages, payment results, fake payment params, refund APIs or callback routes.
- Order list, order detail and cancel-order APIs are not retained.

## Removed Feature Acceptance

Current documentation, Open Design, diagrams and runtime source must not present these as current features:

- Refresh-token API, service logout API or account cancellation.
- Private messages, conversations, message sending, mutual-follow send limits or notification object jumps.
- Reward, checkin or badge.
- Report, moderation or admin APIs.
- Address book, default address, address management pages, order center, order detail or cancel order.
- AI page, AI flow, AI Provider, visual model or large model generation.
- Payment page, payment boundary, WeChat/Alipay SDK/API, refund, reconciliation or production callback.
- Map API, location provider, map picker or automatic location fill.
- Real SMS Provider, production rate limiting or production risk control.
- Full compliance documents such as privacy policy, user agreement, SDK list, filing or complaint mechanism.

OSS upload is explicitly retained and must not be removed or weakened by this cleanup.
