# Auth Email Login/Register Plan

Date: 2026-06-09

## Goal

Implement the first pass of the Login/Register feature using the
multi-agent-dev-loop execution flow. The user-facing auth model changes from
SMS code login to email + password login/register. Open Design becomes the
authority for logged-in and logged-out states, and Android Java/XML must match
the new login, register, logout, and protected-action behavior.

## Non-goals

- No SMS verification or email verification.
- No OAuth, password reset, or admin auth changes.
- No mock/demo runtime data for logged-out or auth screens.
- No device or real-phone verification in this development pass; review will
  cover device validation.

## Contract

- `POST /api/v1/auth/register`
  - Request: `email`, `password`, `confirmPassword`, `nickname`, `ageGroup`.
  - Email is trimmed, lowercased, syntactically validated, and unique.
  - Password must be 8-64 characters; confirmation must match.
  - Success creates a real user and returns the existing `AuthSession` shape.
- `POST /api/v1/auth/login`
  - Request: `email`, `password`.
  - Success returns the existing `AuthSession` shape.
  - Wrong credentials return 401. Duplicate registration returns 409.
- `POST /api/v1/auth/refresh` and `POST /api/v1/auth/logout` remain real token
  operations.
- `/api/v1/auth/sms-code` and `/api/v1/auth/login/sms` are removed from the
  user-facing contract.

## Implementation Steps

1. Update Open Design and docs:
   - Keep `open-design/index.html` as the logged-in board.
   - Add `open-design/index-logged-out.html`, `login-a.html`, and
     `register-a.html`.
   - Update profile and settings auth-state copy.
   - Remove user-facing SMS login/register and email-verification wording from
     current docs and diagrams.
2. Backend:
   - Add migration `V10__user_email_password_auth.sql`.
   - Add email/password fields to `UserEntity`, repository queries, and auth
     service logic.
   - Replace SMS auth controller endpoints with email register/login endpoints.
   - Update backend contract tests and test login helpers.
3. Android:
   - Add request models, auth API methods, repository methods, and `SessionStore`.
   - Add `LoginActivity` and `RegisterActivity` with XML layouts.
   - Add `AuthGate` and use it for profile edit, upload work, comments, and
     post write interactions.
   - Add Settings bottom logout action with confirmation and token clearing.
   - Update `UiCopy.LOGIN_REQUIRED` to the exact required copy.
4. Verification:
   - Run backend tests, Android unit tests, Android Java compile, and
     `git diff --check`.
   - Record device validation as not covered by development.

## Verification Commands

```powershell
cd D:\Studio\SpellBean\doyu-server
mvn test

cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:testDebugUnitTest --console=plain
.\gradlew.bat :app:compileDebugJavaWithJavac --console=plain

cd D:\Studio\SpellBean
git diff --check
```

## Acceptance

- Open Design contains logged-in and logged-out boards plus login/register
  pages.
- Docs no longer describe user-side SMS registration/login or email
  verification.
- Backend email register/login contracts pass.
- Android login/register/logout and protected-action gates are wired through
  real backend/session state.
- The verification record explicitly says device validation is delegated to
  review and was not covered in development.

## Review Fix Pass

Date: 2026-06-09

Review feedback found three in-scope issues to fix before handoff:

- Android instrumentation login helpers must be repeatable against a persistent
  dev database. The helper emails should include a unique suffix instead of
  registering a fixed email every run.
- `SettingsActivity` must refresh the bottom auth action when returning from
  `LoginActivity`, so the page changes from `Login / Register` to `Logout`
  immediately after successful login.
- `diagrams/api-module-map.svg` must remove the old `sms-code` auth wording and
  describe the current email auth endpoints.
