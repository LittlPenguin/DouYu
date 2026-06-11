# 2026-06-11 Main Navigation Unification Verification

## Scope

- Unify the jump behavior for the bottom entries:
  - 社区
  - 商城
  - 上传
  - 消息
  - 我的
- Keep the existing Java + XML app shell.
- Keep `上传` as an action item that opens `PostCreateActivity`, not as a main
  Fragment tab.

## Behavior

- `MainActivity` now handles section routing in one place:
  - first launch via `handleSectionIntent(getIntent())`
  - reused task navigation via `onNewIntent(Intent intent)`
- Section-to-tab mapping is centralized in `sectionToTabIndex`.
- `PostCreateActivity` returns to the existing main shell with:
  - `Intent.FLAG_ACTIVITY_CLEAR_TOP`
  - `Intent.FLAG_ACTIVITY_SINGLE_TOP`
  - `IntentExtras.SECTION`
- This fixes the inconsistent case where tapping 社区/商城/消息/我的 from the
  upload page could bring an existing `MainActivity` to the front without
  switching to the requested section.

## Commands Run

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:testDebugUnitTest --tests "cn.edu.app.douyu.core.MainNavigationContractTest" --console=plain
```

Result: failed before implementation, then passed after adding `onNewIntent`
handling and `FLAG_ACTIVITY_SINGLE_TOP`.

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:testDebugUnitTest --tests "cn.edu.app.douyu.core.OpenDesignLayoutMappingTest.mainNavigationUsesFourContentTabsPlusUploadAction" --tests "cn.edu.app.douyu.core.OpenDesignLayoutMappingTest.postCreateUsesSharedBottomNavigationAndHighlightsUpload" --tests "cn.edu.app.douyu.core.RemovedFeatureContractTest.androidRuntimeDoesNotExposeAiMapApiOrPaymentPages" --console=plain
```

Result: passed.

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
git diff --check
```

Result: no whitespace errors. Git reported line-ending warnings for existing
touched files.

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:testDebugUnitTest --console=plain
```

Result: passed.

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:assembleDebug --console=plain
```

Result: passed.

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:lintDebug --console=plain
```

Result: exit code 0. Lint report generated at
`D:\Studio\SpellBean\DouYu\app\build\reports\lint-results-debug.html` with
timestamp `2026-06-11 10:42:18`.

## Not Covered

- No emulator or physical-device navigation smoke was run in this pass.
- No screenshot capture was produced.
