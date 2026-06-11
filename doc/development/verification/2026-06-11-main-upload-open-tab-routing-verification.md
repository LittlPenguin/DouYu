# 2026-06-11 Main Upload Fragment Routing Verification

## Scope

- Move the main bottom upload entry into `MainActivity.openTab(int index)`.
- Convert upload from a separate `PostCreateActivity` page into the main shell `PostCreateFragment` tab.
- Keep the bottom navigation order as `community / commerce / upload / messages / profile`.
- Keep upload runtime data backend-driven: topics, image upload, and post creation remain repository-backed.

## Behavior Verified

- `tab_upload` calls `openTab(2)`.
- `openTab(2)` opens `PostCreateFragment` through the same `open(label, fragment)` path as the other main tabs.
- `MainActivity` owns five tab arrays, including upload selected state.
- `IntentExtras.SECTION_UPLOAD` maps reused or fresh main intents to upload tab index `2`.
- `PostCreateActivity` is removed from the manifest and source tree.
- `fragment_post_create.xml` is scoped for the main shell content area and does not include nested bottom navigation or a nested back button.
- Upload login handling lives in `PostCreateFragment`; unauthenticated users see the upload boundary and are sent to login when they try to upload or publish.
- Successful post creation still opens `PostDetailActivity` with the backend post id.

## Commands Run

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:assembleDebug --console=plain
```

Result: passed. `BUILD SUCCESSFUL in 6s`.

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:testDebugUnitTest --tests "cn.edu.app.douyu.core.MainNavigationContractTest" --tests "cn.edu.app.douyu.core.OpenDesignLayoutMappingTest" --console=plain
```

Result: passed. `BUILD SUCCESSFUL in 6s`.

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:testDebugUnitTest --console=plain
```

Result: passed. `BUILD SUCCESSFUL in 6s`.

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:assembleDebugAndroidTest --console=plain
```

Result: passed. `BUILD SUCCESSFUL in 5s`.

```powershell
cd D:\Studio\SpellBean
rg --files DouYu\app\src | rg "\.kt$"
```

Result: no output.

```powershell
cd D:\Studio\SpellBean
rg -n "compose|Composable|Navigation Compose|kotlinx|MockData|tab_ai|AiFragment|PaymentBoundaryActivity" DouYu\app\src\main DouYu\app\src\test -S
```

Result: no output.

```powershell
cd D:\Studio\SpellBean
rg -n "PostCreateActivity|activity_post_create|post_bottom_nav|openMainSection|selectUploadNav" DouYu\app\src\main DouYu\app\src\androidTest -S
```

Result: no output.

```powershell
cd D:\Studio\SpellBean
rg -n "PostCreateActivity|activity_post_create|post_bottom_nav|openMainSection|selectUploadNav" DouYu\app\src -S
```

Result: remaining matches are unit-test negative assertions only.

```powershell
cd D:\Studio\SpellBean
git diff --check
```

Result: passed with line-ending warnings for existing modified docs, and no whitespace errors.

## Not Covered

- No emulator or physical-device navigation smoke has been run for this specific Fragment conversion pass.
- No screenshot capture has been produced for this specific pass.
- Backend tests are not required for this Android navigation-only change unless backend files change.
