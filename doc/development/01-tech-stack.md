# 01. Tech Stack

## Android

- Java only for production and test source.
- Activity/Fragment + XML layouts.
- AppCompat / AndroidX Activity and Fragment.
- Material Components, ConstraintLayout, RecyclerView.
- Retrofit + Gson, OkHttp.
- Glide for OSS/URL-backed images.
- CameraX View for retained camera capture flows.
- SharedPreferences for session persistence.

Disallowed:

- Kotlin production/test source under `DouYu/app/src`.
- Jetpack Compose, Navigation Compose, Compose Material, Compose UI tests.
- Kotlin serialization, Kotlin coroutines, Kotlin DataStore.
- Runtime MockData, fake lists, fake orders, fake payment params.

## Backend

- Spring Boot modular monolith.
- `/api/v1` REST API.
- PostgreSQL + Flyway.
- Redis where configured.
- MyBatis/JPA-style repositories according to existing modules.
- OSS-backed image storage via upload presign/PUT/confirm.

## Providers

| Capability        | Current scope                                                              |
| ----------------- | -------------------------------------------------------------------------- |
| Verification code | Development fixed-code boundary for local login only; no real SMS Provider |
| Object storage    | Local / Aliyun OSS Provider retained                                       |
| Upload            | `/uploads/presign -> PUT uploadUrl -> /uploads/confirm` retained           |

Removed from current scope:

- AI Provider, visual model, large model generation, content safety and cost control.
- Payment Provider, WeChat/Alipay SDK/API, refunds, reconciliation and production callbacks.
- Map/location Provider.
- Production rate limiting and production risk control.
- Full compliance document set.

## Integration Rules

- Android never stores OSS credentials.
- Runtime images must use backend/OSS-backed URLs, not bundled mock assets.
- Empty backend results are valid and must render empty states.
- Do not describe removed capabilities as planned current work or current verification targets.
