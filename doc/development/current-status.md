# Current Status

> Updated: 2026-06-09

## Current Architecture

The Android client currently uses traditional Android Java + Activity/Fragment + XML. Android production and test source under `DouYu/app/src` must remain Java/XML only.

The client shell, feature entry screens, model, network, repository, layouts, resources, and unit tests are maintained as Java/XML code while preserving the existing `/api/v1` backend contract.

## Current Scope

In scope:

- Community feed, post compose, post detail, comments and upload.
- Commerce browsing, cart, order confirmation, order state display and address management.
- Messages, notifications and conversation.
- Profile, edit profile, followers/following and settings.
- OSS-backed image storage, upload presign/confirm, local/backend empty/error/login states.

Out of current scope:

- AI page, AI flow, AI Provider, visual model or large model generation.
- Payment page, WeChat/Alipay SDK/API, refund, reconciliation or production callback.
- Map API, location provider, map picker or automatic location fill.
- Real SMS Provider, production rate limiting or production risk control.

- Full compliance documents such as privacy policy, user agreement, SDK list, filing or complaint mechanism.

OSS-backed image storage, upload presign/confirm, Alibaba OSS configuration and backend upload/oss provider stay in scope.

Address management remains in scope. Removing map/location does not remove address fields or address management requirements.

## Current Facts

- `AGENTS.md` defines the Java/XML Android rules, Open Design authority, static-data policy, and verification rules.
- Android production and test source under `DouYu/app/src` must have no `.kt` files.
- Android app/build/version-catalog files must not expose Compose, Navigation Compose, Kotlin serialization, Coil Compose, or Paging Compose dependencies.
- Android list screens must load through Retrofit + Gson repositories and render empty/error states instead of local mock/demo lists.
- Backend runtime seed/demo content must remain removed.
- Backend tests create required product, SKU, post, topic, and sticker data as explicit test fixtures.
- Open Design HTML under `doc/development/open-design/` remains the UI authority for retained pages.

## Target Android Stack

- Java source only for Android production and test code.
- XML layouts and drawable resources for UI.
- Main Activity + retained main Tab Fragments + secondary Activities.
- Dedicated XML layout per retained Open Design main tab and flow screen.
- AppCompat or AndroidX Activity/Fragment, Material Components, ConstraintLayout, RecyclerView, Retrofit + Gson, OkHttp, Glide, CameraX View.
- SharedPreferences for Java login/session persistence.

## Target UI Authority

Retained main screens:

- Community: `community-home-a.html`
- Commerce: `commerce-home-a.html`
- Messages: `messages-a.html`
- Profile: `profile-a.html`

Retained flow screens:

- Search, login, register, post compose, post detail, notification detail, conversation, profile edit, profile lists and retained Settings sections.

UI parity means structure, hierarchy, spacing, copy, states, and interactions. When real data is empty, the app must render the designed empty state instead of filling the page with fake content.

## Static Data Policy

Removed runtime demo data:

- Android `MockData`, mock repositories, demo lists, fake orders, fake payment parameters.
- Backend seed posts, products, topics, stickers, system demo users, static seed images, and `/seed/**` public exposure.

Retained development boundaries:

- Fixed local verification code `123456` for development login.
- OSS-backed image storage and upload presign/confirm for project images.
- Empty API results, login prompts, disabled states and error states.

## Verification Snapshot

Current full verification must be rerun by the implementation/review thread after Android/backend source changes. This documentation Worker did not run Android or backend builds.

Required gates:

- `rg --files DouYu/app/src | rg "\.kt$"`: no output.
- `rg -n "compose|Composable|Navigation Compose|kotlinx|MockData|coil\.compose|paging\.compose" DouYu/app`: no production/test source hits.
- `rg -n "DataInitializer|static/seed|/seed/|post_seed|prod_|seedTopic|seedProduct|seedPost" doyu-server/src/main doyu-server/src/test`: no runtime seed/demo hits.
- Android unit tests, assemble, lint.
- Backend `mvn test`.
- Retained Open Design parity evidence.

## Coverage State

Covered by this Worker:

- Documentation index cleanup.
- Open Design removal/reference cleanup for AI, payment/future capability and full compliance pages.
- Diagrams index cleanup and removed diagram deletion.
- Screenshot evidence file cleanup for AI/payment/future capability page evidence paths.

Not covered by this Worker:

- Android source cleanup.
- Backend source cleanup.
- Android build/test/lint.
- Backend tests.
- Fresh real-device screenshots.

## Current Active Work

This repository is in the middle of a multi-agent cleanup loop. Do not revert other workers' changes. Workers must keep to their assigned scope and report residual search terms for the main thread.
