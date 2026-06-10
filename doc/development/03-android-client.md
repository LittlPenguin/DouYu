# Android Client

## Goal

Maintain the Android app as a traditional Android Java + Activity/Fragment + XML client.

## Target Architecture

- `MainActivity.java` hosts the top bar, Fragment content container, and bottom navigation.
- Current main tabs: Community, Commerce, Messages, Profile. A publish entry may be exposed through the top action or shell shortcut.
- Secondary screens use Activities with explicit Intent extras.
- XML files own layout structure. Java classes bind views, state, navigation, API calls, and click handlers.
- Each retained Open Design main tab and major flow screen has a dedicated XML layout.
- RecyclerView adapters render lists and grids.
- CameraX PreviewView handles camera capture only for retained post/image capture flows.

## XML Screen Mapping

Main tabs:

- `community-home-a.html` -> `fragment_community_home.xml`
- `commerce-home-a.html` -> `fragment_commerce_home.xml`
- `messages-a.html` -> `fragment_messages_home.xml`
- `profile-a.html` -> `fragment_profile_home.xml`

Flow screens:

- `login-a.html` -> `activity_login.xml`
- `register-a.html` -> `activity_register.xml`
- `search-a.html` -> `activity_search.xml`
- `post-compose-a.html` -> `activity_post_create.xml`
- `post-detail-comment-toolbar-a.html` -> `activity_post_detail.xml`
- `message-conversation-a.html` -> `activity_conversation.xml`
- `notification-detail-a.html` -> `activity_notification_detail.xml`
- `profile-edit-a.html` -> `activity_profile_edit.xml`
- `profile-posts-a.html` -> `activity_profile_posts.xml`
- `profile-following-a.html` / `profile-followers-a.html` -> `activity_profile_users.xml`
- `settings-home-a.html` -> `activity_settings_home.xml`
- `settings-account-security-a.html` -> `activity_settings_account_security.xml`
- `settings-privacy-permissions-a.html` -> `activity_settings_privacy_permissions.xml`
- `settings-notifications-a.html` -> `activity_settings_notifications.xml`
- `doyu-design-directions.html` -> documented design authority; not an app runtime page.

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
- AI page, AI flow, payment boundary, map/location provider, production compliance page, or production provider completion claims.

## Navigation Contract

Navigation uses Intent extras and Fragment tab state.

Keep these extra names for retained flows:

- `postId`
- `productId`
- `conversationId`
- `notificationId`
- `uploadedFileId`
- `returnTo`

Do not introduce AI job/pattern extras, payment result extras, or map/location extras for current runtime flows.

## Data Contract

The backend API remains under `/api/v1`. Java DTO field names must match the existing API. Empty list responses are valid and must render empty states.

Address fields and address management requirements remain in scope for commerce/order flows. Removing map/location does not remove manual address fields.

OSS-backed image storage, upload presign/confirm, Alibaba OSS configuration and backend upload/oss provider remain in scope.

## UI Contract

Open Design HTML is authoritative for retained pages. Java/XML screens must match the retained Open Design structure and state model. Empty, loading, error, not logged in, disabled, and UI-only states must be implemented explicitly.

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
