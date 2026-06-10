# Stage Development Roadmap

> Status: Historical completed stage plan for the Java/XML implementation. Use
> `current-status.md`, `10-testing-acceptance.md`, and the latest files under
> `doc/development/verification/` for current gates and evidence.

## Historical Stage 9: Java/XML Android Implementation

This completed stage established the current Java + Activity/Fragment + XML
Android client architecture. It is retained as historical context, not as an
open implementation requirement.

The original Stage 9 text mentioned a fifth AI tab and payment skeletons. Those
items are historical only and were superseded by the 2026-06-09 cleanup scope:
current maintenance keeps four tabs, upload/OSS integration, address management,
and no payment runtime surface.

## Order

1. Documentation and rules.
2. Android build setup.
3. Java core layer.
4. Main shell and tab fragments.
5. Flow Activities.
6. Runtime mock/seed cleanup.
7. Integration and verification.

## Stage 9.1 Documentation

- Create `AGENTS.md`.
- Update development docs for Java/XML architecture.
- Record Open Design authority, seed deletion policy, and test gates.

## Stage 9.2 Android Build

- Remove Kotlin and Compose plugins/dependencies from Android production/test
  source support.
- Add traditional Java Android dependencies.
- Preserve API base URL and debug network configuration.

## Stage 9.3 Java Core

- Java POJO models.
- Retrofit + Gson API client.
- Java repositories using real backend APIs.
- SharedPreferences session persistence.
- Upload and OSS integration boundaries.

## Stage 9.4 Java/XML UI

- Main Activity and four retained Fragment tabs.
- Secondary Activities for core flows.
- XML layouts for every Open Design screen.
- RecyclerView adapters for feeds, products, messages, comments, assets.

## Stage 9.5 Static Data Removal

- Remove Android runtime mock data.
- Remove backend runtime seed/demo content.
- Move required test data into test fixtures.

## Stage 9.6 Verification

- No Android `.kt` files.
- No Android Compose/Kotlin source references.
- Android tests, build, and lint pass.
- Backend tests pass.
- Open Design parity recorded.
