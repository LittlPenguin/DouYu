# Stage Development Roadmap

> Status: Historical stage plan for the Java/XML rewrite. Use
> `current-status.md`, `10-testing-acceptance.md`, and the latest files under
> `doc/development/verification/` for current gates and evidence.

## Stage 9: Java/XML Android Rewrite

This stage replaces the Kotlin + Compose Android client with Java +
Activity/Fragment + XML.

## Order

1. Documentation and rules.
2. Android build migration.
3. Java core layer.
4. Main shell and tab fragments.
5. Flow Activities.
6. Runtime mock/seed cleanup.
7. Integration and verification.

## Stage 9.1 Documentation

- Create `AGENTS.md`.
- Rewrite development docs for Java/XML migration.
- Record Open Design authority, seed deletion policy, and test gates.

## Stage 9.2 Android Build

- Remove Kotlin and Compose plugins/dependencies.
- Add traditional Java Android dependencies.
- Preserve API base URL and debug network configuration.

## Stage 9.3 Java Core

- Java POJO models.
- Retrofit + Gson API client.
- Java repositories using real backend APIs.
- SharedPreferences session persistence.
- Upload and payment integration skeletons.

## Stage 9.4 Java/XML UI

- Main Activity and five Fragment tabs.
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
