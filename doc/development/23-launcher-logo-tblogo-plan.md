# Launcher Logo TBLogo Replacement Plan

## Goal

Use `assets/TBLogo.png` as the single source image for the Android app launcher
logo. This covers the normal launcher icon and the round launcher icon that are
referenced from `DouYu/app/src/main/AndroidManifest.xml`.

## Scope

- Keep Manifest entries unchanged:
  - `android:icon="@mipmap/tb_launcher"`
  - `android:roundIcon="@mipmap/tb_launcher_round"`
- Copy `assets/TBLogo.png` to `DouYu/app/src/main/res/drawable/tb_logo.png` so
  existing adaptive icon foreground resources use the same source image.
- Generate launcher density PNGs from `assets/TBLogo.png`:
  - `mipmap-mdpi`: 48 x 48
  - `mipmap-hdpi`: 72 x 72
  - `mipmap-xhdpi`: 96 x 96
  - `mipmap-xxhdpi`: 144 x 144
  - `mipmap-xxxhdpi`: 192 x 192
- Generate both `tb_launcher.png` and `tb_launcher_round.png` for every density.

## Non-Goals

- Do not change app name, theme colors, splash background, bottom navigation
  icons, in-page icons, Open Design pages, or backend code.
- Do not add same-name adaptive icon XML resources for `tb_launcher`.
- Do not revert or mix in existing message-page worktree changes.

## Verification

- Add a JVM resource constraint test that checks:
  - `assets/TBLogo.png` and `res/drawable/tb_logo.png` have identical SHA-256.
  - all launcher density PNGs exist with the expected dimensions.
  - Manifest launcher icon references stay on `@mipmap/tb_launcher` and
    `@mipmap/tb_launcher_round`.
- Run:
  - `.\gradlew.bat :app:testDebugUnitTest --tests "cn.edu.app.douyu.core.OpenDesignLayoutMappingTest.launcherIconUsesTBLogoSourceAndExpectedDensitySizes" --console=plain`
  - `.\gradlew.bat :app:testDebugUnitTest --console=plain`
  - `.\gradlew.bat :app:compileDebugJavaWithJavac --console=plain`
  - `git diff --check`
- If no Android device is attached, record launcher visual verification as not
  covered instead of passing.
