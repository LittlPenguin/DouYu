# Post Detail Thumbnail Selected Border Verification

## Scope

- Repair selected thumbnail image overflow in Android post detail gallery.
- Preserve existing gallery navigation, swipe, and thumbnail selection behavior.

## TDD

- Red:
  - `.\gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.core.OpenDesignLayoutMappingTest.postDetailAndAuthContractsRemainRealBackendDriven`
  - Result: failed at `OpenDesignLayoutMappingTest.java:217` because `PostDetailActivity` did not yet import/use `FrameLayout` thumbnail containers and still placed the selected background directly on the thumbnail `ImageView`.
- Green:
  - Same targeted test passed after rendering gallery thumbnails with an outer selected/unselected container and clipped inner image.

## Commands

- `.\gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.core.OpenDesignLayoutMappingTest.postDetailAndAuthContractsRemainRealBackendDriven`
- `.\gradlew.bat :app:testDebugUnitTest`
- `.\gradlew.bat :app:assembleDebug`
- `Get-ChildItem -Path DouYu\app\src -Recurse -Filter *.kt`
- `rg -n "Compose|compose|kotlinx|DataStore|coroutines|serialization|Jetpack Compose|androidx\.compose" DouYu\app\src`
- `git diff --check`

## Results

- Target Android contract test:
  - `.\gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.core.OpenDesignLayoutMappingTest.postDetailAndAuthContractsRemainRealBackendDriven`
  - Result: `BUILD SUCCESSFUL`.
- Android JVM suite:
  - `.\gradlew.bat :app:testDebugUnitTest`
  - Result: `BUILD SUCCESSFUL`.
- Android debug build:
  - `.\gradlew.bat :app:assembleDebug`
  - Result: `BUILD SUCCESSFUL`.
- Backend regression suite:
  - First attempted `mvn test` from repository root; result: failed because root has no `pom.xml`.
  - Corrected command: `mvn test` from `doyu-server`.
  - Result: `Tests run: 57, Failures: 0, Errors: 0, Skipped: 0`; `BUILD SUCCESS`.
- Android architecture guards:
  - `Get-ChildItem -Path DouYu\app\src -Recurse -Filter *.kt`
  - Result: no output.
  - `rg -n "Compose|compose|kotlinx|DataStore|coroutines|serialization|Jetpack Compose|androidx\.compose" DouYu\app\src`
  - Result: no matches; `rg` returned exit code 1 as expected for no matches.
- Whitespace check:
  - `git diff --check`
  - Result: exit code 0. Git warned that `doyu-server/src/test/java/cn/edu/app/douyu/server/DouyuBackendContractTests.java` will normalize CRLF to LF next time Git touches it; no whitespace errors.
- Device availability:
  - `adb devices`
  - Result: `10.64.241.158:40765 device`.
  - `adb shell wm size`; `adb shell getprop ro.product.model`
  - Result: `Physical size: 1200x2664`; `ELI-AN00`.

## Notes

- This fix did not start the backend or perform a device smoke flow, so no runtime post-detail screenshot was captured after the code change.
- The thumbnail repair is scoped to the post detail gallery strip. Comment image thumbnails and draft media thumbnails were not changed.
