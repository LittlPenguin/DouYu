# Post Publish Navigation Verification

Date: 2026-06-11

Scope:

- Android post publish success flow.
- Upload tab draft state after successful publish.
- Post detail back/up navigation when opened from publish success.

Open Design parity:

- Relevant references: `post-compose-a.html` and `post-detail-comment-toolbar-a.html`.
- No XML structure, visual hierarchy, copy, or layout spacing changed in this fix.
- The change is limited to Java navigation/state handling and the documented navigation contract.

Verified behavior:

- `PostCreateFragment` clears the upload draft after a successful `createPost` response: title, body, pending images, selected topics, success actions, and publish state are reset.
- The publish-success detail launch passes `returnTo=community`.
- `PostDetailActivity` uses `returnTo=community` only for publish-success launches; back/up starts `MainActivity` with `section=community` and `CLEAR_TOP | SINGLE_TOP`.
- Existing detail entries without `returnTo=community` keep the normal `finish()` behavior.

Commands run:

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.core.PostPublishNavigationContractTest --console=plain
.\gradlew.bat :app:testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
.\gradlew.bat :app:lintDebug --console=plain
```

Results:

- `PostPublishNavigationContractTest`: BUILD SUCCESSFUL.
- `:app:testDebugUnitTest`: BUILD SUCCESSFUL.
- `:app:assembleDebug`: BUILD SUCCESSFUL.
- `:app:lintDebug`: BUILD SUCCESSFUL.

Static checks:

```powershell
rg --files DouYu\app\src | rg "\.kt$"
rg -n "Compose|androidx\.compose|kotlinx|DataStore|Navigation Compose|resetComposerAfterPublish|composer draft" DouYu\app\src\main DouYu\app\src\test -S
```

Results:

- No `.kt` files under `DouYu/app/src`.
- No Compose/Kotlin/DataStore references in Android main/test source.

Not covered:

- No device/manual upload smoke test was run for this focused fix.
