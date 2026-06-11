# Profile Logout State Refresh Plan

Date: 2026-06-11

## Problem

After logging out from Settings and returning to the Profile tab, the existing
ProfileFragment can still show the previously loaded nickname, avatar, and
counts. Runtime profile data must not remain visible after the local session is
cleared.

## Root Cause Hypothesis

SettingsActivity clears SessionStore during logout, but the ProfileFragment
instance in MainActivity is resumed with its previous in-memory UI state.
ProfileFragment currently loads profile data during view creation and after
login/edit results, but does not refresh or reset itself from SessionStore in
onResume.

## Scope

- Android Java/XML only.
- No mock profile data.
- Keep the existing backend-driven profile and asset endpoints.
- When SessionStore has no access token, ProfileFragment must immediately bind
  the logged-out header and clear protected asset content.

## Acceptance Checks

- A regression test covers ProfileFragment session refresh on return.
- Returning to Profile after logout shows logged-out copy, zero counts, default
  avatar, and no previous asset cards.
- Existing profile login/edit refresh behavior remains intact.
