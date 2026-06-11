# Current Status

> Updated: 2026-06-11

## Current Architecture

The Android client uses traditional Android Java + Activity/Fragment + XML. Android production and test source under `DouYu/app/src` must remain Java/XML only.

The retained runtime keeps the completed Java/XML screens and the matching `/api/v1` backend contract. Runtime data comes from backend APIs, and empty results render explicit empty states.

## Current Scope

In scope:

- Community feed, post compose, post detail, comments and upload.
- Search.
- Commerce browsing, cart, manual address snapshot entry and order creation.
- Notifications and notification detail.
- Profile, edit profile, followers/following and settings.
- Email/password register and login.
- OSS-backed image storage, upload presign/confirm, local/backend empty/error/login states.

Out of current scope:

- Refresh-token API and account cancellation.
- Private messages, conversations and message sending.
- Reward, checkin and badge APIs.
- Report, moderation, admin APIs and admin bootstrap.
- Address book, default address, address selection, order center, order detail and order cancellation.
- AI page, AI flow, AI Provider, visual model or large model generation.
- Payment page, WeChat/Alipay SDK/API, refund, reconciliation or production callback.
- Map API, location provider, map picker or automatic location fill.
- Real SMS Provider, production rate limiting or production risk control.
- Full compliance documents such as privacy policy, user agreement, SDK list, filing or complaint mechanism.

## Current Facts

- `AGENTS.md` defines the Java/XML Android rules, Open Design authority, static-data policy, and verification rules.
- Android production and test source under `DouYu/app/src` must have no `.kt` files.
- Android app/build/version-catalog files must not expose Compose, Navigation Compose, Kotlin serialization, Coil Compose, or Paging Compose dependencies.
- Android list screens must load through Retrofit + Gson repositories and render empty/error states instead of local mock/demo lists.
- Backend runtime seed/demo content must remain removed.
- New user default notifications are retained onboarding data, not demo content.
- Backend tests create required product, SKU, post, topic, sticker and notification data as explicit fixtures.
- Open Design HTML under `doc/development/open-design/` remains the UI authority for retained pages.

## Retained UI Authority

Retained main screens:

- Community: `community-home-a.html`
- Commerce: `commerce-home-a.html`
- Messages / notifications: `messages-a.html`
- Upload: `post-compose-a.html`
- Profile: `profile-a.html`

Retained flow screens:

- Search, login, register, post detail, notification detail, profile edit, profile lists and retained Settings sections.

Removed Open Design surfaces:

- Message conversation.
- AI, payment, future capability and compliance pages.
- Address book, order center, reward/checkin/badge, report and admin pages.

## Verification Snapshot

Current full verification must be rerun after Android/backend source changes.

Required gates:

- `rg --files DouYu/app/src | rg "\.kt$"`: no output.
- `rg -n "compose|Composable|Navigation Compose|kotlinx|MockData|coil\.compose|paging\.compose" DouYu/app`: no production/test source hits.
- Android unit tests, assemble, lint.
- Backend `mvn test`.
- Retained Open Design parity evidence or explicit not-covered status.
