# Current Status

> Updated: 2026-06-08

## Stage Conclusion

The Android client has been migrated to the target implementation style for
this stage: traditional Android Java + Activity/Fragment + XML. The previous
Kotlin + Jetpack Compose implementation is no longer present under
`DouYu/app/src`.

This stage replaced the Android client shell, feature entry screens, model,
network, repository, layouts, resources, and unit tests with Java/XML code while
preserving the existing `/api/v1` backend contract.

The 2026-06-07 repair-stage verification is complete for the Java/XML rewrite
and is recorded in
`doc/development/verification/2026-06-07-repair-stage-verification.md`. The
earlier real-backend smoke log remains as historical finding evidence; its
RB-001 through RB-005 findings were closed or reclassified during the repair
stage.

## Current Facts

- `AGENTS.md` defines the Java/XML migration rules, Open Design authority,
  static-data policy, and verification rules.
- Android production and test source under `DouYu/app/src` has no `.kt` files.
- Android app/build/version-catalog files no longer expose Compose, Navigation
  Compose, Kotlin serialization, Coil Compose, or Paging Compose dependencies.
- Android list screens now load through Retrofit + Gson repositories and render
  empty/error states instead of local mock/demo lists.
- Android main tabs and flow screens now have dedicated XML layout resources:
  `fragment_community_home.xml`, `fragment_commerce_home.xml`,
  `fragment_ai_home.xml`, `fragment_messages_home.xml`,
  `fragment_profile_home.xml`, and dedicated `activity_*.xml` files for search,
  post compose/detail, AI flow, commerce/payment, message, profile, Settings,
  and future UI-only capability boundaries. The previous generic
  `SimplePageActivity`, `activity_simple.xml`, and `fragment_list_state.xml`
  shells are no longer present.
- Backend runtime seed/demo content has been removed: `DataInitializer` is gone,
  `AdminBootstrapRunner` only creates the admin bootstrap account, `/seed/**` is
  no longer public, and `static/seed` resources were deleted.
- Backend tests now create required product, SKU, post, topic, and sticker data
  as explicit test fixtures.
- Open Design HTML under `doc/development/open-design/` remains the UI authority.
- A real-device Java/XML visual smoke suite exists in
  `VisualSmokeInstrumentedTest.java` and captures 22 screens on the attached
  device. The current retained screenshot evidence is stored under
  `doc/development/verification/android-java-xml-screenshots/repair-stage/`.
- Repair-stage evidence separates real API-backed detail flows from boundary,
  disabled, empty, and UI-only coverage.

## Target Android Stack

- Java source only for Android production and test code.
- XML layouts and drawable resources for UI.
- Main Activity + five main Tab Fragments + secondary Activities.
- Dedicated XML layout per Open Design main tab and flow screen; Java binds
  state, extras, API data, and click handlers.
- AppCompat or AndroidX Activity/Fragment, Material Components,
  ConstraintLayout, RecyclerView, Retrofit + Gson, OkHttp, Glide, CameraX View.
- SharedPreferences for Java login/session persistence.

## Target UI Authority

Main screens:

- Community: `community-home-a.html`
- Commerce: `commerce-home-a.html`
- AI: `ai-home-a.html`
- Messages: `messages-a.html`
- Profile: `profile-a.html`

Flow screens:

- Search, login, post compose, post detail, notification detail, conversation,
  profile edit, Settings sections, AI image flow, commerce order/payment flow.

UI parity means structure, hierarchy, spacing, copy, states, and interactions.
When real data is empty, the app must render the designed empty state instead of
filling the page with fake content.

## Static Data Policy

Removed runtime demo data:

- Android `MockData`, mock repositories, demo lists, fake orders, fake payments.
- Backend seed posts, products, topics, stickers, system demo users, static seed
  images, and `/seed/**` public exposure.

Stub providers retained:

- SMS stub code, OSS stub/local provider, AI stub/provider boundaries, payment
  stub/integration skeleton. These are development provider boundaries, not
  content filler.

## Verification Snapshot

Fresh full verification run on 2026-06-06:

- `rg --files DouYu/app/src | rg "\.kt$"`: no output.
- `rg -n "compose|Composable|Navigation Compose|kotlinx|MockData|coil\.compose|paging\.compose" DouYu/app DouYu/build.gradle.kts DouYu/gradle/libs.versions.toml`: no output.
- `rg -n "DataInitializer|static/seed|/seed/|post_seed|prod_|seedTopic|seedProduct|seedPost|configuredSeedBaseUrl|topic_beginner|sku_bead" doyu-server/src/main doyu-server/src/test`: no output.
- `cd DouYu && .\gradlew.bat :app:testDebugUnitTest --console=plain`: passed.
- `cd DouYu && .\gradlew.bat :app:assembleDebug --console=plain`: passed.
- `cd DouYu && .\gradlew.bat :app:lintDebug --console=plain`: passed; generated lint XML contained 0 issues.
- `cd doyu-server && mvn test`: passed, 54 tests.

Additional verification captured during the 2026-06-07 UI parity pass:

- Attached real device: `10.64.241.158:38407`, model `ELI-AN00`, Android `16`.
- The device later recovered as `10.64.241.158:40867`; both APK installs
  succeeded and the final real-device visual smoke returned `OK (1 test)`.
- `cd DouYu && .\gradlew.bat :app:assembleDebug --console=plain`: passed after
  the latest Open Design XML/style edits.
- `cd DouYu && .\gradlew.bat :app:testDebugUnitTest --console=plain`: passed.
- `cd DouYu && .\gradlew.bat :app:lintDebug --console=plain`: passed; the
  generated `lint-results-debug.xml` contains 0 `<issue>` entries.
- `cd doyu-server && mvn test`: passed, 54 tests.
- Android `.kt`, Compose/Kotlin, and backend seed/demo residue scans returned no
  matches; `git diff --check` reported line-ending warnings only.
- A static filler spot check for named demo UI copy in Android source returned
  no matches after the latest search/post-compose copy correction.
- `VisualSmokeInstrumentedTest` passed on the real device with `OK (1 test)` in
  `127.766` seconds and captured 22 PNG files, including main tabs, quick menu,
  CameraX preview/capture boundary, search, post compose/detail, product,
  payment, conversation, notification, profile edit, Settings sections, and
  future capability.
- The earlier real-device screenshot pass was superseded by the repair-stage
  evidence set. Old non-current screenshot directories were removed so the
  retained evidence path points to the latest repair-stage screenshots.
- Final post-fix gate was rerun after replacing text back buttons and
  unreadable disabled CTAs: Android unit tests passed, Android lint passed with
  0 issues, backend tests passed with 54 tests, and all residue/static-filler
  scans returned no output.

## Coverage State

Covered:

- Android Java/XML source and dependency residue checks.
- Android unit test, debug build, and lint task.
- Backend seed/demo residue checks.
- Backend unit/integration test suite.
- Real-device visual smoke harness and screenshot capture path.
- Open Design parity matrix for all 18 HTML references, backed by latest
  real-device screenshots.

Not covered:

- True production-provider verification for SMS, payment, object storage, AI
  generation, map/location, and full compliance documents.

The latest repair-stage real-device screenshots and
`doc/development/verification/2026-06-07-open-design-parity-matrix.md` record
screen-level Open Design parity for all 18 references. Accepted differences are
limited to real-device system bars versus the browser mock phone frame,
environment-dependent CameraX preview content, and empty/error states replacing
populated design examples when runtime data is not available.

## Current Active Work

- No open-design feature branch is currently in flight. The five Open Design
  branches below were merged into `main` on 2026-06-08; see "2026-06-08
  Post-Merge Snapshot".

## 2026-06-08 Open Design Merge

Five Open Design feature branches were merged into `main` and verified:

- `codex/commerce-open-design-boundary` — commerce home/payment boundary parity.
- `codex/messages-open-design` — messages three-screen rebuild (home,
  conversation, notification detail).
- `codex/ai-creation-detail` — AI creation detail flow (upload presign/confirm,
  pattern job create/cancel/favorite, quota).
- `profile-open-design` — "我的" page Open Design with real `/me` statistics and
  the `asset_*` state containers.
- `codex/community-post-detail-open-design` — community post detail with comment
  endpoints and toolbar.

Per-branch plans/verification:
`doc/development/19-community-post-detail-open-design-plan.md`,
`doc/development/19-messages-open-design-rebuild-plan.md`,
`doc/development/19-profile-open-design-plan.md`,
`doc/development/verification/2026-06-07-ai-creation-detail-plan.md`,
`doc/development/verification/2026-06-07-commerce-open-design-plan.md`,
`doc/development/verification/2026-06-08-profile-open-design-verification.md`.

Known follow-up: the AI and profile flows merged two parallel client-side
upload DTO pairs (`PresignUploadRequest`/`ConfirmUploadRequest` vs
`UploadPresignRequest`/`UploadConfirmRequest`) that target the same backend
upload endpoints; unifying them is a pending cleanup (see `05-api-contract.md`,
Upload section).

## 2026-06-08 Commerce Real Products Repair

The current commerce follow-up replaces the previous empty commerce boundary
with real API-backed product content. The target commerce home is:

- backend category chips with `精选` as the all-products default;
- a two-column masonry product grid whose card image heights follow backend
  `imageWidth` and `imageHeight`;
- real OSS product cover images imported by an explicit dev-only command;
- no runtime `商城规则说明` card or local mock product filler.

This import is a manual dev verification dataset. It is not a startup seed and
does not change the rule that fresh runtime databases start without content.

## 2026-06-08 Post Detail Repair

The post-detail follow-up has been implemented and verified against
`post-detail-comment-toolbar-a.html`:

- the standalone engagement card above comments is removed;
- like/comment/favorite actions live in the bottom static comment entry;
- tapping the static entry or comment action hides the static bar and shows a
  separate real-input overlay above the IME keyboard;
- author metadata now renders readable copy such as `作品 · yyyy-MM-dd` instead
  of raw backend status and full ISO timestamps;
- comment rows use backend data and render mentions, topics, stickers, and
  image-comment summaries through `PostDetailFormatter`;
- real-backend post-detail smoke captured home, focused comment input, and
  refreshed comments after a real submission on `10.64.241.158:41817`.

Verification is recorded in
`doc/development/verification/2026-06-08-post-detail-repair-verification.md`.

## 2026-06-08 Post-Merge Snapshot

Verification run on `main` after the five-branch merge:

- `cd doyu-server && mvn -o test`: passed, `Tests run: 65`.
- `cd DouYu && .\gradlew.bat :app:compileDebugUnitTestJavaWithJavac`: BUILD
  SUCCESSFUL (Android Java/XML production + unit-test sources compile, confirming
  the merged `DoyuApi` overloads and repository changes).
- `cd DouYu && .\gradlew.bat :app:testDebugUnitTest`: passed, 38 unit tests.
  `OpenDesignLayoutMappingTest.mainTabStateContainersStartHiddenInXml` was
  updated to assert the profile tab's redesigned `asset_error_box`/`asset_empty`
  gone-state containers (AI/messages still assert `error_box`/`empty_text`).

## Closed Repair Verification Targets

- `RB-001`: Closed. `qa-empty` feed returned an empty list and
  `main_community.png` records the repair-stage real-device boundary.
- `RB-002`: Closed. `qa-empty` products returned an empty list and
  `main_commerce.png` records the repair-stage real-device boundary.
- `RB-003`: Clean-backend topics and sticker packs are closed on `qa-empty`;
  dev persistent rows remain diagnostic-only and are not deleted.
- `RB-004`: Closed. Local Android code maps 401 to a login boundary, and
  repair-stage AI, Messages, and Profile screenshots do not show raw `HTTP 401`.
- `RB-005`: Closed for supported real-ID flows. The repair-stage real-device
  smoke captured backend-returned post, product, AI job/pattern, conversation,
  and notification detail screenshots.

Final repair-stage evidence is stored in
`doc/development/verification/2026-06-07-repair-stage-verification.md`,
`doc/development/verification/2026-06-07-open-design-parity-matrix.md`, and the
repair-stage screenshot directory.

## Latest Repair-Stage Verification

Captured on 2026-06-07 across the initial non-device pass and the later
device-resumed pass:

- `rg --files DouYu/app/src | rg "\.kt$"`: no output.
- `rg -n "compose|Composable|Navigation Compose|kotlinx|MockData|coil\.compose|paging\.compose" DouYu/app`: no output.
- Backend seed/demo scan found only explicit negative test assertions, not
  production runtime seed code.
- `cd DouYu && .\gradlew.bat :app:testDebugUnitTest --console=plain`: passed.
- `cd DouYu && .\gradlew.bat :app:assembleDebug --console=plain`: passed.
- `cd DouYu && .\gradlew.bat :app:lintDebug --console=plain`: passed with 0
  lint issues in `lint-results-debug.xml`.
- `cd DouYu && .\gradlew.bat :app:assembleDebugAndroidTest --console=plain`:
  passed.
- `cd doyu-server && mvn "-Dtest=QaEmptyBackendContractTests,DefaultRuntimeNoSeedContractTests" test`:
  passed, 8 tests.
- `cd doyu-server && mvn test`: passed, 62 tests.
- Initial `qa-empty` HTTP verification on `http://127.0.0.1:8082`: feed,
  products, topics, and sticker packs all returned 200 with empty lists.
- Authenticated QA fixture verification returned real `conversationId` and
  `notificationId` with `userAId` matching the logged-in user.
- Anonymous QA fixture access returned 401.
- Read-only dev residue diagnostic found historical rows in the persistent dev
  volume: `post_seed% = 4`, `prod_% = 6`, `topics = 6`, and
  `sticker_packs = 1`. They were recorded as environment residue only and were
  not deleted.
- The `qa-empty` Spring Boot process, PostgreSQL, and Redis were stopped after
  verification; `8082` had no listener, only `TIME_WAIT`.
- Device-resumed `qa-empty` verification ran on `http://127.0.0.1:8081` with
  Android API base URL `http://10.64.241.153:8081/`; Spring Boot used the
  isolated `jdbc:postgresql://localhost:55433/douyu_qa_empty` datasource.
- `VisualSmokeInstrumentedTest` passed on `10.64.241.158:40739` with
  `OK (1 test)` in `126.924` seconds and captured 22 repair-stage screenshots.
- `RealBackendSmokeInstrumentedTest` passed on `10.64.241.158:40739` with
  `OK (1 test)` in `23.596` seconds and captured 5 real-ID screenshots.
- Repair-stage screenshot directories:
  `doc/development/verification/android-java-xml-screenshots/repair-stage/visual-smoke`
  and
  `doc/development/verification/android-java-xml-screenshots/repair-stage/real-backend-smoke`.
- Repair-stage contact sheets:
  `doc/development/verification/android-java-xml-screenshots/repair-stage/visual-smoke-contact-sheet.png`
  and
  `doc/development/verification/android-java-xml-screenshots/repair-stage/real-backend-smoke-contact-sheet.png`.
- After verification, Spring Boot PID `7100` was stopped and `8081` had no
  listener; `douyu-postgres-qa-empty` and `douyu-redis-qa-empty` were stopped.
