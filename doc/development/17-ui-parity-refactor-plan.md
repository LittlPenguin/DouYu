# Java/XML UI Parity Plan

## Goal

Rebuild Android UI in Java/XML and match Open Design screens 1:1 for structure,
copy, state handling, and interaction.

## Authority

Open Design HTML files in `doc/development/open-design/` are authoritative.
Existing Kotlin/Compose UI is implementation history only.

## Parity Rules

- Main screens must match their Open Design first-screen structure.
- Flow screens must match the referenced Open Design page.
- Empty data must render empty states, not fake content.
- UI-only capability must be disabled or explicitly marked.
- Bottom navigation labels are `社区 / 商城 / AI / 消息 / 我的`.
- No emoji icons, no fake success, no empty clicks.

## Page Groups

1. App shell and main tabs.
2. Community and post flows.
3. AI image and pattern flow.
4. Commerce and payment boundary flow.
5. Messages and notification flow.
6. Profile, profile edit, and Settings.

## QA Evidence

For every page group, record:

- HTML reference file.
- Android screen name.
- Screenshot or device coverage status.
- Known visual differences.
- Blockers if not covered.

## Completion Gate

This plan is complete only when Java/XML screens have replaced Compose screens
and the parity table in `10-testing-acceptance.md` is updated with evidence or
explicit non-coverage.

## Repair Stage Gate

The 2026-06-07 real-backend smoke found follow-up gaps recorded in
`doc/development/verification/2026-06-07-real-backend-repair-log.md`.
The repair-stage execution plan is documented in
`doc/development/verification/2026-06-07-repair-stage-plan.md`.
The next parity pass is not complete until these are addressed or explicitly
reclassified with evidence:

- `RB-001`: Community must be verified against a clean real backend and show the
  designed empty state when feed data is empty.
- `RB-002`: Commerce must be verified against a clean real backend and show the
  designed empty state when products are empty.
- `RB-003`: Topics and sticker packs must be verified as either legitimate QA
  fixtures or old persistent seed residue; clean-backend parity must not depend
  on them.
- `RB-004`: AI, Messages, and Profile must show login or empty-state boundaries
  for HTTP 401 instead of raw technical error cards.
- `RB-005`: Detail-flow screenshots must distinguish real API-backed IDs from
  visual-only test extras.

Repair-stage evidence must include a fresh real-device screenshot set, a contact
sheet, API summaries from the clean backend, and shutdown evidence for the
backend and dependency services. Any unavoidable UI-only detail boundary must be
called out as `boundary`, not counted as `real-api`.
