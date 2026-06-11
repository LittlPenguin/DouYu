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

## Settings Acceptance

Settings pages must be real Java/XML surfaces, not placeholder explanation pages.

- Account and Security loads `GET /api/v1/users/me`, shows real email/account status/session state, and uses backend logout/cancel-account requests for destructive actions.
- Privacy and Permissions reads real Android camera and notification permission state, can request camera and Android 13+ notification runtime permissions, and must open the OS app settings page for manual permission changes instead of showing fake success.
- Privacy switches save through `PATCH /api/v1/users/me/settings` and roll back or show retry on failure.
- Notification switches save through `PATCH /api/v1/users/me/settings`; they must not imply message history deletion, push-provider completion, or private-message rule changes.
- Help and About shows real build/API/session diagnostics and only exposes executable actions such as copy diagnostics or open system app settings.
- Open Design parity records must cover settings home, account/security, privacy/permissions, notifications, and help/about. If no device is available, record these as `not-covered`.

## Search Acceptance

Search must be a real Java/XML surface backed by retained backend APIs.

- `SearchActivity` must not contain `UI-only`, `待接入`, `开发态`, or fake-result copy.
- Search input, clear, cancel, retry, and all range filters must have executable behavior.
- `GET /api/v1/search` must support `all`, `posts`, `products`, `users`, and `topics`.
- Empty keyword must render an input/empty state, not local hot lists or personalized recommendations.
- Post results open post detail; product results open product detail; user and topic results expose real returned data without fake navigation.
- Android API/repository tests must cover search endpoint mapping and request type/keyword propagation.
- Backend tests must cover mixed results, type filters, empty keyword, visible/on-sale boundaries, and no payment/map/AI/SMS runtime additions.

## Removed Feature Acceptance

Current documentation, Open Design, diagrams and verification evidence must not present these as current features:

- AI page, AI flow, AI Provider, visual model or large model generation.
- Payment page, payment boundary, WeChat/Alipay SDK/API, refund, reconciliation, production callback.
- Map API, location provider, map picker or automatic location fill.
- Real SMS Provider, production rate limiting, production risk control.
- Full compliance documents such as privacy policy, user agreement, SDK list, filing or complaint mechanism.

Address management is explicitly retained and must not be removed by the map/location cleanup.
OSS is explicitly retained and must not be removed or weakened by this cleanup.

## 商品购买验收

- 商品详情页数量控件使用后端 SKU 库存，且不得超过可用库存。
- 加入购物车和立即下单只能在后端 API 成功后展示成功状态。
- 商品详情、购物车、数量、收货信息和订单创建相关 UI 文案必须使用自然中文。
- 后端购物车和订单测试覆盖自营边界、库存校验、立即下单、购物车下单、幂等和取消订单释放库存。
- Android API/repository 测试覆盖购物车和订单 Retrofit 映射。
- Android 或后端运行时不得引入支付端点、支付页面、支付结果、虚假支付参数、退款 API 或回调路由。

## Documentation Acceptance

Development docs must describe Java/XML as the current Android architecture and must not describe Kotlin/Compose as the active implementation target.
