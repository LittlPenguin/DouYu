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

- Current bottom tabs: `社区 / 商城 / 消息 / 我的`. A publish action may appear as a top action or shell shortcut.
- Bottom navigation uses icon + label for every item.
- Main tabs show bottom navigation.
- Detail and flow screens hide bottom navigation unless the Open Design screen explicitly shows it.
- Top bar actions must have real navigation, disabled state, or UI-only copy.

## Screen States

Every screen must support the states that apply to its data source:

- Loading
- Empty
- Error
- Retry
- Not logged in
- Forbidden/disabled
- UI-only/development boundary

The empty state is the required replacement for deleted mock/seed content.

## Layout Rules

- XML layouts must be stable at common phone widths around 360dp to 430dp.
- Buttons and tabs must not overflow.
- RecyclerView items must keep stable dimensions and not resize surrounding UI.
- Cards are for repeated items or framed tools, not for every page section.
- Text must be left aligned for content and commerce descriptions unless the Open Design file shows another structure.

## Component Rules

- Use Material Components for buttons, text fields, chips, tabs, and bottom navigation.
- Use RecyclerView for feeds, product grids, messages, comments, and assets.
- Use Glide for network images and placeholders.
- Use CameraX PreviewView for retained camera capture screens.
- Use disabled controls and explanatory copy for unavailable retained features.

## Screen Mapping

- Community home: `community-home-a.html`
- Post detail: `post-detail-comment-toolbar-a.html`
- Post compose: `post-compose-a.html`
- Search: `search-a.html`
- Commerce home: `commerce-home-a.html`
- Messages and conversation: `messages-a.html`, `message-conversation-a.html`
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
- Claims of real payment, real AI provider, production compliance, map/location support or real SMS provider.
