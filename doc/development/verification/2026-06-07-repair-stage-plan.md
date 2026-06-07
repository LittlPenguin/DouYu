# 2026-06-07 Repair Stage Plan

> Status: Historical plan. The repair stage was later verified in
> `2026-06-07-repair-stage-verification.md`, and the retained screen evidence is
> under `android-java-xml-screenshots/repair-stage/`.

## Purpose

This plan closes the remaining parity gaps found during the real-backend,
real-device smoke captured in
`doc/development/verification/2026-06-07-real-backend-repair-log.md`.

The goal of this stage is not to add new product scope. The goal is to prove,
with fresh evidence, that the current Java/XML Android client and the cleaned
backend behavior match the active `doc/development` requirements and Open
Design references without relying on static filler.

## Hard Rules

- Update documentation before code.
- Keep the Android stack on Java + Activity/Fragment + XML.
- Keep the backend public API paths unchanged.
- Do not clear the user's persistent dev database volume.
- Treat Open Design HTML under `doc/development/open-design/` as the visual
  authority.
- Never use local fake lists or placeholder content to hide empty states.
- Separate real API-backed detail flows from visual-only boundary coverage.

## Closure Targets

The stage closes only when all of the following are proven again:

1. Community and Commerce are revalidated against an isolated clean backend and
   show the designed empty-state behavior when their lists are empty.
2. AI, Messages, and Profile render designed login or empty boundaries for
   unauthenticated access instead of raw technical error cards.
3. Detail flows use real returned IDs where the backend provides them, and any
   unavoidable UI-only boundary pages are labeled as such in the evidence.
4. The parity matrix is refreshed with the latest real-device screenshots and
   explicitly separates parity, UI-only boundary, and not-covered items.
5. The repair verification doc records command output summaries, screenshot
   paths, and backend shutdown evidence.

## Execution Order

1. Refresh docs and acceptance wording so the repair stage has a single source
   of truth.
2. Use the isolated `qa-empty` backend for clean empty-state checks.
3. Re-run the real-device smoke with real IDs for the flows that support them.
4. Capture a fresh screenshot set and contact sheet for repair-stage evidence.
5. Record every remaining deviation in the repair log instead of counting it as
   passed.
6. Stop Spring Boot, PostgreSQL, and Redis after verification and confirm the
   backend is no longer listening.

## Evidence Split

Repair-stage evidence must be split into three categories:

- `real-api` for pages opened with true backend-returned IDs.
- `boundary` for designed login, empty, disabled, or UI-only states.
- `not-covered` for pages or provider capabilities that the current backend
  contract does not expose yet.

This split is mandatory for detail screens so that layout smoke does not get
mistaken for end-to-end flow coverage.

## Working Assumption

Conversation and notification detail pages may require either a QA-only fixture
path or a clearly documented boundary-only fallback if no public create flow
exists. The chosen path must be documented before the stage is closed.
