# 39. Settings Real Functions Plan

## Goal

Turn the Settings Java/XML screens from static explanation pages into real account, permission, preference, and diagnostics surfaces.

## Scope

- Account and Security: load current user, show email/account state, keep real logout, add account cancellation request.
- Privacy and Permissions: render real Android camera/notification/photo capability state, open OS settings when needed, and persist privacy switches through the backend.
- Notifications: persist message, interaction, publish, and system reminder preferences through the backend.
- Help and About: show real app version, build code, API base URL, and login state; expose executable diagnostics/system-settings actions.

## Backend Contract

- Add nullable PATCH request fields and persisted boolean defaults on `users`.
- Add `GET /api/v1/users/me/settings`.
- Add `PATCH /api/v1/users/me/settings`.
- Include settings fields in `GET /api/v1/users/me`.
- Keep `POST /api/v1/auth/account/cancel` as a cancellation request, not full deletion.

## Android Contract

- Add `UserSettings` and `UpdateUserSettingsRequest` DTOs.
- Extend `DoyuApi` and `DoyuRepository` with settings and cancellation methods.
- Rebuild Settings XML controls with stable IDs and no disabled placeholder buttons.
- `SettingsActivity` waits for backend success before showing saved state and rolls back failed switch changes.

## Verification

- Android unit tests cover Retrofit mapping, repository settings calls, SettingsActivity real-action references, and removed-feature boundaries.
- Backend tests cover settings defaults, partial PATCH persistence, `GET /users/me` settings fields, and cancellation status.
- Static checks confirm no Kotlin/Compose/mock data and no map/payment/SMS runtime additions.
