# Java/XML UI Parity Plan

> Status: Historical completed plan for the Java/XML parity pass.
> This record was normalized after the 2026-06-09 cleanup. It no longer lists
> removed feature pages as parity targets. The retained parity evidence is in
> `doc/development/verification/2026-06-07-open-design-parity-matrix.md` and
> `doc/development/verification/2026-06-07-repair-stage-verification.md`.

## Current Goal

Record how retained Android UI screens are matched to Open Design screens for
structure, copy, state handling, and interaction.

## Authority

Open Design HTML files in `doc/development/open-design/` are authoritative for
retained screens only. Deleted feature pages and deleted screenshots are not
current UI requirements.

## Parity Rules

- Main screens must match their retained Open Design first-screen structure.
- Empty data must render empty states, not fake content.
- UI-only capability must be disabled or explicitly marked.
- Bottom navigation labels are `Community / Commerce / Messages / Profile`.
- No fake success, no empty clicks, and no local mock content.
- OSS-backed uploads and address management remain in the parity scope.

## Retained Page Groups

1. App shell and four main tabs.
2. Community, post detail, post creation, comments, and search boundary.
3. Commerce product browsing, cart, order confirmation, and address management.
4. Messages, conversations, notifications, and notification detail boundary.
5. Profile, profile edit, and settings pages.

## Removed From This Plan

Deleted feature surfaces named in the 2026-06-09 cleanup plan are no longer
parity targets.

## QA Evidence

For every retained page group, record:

- HTML reference file.
- Android screen name.
- Screenshot or device coverage status.
- Known visual differences.
- Blockers if not covered.

## Historical Completion Gate

Current parity claims must use the latest verification records and must not
depend on deleted feature screenshots or previous removed-page targets.

## Repair Stage Result

The 2026-06-07 real-backend smoke found follow-up gaps recorded in
`doc/development/verification/2026-06-07-real-backend-repair-log.md`.

`RB-001` through `RB-005` were later closed or reclassified with evidence in
`doc/development/verification/2026-06-07-repair-stage-verification.md`. The
latest parity matrix records retained repair-stage screenshots, real API detail
evidence, accepted device/mock-frame differences, and remaining unverified
device coverage.
