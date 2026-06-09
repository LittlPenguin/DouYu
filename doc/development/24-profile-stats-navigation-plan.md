# Profile Stats Navigation Plan

## Goal

Complete the Profile stat interactions from `profile-a.html`:

- `获赞` opens a clear source explanation dialog.
- `作品` opens the current user's real community posts.
- `关注` opens the real users followed by the current user.
- `粉丝` opens the real users who follow the current user.

This is the `multi-agent-dev-loop` first-pass execution plan for the feature.

## Non-Goals

- Do not add a public user profile page.
- Do not add comment-like capability.
- Do not change the login/session model.
- Do not hardcode local sample users, posts, or fake counts.
- Do not change Profile Edit backend fields beyond the existing nickname, bio,
  and avatar upload/save flow.

## Backend Contract

Add three authenticated read-only endpoints under `/api/v1/users`:

- `GET /me/posts`: current user's listable community posts.
- `GET /me/following`: users the current user follows.
- `GET /me/followers`: users who follow the current user.

The endpoints keep the existing `ApiResponse<PageResult<...>>` response shape.
Posts reuse `CommunityController.postView(...)`; users reuse
`AuthService.userView(...)`. Empty results are valid empty pages. Unauthenticated
requests return `401`.

`likedCount` remains the real sum of `likeCount` over the current user's
listable posts. The likes dialog copy must describe this exact source.

## Android Scope

- Update `ProfileFragment` so the four stat cells are clickable.
- Add `ProfilePostsActivity` for `/me/posts`, clicking post cards into
  `PostDetailActivity`.
- Add `ProfileUsersActivity` for following/followers, selected by an intent
  extra. User rows are display-only in this round.
- Add the needed Java adapters, XML layouts, `DoyuApi` and `DoyuRepository`
  methods, and Manifest entries.
- Keep `ProfileEditActivity` on the existing real profile load, avatar upload,
  nickname validation, and profile save flow.

## Open Design

- Update `profile-a.html` so `获赞` shows a dialog design and `作品/关注/粉丝`
  link to dedicated pages.
- Add:
  - `profile-posts-a.html`
  - `profile-following-a.html`
  - `profile-followers-a.html`

These design pages must show runtime state boundaries and must not promise local
demo data.

## Verification

- Add Android source/resource constraint tests before implementation and confirm
  they fail against the current implementation.
- Add backend contract tests before implementation and confirm they fail against
  the missing endpoints.
- Run:
  - `cd D:\Studio\SpellBean\doyu-server && mvn test`
  - `cd D:\Studio\SpellBean\DouYu && .\gradlew.bat :app:testDebugUnitTest --console=plain`
  - `cd D:\Studio\SpellBean\DouYu && .\gradlew.bat :app:compileDebugJavaWithJavac --console=plain`
  - `cd D:\Studio\SpellBean && git diff --check`
- If an Android device is attached, capture evidence for profile home, edit,
  posts, following, followers, and likes dialog. If no device is attached,
  record visual verification as not covered.
