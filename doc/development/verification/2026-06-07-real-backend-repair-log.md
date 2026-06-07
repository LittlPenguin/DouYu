# 2026-06-07 Real Backend Repair Log

## Purpose

This record captures the real-backend, real-device smoke run requested after the
Java/XML migration. It records only evidence from commands executed in this run
and lists differences from `doc/development` requirements and Open Design HTML
references.

Secrets from `.env` are intentionally not copied into this document.

## Environment

- Workspace: `D:\Studio\SpellBean`
- Backend profile: `dev`
- Backend URL checked from host: `http://127.0.0.1:8081`
- Android API base URL used by the debug app: `http://10.64.241.153:8081/`
- Device serial: `10.64.241.158:40867`
- Device model: `ELI-AN00`
- Android version: `16`
- Screen size: `1200x2664`
- Screen density: `520`
- Screenshot directory:
  `doc/development/verification/android-java-xml-screenshots/real-backend/visual-smoke`
- Contact sheet:
  `doc/development/verification/android-java-xml-screenshots/real-backend/visual-smoke-contact-sheet.png`

## Commands And Results

### Preflight

```powershell
adb devices -l
```

Result: `10.64.241.158:40867` was online as `device`, product `ELI-AN00`,
model `ELI_AN00`.

```powershell
Get-NetTCPConnection -LocalPort 8081 -ErrorAction SilentlyContinue
```

Result: no listener before backend startup.

```powershell
docker compose ps
```

Result: no running `doyu-server` compose services before startup.

### Backend Startup

```powershell
cd doyu-server
docker compose up -d postgres redis
```

Result: `douyu-postgres` and `douyu-redis` started.

```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Result: Spring Boot started with profile `dev`; launcher PID was `10236`, Java
listener PID was `25136`, and `8081` listened on `0.0.0.0` and `[::]`.
Startup logs were written under `doyu-server/.qa-output/real-backend-2026-06-07/`.

```powershell
Invoke-RestMethod http://127.0.0.1:8081/actuator/health
```

Result:

```json
{
  "groups": ["liveness", "readiness"],
  "status": "UP"
}
```

### API Sanity

| Endpoint                            | Status | Item count | Seed/demo hit summary |
| ----------------------------------- | ------:| ----------:| --------------------- |
| `/actuator/health`                  | 200    | n/a        | none                  |
| `/api/v1/posts/feed?page=1&size=20` | 200    | 4          | `post_seed`           |
| `/api/v1/products?page=1&size=20`   | 200    | 6          | `prod_`               |
| `/api/v1/topics?page=1&size=20`     | 200    | 6          | none by string scan   |
| `/api/v1/sticker-packs`             | 200    | 1          | none by string scan   |

The scan intentionally stored only counts and string-hit summaries, not full API
response bodies.

### Android Build And Device Smoke

```powershell
cd DouYu
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
```

Result: `BUILD SUCCESSFUL in 10s`; `compileDebugKotlin NO-SOURCE` and
`compileDebugAndroidTestKotlin NO-SOURCE` were reported.

```powershell
adb -s 10.64.241.158:40867 install -r app\build\outputs\apk\debug\app-debug.apk
adb -s 10.64.241.158:40867 install -r app\build\outputs\apk\androidTest\debug\app-debug-androidTest.apk
```

Result: both installs returned `Success`.

```powershell
adb -s 10.64.241.158:40867 shell am instrument -w -r -e class cn.edu.app.douyu.VisualSmokeInstrumentedTest cn.edu.app.douyu.test/androidx.test.runner.AndroidJUnitRunner
```

Result: `OK (1 test)`, time `128.255` seconds. The test reported device output
directory `/storage/emulated/0/Android/data/cn.edu.app.douyu/files/visual-smoke`.

```powershell
adb -s 10.64.241.158:40867 pull /storage/emulated/0/Android/data/cn.edu.app.douyu/files/visual-smoke doc\development\verification\android-java-xml-screenshots\real-backend
```

Result: 22 PNG screenshots pulled, all `1200x2664`.

## Screen Evidence

| Screenshot                         | Coverage                                     |
| ---------------------------------- | -------------------------------------------- |
| `main_community.png`               | Community tab with real backend response     |
| `main_commerce.png`                | Commerce tab with real backend response      |
| `main_ai.png`                      | AI tab with authenticated API boundary       |
| `main_messages.png`                | Messages tab with authenticated API boundary |
| `main_profile.png`                 | Profile tab with authenticated API boundary  |
| `main_quick_menu.png`              | Global quick menu                            |
| `search.png`                       | Search UI-only boundary                      |
| `post_create.png`                  | Post compose UI boundary                     |
| `post_detail.png`                  | Post detail entered by test extra            |
| `ai_flow.png`                      | AI flow entered by test extras               |
| `camera.png`                       | CameraX visual boundary                      |
| `product_detail.png`               | Product detail entered by test extra         |
| `payment_boundary.png`             | Payment boundary                             |
| `conversation.png`                 | Conversation entered by test extra           |
| `notification_detail.png`          | Notification detail entered by test extras   |
| `profile_edit.png`                 | Profile edit boundary                        |
| `settings_home.png`                | Settings home                                |
| `settings_account_security.png`    | Settings account/security                    |
| `settings_privacy_permissions.png` | Settings privacy/permissions                 |
| `settings_notifications.png`       | Settings notifications                       |
| `settings_about_compliance.png`    | Settings about/compliance                    |
| `future_capability.png`            | Future capability boundary                   |

## Differences To Fix

| ID     | Severity | Evidence                                                                                              | Expected from docs/design                                                                                                                                                                                                                                                          | Actual in this run                                                                                                                                                                                                                                                 | Repair recommendation                                                                                                                                                                                                                           | Status |
| ------ | -------- | ----------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------ |
| RB-001 | High     | API summary; `main_community.png`; `main_quick_menu.png`                                              | `doc/development/current-status.md` and `18-unfinished-and-blockers.md` state runtime seed/demo content was removed. `10-testing-acceptance.md` requires empty feed state after seed/demo removal. Open Design permits real content only when it is real, not startup demo filler. | The persistent dev database returned 4 feed items and the response body matched `post_seed`. The Community UI rendered those old seeded posts instead of an empty state.                                                                                           | Add a non-destructive local QA cleanup or clean-database verification path for persisted seed rows. Re-run real-backend smoke against a clean database and record whether Community shows the empty feed state. Do not restore runtime seeders. | Open   |
| RB-002 | High     | API summary; `main_commerce.png`                                                                      | `13-ui-screen-blueprints.md` requires product grid from backend data and empty product state without mock products. `10-testing-acceptance.md` requires empty product list state after runtime seed/demo removal.                                                                  | The persistent dev database returned 6 product items and the response body matched `prod_`. The Commerce UI rendered old seeded products instead of the empty product state.                                                                                       | Add a clean-database verification path or explicit QA fixture separation for products. Re-run with no persisted seeded products and confirm the Commerce empty state.                                                                           | Open   |
| RB-003 | Medium   | API summary; `main_community.png`; `main_commerce.png`                                                | Seed/demo cleanup scope included posts, products, topics, stickers, system demo users, static seed images, and `/seed/**` exposure.                                                                                                                                                | Topics returned 6 items and sticker packs returned 1 item. The simple string scan did not match `seedTopic` or `seedPost`, but the counts are suspicious in a database expected to be empty after cleanup.                                                         | Inspect the local dev database data source and decide whether these are legitimate user-created rows or old seed residue. Document the decision before claiming empty-data parity for topics/stickers.                                          | Open   |
| RB-004 | Medium   | `main_ai.png`, `main_messages.png`, `main_profile.png`                                                | `13-ui-screen-blueprints.md` and Open Design require empty states, login prompts, or explicit development boundaries. `10-testing-acceptance.md` specifically expects AI history, Messages, and Profile asset empty states to be visible after seed/demo removal.                  | The unauthenticated tabs displayed generic error cards with `HTTP 401` and a retry action. This is technically clear but not the designed login/empty boundary.                                                                                                    | Map HTTP 401 in Java repositories/UI state to an unauthenticated state, with copy and actions matching AI, Messages, Profile, and Settings designs. Keep network failures as retryable errors.                                                  | Open   |
| RB-005 | Medium   | `post_detail.png`, `product_detail.png`, `conversation.png`, `notification_detail.png`, `ai_flow.png` | Real-backend validation should distinguish real API-backed screens from UI/test-fixture entry screens.                                                                                                                                                                             | Several secondary screenshots were entered with `VisualSmokeInstrumentedTest` extras such as `post_visual_check`, `product_visual_check`, `conv_visual_check`, `notif_visual_check`, and `job_visual_check`. They prove layout rendering, not full real-data flow. | Extend a later instrumentation suite to create or select explicit QA fixtures through API, then open secondary screens with real returned IDs. Keep this smoke suite for visual layout coverage.                                                | Open   |

## Accepted Differences

- Android screenshots include the physical device status bar and gesture
  navigation bar; Open Design HTML uses a browser mock phone frame.
- Camera preview content is environment-dependent and only verifies CameraX
  screen framing and controls.
- Stub provider boundaries for SMS, OSS/local storage, AI, and payment remain
  development integration boundaries and are not production capability claims.

## Shutdown Evidence

After screenshot capture, the test environment was closed.

```powershell
Stop-Process -Id 10236 -Force
Stop-Process -Id 25136 -Force
docker compose stop postgres redis
netstat -ano | Select-String ':8081'
docker compose ps
```

Result:

- Stopped PID `10236` (`cmd` launcher).
- Stopped PID `25136` (`java` Spring Boot process).
- `douyu-postgres` stopped.
- `douyu-redis` stopped.
- `8081` had no listener after shutdown.
- `docker compose ps` returned no running services.

## Follow-Up Gate

Before closing RB-001 through RB-004, run a fresh real-backend smoke against a
known clean database or an explicitly documented QA fixture database and update
this log with:

- API counts for feed, products, topics, and sticker packs.
- Whether AI, Messages, and Profile use login/empty boundaries instead of raw
  `HTTP 401` cards.
- New screenshot directory and contact sheet path.
- Backend shutdown evidence for that run.

## Repair-Stage Update

See `doc/development/verification/2026-06-07-repair-stage-verification.md` for
the follow-up run.

Summary of the follow-up evidence:

- `qa-empty` clean-backend HTTP checks closed the API side of `RB-001`,
  `RB-002`, and `RB-003`: feed, products, topics, and sticker packs all returned
  200 with empty `items` and `total=0`.
- Android local unit coverage closed the code-path side of `RB-004`: HTTP 401 is
  preserved as `ApiException.statusCode=401` and maps to `LoadState.LOGIN_REQUIRED`.
- `RB-005` is closed for supported real-ID flows: the real-backend smoke
  instrumentation now captures backend-returned post, product, AI job/pattern,
  conversation, and notification detail screenshots.
- The device later became available again. Repair-stage real-device evidence is
  now stored under
  `doc/development/verification/android-java-xml-screenshots/repair-stage/`,
  and the repair-stage parity status is recorded in
  `doc/development/verification/2026-06-07-repair-stage-verification.md`.
