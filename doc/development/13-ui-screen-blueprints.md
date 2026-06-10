# UI Screen Blueprints

## Purpose

This file maps retained Open Design screens to the current Java/XML Android client.

## Main Navigation

Bottom navigation order is `社区 / 商城 / 上传 / 消息 / 我的`.

- `社区`, `商城`, `消息`, and `我的` are content tabs backed by Fragment screens.
- `上传` is an action item. It opens `PostCreateActivity` after the login gate and must not replace the current Fragment.

### Community

Reference: `open-design/community-home-a.html`
Android XML: `fragment_community_home.xml`

Required structure:

- Main top bar.
- Channel row.
- Two-column content grid or masonry-style RecyclerView.
- Empty feed state when backend returns no posts.
- Bottom navigation with upload action between commerce and messages.

### Commerce

Reference: `open-design/commerce-home-a.html`
Android XML: `fragment_commerce_home.xml`

Required structure:

- Search or filter entry.
- Backend-driven category chips with a default featured tab.
- Two-column masonry product grid from backend data; image height follows returned cover dimensions.
- Empty product state without mock products.
- Order/address boundary copy without payment success claims.

### Messages

Reference: `open-design/messages-a.html`
Android XML: `fragment_messages_home.xml`

Required structure:

- Notifications section above messages section.
- Conversation rows with avatar, peer name, and last message summary only.
- Notification rows with detail navigation based on returned list data.
- Empty state when no messages or notifications exist.

### Profile

Reference: `open-design/profile-a.html`
Android XML: `fragment_profile_home.xml`

Required structure:

- User header or login prompt.
- Stats row. `获赞` opens a source explanation dialog; `作品`, `关注`, and `粉丝` navigate to dedicated real-data list screens.
- Asset tabs for posts, liked posts, and favorites.
- Profile edit entry.
- Empty asset states without mock assets.

## Flow Screens

- Login follows `login-a.html`; Android XML is `activity_login.xml`.
- Register follows `register-a.html`; Android XML is `activity_register.xml`.
- Search follows `search-a.html`; Android XML is `activity_search.xml`; global search remains UI-only unless backend support exists.
- Post detail follows `post-detail-comment-toolbar-a.html`; Android XML is `activity_post_detail.xml`.
- Post compose follows `post-compose-a.html`; Android XML is `activity_post_create.xml`. Required states include logged-out gate, title/body validation, topic loading or empty state, image picker, CameraX capture, OSS upload, failed upload retry/delete, 9 image limit, preview, publish disabled states, and backend `REVIEWING` success.
- Conversation follows `message-conversation-a.html`; Android XML is `activity_conversation.xml`.
- Notification detail follows `notification-detail-a.html`; Android XML is `activity_notification_detail.xml`.
- Profile edit follows `profile-edit-a.html`; Android XML is `activity_profile_edit.xml`.
- Profile posts follows `profile-posts-a.html`; Android XML is `activity_profile_posts.xml`.
- Profile following and followers follow `profile-following-a.html` and `profile-followers-a.html`; Android XML is `activity_profile_users.xml`.
- Settings follows retained settings files: `settings-home-a.html`, `settings-account-security-a.html`, `settings-privacy-permissions-a.html`, and `settings-notifications-a.html`.
- `doyu-design-directions.html` records the selected visual direction and is not a runtime Android screen.

## Data and State Rules

- Lists render backend data only.
- Empty backend results render empty states.
- Unavailable retained flows render disabled or UI-only states.
- Pure UI-only pages must not create new API claims.
- Logged-out protected actions show a confirmation dialog before opening Login: profile edit, upload work, comments, likes, favorites, follow, cart and order actions.
- Address management remains in scope. Manual address fields must remain available even though map/location is removed.
- OSS-backed image storage and upload presign/confirm remain in scope.

## Acceptance

Each retained screen passes when the Java/XML implementation:

- Uses the Open Design structure.
- Has loading, empty, error, and disabled states where applicable.
- Does not render fake content.
- Does not expose empty clicks.
- Has screenshot evidence or is recorded as not covered.

## Evidence Rules

Screenshot evidence must not reference AI/payment/future capability pages as current coverage. Historical evidence records may mention older screenshots only as past artifacts, but current parity matrices must use retained screen names.
