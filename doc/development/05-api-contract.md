# API Contract

## Contract Stability

The Android Java/XML rewrite must not change public backend paths. API prefix
remains `/api/v1`.

## Response Shape

Backend responses continue to use:

```json
{
  "code": "OK",
  "message": "success",
  "data": {},
  "traceId": "..."
}
```

Paged data continues to return items and page metadata.

## Empty Result Policy

Empty lists are valid. Android must render empty states and must not fill empty
responses with local mock data.

## Java DTO Policy

Android DTOs are Java POJOs parsed with Gson. Field names must match backend
JSON. Existing semantic fields such as IDs, statuses, counts, URLs, and nested
items must remain compatible.

## Stub Provider Policy

The following are explicit development boundaries:

- SMS stub verification.
- OSS local/stub URLs.
- AI stub/provider routing.
- Payment stub parameters.

Do not document these as production capabilities.

## Seed Removal Impact

After seed/demo removal, API tests must create data explicitly. Contract tests
must not depend on application startup inserting demo content.
