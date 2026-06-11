# SpellBean Development Docs

This directory describes the current completed product scope after pruning unfinished runtime surfaces.

## Current Scope

Retained:

- Login/register and local logout.
- Community, upload, post detail, comments, likes, favorites and follows.
- Search.
- Commerce browse, cart and `POST /api/v1/orders` order creation.
- Profile and settings.
- Notifications and notification detail.
- OSS-backed image upload.

Removed:

- Refresh token API and account cancellation.
- Private messages, conversations and message sending.
- Reward, checkin and badge.
- Report, moderation and admin APIs.
- Address book, default address, address selection, order center, order detail and cancel order.
- AI, payment, map/location, real SMS, production risk control and full compliance pages.

## Source Of Truth

1. Current code and fresh test output.
2. `current-status.md`.
3. API contract: `05-api-contract.md`.
4. Android contract: `03-android-client.md`.
5. Backend contract: `04-backend-services.md`.
6. UI authority: retained files under `open-design/`.
7. Verification requirements: `10-testing-acceptance.md`.

## Key Docs

| File                                   | Purpose                                                                      |
| -------------------------------------- | ---------------------------------------------------------------------------- |
| `02-architecture.md`                   | Current retained architecture and removed surfaces.                          |
| `03-android-client.md`                 | Java/XML Android structure, screen mapping and prohibited Android content.   |
| `04-backend-services.md`               | Backend modules, notifications, order creation and removed backend surfaces. |
| `05-api-contract.md`                   | Current `/api/v1` contract.                                                  |
| `06-data-model.md`                     | Retained tables, removed tables and current DTO boundaries.                  |
| `08-commerce-orders-address.md`        | Commerce, cart, order creation and manual address snapshot boundaries.       |
| `10-testing-acceptance.md`             | Android/backend/static verification gates.                                   |
| `11-ui-style-guide.md`                 | Retained UI authority and prohibited UI behavior.                            |
| `12-feature-and-flow-map.md`           | Current feature flow map and explicit non-goals.                             |
| `13-ui-screen-blueprints.md`           | Open Design to Java/XML screen mapping.                                      |
| `14-frontend-backend-collaboration.md` | Android/backend integration points and removed API paths.                    |
| `18-unfinished-and-blockers.md`        | Current removed/not-covered scope.                                           |

## Retained Open Design Pages

| File                                  | Purpose                            |
| ------------------------------------- | ---------------------------------- |
| `community-home-a.html`               | Community home.                    |
| `commerce-home-a.html`                | Commerce home.                     |
| `messages-a.html`                     | Notifications list.                |
| `notification-detail-a.html`          | Notification detail.               |
| `profile-a.html`                      | Profile home.                      |
| `search-a.html`                       | Search.                            |
| `post-compose-a.html`                 | Upload/post compose.               |
| `post-detail-comment-toolbar-a.html`  | Post detail and comments.          |
| `profile-edit-a.html`                 | Edit profile.                      |
| `profile-posts-a.html`                | Profile posts.                     |
| `profile-following-a.html`            | Following list.                    |
| `profile-followers-a.html`            | Followers list.                    |
| `settings-home-a.html`                | Settings home.                     |
| `settings-account-security-a.html`    | Account/security and local logout. |
| `settings-privacy-permissions-a.html` | Privacy and system permissions.    |
| `settings-notifications-a.html`       | Notification preferences.          |
| `login-a.html`                        | Login.                             |
| `register-a.html`                     | Register.                          |
| `index.html`                          | Open Design entry.                 |

## Minimum Static Checks

```powershell
rg --files DouYu/app/src | rg "\.kt$"
rg -n "compose|Composable|Navigation Compose|kotlinx|MockData|coil.compose|paging.compose" DouYu/app
Run `RemovedFeatureContractTests` for deleted API path coverage, then scan Android and backend runtime source for removed feature names before release.
```

If `DouYu/` or `doyu-server/` source changes, run the matching Android/backend test gates from `10-testing-acceptance.md`.
