# Post Create Topic Chip Selection Style Plan

## Background

The post creation page topic selector sits below the media picker. Its Material `Chip` controls currently rely mostly on default checked styling with one muted text color, so selected and unselected topic tags look too similar.

## Goal

- Make selected topic tags visually distinct from unselected tags.
- Keep runtime data from the backend topic API; do not add mock topics or fallback content.
- Keep the existing Java/XML `PostCreateActivity` and Material `ChipGroup` architecture.
- Do not change the shared bottom navigation tab behavior or post detail comment chips.

## Implementation

- Add an Android static/JVM contract test before changing production code.
- Style each post-create topic chip with a checked-state `ColorStateList`:
  - checked: deep pink background, white text, deep pink stroke.
  - unchecked: white surface background, muted text, open-line stroke.
- Keep `setCheckedIconVisible(false)` and multi-select behavior unchanged.

## Verification

- Run the targeted Android unit contract test red, then green.
- Run Android unit tests and `assembleDebug`.
- Re-run Kotlin/Compose guard checks.
- Record results in `doc/development/verification/2026-06-10-post-create-topic-chip-selection-style-verification.md`.
