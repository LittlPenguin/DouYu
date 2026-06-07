# Android Client

## Goal

Rewrite the Android app from Kotlin + Compose to traditional Android Java +
Activity/Fragment + XML.

## Target Architecture

- `MainActivity.java` hosts the top bar, Fragment content container, and bottom
  navigation.
- Five main Tab Fragments: Community, Commerce, AI, Messages, Profile.
- Secondary screens use Activities with explicit Intent extras.
- XML files own layout structure. Java classes bind views, state, navigation,
  API calls, and click handlers.
- Each Open Design main tab and major flow screen has a dedicated XML layout.
  Generic runtime UI shells such as `SimplePageActivity`, `activity_simple.xml`,
  or a single shared main-tab layout are not the target architecture.
- RecyclerView adapters render lists and grids.
- CameraX PreviewView handles camera capture.

## XML Screen Mapping

Main tabs:

- `community-home-a.html` -> `fragment_community_home.xml`
- `commerce-home-a.html` -> `fragment_commerce_home.xml`
- `ai-home-a.html` -> `fragment_ai_home.xml`
- `messages-a.html` -> `fragment_messages_home.xml`
- `profile-a.html` -> `fragment_profile_home.xml`

Flow screens:

- `search-a.html` -> `activity_search.xml`
- `post-compose-a.html` -> `activity_post_create.xml`
- `post-detail-comment-toolbar-a.html` -> `activity_post_detail.xml`
- `message-conversation-a.html` -> `activity_conversation.xml`
- `notification-detail-a.html` -> `activity_notification_detail.xml`
- `profile-edit-a.html` -> `activity_profile_edit.xml`
- `settings-home-a.html` -> `activity_settings_home.xml`
- `settings-account-security-a.html` -> `activity_settings_account_security.xml`
- `settings-privacy-permissions-a.html` -> `activity_settings_privacy_permissions.xml`
- `settings-notifications-a.html` -> `activity_settings_notifications.xml`
- `settings-about-compliance-a.html` -> `activity_settings_about_compliance.xml`
- `future-capability-ui-a.html` -> `activity_future_capability.xml`
- `doyu-design-directions.html` -> documented design authority; not an app
  runtime page.

## Required Java Packages

- `core`: constants, formatting, state, common UI helpers.
- `model`: Java POJO DTOs matching backend JSON.
- `network`: Retrofit client, API interfaces, upload transport.
- `data`: repositories that call real APIs only.
- `auth`: login and session flows.
- `community`, `commerce`, `ai`, `message`, `profile`: feature screens.
- `ui`: reusable adapters, empty/error/loading views, image helpers.

## Disallowed Android Content

- Kotlin files under `DouYu/app/src`.
- Jetpack Compose, Navigation Compose, Compose Material, Compose UI tests.
- Kotlin serialization, Kotlin coroutines, Kotlin DataStore.
- Runtime `MockData`, mock repositories, fake cards, fake orders, fake payments.
- Empty click handlers and fake success Toasts.

## Navigation Contract

Compose routes are replaced with Intent extras and Fragment tab state.

Keep these extra names:

- `postId`
- `productId`
- `conversationId`
- `notificationId`
- `uploadedFileId`
- `jobId`
- `patternId`
- `returnTo`

## Data Contract

The backend API remains under `/api/v1`. Java DTO field names must match the
existing API. Empty list responses are valid and must render empty states.

## UI Contract

Open Design HTML is authoritative. Java/XML screens must match the Open Design
structure and state model. Empty, loading, error, not logged in, disabled, and
UI-only states must be implemented explicitly.

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

The first command must produce no files. The second command must have no Android
production/test source hits except migration documentation or generated reports.
