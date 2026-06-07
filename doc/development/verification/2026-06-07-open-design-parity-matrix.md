# 2026-06-07 Open Design Parity Matrix

## Evidence Set

- Latest repair-stage device: `10.64.241.158:40739`, model `ELI-AN00`, screen
  `1200x2664`, density `520`.
- Latest repair-stage screenshot directories:
  - `doc/development/verification/android-java-xml-screenshots/repair-stage/visual-smoke`
  - `doc/development/verification/android-java-xml-screenshots/repair-stage/real-backend-smoke`
- Latest repair-stage contact sheets:
  - `doc/development/verification/android-java-xml-screenshots/repair-stage/visual-smoke-contact-sheet.png`
  - `doc/development/verification/android-java-xml-screenshots/repair-stage/real-backend-smoke-contact-sheet.png`
- Instrumentation:
  - `VisualSmokeInstrumentedTest`: `OK (1 test)`, 22 screenshots,
    1200x2664 each.
  - `RealBackendSmokeInstrumentedTest`: `OK (1 test)`, 5 real-ID screenshots,
    1200x2664 each.
- Historical real-device evidence remains under
  `doc/development/verification/android-java-xml-screenshots/real-device/`.

## Acceptance Meaning

This matrix records Java/XML screen coverage against the 18 Open Design HTML references. It verifies structure, visible hierarchy, state coverage, navigation surfaces, and no runtime static filler. It is not a pixel-diff report because the Open Design HTML is a browser phone mock while the Android evidence is a real device with system status/navigation bars.

| Open Design reference | Java/XML screen | Real-device evidence | Status | Notes |
| --- | --- | --- | --- | --- |
| `community-home-a.html` | `MainActivity` + `CommunityFragment` / `fragment_community_home.xml` | `main_community.png`, `main_quick_menu.png` | Covered | Top add/title/search shell, quick menu, chips, state card, error boundary, and bottom brand nav are visible. Empty/error state replaces deleted mock content. |
| `commerce-home-a.html` | `MainActivity` + `CommerceFragment` / `fragment_commerce_home.xml` | `main_commerce.png` | Covered | Category chips, commerce hero/state card, error boundary, and bottom nav visible. Runtime products come from API only. |
| `ai-home-a.html` | `MainActivity` + `AiFragment` / `fragment_ai_home.xml` | `main_ai.png` | Covered | Upload/photo/history/UI-only tabs, AI state card, backend failure boundary, and AI bottom nav visible. |
| `messages-a.html` | `MainActivity` + `MessagesFragment` / `fragment_messages_home.xml` | `main_messages.png` | Covered | Private/notification/system tabs, state card, error boundary, and bottom nav visible. No local fake conversations. |
| `profile-a.html` | `MainActivity` + `ProfileFragment` / `fragment_profile_home.xml` | `main_profile.png` | Covered | Profile tabs, asset state card, error boundary, and bottom nav visible. Profile assets are not locally faked. |
| `search-a.html` | `SearchActivity` / `activity_search.xml` | `search.png` | Covered | Search input, scope chips, UI-only state, empty result boundary, and visible disabled boundary CTA match design intent without fake results. |
| `post-compose-a.html` | `PostCreateActivity` / `activity_post_create.xml` | `post_create.png` | Covered | Title/body boundary, upload grid, tags, preview card, upload/review warning, and visible UI-only CTA are present. No named demo post remains. |
| `post-detail-comment-toolbar-a.html` | `PostDetailActivity` / `activity_post_detail.xml` | `post_detail.png`, `real_post_detail.png` | Covered | Image-first gallery panel, content card, tags, comment section, and bottom comment toolbar are visible. The repair-stage real-ID screenshot uses a backend-created post ID. |
| `message-conversation-a.html` | `ConversationActivity` / `activity_conversation.xml` | `conversation.png`, `real_conversation.png` | Covered | Conversation ID, send rules, send failure boundary, and disabled send state visible. The repair-stage real-ID screenshot uses a QA fixture conversation tied to the logged-in user. |
| `notification-detail-a.html` | `NotificationDetailActivity` / `activity_notification_detail.xml` | `notification_detail.png`, `real_notification_detail.png` | Covered | Notification ID/title/body/boundary cards visible. The repair-stage real-ID screenshot uses a QA fixture notification summary. |
| `profile-edit-a.html` | `ProfileEditActivity` / `activity_profile_edit.xml` | `profile_edit.png` | Covered | Avatar, nickname validation, bio boundary, and UI-only profile fields visible. |
| `settings-home-a.html` | `SettingsActivity` / `activity_settings_home.xml` | `settings_home.png` | Covered | Settings hero, account/security, privacy, notification, about/compliance, future capability, and compliance note visible. |
| `settings-account-security-a.html` | `SettingsActivity` section / `activity_settings_account_security.xml` | `settings_account_security.png` | Covered | Account state, dangerous operation confirmation boundary, and incomplete auth/payment/security warnings visible. |
| `settings-privacy-permissions-a.html` | `SettingsActivity` section / `activity_settings_privacy_permissions.xml` | `settings_privacy_permissions.png` | Covered | Minimum authorization, permission status, and privacy switches visible. |
| `settings-notifications-a.html` | `SettingsActivity` section / `activity_settings_notifications.xml` | `settings_notifications.png` | Covered | Notification grouping, persistence rule, and private limit boundary visible. |
| `settings-about-compliance-a.html` | `SettingsActivity` section / `activity_settings_about_compliance.xml` | `settings_about_compliance.png` | Covered | About/dev status, production pending material, and prohibited claims visible. |
| `future-capability-ui-a.html` | `FutureCapabilityActivity` / `activity_future_capability.xml` | `future_capability.png` | Covered | Map, real payment, and model generation API boundaries visible as UI-only. |
| `doyu-design-directions.html` | Global design system applied across Java/XML screens | all screenshots, especially `main_community.png`, `main_quick_menu.png`, `settings_home.png` | Covered | Brand colors, rounded card language, chip system, top bars, bottom nav, and explicit state boundaries are consistently applied. |

## Known Accepted Differences

- Android screenshots include the physical device status bar and gesture navigation bar; Open Design HTML uses an embedded mock phone frame.
- Empty/error states appear in place of populated example cards when backend data is unavailable, by design, because runtime mock/seed filling is prohibited.
- UI-only provider boundaries are visible for search, payment, map/location, full compliance documents, and production AI/payment providers; this is the documented functional boundary, not a parity failure.
- Camera preview content is environment-dependent; `camera.png` verifies the
  CameraX preview/capture boundary.

## Final Parity Conclusion

The repair-stage Java/XML real-device evidence covers all 18 Open Design
references in structure, hierarchy, state handling, navigation surfaces, and
documented functional boundaries. `RealBackendSmokeInstrumentedTest` separately
covers supported real API-backed detail flows for post detail, product detail,
AI job/detail, conversation, and notification detail. No new UI deviation was
found in the repair-stage contact-sheet review.
