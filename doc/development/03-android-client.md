# Android Client

## Goal

Maintain the Android app as a traditional Android Java + Activity/Fragment + XML client.

## Target Architecture

- `MainActivity.java` hosts the top bar, Fragment content container, and bottom navigation.
- Current main tabs: Community, Commerce, Upload, Messages, Profile.
- Secondary screens use Activities with explicit Intent extras.
- XML files own layout structure. Java classes bind views, state, navigation, API calls, and click handlers.
- RecyclerView adapters render lists and grids.
- CameraX PreviewView handles camera capture only for retained post/image capture flows.

## XML Screen Mapping

Main tabs:

- `community-home-a.html` -> `fragment_community_home.xml`
- `commerce-home-a.html` -> `fragment_commerce_home.xml`
- `post-compose-a.html` -> `fragment_post_create.xml`
- `messages-a.html` -> `fragment_messages_home.xml`
- `profile-a.html` -> `fragment_profile_home.xml`

Flow screens:

- `login-a.html` -> `activity_login.xml`
- `register-a.html` -> `activity_register.xml`
- `search-a.html` -> `activity_search.xml`
- `post-detail-comment-toolbar-a.html` -> `activity_post_detail.xml`
- `notification-detail-a.html` -> `activity_notification_detail.xml`
- `profile-edit-a.html` -> `activity_profile_edit.xml`
- `profile-posts-a.html` -> `activity_profile_posts.xml`
- `profile-following-a.html` / `profile-followers-a.html` -> `activity_profile_users.xml`
- `settings-home-a.html` -> `activity_settings_home.xml`
- `settings-account-security-a.html` -> `activity_settings_account_security.xml`
- `settings-privacy-permissions-a.html` -> `activity_settings_privacy_permissions.xml`
- `settings-notifications-a.html` -> `activity_settings_notifications.xml`
- `doyu-design-directions.html` -> documented design authority; not an app runtime page.

Removed mappings:

- Private-message and conversation screens.
- Any AI, payment, map, compliance, address book, order center, reward, report or admin screen.

## Required Java Packages

- `core`: constants, formatting, state, common UI helpers.
- `model`: Java POJO DTOs matching backend JSON.
- `network`: Retrofit client, API interfaces, upload transport.
- `data`: repositories that call real APIs only.
- `auth`: login and session flows.
- `community`, `commerce`, `message`, `profile`: retained feature screens.
- `ui`: reusable adapters, empty/error/loading views, image helpers.

## Disallowed Android Content

- Kotlin files under `DouYu/app/src`.
- Jetpack Compose, Navigation Compose, Compose Material, Compose UI tests.
- Kotlin serialization, Kotlin coroutines, Kotlin DataStore.
- Runtime `MockData`, mock repositories, fake cards, fake orders, fake payment parameters.
- Empty click handlers and fake success Toasts.
- AI, payment, map/location, compliance, private-message, account-cancel, reward, report, admin, address-book or order-center pages.

## Navigation Contract

Navigation uses Intent extras and Fragment tab state.

- Main bottom navigation section ids: `community`, `commerce`, `upload`, `messages`, `profile`.
- `MainActivity` owns the section-to-tab mapping and must handle first launch and reused-instance navigation via `onNewIntent`.
- `上传` is a main-shell `PostCreateFragment` tab.
- After a successful post publish, `PostCreateFragment` clears the upload draft before opening `PostDetailActivity`.
- Publish-success detail launches carry `returnTo=community`; back/up from that detail returns to the `MainActivity` community tab instead of the upload tab.

Keep these extra names for retained flows:

- `postId`
- `productId`
- `notificationId`
- `uploadedFileId`
- `returnTo`

Do not introduce conversation, AI job/pattern, payment result, map/location, reward, report or admin extras for current runtime flows.

## Data Contract

The backend API remains under `/api/v1`. Java DTO field names must match the API. Empty list responses are valid and must render empty states.

OSS-backed image storage, upload presign/confirm, Alibaba OSS configuration and backend upload/oss provider remain in scope.

Android must normalize backend image URLs that point at loopback/local OSS hosts before rendering them on a device. This applies to post images, comment media and user `avatarUrl`.

Settings is a real account and device-state surface:

- `GET /api/v1/users/me` drives account/security identity fields such as email, account status and manual profile region.
- `GET /api/v1/users/me/settings` and `PATCH /api/v1/users/me/settings` persist privacy and notification preferences.
- System permission rows read real Android permission state for camera and notifications.
- Notification preferences only control app reminder preferences. They do not delete notification records or claim that a push provider is complete.
- Logout clears local session state.
- Help/About shows real build and API information.

Commerce purchase data must come from backend:

- Product detail loads `/api/v1/products/{productId}`.
- Quantity selection is limited to available SKU stock.
- Add-to-cart uses `POST /api/v1/cart/items`.
- Immediate order uses `POST /api/v1/orders` with real selected SKU and manual address snapshot fields.
- Cart order uses selected backend cart item ids and manual address snapshot fields.
- Created orders may display order ID and `CREATED` status, but must not display payment, refund, order-center, order-detail or cancellation claims.

## Verification

Required checks:

```powershell
rg --files DouYu/app/src | rg "\.kt$"
rg -n "compose|Composable|Navigation Compose|kotlinx|MockData" DouYu/app
cd DouYu
.\gradlew.bat :app:testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
.\gradlew.bat :app:lintDebug --console=plain
```

The first command must produce no files. The second command must have no Android production/test source hits except historical documentation or generated reports.
