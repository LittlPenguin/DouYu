# Unfinished Items And Blockers

## Current Uncovered Items

- Repair-stage verification is recorded in
  `doc/development/verification/2026-06-07-repair-stage-verification.md`.
- The active repair-stage execution plan is
  `doc/development/verification/2026-06-07-repair-stage-plan.md`.
- Production provider integrations remain outside this rewrite scope: real SMS,
  real payment SDK/callbacks, production object storage hardening, map/location
  provider, production AI provider, and complete compliance documents.

## No Longer Blocked

- Android source migration from Kotlin/Compose to Java/XML is no longer a known
  blocker under `DouYu/app/src`.
- Android Kotlin/Compose dependency residue is no longer a known blocker in
  `DouYu/app`, `DouYu/build.gradle.kts`, or `DouYu/gradle/libs.versions.toml`.
- Backend runtime seed/demo content is no longer a known blocker.
- Static seed resources and `/seed/**` public exposure are no longer present.
- Clean-backend API verification is no longer a blocker for the repair stage:
  `qa-empty` returned empty lists for feed, products, topics, and sticker packs.
- Android 401 mapping is no longer a local-code blocker: unit tests cover 401 to
  login-boundary state.
- Repair-stage real-device UI parity is no longer blocked:
  `VisualSmokeInstrumentedTest` and `RealBackendSmokeInstrumentedTest` passed on
  `10.64.241.158:40739`, and screenshots were pulled to the repair-stage
  evidence directory.

## Repair Stage Blockers

- `RB-001`: Closed. `qa-empty` feed returned an empty list and
  `main_community.png` records the real-device empty-state boundary.
- `RB-002`: Closed. `qa-empty` products returned an empty list and
  `main_commerce.png` records the real-device empty-state boundary.
- `RB-003`: Clean-backend topic and sticker-pack behavior is closed on
  `qa-empty`. Dev persistent rows remain diagnostic-only and must not be deleted
  automatically.
- `RB-004`: Closed. Unit tests cover 401 mapping and the repair-stage AI,
  Messages, and Profile screenshots do not show raw `HTTP 401`.
- `RB-005`: Closed for supported real-ID flows. Real-device smoke captured
  backend-returned post, product, AI job/pattern, conversation, and notification
  detail screenshots.
- Current active blockers are limited to future production integrations outside
  this Java/XML repair stage.

## Out Of Scope For This Rewrite

These remain future work unless separate requirements are added:

- Real SMS provider, production rate limits, and fraud controls.
- Real payment channel SDKs, refunds, reconciliation, and production callbacks.
- Real AI vision provider or large-model generation quality guarantee.
- Production object storage security, CDN, and signed delivery.
- Full address management.
- Map/location provider.
- Production privacy policy, user agreement, SDK list, filing, and copyright
  complaint documents.
- Complete player marketplace transaction lifecycle.

## Static Data Follow-Up

Tests and local QA must continue to use explicit test fixtures. Runtime startup
must not restore content seeders. App screens must render empty states instead
of adding local fake data when APIs return empty lists.

The clean-backend verification environment must be non-destructive. It may use a
dedicated Docker Compose project, dedicated ports, or a dedicated profile, but
it must not clear `douyu_postgres_data` or any existing developer data volume.

## Verification Risks

- Passing unit tests alone do not prove Open Design parity; use the 2026-06-07
  parity matrix and real-device screenshots for UI evidence.
- Screenshots from old Kotlin/Compose builds do not prove Java/XML parity; only
  current Java/XML real-device evidence is accepted.
- Empty search results are valid only if the empty state is visible and clear.
- Stub provider behavior must be labeled as development integration, not
  production capability.
