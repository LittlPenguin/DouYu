# Open Design Decision

## Current Decision

The retained Open Design set describes the completed product scope only:

- Community.
- Upload.
- Search.
- Commerce browse, cart and create order.
- Notifications.
- Profile and settings.
- Login/register.

## Removed From Open Design

The current Open Design set does not include:

- Direct-message conversation pages.
- Notification object-jump pages.
- Reward/checkin/badge pages.
- Report/admin pages.
- Address-book or order-center pages.
- Refresh/account-cancel pages.
- AI, payment, map/location, SMS or full compliance pages.

## Implementation Rule

Android Java/XML screens follow the retained HTML files. If a feature is not represented by a retained Open Design page and a retained backend API, it must not appear as an enabled runtime entry.
