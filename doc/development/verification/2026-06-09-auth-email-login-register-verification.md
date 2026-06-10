# Login/Register Email Auth Verification

Date: 2026-06-09

## Scope

- Implemented email + password login and registration for user-facing auth.
- Added Open Design login/register pages and a logged-out index.
- Added Android Java/XML login, register, session storage, protected-action login gate, and Settings logout confirmation.
- Updated backend auth endpoints, user email/password schema, and contract tests.
- Device validation is intentionally not covered in this development pass.

## Commands

### Backend

Command:

```powershell
cd D:\Studio\SpellBean\doyu-server
mvn test
```

Result: passed.

Key output:

```text
Tests run: 71, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Notes:

- First backend run failed because `RefreshTokenEntity` did not expose `setUpdatedAt`.
- Second backend run failed because controller-level `@Email` rejected trim-normalized emails before service validation, and repeated helper registrations reused the same email.
- Final run passed after fixing logout persistence, moving email format validation into service normalization, and making test registration emails unique.

### Android Unit Tests

Command:

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:testDebugUnitTest --console=plain
```

Result: passed.

Key output:

```text
BUILD SUCCESSFUL in 6s
```

Targeted auth constraint command also passed before the full run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "cn.edu.app.douyu.core.OpenDesignLayoutMappingTest.authEmailLoginRegisterAndLoggedOutBoundariesAreMapped" --console=plain
```

Key output:

```text
BUILD SUCCESSFUL in 33s
```

### Android Java Compile

Command:

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:compileDebugJavaWithJavac --console=plain
```

Result: passed.

Key output:

```text
BUILD SUCCESSFUL in 3s
```

### Diff Whitespace

Command:

```powershell
cd D:\Studio\SpellBean
git diff --check
```

Result: passed.

Key output:

```text
DIFF_CHECK_EXIT=0
```

Notes:

- Git reported line-ending normalization warnings for several documentation/Open Design files.
- No whitespace errors were reported.

## Source Checks

Commands:

```powershell
rg -n "auth/sms-code|auth/login/sms|短信验证码|短信注册|邮箱验证码|登录请求仍传" doc/development/open-design doc/development/00-project-handoff.md doc/development/01-tech-stack.md doc/development/02-architecture.md doc/development/05-api-contract.md doc/development/10-testing-acceptance.md doc/development/12-feature-and-flow-map.md doc/development/13-ui-screen-blueprints.md doc/development/diagrams -S

rg -n "Sms|sms|auth/sms-code|auth/login/sms|短信验证码|短信注册|邮箱验证码" doyu-server/src/main/java DouYu/app/src/main -S
```

Result: passed.

Key output:

```text
No matches.
```

## Device / Visual Coverage

真机登录、注册、退出登录、未登录拦截与页面 1:1 视觉验收由审核侧覆盖；开发侧未覆盖设备验证。

No adb, emulator, real-device screenshot, or real-device smoke test was run in this development pass.

## Acceptance Notes

- User-facing auth endpoints are now:
  - `POST /api/v1/auth/register`
  - `POST /api/v1/auth/login`
  - `POST /api/v1/auth/refresh`
  - `POST /api/v1/auth/logout`
- User-facing `/api/v1/auth/sms-code` and `/api/v1/auth/login/sms` are not exposed by current backend/Android production code.
- `UiCopy.LOGIN_REQUIRED` is exactly `需要登录后才能查看此页面的数据。`.
- Protected Android actions now use the confirmation login gate for profile edit, upload work, and post detail comment/write interactions.
- Settings bottom auth action shows `登录 / 注册` when logged out and `退出登录` when logged in; logout requires confirmation.

## Review Fix Pass

Date: 2026-06-09

Review result addressed:

- I1 fixed: `CommerceRealProductsInstrumentedTest` and
  `RealBackendSmokeInstrumentedTest` now append `UUID.randomUUID()` to the
  review helper email before calling `/api/v1/auth/register`, so repeated
  instrumentation runs against a persistent dev database do not collide on the
  unique email index.
- I2 fixed: `SettingsActivity` now calls `bindLogoutAction()` from
  `onResume()`, so returning from `LoginActivity` refreshes the bottom
  `登录 / 注册` versus `退出登录` action immediately.
- M1 fixed: `doc/development/diagrams/api-module-map.svg` now labels Auth as
  `register / login / refresh / logout` instead of the removed SMS flow.

Additional constraint coverage added to
`OpenDesignLayoutMappingTest.authEmailLoginRegisterAndLoggedOutBoundariesAreMapped`:

- Asserts `SettingsActivity` keeps the `onResume()` refresh path.
- Asserts the API module diagram contains current auth endpoints and no
  `sms-code` wording.
- Asserts both real-device review helper sources generate unique email suffixes
  with `UUID.randomUUID()`.

Commands run after the review fixes:

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:testDebugUnitTest --tests "cn.edu.app.douyu.core.OpenDesignLayoutMappingTest.authEmailLoginRegisterAndLoggedOutBoundariesAreMapped" --console=plain
.\gradlew.bat :app:testDebugUnitTest --console=plain
.\gradlew.bat :app:compileDebugJavaWithJavac --console=plain
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac --console=plain

cd D:\Studio\SpellBean\doyu-server
mvn test

cd D:\Studio\SpellBean
git diff --check
rg -n "commerce-13900002081@example.com|smoke-13900001999@example.com|smoke-13900002777@example.com|sms-code / login / refresh" DouYu\app\src\androidTest doc\development\diagrams -S
```

Results:

- Targeted auth constraint test: `BUILD SUCCESSFUL in 8s`.
- Android unit tests: `BUILD SUCCESSFUL in 5s`.
- Android Java compile: `BUILD SUCCESSFUL in 3s`.
- Android instrumentation test source compile:
  `BUILD SUCCESSFUL in 5s`.
- Backend tests: `Tests run: 71, Failures: 0, Errors: 0, Skipped: 0`;
  `BUILD SUCCESS`.
- `git diff --check`: no whitespace errors; Git only reported line-ending
  normalization warnings for existing documentation/Open Design files.
- Fixed-email/old-diagram scan: no matches.

Device validation remains intentionally not run in this development pass.
真机登录、注册、退出登录、未登录拦截与页面 1:1 视觉验收由审核侧覆盖；开发侧未覆盖设备验证。
