# 2026-06-07 Java/XML Verification

## Scope

This record covers the Java/XML Open Design refinement pass for:

- `activity_main.xml` quick menu, top icons, and bottom navigation selected state.
- `include_page_toolbar.xml` secondary page toolbar.
- `activity_search.xml`, `activity_post_create.xml`,
  `activity_post_detail.xml`, and `activity_settings_home.xml`.
- Development documentation updates for current real-device evidence and
  remaining UI parity work.
- Follow-up removal of UI text that could be interpreted as runtime static
  filler. Search and post-compose now use empty/input/development boundary copy
  instead of named demo content.

## Commands And Results

Android residue checks:

```powershell
rg --files DouYu\app\src | rg "\.kt$"
rg -n "compose|Composable|Navigation Compose|kotlinx|MockData|coil\.compose|paging\.compose|SimplePageActivity|activity_simple|fragment_list_state" DouYu\app DouYu\build.gradle.kts DouYu\gradle\libs.versions.toml
```

Result: no output.

Runtime static filler spot check:

```powershell
rg -n "草莓小熊|小屿|阿澄|知知|海盐|薄荷色卡|新手杯垫|示例卡片" DouYu\app\src\main
```

Result: no output.

Backend seed/demo residue check:

```powershell
rg -n "DataInitializer|static/seed|/seed/|post_seed|prod_|seedTopic|seedProduct|seedPost|configuredSeedBaseUrl|topic_beginner|sku_bead" doyu-server\src\main doyu-server\src\test
```

Result: no output.

Whitespace check:

```powershell
git diff --check
```

Result: exit 0. Git reported line-ending warnings only.

Android build and tests:

```powershell
cd DouYu
.\gradlew.bat :app:assembleDebug --console=plain
.\gradlew.bat :app:testDebugUnitTest --console=plain
.\gradlew.bat :app:lintDebug --console=plain
```

Result: all exit 0. `lint-results-debug.xml` contains 0 `<issue>` entries.

After removing the UI static-filler risk, `cd DouYu &&
.\gradlew.bat :app:assembleDebug --console=plain` was rerun and exited 0.

Backend tests:

```powershell
cd doyu-server
mvn test
```

Result: exit 0, `Tests run: 54, Failures: 0, Errors: 0, Skipped: 0`.

Android device build:

```powershell
cd DouYu
$env:DOUYU_ANDROID_API_BASE_URL='http://127.0.0.1:65534/'
$env:DOUYU_ANDROID_CLEARTEXT_HOSTS='127.0.0.1,localhost'
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
```

Result: exit 0.

Real-device install/smoke attempt before reconnect:

```powershell
adb -s 10.64.241.158:43089 install -r D:\Studio\SpellBean\DouYu\app\build\outputs\apk\debug\app-debug.apk
adb -s 10.64.241.158:43089 install -r D:\Studio\SpellBean\DouYu\app\build\outputs\apk\androidTest\debug\app-debug-androidTest.apk
adb -s 10.64.241.158:43089 shell am instrument -w -r -e class cn.edu.app.douyu.VisualSmokeInstrumentedTest cn.edu.app.douyu.test/androidx.test.runner.AndroidJUnitRunner
```

Result: debug APK install succeeded. The device went offline during the
androidTest APK install, so the refreshed instrumentation screenshot suite did
not run.

ADB recovery attempts:

```powershell
adb reconnect offline
adb kill-server
adb start-server
adb devices -l
adb connect 10.64.241.158:43089
adb connect 10.64.241.158:38407
adb connect 10.64.241.158:5555
adb mdns services
```

Result: no connected device after server restart; previous ports refused
connections and mDNS reported no wireless debug service.

Real-device recovery and final smoke:

```powershell
adb devices -l
cd DouYu
$env:DOUYU_ANDROID_API_BASE_URL='http://127.0.0.1:65534/'
$env:DOUYU_ANDROID_CLEARTEXT_HOSTS='127.0.0.1,localhost'
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
adb -s 10.64.241.158:40867 install -r D:\Studio\SpellBean\DouYu\app\build\outputs\apk\debug\app-debug.apk
adb -s 10.64.241.158:40867 install -r D:\Studio\SpellBean\DouYu\app\build\outputs\apk\androidTest\debug\app-debug-androidTest.apk
adb -s 10.64.241.158:40867 shell am instrument -w -r -e class cn.edu.app.douyu.VisualSmokeInstrumentedTest cn.edu.app.douyu.test/androidx.test.runner.AndroidJUnitRunner
adb -s 10.64.241.158:40867 pull /storage/emulated/0/Android/data/cn.edu.app.douyu/files/visual-smoke D:\Studio\SpellBean\doc\development\verification\android-java-xml-screenshots\real-device
```

Result: device recovered as `10.64.241.158:40867`; both APK installs succeeded;
instrumentation returned `OK (1 test)` in `127.766` seconds; 22 screenshots were
pulled to the real-device evidence directory.

Follow-up visual fix:

- `include_page_toolbar.xml` now uses `ic_action_back.xml` instead of a text
  arrow.
- Disabled/UI-only CTA surfaces in `activity_search.xml` and
  `activity_post_create.xml` were changed from disabled `MaterialButton`
  controls to visible warning-pill TextViews so the boundary copy remains
  readable on the real device.
- The final smoke after this fix again returned `OK (1 test)`, and the 22 PNG
  screenshots under `real-device/visual-smoke` have 2026-06-07 11:52 timestamps.

Final gate after the last visual fix:

```powershell
rg --files DouYu\app\src | rg "\.kt$"
rg -n "compose|Composable|Navigation Compose|kotlinx|MockData|coil\.compose|paging\.compose|SimplePageActivity|activity_simple|fragment_list_state" DouYu\app DouYu\build.gradle.kts DouYu\gradle\libs.versions.toml
rg -n "DataInitializer|static/seed|/seed/|post_seed|prod_|seedTopic|seedProduct|seedPost|configuredSeedBaseUrl|topic_beginner|sku_bead" doyu-server\src\main doyu-server\src\test
rg -n "草莓小熊|小屿|阿澄|知知|海盐|薄荷色卡|新手杯垫|示例卡片" DouYu\app\src\main
git diff --check
cd DouYu
.\gradlew.bat :app:testDebugUnitTest --console=plain
.\gradlew.bat :app:lintDebug --console=plain
cd ..\doyu-server
mvn test
```

Result:

- All `rg` scans returned no output.
- `git diff --check` exited 0 with line-ending warnings only.
- Android unit tests passed.
- Android lint passed and `lint-results-debug.xml` contained 0 `<issue>`
  entries.
- Backend tests passed: `Tests run: 54, Failures: 0, Errors: 0, Skipped: 0`.

## Evidence

Latest real-device screenshot evidence from the successful Java/XML smoke run:

`doc/development/verification/android-java-xml-screenshots/real-device/visual-smoke`

This evidence proves the Java/XML screens render on hardware after the latest
XML edits, including CameraX preview, quick menu, five main tabs, search, post
compose/detail, commerce/payment, message, profile edit, Settings sections, and
future capability boundary. Screen-level Open Design parity for all 18 HTML
references is recorded separately in
`doc/development/verification/2026-06-07-open-design-parity-matrix.md`.
