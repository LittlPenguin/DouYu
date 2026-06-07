# Frontend Backend Collaboration

## Android Client Boundary

The Android client is being rewritten in Java/XML. It must keep using the
existing backend API under `/api/v1`.

## API Use

- Retrofit + Gson is the Android client default.
- DTO field names must match backend JSON.
- Empty lists are valid API responses.
- Java repositories must not substitute mock data when backend data is empty.

## Provider Boundaries

The following Stub or development providers remain valid:

- SMS stub code.
- OSS local/stub provider.
- AI stub/provider routing.
- Payment stub/integration skeleton.

These providers must be described as development integration boundaries. They
must not be presented as production capabilities.

## Seed/Demo Boundary

Runtime seed/demo content is being removed from backend startup. Local QA must
create explicit test fixtures instead of relying on application startup data.

## Android Empty State Contract

When backend returns empty lists or missing optional content, Android must render
Open Design empty states. It must not synthesize demo posts, products, messages,
orders, payments, patterns, badges, or assets.
