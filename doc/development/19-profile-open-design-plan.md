# 19 Profile Open Design Plan

> Status: Historical implementation plan, normalized after the 2026-06-09
> removed-feature cleanup. Current profile scope keeps profile, profile edit,
> liked posts, favorite posts, and OSS-backed avatar upload. Removed feature
> pages and deleted endpoints are not current requirements.

## Scope

- `feature/profile/ProfileFragment.java` and
  `res/layout/fragment_profile_home.xml`.
- `feature/profile/ProfileEditActivity.java` and
  `res/layout/activity_profile_edit.xml`.
- `model/UserProfile.java`, `model/UpdateProfileRequest.java`, upload request
  and response models.
- `network/DoyuApi.java` and `data/DoyuRepository.java` methods for retained
  profile and upload APIs.
- Backend `server/user/UserController.java` profile statistics and profile
  update behavior.

The bottom navigation, top bar, quick menu, messages, community, commerce, and
settings shell are shared app concerns and are not owned by this historical
profile plan.

## Current Data Mapping

| Area | API | Current behavior |
| --- | --- | --- |
| Profile summary | `GET /api/v1/users/me` | Real nickname, resolved avatar URL, bio, region, stats, age group, and follow counts. |
| Liked posts | `GET /api/v1/users/me/liked-posts` | Real paged `Post` list. |
| Favorite posts | `GET /api/v1/users/me/favorite-posts` | Real paged `Post` list. |
| Avatar upload | `POST /uploads/presign -> PUT uploadUrl -> POST /uploads/confirm -> PATCH /me {avatarFileId}` | Uses the retained OSS-backed upload flow. Backend resolves `avatarFileId` into `avatarUrl` for display. Android never stores OSS credentials. |

## Profile Page Structure

1. Profile hero: avatar, nickname, bio, account state, and edit profile entry.
2. Stat grid: likes, posts, following, and fans in a stable order.
3. Asset tabs: liked posts and favorite posts.
4. Content grid/list: one real backend list per tab.
5. States: loading, content, empty, login prompt, and retryable error.

No local mock cards are used. Empty backend results are valid and render empty
states.

## Profile Edit Structure

1. Avatar picker uses the retained OSS upload flow.
2. Nickname validates non-empty text before save.
3. Bio is saved through the profile update API.
4. City/region is a real manual profile field. Android provides common choices
   and free text, then saves the selected value through the profile update API.
5. Age group and interest chips are not shown in profile edit.

## Removed From This Plan

This plan no longer contains or depends on deleted feature surfaces named in the
2026-06-09 cleanup plan.

## Verification Expectations

- Backend profile and upload contract tests pass.
- Android unit/build checks pass.
- `DouYu/app/src` keeps Java/XML only.
- Missing real-device coverage is recorded as not covered, not as passed.
