# 2026-06-09 Launcher Logo TBLogo Verification

## Scope

This verification covers the Android app launcher logo replacement only.
`assets/TBLogo.png` is the source image for:

- `DouYu/app/src/main/res/drawable/tb_logo.png`
- `DouYu/app/src/main/res/mipmap-mdpi/tb_launcher.png`
- `DouYu/app/src/main/res/mipmap-mdpi/tb_launcher_round.png`
- `DouYu/app/src/main/res/mipmap-hdpi/tb_launcher.png`
- `DouYu/app/src/main/res/mipmap-hdpi/tb_launcher_round.png`
- `DouYu/app/src/main/res/mipmap-xhdpi/tb_launcher.png`
- `DouYu/app/src/main/res/mipmap-xhdpi/tb_launcher_round.png`
- `DouYu/app/src/main/res/mipmap-xxhdpi/tb_launcher.png`
- `DouYu/app/src/main/res/mipmap-xxhdpi/tb_launcher_round.png`
- `DouYu/app/src/main/res/mipmap-xxxhdpi/tb_launcher.png`
- `DouYu/app/src/main/res/mipmap-xxxhdpi/tb_launcher_round.png`

Manifest entries stayed unchanged:

- `android:icon="@mipmap/tb_launcher"`
- `android:roundIcon="@mipmap/tb_launcher_round"`

Backend code was not changed.

## Resource Evidence

- `assets/TBLogo.png`: `1024x1024`, SHA-256 `82AB7B42F8F5738C8B5FAA97392273106165BB27DB0AD3988230BFB7CE8106D4`
- `DouYu/app/src/main/res/drawable/tb_logo.png`: SHA-256 `82AB7B42F8F5738C8B5FAA97392273106165BB27DB0AD3988230BFB7CE8106D4`
- Generated launcher sizes:
  - `mipmap-mdpi`: `48x48`
  - `mipmap-hdpi`: `72x72`
  - `mipmap-xhdpi`: `96x96`
  - `mipmap-xxhdpi`: `144x144`
  - `mipmap-xxxhdpi`: `192x192`

## Test Results

Command:

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:testDebugUnitTest --tests "cn.edu.app.douyu.core.OpenDesignLayoutMappingTest.launcherIconUsesTBLogoSourceAndExpectedDensitySizes" --console=plain
```

Red run after the test was corrected to avoid unavailable `java.desktop`
classes:

- Exit code: `1`
- Result: `1 test completed, 1 failed`
- Failure: `OpenDesignLayoutMappingTest > launcherIconUsesTBLogoSourceAndExpectedDensitySizes FAILED`
- Reason: `org.junit.ComparisonFailure`, caused by the old
  `res/drawable/tb_logo.png` SHA-256 not matching `assets/TBLogo.png`.

Green run after resource replacement:

- Exit code: `0`
- Result: `BUILD SUCCESSFUL in 6s`
- Scope: single launcher icon constraint test.

Full Android JVM unit tests:

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:testDebugUnitTest --console=plain
```

- Exit code: `0`
- Result: `BUILD SUCCESSFUL in 4s`

Java/XML compile:

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:compileDebugJavaWithJavac --console=plain
```

- Exit code: `0`
- Result: `BUILD SUCCESSFUL in 2s`

Diff whitespace check:

```powershell
cd D:\Studio\SpellBean
git diff --check
```

- Exit code: `0`
- Output included an existing line-ending warning for
  `doc/development/open-design/messages-a.html`; no whitespace errors were
  reported.

## Device Visual Verification

Device check:

```powershell
D:\AndroidChace\platform-tools\adb.exe devices
```

- Exit code: `0`
- Device: `10.64.241.158:40549 device`

Install:

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:installDebug --console=plain
```

- Exit code: `0`
- Result: `Installed on 1 device.`
- Device label from Gradle: `ELI-AN00 - 16`

Screenshot:

- Path:
  `doc/development/verification/android-java-xml-screenshots/2026-06-09-launcher-logo/app-info-icon.png`
- Coverage: system app-info page opened for `cn.edu.app.douyu`; screenshot shows
  the application icon, app name, and version.

## Notes

- Existing uncommitted message-page changes were left in place and not reverted.
- No backend verification was run because backend files were not modified.
