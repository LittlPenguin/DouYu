# 2026-06-07 Repair Stage Verification

## Scope

This verification records the repair-stage work after
`2026-06-07-real-backend-repair-log.md`. It covers the initial non-device
evidence captured while the device was unavailable, plus the later real-device
pass after the user confirmed the device was available again:

- Android Java/XML local unit tests, debug build, lint, and androidTest build.
- Backend unit/integration tests.
- Isolated `qa-empty` real HTTP backend checks on PostgreSQL/Redis.
- QA fixture authentication and real-ID detail smoke.
- Real-device 18-page visual smoke, screenshot pull, and contact-sheet evidence.

## Environment

- Workspace: `D:\Studio\SpellBean`
- Date: `2026-06-07`
- Backend QA profile: `qa-empty`
- Initial non-device QA URL: `http://127.0.0.1:8082`
- Real-device QA URL: `http://10.64.241.153:8081/`
- Device-resumed local QA URL: `http://127.0.0.1:8081`
- QA Spring Boot evidence directories:
  `doyu-server/.qa-output/repair-stage-20260607-153232`
  and `doyu-server/.qa-output/repair-stage-springboot-device.log`
- Android device: `10.64.241.158:40739`, model `ELI_AN00`, screen
  `1200x2664`, density `520`.
- Repair-stage screenshot directory:
  `doc/development/verification/android-java-xml-screenshots/repair-stage/`
- Repair-stage contact sheets:
  `visual-smoke-contact-sheet.png` and `real-backend-smoke-contact-sheet.png`

## Code Repairs Covered By This Run

| Area | Change | Evidence |
| --- | --- | --- |
| Backend QA fixture auth | `QaEmptyMessageFixtureController` now creates the conversation with the authenticated current user as `userA`, and the contract test logs in before calling it. | `QaEmptyBackendContractTests` passed; HTTP fixture check returned `userMatches=true`. |
| Backend default runtime exposure | `/api/v1/qa-empty/fixtures/**` is no longer permit-all in the global security allowlist; the controller remains profile-gated to `qa-empty`. Missing resource routes are mapped to 404 JSON instead of 500. | `DefaultRuntimeNoSeedContractTests` passed; default runtime QA fixture endpoint is 4xx. |
| Android 401 boundary | `ApiException` preserves HTTP status, `LoadState.from()` maps 401 to `LOGIN_REQUIRED`, and list/detail UI uses `UiCopy.LOGIN_REQUIRED` rather than raw `HTTP 401` as the user-facing state. | `ApiExceptionStatusTest` passed as part of `:app:testDebugUnitTest`. |
| Android real-ID smoke preparation | `RealBackendSmokeInstrumentedTest` sends fixture Authorization, uses one uploaded file ID for the AI job and detail screen, and compiles. | `:app:compileDebugAndroidTestJavaWithJavac` and `:app:assembleDebugAndroidTest` passed. |
| Real-backend smoke URL join | `RealBackendSmokeInstrumentedTest` now uses `apiUrl(baseUrl, path)` so a trailing `BuildConfig.API_BASE_URL` slash cannot create `//api/...` paths that bypass the backend security allowlist. | First real-device run failed with HTTP 401; after the URL join fix, `RealBackendSmokeInstrumentedTest` returned `OK (1 test)`. |
| Active architecture wording | Open Design entry pages and architecture SVGs no longer describe Compose/DataStore as the current target architecture. | `OpenDesignLayoutMappingTest.openDesignAndDiagramsUseJavaXmlArchitectureWording` passed. |

## Static Residue Checks

```powershell
rg --files DouYu/app/src | rg "\.kt$"
```

Result: no output.

```powershell
rg -n "compose|Composable|Navigation Compose|kotlinx|MockData|coil\.compose|paging\.compose" DouYu/app
```

Result: no output.

```powershell
rg -n "DataInitializer|static/seed|/seed/|post_seed|prod_|seedTopic|seedProduct|seedPost" doyu-server/src/main doyu-server/src/test
```

Result: only the explicit negative assertions in test files matched:

- `DefaultRuntimeNoSeedContractTests.java`: `doesNotContain("DataInitializer")`
- `QaEmptyBackendContractTests.java`: `doesNotContain("DataInitializer")`

No production runtime seed/demo implementation was found by this scan.

## Android Verification

```powershell
cd DouYu
.\gradlew.bat :app:testDebugUnitTest --console=plain
```

Result: `BUILD SUCCESSFUL in 6s`.

```powershell
cd DouYu
.\gradlew.bat :app:assembleDebug --console=plain
```

Result: `BUILD SUCCESSFUL in 5s`.

```powershell
cd DouYu
.\gradlew.bat :app:lintDebug --console=plain
```

Result: command exited with code `0`. Generated lint reports:

- `DouYu/app/build/reports/lint-results-debug.html`
- `DouYu/app/build/reports/lint-results-debug.txt`
- `DouYu/app/build/reports/lint-results-debug.xml`

`Select-String '<issue ' DouYu/app/build/reports/lint-results-debug.xml`
reported `0` issues.

```powershell
cd DouYu
.\gradlew.bat :app:assembleDebugAndroidTest --console=plain
```

Result: command exited with code `0`.

Device instrumentation was not run in this stage because the user paused all
real-device work.

## Backend Verification

Focused repair-stage backend tests:

```powershell
cd doyu-server
mvn "-Dtest=QaEmptyBackendContractTests,DefaultRuntimeNoSeedContractTests" test
```

Result: `BUILD SUCCESS`, `Tests run: 8, Failures: 0, Errors: 0, Skipped: 0`.

Full backend tests:

```powershell
cd doyu-server
mvn test
```

Result: `BUILD SUCCESS`, `Tests run: 62, Failures: 0, Errors: 0, Skipped: 0`.

## QA Empty Real HTTP Verification

The previous listener on `8082` was stopped before this run. Then `qa-empty`
PostgreSQL/Redis and the current Spring Boot code were started fresh.

```powershell
cd doyu-server
docker compose -f docker-compose.qa-empty.yml up -d postgres-qa-empty redis-qa-empty
mvn spring-boot:run -Dspring-boot.run.profiles=qa-empty
```

Runtime evidence:

- `douyu-postgres-qa-empty`: healthy during verification.
- `douyu-redis-qa-empty`: healthy during verification.
- `8082`: listened on the new Java process for this run.
- Health endpoint returned:

```json
{
  "groups": ["liveness", "readiness"],
  "status": "UP"
}
```

Clean-list script:

```powershell
cd doyu-server
powershell -ExecutionPolicy Bypass -File .\scripts\verify-qa-empty.ps1 -BaseUrl http://127.0.0.1:8082
```

Result:

```text
OK feed: 200 empty list
OK products: 200 empty list
OK topics: 200 empty list
OK sticker-packs: 200 empty list
qa-empty HTTP verification passed for http://127.0.0.1:8082
```

Authenticated fixture check:

```powershell
POST /api/v1/auth/sms-code
POST /api/v1/auth/login/sms
GET /api/v1/users/me
POST /api/v1/qa-empty/fixtures/message-thread
```

Result summary:

```json
{
  "userId": "usr_99709d0dff064c818c8a951627323beb",
  "conversationId": "conv_e168f687384647848c56243c111f4b05",
  "notificationId": "ntf_31512369536a4fb3aa6b805d9edc5271",
  "userAId": "usr_99709d0dff064c818c8a951627323beb",
  "userBId": "qa_fixture_user_b",
  "userMatches": true
}
```

Anonymous fixture check:

```powershell
POST /api/v1/qa-empty/fixtures/message-thread
```

Result: HTTP `401`.

## Device-Resumed QA Empty Real HTTP Verification

After the user confirmed the device was available again, `qa-empty`
PostgreSQL/Redis were restarted. Spring Boot was intended to use port `8082`,
but the background PowerShell wrapper did not preserve `DOUYU_BACKEND_PORT`;
the application therefore listened on `8081`. The Spring Boot log still proves
the active profile and datasource were the isolated QA environment:

- Active profile: `qa-empty`.
- Listener: `8081`.
- Datasource: `jdbc:postgresql://localhost:55433/douyu_qa_empty`.
- Device API base URL used by the APK:
  `http://10.64.241.153:8081/`.

Health endpoint:

```powershell
Invoke-RestMethod -Uri http://127.0.0.1:8081/actuator/health
```

Result:

```json
{"groups":["liveness","readiness"],"status":"UP"}
```

Clean-list script:

```powershell
cd doyu-server
powershell -ExecutionPolicy Bypass -File .\scripts\verify-qa-empty.ps1 -BaseUrl http://127.0.0.1:8081
```

Result:

```text
OK feed: 200 empty list
OK products: 200 empty list
OK topics: 200 empty list
OK sticker-packs: 200 empty list
qa-empty HTTP verification passed for http://127.0.0.1:8081
```

Authenticated fixture check:

```json
{
  "userId": "usr_6fc52629b0844de9b108625698fee6e2",
  "conversationId": "conv_e7b8bbbd1c834bf5b1146833fc493d01",
  "notificationId": "ntf_52f74d618b77401a8d5b2df8a6f7611b",
  "userAId": "usr_6fc52629b0844de9b108625698fee6e2",
  "userMatches": true,
  "anonymousStatus": 401
}
```

## Dev Persistent Database Read-Only Diagnostic

The dev PostgreSQL container was started only long enough to run the read-only
diagnostic script. No data was deleted or updated.

```powershell
cd doyu-server
docker compose up -d postgres
powershell -ExecutionPolicy Bypass -File .\scripts\diagnose-dev-seed-residue.ps1
docker compose stop postgres
```

Result:

```text
Read-only dev seed/demo residue diagnostic. This script does not delete or update data.
      bucket      | rows
------------------+------
 posts post_seed% |    4
 products prod_%  |    6
 topics           |    6
 sticker_packs    |    1
(4 rows)
```

Interpretation: the persistent dev volume still contains historical seed/demo
rows. This was recorded as environment residue only; it was not deleted. The
isolated `qa-empty` profile remains the clean-backend proof for this repair
stage.

## Repair-Stage Real Device Verification

Device precheck:

```powershell
adb devices -l
adb -s 10.64.241.158:40739 shell getprop ro.product.model
adb -s 10.64.241.158:40739 shell wm size
adb -s 10.64.241.158:40739 shell wm density
```

Result summary:

- Serial: `10.64.241.158:40739`.
- Model: `ELI-AN00`.
- Size: `1200x2664`.
- Density: `520`.

Android debug build confirmed the real-device API target:

```text
public static final String API_BASE_URL = "http://10.64.241.153:8081/";
```

Debug network security config included cleartext access for
`10.64.241.153`.

Build and install:

```powershell
cd DouYu
$env:DOUYU_ANDROID_API_BASE_URL='http://10.64.241.153:8081/'
$env:DOUYU_ANDROID_CLEARTEXT_HOSTS='10.64.241.153,10.0.2.2,localhost'
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
adb -s 10.64.241.158:40739 install -r app\build\outputs\apk\debug\app-debug.apk
adb -s 10.64.241.158:40739 install -r app\build\outputs\apk\androidTest\debug\app-debug-androidTest.apk
```

Result summary:

- Gradle build: `BUILD SUCCESSFUL in 8s`.
- App install: `Success`.
- Android test APK install: `Success`.

Visual smoke:

```powershell
adb -s 10.64.241.158:40739 shell am instrument -w -r -e class cn.edu.app.douyu.VisualSmokeInstrumentedTest cn.edu.app.douyu.test/androidx.test.runner.AndroidJUnitRunner
```

Result:

```text
visualSmokeDir=/storage/emulated/0/Android/data/cn.edu.app.douyu/files/visual-smoke
Time: 126.924
OK (1 test)
```

Real-backend detail smoke:

```powershell
adb -s 10.64.241.158:40739 shell am instrument -w -r -e class cn.edu.app.douyu.RealBackendSmokeInstrumentedTest cn.edu.app.douyu.test/androidx.test.runner.AndroidJUnitRunner
```

First run result: failed with HTTP `401` during `/auth/sms-code` because the
test concatenated a trailing-slash base URL with `"/api/..."`, producing a
double-slash API path. After the URL join fix and test APK reinstall, the rerun
returned:

```text
realBackendSmokeDir=/storage/emulated/0/Android/data/cn.edu.app.douyu/files/real-backend-smoke
Time: 23.596
OK (1 test)
```

Screenshot pull:

```powershell
adb -s 10.64.241.158:40739 pull /storage/emulated/0/Android/data/cn.edu.app.douyu/files/visual-smoke doc\development\verification\android-java-xml-screenshots\repair-stage
adb -s 10.64.241.158:40739 pull /storage/emulated/0/Android/data/cn.edu.app.douyu/files/real-backend-smoke doc\development\verification\android-java-xml-screenshots\repair-stage
```

Result:

- `visual-smoke`: 22 PNG screenshots pulled.
- `real-backend-smoke`: 5 PNG screenshots pulled.
- Every pulled screenshot was `1200x2664`.
- Contact sheets generated:
  - `doc/development/verification/android-java-xml-screenshots/repair-stage/visual-smoke-contact-sheet.png`
  - `doc/development/verification/android-java-xml-screenshots/repair-stage/real-backend-smoke-contact-sheet.png`

## Shutdown Evidence

After API verification:

```powershell
Stop-Process <8082 listener>
docker compose -f docker-compose.qa-empty.yml stop postgres-qa-empty redis-qa-empty
Get-NetTCPConnection -LocalPort 8082 -ErrorAction SilentlyContinue
docker compose -f docker-compose.qa-empty.yml ps -a
```

Result:

- `8082` had no listener after shutdown; only `TIME_WAIT` remained.
- `douyu-postgres-qa-empty` status: `Exited`.
- `douyu-redis-qa-empty` status: `Exited`.
- Default dev containers were also exited and were not modified or cleared.

After the device-resumed verification:

```powershell
Stop-Process <8081 listener>
docker compose -f docker-compose.qa-empty.yml stop postgres-qa-empty redis-qa-empty
Get-NetTCPConnection -LocalPort 8081 -ErrorAction SilentlyContinue
docker compose -f docker-compose.qa-empty.yml ps -a
```

Result:

- Stopped `8081` listener PID `7100`.
- `8081` had no listener after shutdown.
- `douyu-postgres-qa-empty` status: `Exited`.
- `douyu-redis-qa-empty` status: `Exited`.
- Default dev containers were exited and were not modified or cleared.

## RB Closure State

| ID | Status | Evidence | Remaining work |
| --- | --- | --- | --- |
| `RB-001` | Closed | `qa-empty` feed returned 200 with empty `items` and `total=0`; `main_community.png` shows the repair-stage empty-state boundary on the real device. | None for this repair stage. |
| `RB-002` | Closed | `qa-empty` products returned 200 with empty `items` and `total=0`; `main_commerce.png` shows the repair-stage empty-state boundary on the real device. | None for this repair stage. |
| `RB-003` | Closed for clean-backend API behavior; dev residue remains diagnostic-only | `qa-empty` topics and sticker packs returned 200 with empty `items` and `total=0`. Dev persistent data was not cleared. | If needed, run the read-only dev residue diagnostic and classify existing local rows. |
| `RB-004` | Closed | `ApiExceptionStatusTest` passed; `main_ai.png`, `main_messages.png`, and `main_profile.png` show boundary states without raw `HTTP 401`. | None for this repair stage. |
| `RB-005` | Closed for supported real-ID flows | `RealBackendSmokeInstrumentedTest` returned `OK (1 test)` and captured `real_post_detail.png`, `real_product_detail.png`, `real_ai_flow.png`, `real_conversation.png`, and `real_notification_detail.png` using backend-returned IDs. | Public production endpoints for complete notification-detail creation and provider integrations remain future scope. |

## UI Parity Status

The repair-stage 18-page screenshot pass was executed after the device became
available again. The latest repair-stage evidence is under:

- `doc/development/verification/android-java-xml-screenshots/repair-stage/visual-smoke`
- `doc/development/verification/android-java-xml-screenshots/repair-stage/real-backend-smoke`

| Open Design reference | Repair-stage status | Evidence category |
| --- | --- | --- |
| `community-home-a.html` | Covered by `main_community.png` and `main_quick_menu.png`; API empty list verified | `boundary` |
| `commerce-home-a.html` | Covered by `main_commerce.png`; API empty list verified | `boundary` |
| `ai-home-a.html` | Covered by `main_ai.png`; no raw `HTTP 401`; history boundary visible | `boundary` |
| `messages-a.html` | Covered by `main_messages.png`; no raw `HTTP 401`; message boundary visible | `boundary` |
| `profile-a.html` | Covered by `main_profile.png`; no raw `HTTP 401`; asset boundary visible | `boundary` |
| `search-a.html` | Covered by `search.png` | `boundary`, `UI-only` |
| `post-compose-a.html` | Covered by `post_create.png` | `boundary`, `UI-only` |
| `post-detail-comment-toolbar-a.html` | Covered by `post_detail.png` and real-ID `real_post_detail.png` | `real-api` |
| `message-conversation-a.html` | Covered by `conversation.png` and real-ID `real_conversation.png` | `real-api` |
| `notification-detail-a.html` | Covered by `notification_detail.png` and real-ID `real_notification_detail.png` | `real-api` |
| `profile-edit-a.html` | Covered by `profile_edit.png` | `boundary`, `UI-only` |
| `settings-home-a.html` | Covered by `settings_home.png` | `boundary` |
| `settings-account-security-a.html` | Covered by `settings_account_security.png` | `boundary` |
| `settings-privacy-permissions-a.html` | Covered by `settings_privacy_permissions.png` | `boundary` |
| `settings-notifications-a.html` | Covered by `settings_notifications.png` | `boundary` |
| `settings-about-compliance-a.html` | Covered by `settings_about_compliance.png` | `boundary`, `UI-only` |
| `future-capability-ui-a.html` | Covered by `future_capability.png` | `UI-only` |
| `doyu-design-directions.html` | Covered across the full repair-stage screenshot set and contact sheets | `design-system` |

## Remaining Deviations

No new repair-stage UI deviation was found in the contact-sheet review for the
18 Open Design references. Accepted differences remain:

- Real device status/navigation bars are visible, while Open Design HTML uses a
  browser mock-phone frame.
- Empty, login, disabled, and UI-only states replace populated design examples
  when the clean backend has no runtime data.
- Camera preview content is environment-dependent; `camera.png` verifies the
  CameraX preview/capture boundary, not image quality.
- Production SMS, payment, AI provider, map/location, object storage hardening,
  compliance documents, and player marketplace completion remain out of scope.

## Final Post-Update Verification

After updating the verification docs and parity matrix, the final local gate was
rerun:

```powershell
rg --files DouYu/app/src | rg "\.kt$"
rg -n "compose|Composable|Navigation Compose|kotlinx|MockData|coil\.compose|paging\.compose" DouYu/app
rg -n "DataInitializer|static/seed|/seed/|post_seed|prod_|seedTopic|seedProduct|seedPost" doyu-server/src/main doyu-server/src/test
cd DouYu
.\gradlew.bat :app:testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug :app:lintDebug :app:assembleDebugAndroidTest --console=plain
cd ..\doyu-server
mvn test
git diff --check
```

Result summary:

- Android `.kt` scan: no output.
- Android Compose/Kotlin/mock scan: no output.
- Backend seed/demo scan: only explicit negative assertions in tests matched
  `DataInitializer`; no production runtime seed implementation was found.
- `:app:testDebugUnitTest`: `BUILD SUCCESSFUL in 6s`.
- `:app:assembleDebug :app:lintDebug :app:assembleDebugAndroidTest`:
  `BUILD SUCCESSFUL in 10s`.
- Lint report issue count: `0`.
- Backend `mvn test`: `BUILD SUCCESS`, `Tests run: 62, Failures: 0,
  Errors: 0, Skipped: 0`.
- `git diff --check`: exit code `0`; Git reported line-ending conversion
  warnings only.
- `8081` and `8082`: no listeners after shutdown.
- `douyu-postgres-qa-empty` and `douyu-redis-qa-empty`: `Exited`.
- Read-only dev residue diagnostic found historical rows in the dev volume:
  `post_seed% = 4`, `prod_% = 6`, `topics = 6`, `sticker_packs = 1`. These
  rows were recorded only and were not deleted.
