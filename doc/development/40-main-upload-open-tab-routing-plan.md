# Main Upload Fragment Routing Plan

Date: 2026-06-11

## Goal

- Make upload a real `MainActivity` Fragment tab instead of a separate upload Activity.
- Keep the bottom navigation order as community, commerce, upload, messages, profile.
- Route `openTab(2)` to the upload Fragment directly.
- Keep all upload data real-backend driven: topics, image presign/PUT/confirm, and post creation remain repository-backed.
- Avoid client mock data, fake file IDs, or local-only upload content.

## Scope

- Android Java/XML only.
- Convert the existing upload page behavior into `PostCreateFragment`.
- Add a fragment layout that fits inside the main shell content area and does not include a nested toolbar or nested bottom navigation.
- Update `MainActivity`, manifest, and static contract tests.
- Keep `PostCaptureActivity` as the camera capture helper Activity.

## Implementation Notes

- `MainActivity` should own all five bottom-nav entries in the tab arrays.
- `openTab(2)` should call `open("上传", new PostCreateFragment())`.
- Upload login handling should live inside `PostCreateFragment`; unauthenticated users can see the upload tab boundary and are prompted when they try to upload or publish.
- `PostCreateFragment` should reuse the existing upload flow, adapted to Fragment lifecycle APIs:
  - `registerForActivityResult(...)` from the Fragment.
  - `requireContext()` / `requireActivity()` instead of Activity `this`.
  - Fragment-safe repository background work and UI callbacks.
- Remove `PostCreateActivity` from the manifest so the upload page is not exposed as a separate Activity.
- The upload Fragment layout should reuse upload inputs, image grid, topic chips, preview, status band, publish button, and success actions, but must not include `include_bottom_nav`.

## Verification

- Add focused unit contract tests that fail against the current separate-Activity upload implementation.
- Run the focused contract test before implementation to confirm the failure.
- After implementation, run focused navigation tests plus the relevant layout mapping tests.
- Run Kotlin/Compose guard searches and `git diff --check`.
