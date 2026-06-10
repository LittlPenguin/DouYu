# 过期迁移文档清理验收记录

Date: 2026-06-09

## Scope

本轮按 `multi-agent-dev-loop` 执行 Agent / First Pass 清理文档中过期的
“Android 正在从 Kotlin/Compose 迁移到 Java/XML”当前态描述。

本轮只改 `AGENTS.md` 和 `doc/development` 下文档，不修改 Android、后端、
Open Design 运行时代码，也不运行真机或启动服务。

## Updated

- `AGENTS.md`：从 `Current Task Contract / migration` 改为
  `Current Engineering Contract / Quality Gates`，保留 Java/XML、禁止
  Kotlin/Compose、Open Design、真实数据和验证证据规则。
  `AGENTS.md` is ignored by `.gitignore`, so it does not appear in ordinary
  `git diff` output, but the local workspace file was updated.
- `doc/development/README.md`、`00-project-handoff.md`、`current-status.md`：
  改为描述当前 Java/XML 架构与工程事实，不再描述当前仍处于迁移阶段。
- `01-tech-stack.md`、`02-architecture.md`、`03-android-client.md`、
  `05-api-contract.md`、`06-data-model.md`、`10-testing-acceptance.md`、
  `12-feature-and-flow-map.md`、`13-ui-screen-blueprints.md`、
  `14-frontend-backend-collaboration.md`、`18-unfinished-and-blockers.md`、
  `diagrams/README.md`：删除或改写 `rewrite`、`migration`、`being
  rewritten` 等当前态表述，保留当前 Java/XML 和禁止 Kotlin/Compose 的工程门禁。
- `16-stage-development-roadmap.md`、`17-ui-parity-refactor-plan.md`：
  明确是历史已完成计划，不作为当前未完成迁移需求。
- `26-doc-stale-migration-cleanup-plan.md`：记录本轮目标、非目标、文件范围和验证命令。

## Verification

Command:

```powershell
cd D:\Studio\SpellBean
rg -n "being migrated|being rewritten|Rewrite the Android app|current migration|当前迁移|本轮迁移|迁移规则|迁移口径|The migration is not complete|reflect the Java/XML migration|Java/XML rewrite must not change|Android source migration from Kotlin/Compose|defines the Java/XML migration rules|source migration from Kotlin/Compose|Java/XML migration rules" AGENTS.md doc/development -S -g "!doc/development/26-doc-stale-migration-cleanup-plan.md" -g "!doc/development/verification/**"
```

Result:

```text
No matches in active docs.
```

Assessment:

- Active rules, handoff, architecture, client, status, testing, roadmap, and
  diagram index documents no longer describe the Android client as currently
  migrating from Kotlin/Compose to Java/XML.
- Historical verification records are preserved as evidence and were not rewritten.

Command:

```powershell
rg -n "迁移|migrat|rewrite" AGENTS.md doc/development/README.md doc/development/00-project-handoff.md doc/development/01-tech-stack.md doc/development/02-architecture.md doc/development/03-android-client.md doc/development/05-api-contract.md doc/development/06-data-model.md doc/development/10-testing-acceptance.md doc/development/12-feature-and-flow-map.md doc/development/13-ui-screen-blueprints.md doc/development/14-frontend-backend-collaboration.md doc/development/16-stage-development-roadmap.md doc/development/17-ui-parity-refactor-plan.md doc/development/18-unfinished-and-blockers.md doc/development/diagrams/README.md -S
```

Result:

```text
doc\development\01-tech-stack.md:56:| 数据库迁移    | Flyway                                              |
doc\development\10-testing-acceptance.md:160:not a current unfinished migration task.
```

Assessment:

- `数据库迁移` refers to Flyway database migration and is still correct.
- `not a current unfinished migration task` is a historical clarification, not
  an active migration requirement.

Command:

```powershell
git diff --check
```

Result:

```text
No whitespace errors. Git reported line-ending normalization warnings for
existing documentation/Open Design files.
```

## Not Run

- No Android build/test was run because no Android source or resource files were modified.
- No backend tests were run because no backend source, configuration, or migration file was modified.
- No device or screenshot verification was run.
