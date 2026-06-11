# Data Model

## Current Runtime Tables

Retained runtime data includes:

- `users`
- `user_settings`
- `file_assets`
- `posts`, `comments`, `likes`, `favorites`, `follows`, `topics`, `stickers`
- `products`, `skus`, `cart_items`
- `orders`, `order_items`
- `notifications`

## Removed Runtime Tables

The cleanup migration drops obsolete or unfinished tables:

- `refresh_tokens`
- `conversations`
- `messages`
- `reward_accounts`
- `checkin_records`
- `reward_ledgers`
- `reports`
- `moderation_records`
- `admin_users`
- `admin_operation_logs`
- legacy AI/payment/pattern tables already removed in earlier cleanup migrations.

## Auth Data

Login/register responses contain:

- `accessToken`
- `expiresIn`
- `user`

There is no refresh token persistence in the current runtime.

## Notifications

`notifications` stores retained notification records:

- `id`
- `user_id`
- `type`
- `title`
- `content`
- `read_at`
- `created_at`
- `updated_at`

New users receive three `SYSTEM` notifications at registration time. These are onboarding records, not demo seed content.

## Orders

`orders` and `order_items` are retained only for order creation.

- `POST /api/v1/orders` creates orders with status `CREATED`.
- Address information is stored as the submitted `addressSnapshot`.
- `addressId` is not part of the current request model.
- Order list, detail and cancel APIs are not retained.

## Android Model Target

Android models are Java POJOs. Kotlin data classes are not used in Android production or test source.

## Empty Data Meaning

An empty posts/products/notifications/assets result is normal after seed/demo removal. Empty does not mean the client should create sample rows.

## Test Data

Tests must create explicit fixtures. Production startup code must not create demo posts, products, topics, stickers, conversations or admin users.
