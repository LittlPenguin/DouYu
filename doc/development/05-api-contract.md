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

## Profile (`/api/v1/users/me`)

`GET /api/v1/users/me` returns the current profile view. Real fields used by the
"我的" page: `nickname`, `avatarUrl` (file id, not a resolved URL), `bio`,
`ageGroup`, `level`, `isMinor`, `followingCount`, `followerCount`, and the
statistics `likedCount` (sum of `likeCount` over the user's listable posts) and
`postCount` (count of the user's listable posts). `likedCount`/`postCount` are
computed from real `PostEntity` data in `UserController.me()`; they are not
seeded or mocked. When a user has no posts, both are `0` (a valid empty state).

`PATCH /api/v1/users/me` accepts `nickname`, `bio`, and optional `avatarFileId`.
City/region and interest tags are UI-only and are not part of this contract.

The "我的" asset tabs map to real endpoints:

- 我的图纸 → `GET /api/v1/patterns/jobs` (the user's own AI pattern jobs).
- 点赞作品 → `GET /api/v1/users/me/liked-posts`.
- 收藏作品 → `GET /api/v1/users/me/favorite-posts` (there is no
  `/me/favorite-patterns` endpoint).

## Seed Removal Impact

After seed/demo removal, API tests must create data explicitly. Contract tests
must not depend on application startup inserting demo content.
