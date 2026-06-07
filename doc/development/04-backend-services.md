# Backend Services

## Current Backend Role

The backend keeps the existing `/api/v1` contract while Android is rewritten in
Java/XML.

## Runtime Seed Policy

Runtime seed/demo content must be removed in this stage:

- No startup demo posts.
- No startup demo products/SKUs.
- No startup demo topics or stickers.
- No system demo user used for feed or commerce filler.
- No committed `static/seed/**` images used as live content.
- No public `/seed/**` static exposure.

Tests that require data must create explicit fixtures in test code or test
resources.

## Providers Kept

Keep these as development/integration providers:

- SMS stub.
- OSS local/stub/aliyun provider switch.
- AI provider abstraction and stub routing.
- Payment stub/integration skeleton.

They are not static content filler and should remain labeled as non-production
unless replaced by real providers.

## Empty Database Behavior

An empty development database must not break APIs. List endpoints should return
empty pages/lists and enough metadata for Android to render empty states.

## Verification

Run:

```powershell
cd doyu-server
mvn test
```

Production source must not contain runtime seed initialization after this stage.
