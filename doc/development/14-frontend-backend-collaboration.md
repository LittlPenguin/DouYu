# 14. Frontend Backend Collaboration

## Local Environment

- `.env` in the repository root is the local configuration source.
- Android `DOUYU_ANDROID_API_BASE_URL` must point to the current backend host.
- `DOUYU_STORAGE_BASE_URL`, `DOUYU_OSS_PROVIDER` and Aliyun OSS template values remain part of the supported upload/OSS workflow.

## Retained Integration Flows

- Login/register/session.
- Upload presign -> PUT -> confirm.
- Community feed, post detail, comments and post compose.
- Commerce categories/products/cart/orders/address.
- Messages, notifications and conversation.
- Profile and settings.

## Collaboration Rules

- Frontend must not fabricate backend rows, fake orders or fake success states.
- Backend empty responses must be treated as valid.
- OSS-backed image storage remains the image path for runtime assets.
- Address management remains in scope without requiring map/location.

## Removed Integration Areas

- AI Provider, visual model, content safety and cost controls.
- Payment SDK/API, refund, reconciliation and production callback integration.
- Map/location Provider integration.
- Real SMS Provider integration.
- Production rate limiting and production risk control.
- Full compliance document workflow.
