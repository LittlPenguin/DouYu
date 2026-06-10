# 2026-06-08 Post-Merge Real Device Verification

## Scope

This record verifies the merged Java/XML Android client on a real device against
`doc/development` and the 18 Open Design screen references under
`doc/development/open-design/`.

This run did not delete Docker volumes, clear the dev database, or restore
runtime seed/demo content. The user requested the backend, Docker services, and
App runtime to remain available after the work.

## Preflight And Runtime State

| Check | Result |
| --- | --- |
| Initial branch | `main...origin/main [ahead 12]` |
| Initial ADB devices | `10.64.241.158:37387` and `adb-AU7KVB4713000875-e2LpPr._adb-tls-connect._tcp`, both `ELI_AN00`. |
| Selected device serial | Initial run used `10.64.241.158:37387`; final connected device is `10.64.241.158:39591`. |
| Device metadata | `ELI-AN00`, Android `16`, `1200x2664`, density `520`. |
| Initial port 8081 | Not listening before this run. |
| Initial Docker Compose | No running `doyu-server` services before this run. |
| Final backend | Running on `8081`, Java PID `34664`. |
| Final Docker Compose | `douyu-postgres` and `douyu-redis` are running and healthy. |
| Final ADB state | Device reconnected as `10.64.241.158:39591`, model `ELI_AN00`, and remains online. |

Spring Boot logs:

- `doyu-server/.qa-output/post-merge-20260608-springboot.log`
- `doyu-server/.qa-output/post-merge-20260608-springboot.err.log`

## Backend And API Smoke

| Endpoint | HTTP | Summary | Note |
| --- | --- | --- | --- |
| `/actuator/health` | 200 | `status=UP` | Healthy after restart. |
| `/api/v1/posts/feed?page=1&size=20` | 200 | `items=10`, `total=10` | Existing dev real content is present. No `post_seed` marker detected. |
| `/api/v1/products?page=1&size=20` | 200 | `items=[]`, `total=0` | Public product list now hides draft/reviewing smoke products. |
| `/api/v1/topics?page=1&size=20` | 200 | `items=6`, `total=6` | Existing dev topics are present. No `seedTopic` marker detected. |
| `/api/v1/sticker-packs` | 200 | `items=[]`, `total=0` | Empty state is valid. |

## Build And Test Evidence

| Gate | Result | Notes |
| --- | --- | --- |
| Android `.kt` residue | Pass | Count `0`. |
| Android Compose/Kotlin/mock residue | Pass | Count `0`. |
| Backend seed/demo residue scan | Pass with allowed test hits | Count `2`, both are test assertions checking absence of `DataInitializer`. |
| Backend tests | Pass | `mvn test`: `Tests run: 65, Failures: 0, Errors: 0, Skipped: 0`; `BUILD SUCCESS`. |
| Android unit tests | Pass | `:app:testDebugUnitTest`: `BUILD SUCCESSFUL`. |
| Android debug/APK build | Pass | `:app:assembleDebug :app:assembleDebugAndroidTest`: `BUILD SUCCESSFUL`. |
| Android lint | Pass | `:app:lintDebug` returned exit `0`. |
| Visual smoke | Pass | Final run: `OK (1 test)`, time `126.846`. |
| Real backend smoke | Pass | Final rerun after device reconnect: `OK (1 test)`, time `23.723`; screenshots pulled successfully. |

## Screenshot Evidence

| Evidence | Result |
| --- | --- |
| Visual screenshots | `doc/development/verification/android-java-xml-screenshots/2026-06-08-post-merge/visual-smoke`, 22 PNG files pulled. |
| Visual contact sheet | `doc/development/verification/android-java-xml-screenshots/2026-06-08-post-merge/visual-smoke-contact-sheet.png`, `1068x6240`, 22 files. |
| Real-backend screenshots | `doc/development/verification/android-java-xml-screenshots/2026-06-08-post-merge/real-backend-smoke`, 5 PNG files pulled: `real_post_detail.png`, `real_product_detail.png`, `real_conversation.png`, `real_notification_detail.png`. |
| Real-backend contact sheet | `doc/development/verification/android-java-xml-screenshots/2026-06-08-post-merge/real-backend-smoke-contact-sheet.png`, `1068x1560`, 5 files. |

## Open Design Parity Matrix

| Open Design reference | Android screen | Latest evidence | Result | Notes |
| --- | --- | --- | --- | --- |
| `community-home-a.html` | `MainActivity` + `CommunityFragment` | `main_community.png` | Covered | Real dev community content renders in a two-column image grid. |
| `commerce-home-a.html` | `MainActivity` + `CommerceFragment` | `main_commerce.png` | Covered after PMV-003 | Public list shows empty state when only draft/reviewing products exist. |

| `messages-a.html` | `MainActivity` + `MessagesFragment` | `main_messages.png` | Covered after PMV-004 | Fixture peer copy is readable Chinese. |
| `profile-a.html` | `MainActivity` + `ProfileFragment` | `main_profile.png` | Covered after PMV-004 | Logged-in smoke user is readable Chinese. |
| `search-a.html` | `SearchActivity` | `search.png` | Covered | Search boundary and empty state visible. |
| `post-compose-a.html` | `PostCreateActivity` | `post_create.png` | Covered | Upload/review boundary and form surfaces visible. |
| `post-detail-comment-toolbar-a.html` | `PostDetailActivity` | `post_detail.png`, `real_post_detail.png` | Covered after PMV-005 | Visual layout and real-ID detail flow are both covered. |
| `message-conversation-a.html` | `ConversationActivity` | `conversation.png`, `real_conversation.png` | Covered after PMV-004 and PMV-005 | Visual layout and real backend conversation flow are both covered with readable Chinese copy. |
| `notification-detail-a.html` | `NotificationDetailActivity` | `notification_detail.png`, `real_notification_detail.png` | Covered after PMV-001, PMV-004, and PMV-005 | Visual fixture and real backend notification copy are readable Chinese. |
| `profile-edit-a.html` | `ProfileEditActivity` | `profile_edit.png` | Covered | Profile form and UI-only fields visible. |
| `settings-home-a.html` | `SettingsActivity` | `settings_home.png` | Covered | Settings sections visible. |
| `settings-account-security-a.html` | `SettingsActivity` section | `settings_account_security.png` | Covered | Security boundary visible. |
| `settings-privacy-permissions-a.html` | `SettingsActivity` section | `settings_privacy_permissions.png` | Covered | Permission/privacy boundaries visible. |
| `settings-notifications-a.html` | `SettingsActivity` section | `settings_notifications.png` | Covered | Notification options visible. |


| `doyu-design-directions.html` | Global Java/XML UI system | Visual contact sheet | Covered visually | Brand colors, card language, chips, top bars, bottom nav, and boundary states are consistently visible. |

## Deviations And Repairs

| ID | Severity | Area | Evidence | Fix | Status |
| --- | --- | --- | --- | --- | --- |
| PMV-001 | P1 | Notification detail visual smoke fixture | Source precheck found mojibake title/body extras. | Rebuilt `VisualSmokeInstrumentedTest` with readable Chinese copy and extended `SourceMojibakeSpotTest`. | Closed. `SourceMojibakeSpotTest` and final `VisualSmokeInstrumentedTest` passed; `notification_detail.png` is readable. |
| PMV-002 | P0 | Real backend smoke message fixture | `POST /api/v1/qa-empty/fixtures/message-thread` returned 404 under `dev`. | Limited the explicit fixture controller to `dev` and `qa-empty` profiles, and added `DevMessageFixtureContractTests`. | Closed. Focused backend tests passed and final `RealBackendSmokeInstrumentedTest` returned `OK (1 test)`. |
| PMV-003 | P1 | Commerce home public product list | `main_commerce.png` showed a `DRAFT/NEED_MANUAL_REVIEW` smoke product. | Changed `/api/v1/products` to list only `ON_SALE/PASS` products and added backend regression coverage. | Closed. Products API returned empty list and final `main_commerce.png` shows the empty state. |
| PMV-004 | P2 | Real backend smoke fixture copy | Real smoke fixtures used visible English `QA ...` copy. | Changed backend and Android smoke fixture copy to readable Chinese and used a new smoke phone number to avoid old user nickname residue. | Closed. Backend/Android tests passed, final real backend smoke passed, and `real_conversation.png` / `real_notification_detail.png` show readable Chinese copy. |
| PMV-005 | P1 | Final real-backend screenshot evidence | ADB went offline during final pull; local `real-backend-smoke` did not retain a complete valid PNG set. | Reconnected the device, reran/pulled `RealBackendSmokeInstrumentedTest` screenshots, and regenerated the contact sheet. | Closed. `real-backend-smoke` now contains 5 valid PNG files and `real-backend-smoke-contact-sheet.png` was regenerated. |

## Final State

- Backend remains running at `http://127.0.0.1:8081`, Java PID `34664`.
- Docker services remain running and healthy: `douyu-postgres`, `douyu-redis`.
- Final device remains online as `10.64.241.158:39591` (`ELI_AN00`).
- Android App and androidTest APK were installed successfully.
- Final visual evidence and final real-backend evidence are present and valid.
- `PMV-001` through `PMV-005` are closed. No new P0/P1 UI or function mismatch was found in the final post-merge real-device smoke evidence.
