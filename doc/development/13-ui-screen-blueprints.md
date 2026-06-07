# UI Screen Blueprints

## Purpose

This file maps the Open Design screens to the Java/XML Android rewrite.

## Main Tabs

### Community

Reference: `open-design/community-home-a.html`
Android XML: `fragment_community_home.xml`

Required structure:

- Main top bar.
- Channel row: recommended/following/tutorial/pattern/beginner equivalents.
- Two-column content grid or masonry-style RecyclerView.
- Empty feed state when backend returns no posts.
- Bottom navigation.

### Commerce

Reference: `open-design/commerce-home-a.html`
Android XML: `fragment_commerce_home.xml`

Required structure:

- Search or filter entry.
- Category chips.
- Operational banner only if it is real or clearly UI-only.
- Product grid from backend data.
- Empty product state without mock products.

### AI

Reference: `open-design/ai-home-a.html`
Android XML: `fragment_ai_home.xml`

Required structure:

- Creation entry.
- Album upload and camera entry.
- Current task/history entry.
- Development boundary for real visual provider and model generation.
- Result actions disabled when backend capability is not implemented.

### Messages

Reference: `open-design/messages-a.html`
Android XML: `fragment_messages_home.xml`

Required structure:

- Private messages and notifications sections.
- Conversation rows with unread and mutual-follow status.
- Notification rows with detail navigation based on returned list data.
- Empty state when no messages or notifications exist.

### Profile

Reference: `open-design/profile-a.html`
Android XML: `fragment_profile_home.xml`

Required structure:

- User header or login prompt.
- Stats row.
- Asset tabs for patterns, liked posts, and favorites.
- Profile edit entry.
- Empty asset states without mock assets.

## Flow Screens

- Search follows `search-a.html`; Android XML is `activity_search.xml`; global
  search remains UI-only unless backend support exists.
- Post detail follows `post-detail-comment-toolbar-a.html`; Android XML is
  `activity_post_detail.xml`.
- Post compose follows `post-compose-a.html`; Android XML is
  `activity_post_create.xml`.
- Conversation follows `message-conversation-a.html`; Android XML is
  `activity_conversation.xml`.
- Notification detail follows `notification-detail-a.html`; Android XML is
  `activity_notification_detail.xml`.
- Profile edit follows `profile-edit-a.html`; Android XML is
  `activity_profile_edit.xml`.
- Settings follows the five `settings-*.html` files; Android XML files are
  `activity_settings_home.xml`, `activity_settings_account_security.xml`,
  `activity_settings_privacy_permissions.xml`,
  `activity_settings_notifications.xml`, and
  `activity_settings_about_compliance.xml`.
- Future capability follows `future-capability-ui-a.html`; Android XML is
  `activity_future_capability.xml`.
- `doyu-design-directions.html` records the selected visual direction and is
  not a runtime Android screen.

## Data and State Rules

- Lists render backend data only.
- Empty backend results render empty states.
- Future features render disabled or UI-only states.
- Pure UI-only pages must not create new API claims.

## Acceptance

Each screen passes when the Java/XML implementation:

- Uses the Open Design structure.
- Has loading, empty, error, and disabled states where applicable.
- Does not render fake content.
- Does not expose empty clicks.
- Has screenshot evidence or is recorded as not covered.

## Current Real-Device Evidence

The Java/XML visual smoke test captures these screenshot names on the attached
real device:

- Main tabs: `main_community.png`, `main_commerce.png`, `main_ai.png`,
  `main_messages.png`, `main_profile.png`.
- Global shell: `main_quick_menu.png`.
- Community/search/post: `search.png`, `post_create.png`, `post_detail.png`.
- AI: `ai_flow.png`, `camera.png`.
- Commerce: `product_detail.png`, `payment_boundary.png`.
- Message: `conversation.png`, `notification_detail.png`.
- Profile/Settings: `profile_edit.png`, `settings_home.png`,
  `settings_account_security.png`, `settings_privacy_permissions.png`,
  `settings_notifications.png`, `settings_about_compliance.png`,
  `future_capability.png`.

Evidence directory:
`doc/development/verification/android-java-xml-screenshots/real-device/visual-smoke`.

After any XML or drawable change, rerun the visual smoke suite and replace the
evidence screenshots before making final 1:1 parity claims.
