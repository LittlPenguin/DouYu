# Post Detail Thumbnail Selected Border Repair Plan

## Background

The selected thumbnail in the Android post detail gallery can visually overflow the highlighted area. The current thumbnail rendering places the selected border background and the loaded bitmap on the same `ImageView`, so the Glide-loaded image can cover the pink stroke and ignore the rounded selected boundary.

## Goal

- Keep the selected pink thumbnail border visible.
- Prevent thumbnail bitmap content from overflowing the selected rounded area.
- Keep the existing Java/XML detail gallery, swipe behavior, thumbnail click behavior, and backend contracts unchanged.
- Avoid touching unrelated comment/media thumbnails.

## Implementation

- Add an Android JVM/static contract test before changing production code.
- Render each gallery thumbnail as a fixed-size outer container that owns the selected/unselected background.
- Place the actual image in an inner `ImageView` with its own rounded placeholder background and outline clipping.
- Keep the selected thumbnail scroll behavior based on `thumbnailStrip` child index.

## Verification

- Run the targeted Android unit contract test red, then green.
- Run the Android unit suite and debug assemble.
- Re-run Kotlin/Compose guard checks.
- Record all verification output in `doc/development/verification/2026-06-10-post-detail-thumbnail-selected-border-verification.md`.
