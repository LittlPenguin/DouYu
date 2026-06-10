# 过期迁移文档清理计划

Date: 2026-06-09

## Goal

清理 `AGENTS.md` 和 `doc/development` 中把 Android 客户端描述为“正在从
Kotlin/Jetpack Compose 迁移到 Java/XML”的过期当前态。当前文档应表达：

- Android 客户端现行技术栈是 Java + Activity/Fragment + XML。
- Kotlin/Jetpack Compose 是历史实现，不再作为当前任务阶段或未完成迁移来描述。
- 禁止重新引入 Kotlin/Compose 仍是有效工程约束。
- 历史 verification / plan 记录可以保留历史语境，但当前入口文档不能误导后续 Agent。

## Non-goals

- 不修改 Android、后端或 Open Design 运行时代码。
- 不重写历史验收记录中的历史事实。
- 不删除 Java/XML 技术栈、Open Design 权威、禁止 mock 数据等仍有效规则。
- 不运行真机或服务。

## Likely Files

- `AGENTS.md`
- `doc/development/README.md`
- `doc/development/00-project-handoff.md`
- `doc/development/current-status.md`
- `doc/development/01-tech-stack.md`
- `doc/development/02-architecture.md`
- `doc/development/03-android-client.md`
- `doc/development/10-testing-acceptance.md`
- `doc/development/12-feature-and-flow-map.md`
- `doc/development/13-ui-screen-blueprints.md`
- `doc/development/14-frontend-backend-collaboration.md`
- `doc/development/16-stage-development-roadmap.md`
- `doc/development/17-ui-parity-refactor-plan.md`
- `doc/development/18-unfinished-and-blockers.md`
- `doc/development/diagrams/README.md`

## Steps

1. 扫描 `AGENTS.md` 和 `doc` 中的 `migration`、`迁移`、`Kotlin`、`Compose`、
   `.kt`、`Java/XML rewrite` 等表述。
2. 将活跃入口文档从“迁移阶段”改为“当前 Java/XML 实现与维护规则”。
3. 对历史计划文档保留历史属性，但明确它们不是当前未完成任务。
4. 保留 residue scan 和禁止 Kotlin/Compose 的验收约束。
5. 用搜索验证不再存在“正在迁移/迁移未完成”的当前态描述，并运行
   `git diff --check`。

## Verification

```powershell
cd D:\Studio\SpellBean
rg -n "being migrated|being rewritten|当前迁移阶段|本轮迁移|Java/XML 迁移口径|迁移未完成|The migration is not complete|reflect the Java/XML migration|Java/XML rewrite must not change|Android source migration from Kotlin/Compose|defines the Java/XML migration rules" AGENTS.md doc/development -S -g "!doc/development/26-doc-stale-migration-cleanup-plan.md"
git diff --check
```
