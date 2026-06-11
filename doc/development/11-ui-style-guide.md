# UI Style Guide

## Authority

The UI authority is the retained Open Design HTML set in `doc/development/open-design/`. Android Java/XML screens must follow those files for structure, copy, hierarchy, states, and interaction.

## Visual Direction

- Light, clear, handmade, restrained.
- Warm off-white page background.
- Petal pink, mint, sky blue, and coral accents.
- Content and commerce readability before decoration.
- No emoji as functional icons.
- No fake content to make screens look populated.

## App Shell

- Current bottom tabs: `社区 / 商城 / 上传 / 消息 / 我的`.
- Bottom navigation uses icon + label for every item.
- Main tabs show bottom navigation.
- Detail and flow screens hide bottom navigation unless the Open Design screen explicitly shows it.
- Top bar actions must have real navigation, disabled state, or clear boundary copy.

## Screen States

Every screen must support the states that apply to its data source:

- Loading.
- Empty.
- Error.
- Retry.
- Not logged in.
- Forbidden or disabled.

The empty state is the required replacement for deleted mock/seed content.

## Component Rules

- Use Material Components for buttons, text fields, chips, tabs, and bottom navigation.
- Use RecyclerView for feeds, product grids, notifications, comments, and assets.
- Use Glide for network images and placeholders.
- Use CameraX PreviewView for retained camera capture screens.
- Use disabled controls and explanatory copy for unavailable retained actions.

## Screen Mapping

- Community home: `community-home-a.html`
- Post detail: `post-detail-comment-toolbar-a.html`
- Post compose: `post-compose-a.html`
- Search: `search-a.html`
- Commerce home: `commerce-home-a.html`
- Notifications: `messages-a.html`
- Notification detail: `notification-detail-a.html`
- Profile and edit: `profile-a.html`, `profile-edit-a.html`
- Profile lists: `profile-posts-a.html`, `profile-following-a.html`, `profile-followers-a.html`
- Settings: `settings-home-a.html`, `settings-account-security-a.html`, `settings-privacy-permissions-a.html`, `settings-notifications-a.html`
- Login/register: `login-a.html`, `register-a.html`

## Prohibited UI Behavior

- Fake success Toasts.
- Clickable controls with no result.
- Runtime demo cards or fake lists.
- Static seed images as live content.
- Private-message, refresh, account-cancel, reward, report/admin, address-book, order-center, payment, AI, map/location, production compliance or real SMS claims.
