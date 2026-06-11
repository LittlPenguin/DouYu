# 2026-06-11 Profile Logout State Refresh Verification

## Scope

Fix Profile tab state after logout from Settings. Returning to Profile must not
continue showing the previous user's nickname, avatar, stats, or protected asset
cards after the local session is cleared.

## Implementation Notes

- `ProfileFragment` now refreshes from `SessionStore` in `onResume`.
- When `accessToken` is empty, Profile binds the logged-out header, resets stats
  to zero, restores the default avatar background, clears the asset adapter, and
  shows the logged-out empty state.
- Protected profile and asset requests now carry an access-token snapshot. If a
  request returns after logout or token change, its UI callback is discarded.
- Login/edit result callbacks route through the same session refresh path.

## Open Design Parity

`doc/development/open-design/profile-a.html` includes a logged-out Profile state
that says protected Profile data requires login and must not be filled with
local fake content. The Android behavior now follows that boundary by clearing
previous in-memory content when the local session is no longer present.

## Commands

| Command | Result | Evidence |
| --- | --- | --- |
| `cd DouYu; .\gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.core.ProfileLogoutStateContractTest` before the fix | Failed as expected | `ProfileLogoutStateContractTest > profileFragmentRefreshesAndClearsProtectedUiAfterLogout FAILED` at line 17, proving the regression contract caught missing refresh behavior. |
| `cd DouYu; .\gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.core.ProfileLogoutStateContractTest --rerun-tasks` after adding refresh and token-snapshot checks | Passed once | `BUILD SUCCESSFUL in 13s`; 1 test completed, 0 failures. |
| `Get-ChildItem -Path DouYu\app\src -Recurse -Filter *.kt` | Pass | Exit code 0 with no output: no `.kt` files under `DouYu/app/src`. |
| `rg -n "Compose\|compose\|kotlinx\|DataStore\|coroutines\|Navigation Compose\|androidx\.compose\|org\.jetbrains\.kotlin\|kotlin\." DouYu\app\src -g "*.java" -g "*.xml" -g "*.gradle" -g "*.kts"` | Pass | Exit code 1 with no output: no Compose/Kotlin runtime references in Android source. |
| `cd DouYu; .\gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.core.ProfileLogoutStateContractTest --rerun-tasks` after the final code cleanup | Blocked before test execution | Fresh Java compilation failed because existing untracked `DouYu/app/src/main/java/cn/edu/app/douyu/feature/community/PostCreateFragment.java` declares `public class PostCreateActivity`; javac requires that public class to be in `PostCreateActivity.java`. This file is not part of the logout fix. |
| `cd DouYu; .\gradlew.bat :app:assembleDebug` | Previously passed incrementally, fresh compile blocked | Incremental run passed before the fresh compile check. A later fresh compile is blocked by the same unrelated untracked `PostCreateFragment.java` public-class/file-name mismatch, so this pass is not counted as final proof. |
| `cd doyu-server; mvn test` | Pass | `Tests run: 64, Failures: 0, Errors: 0, Skipped: 0`; `BUILD SUCCESS`; finished at `2026-06-11T11:40:09+08:00`. |

## Existing Worktree Blocker

The Android fresh build/test gate is currently blocked by pre-existing,
untracked upload-tab WIP files:

- `DouYu/app/src/main/java/cn/edu/app/douyu/feature/community/PostCreateFragment.java`
- `DouYu/app/src/main/res/layout/fragment_post_create.xml`
- `doc/development/40-main-upload-open-tab-routing-plan.md`
- `doc/development/verification/2026-06-11-main-upload-open-tab-routing-verification.md`

`PostCreateFragment.java` is byte-for-byte identical to
`PostCreateActivity.java` and still declares `public class PostCreateActivity`,
which breaks any fresh Android Java compilation. This verification does not
modify or resolve that unrelated WIP.

## Device Coverage

Not covered. No emulator or physical device smoke was run in this pass.

Manual/device items still requiring coverage after the unrelated compile blocker
is resolved:

- Login, open Profile, verify user data appears.
- Logout from Settings, return to Profile, verify the page immediately shows the
  logged-out state with zero stats and no old asset cards.
- Repeat with slow network or backgrounded app to confirm stale profile/asset
  callbacks are ignored after logout.
