# UI Screen Blueprints

## Purpose

This file maps retained Open Design screens to the current Java/XML Android client.

## Main Navigation

Bottom navigation order is `社区 / 商城 / 上传 / 消息 / 我的`.

- `社区`, `商城`, `上传`, `消息`, and `我的` are Fragment tabs inside `MainActivity`.
- `上传` opens `PostCreateFragment` inside the main shell.
- Content-tab routing is centralized in `MainActivity`: first launch and `onNewIntent` both consume `IntentExtras.SECTION`.

## Main Screens

### Community

Reference: `open-design/community-home-a.html`  
Android XML: `fragment_community_home.xml`

- Main top bar.
- Channel row.
- Two-column content grid or masonry-style RecyclerView.
- Empty feed state when backend returns no posts.

### Commerce

Reference: `open-design/commerce-home-a.html`  
Android XML: `fragment_commerce_home.xml`

- Backend-driven category chips.
- Two-column product grid from backend data.
- Empty product state without mock products.
- Cart entry.

### Upload

Reference: `open-design/post-compose-a.html`  
Android XML: `fragment_post_create.xml`

- Logged-out gate.
- Title/body validation.
- Topic loading or empty state.
- Image picker and CameraX capture.
- OSS upload, failed upload retry/delete, 9 image limit.
- Preview, disabled states and backend `VISIBLE` publish success.

### Messages

Reference: `open-design/messages-a.html`  
Android XML: `fragment_messages_home.xml`

- Notification list only.
- Notification rows open notification detail.
- Empty state when no notifications exist.
- No private-message section, conversation rows, send box or mutual-follow quota state.

### Profile

Reference: `open-design/profile-a.html`  
Android XML: `fragment_profile_home.xml`

- User header or login prompt.
- Stats row.
- Asset tabs for posts, liked posts and favorites.
- Profile edit entry.
- Empty asset states without mock assets.

## Flow Screens

- Login follows `login-a.html`; Android XML is `activity_login.xml`.
- Register follows `register-a.html`; Android XML is `activity_register.xml`.
- Search follows `search-a.html`; Android XML is `activity_search.xml`.
- Post detail follows `post-detail-comment-toolbar-a.html`; Android XML is `activity_post_detail.xml`.
- Notification detail follows `notification-detail-a.html`; Android XML is `activity_notification_detail.xml`.
- Profile edit follows `profile-edit-a.html`; Android XML is `activity_profile_edit.xml`.
- Profile posts follows `profile-posts-a.html`; Android XML is `activity_profile_posts.xml`.
- Profile following and followers follow `profile-following-a.html` and `profile-followers-a.html`; Android XML is `activity_profile_users.xml`.
- Settings follows retained settings files: `settings-home-a.html`, `settings-account-security-a.html`, `settings-privacy-permissions-a.html`, and `settings-notifications-a.html`.
- `doyu-design-directions.html` records the selected visual direction and is not a runtime Android screen.

Removed flow screens:

- Private-message and conversation screens.
- Any address book, order center, reward/checkin/badge, report/admin, AI, payment, map or compliance screen.

## Data and State Rules

- Lists render backend data only.
- Empty backend results render empty states.
- Logged-out protected actions show a confirmation dialog before opening Login.
- Manual order address fields remain on product detail and cart checkout.
- Address management is not retained as a separate product feature.
- OSS-backed image storage and upload presign/confirm remain in scope.

## Acceptance

Each retained screen passes when the Java/XML implementation:

- Uses the retained Open Design structure.
- Has loading, empty, error, and disabled states where applicable.
- Does not render fake content.
- Does not expose empty clicks.
- Has screenshot evidence or is recorded as not covered.
