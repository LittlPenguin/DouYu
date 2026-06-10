# 27. Remove AI, Map API, And Payment Surfaces

## Goal

Remove AI page, map API, payment, and selected production-only capability
surfaces from the current SpellBean workspace: runtime features, descriptions,
documentation, design artifacts, and requirement statements.

## Non-goals

- Do not remove unrelated community, commerce browsing, messages, profile,
  authentication, upload, OSS-backed object image storage, address management,
  or moderation flows.
- Do not remove ordinary code uses of `map`, `Map`, or endpoint fields when they
  are generic data mapping, Java collections, OpenAPI endpoint wording, OSS
  endpoint configuration, or unrelated diagrams.
- Do not introduce mock data or replacement placeholder screens.
- Do not delete the plan document itself or other workflow records created in
  this cleanup round.

## Target Scope

Android:

- Remove the AI main tab, quick AI entry, AI home fragment, AI flow activity,
  camera activity, AI formatting helpers, AI-specific layouts, AI drawables, and
  AI unit/instrumentation tests.
- Remove Retrofit/repository/model methods that only serve AI pattern job,
  quota, pattern detail, or favorite-pattern flows.
- Remove payment boundary page/activity/layout and Android API/test references.
- Remove map API / location UI-only copy from settings and profile edit screens.
- Remove references to real SMS Provider, production rate limiting, production
  risk control, and any production-only AI, payment, or map wording from
  client-facing copy.

Backend:

- Remove AI pattern generation controllers, provider classes, usage controls,
  AI job endpoints, AI usage/pattern job tests where they only validate removed
  features.
- Remove payment controller, callback verifier, payment entity/repository,
  payment endpoint tests, and runtime descriptions.
- Keep OSS object image storage and the existing upload provider boundary.
  Do not remove OSS configuration, OSS documentation, upload presign/confirm
  APIs, Aliyun OSS template values, or OSS-related requirements.
- Remove real SMS Provider, production limit / risk-control plumbing, real
  payment SDK/API / refund / reconciliation / production callback wiring, real
  AI vision / LLM provider wiring, content safety, cost controls, and
  map/location provider wiring if it exists.
- Remove or neutralize database schema references that only support removed AI
  job/payment runtime tables when feasible without rewriting historical Flyway
  migrations in a way that breaks migration ordering.

Documentation and design:

- Remove AI-specific development docs, provider-selection docs, payment docs,
  compliance docs, map/location docs, AI/map/payment future capability Open
  Design files, AI wireframes, and AI / map API / payment / production-provider
  requirement statements.
- Update architecture, API contract, data model, Android, testing, status,
  feature-flow, screen blueprint, collaboration, roadmap, unfinished-blocker,
  Open Design index, diagram README, and top-level plan references so they no
  longer list these removed capabilities.
- Remove retained screenshot evidence paths that are specifically AI or payment
  page evidence when they are part of the project artifacts.

## Implementation Steps

1. Add static removal contract tests first and verify they fail on the current
   codebase.
2. Delete Android AI/payment/map/production-only entry points and disconnect
   navigation.
3. Delete Android AI/payment/map/production-only Retrofit, repository, models,
   resources, and tests; adjust remaining profile/commerce flows to avoid
   removed references.
4. Delete backend AI/payment/map/production-only runtime modules and adjust
   schema/tests/contracts.
5. Delete or rewrite docs, Open Design pages, diagrams, and requirements.
6. Run targeted source searches for AI, map API/location, payment, real SMS,
   production risk-control, real AI provider, and compliance terms.
7. Run Android unit/build checks and backend tests that prove removed contracts
   do not remain.

## Verification Commands

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
```

```powershell
cd D:\Studio\SpellBean\doyu-server
mvn test
```

```powershell
cd D:\Studio\SpellBean
rg -n -i "ai|人工智能|智能生成|大模型|地图 API|地图API|真实地图|定位服务|位置能力|支付|payment|wechat|alipay|支付宝|微信支付|短信服务|SMS Provider|限流|风控|合规|隐私政策|用户协议|备案|投诉机制" --glob "!**/.git/**" --glob "!**/build/**" --glob "!**/.gradle/**"
```
