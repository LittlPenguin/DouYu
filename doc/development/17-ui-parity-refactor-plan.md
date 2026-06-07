# Java/XML UI Parity Plan

> Status: Historical plan for the Java/XML parity pass. The latest retained
> parity evidence is recorded in
> `doc/development/verification/2026-06-07-open-design-parity-matrix.md` and
> `doc/development/verification/2026-06-07-repair-stage-verification.md`.

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

## Repair Stage Result

The 2026-06-07 real-backend smoke found follow-up gaps recorded in
`doc/development/verification/2026-06-07-real-backend-repair-log.md`.
The repair-stage execution plan is preserved as historical context in
`doc/development/verification/2026-06-07-repair-stage-plan.md`.

`RB-001` through `RB-005` were later closed or reclassified with evidence in
`doc/development/verification/2026-06-07-repair-stage-verification.md`. The
latest parity matrix records the retained repair-stage screenshots, real API
detail evidence, accepted device/mock-frame differences, and remaining
production-provider exclusions.
