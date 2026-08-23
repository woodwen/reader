## Context

Reader 当前是单模块 Android/Kotlin 项目，仓库已有较多未提交源码和测试改动。本次目标是新增项目级 `AGENTS.md`，并顺便初始化 OpenSpec，让后续需求可以通过 active change 管理。

当前约束：

- 仓库根目录此前没有 `AGENTS.md`。
- 仓库此前没有 `openspec/`。
- 当前工作区已有大量未提交改动，本 change 不能清理、回退或格式化无关文件。
- `AGENTS.md` 是协作文档，不应改变应用编译、运行或测试行为。

## Goals / Non-Goals

**Goals:**

- 用 OpenSpec 记录新增 `AGENTS.md` 的动机、验收要求和实施任务。
- 在 `openspec/config.yaml` 中固定 Reader 项目的默认协作规则。
- 后续实施时只新增根目录 `AGENTS.md`，不触碰现有源码改动。
- 让 `AGENTS.md` 明确覆盖思考方式、简洁优先、精准修改和目标驱动验证。

**Non-Goals:**

- 不在本 change 中重构代码、调整 Gradle、改 README 或补业务测试。
- 不补齐完整业务 baseline specs；本次只新增 `agent-guidelines` 能力。
- 不自动 archive OpenSpec change。
- 不提交本地 commit，不 push。

## Decisions

### Decision 1: 根目录 `AGENTS.md` 作用于整个仓库

`AGENTS.md` SHALL 放在仓库根目录，作为所有后续 Agent 在 Reader 项目内工作的默认规则。

备选方案是在 `app/` 下放置局部 `AGENTS.md`。该方案对 Android 模块更贴近，但无法覆盖 OpenSpec、README、Gradle 和项目级工作流，默认不采用。

### Decision 2: 只新增一个 `agent-guidelines` capability

本次 OpenSpec 初始化不做完整业务能力建模，只新增与 `AGENTS.md` 直接相关的 `agent-guidelines` capability。

备选方案是同时建立书城、书架、阅读、书源管理等 baseline specs。该方案覆盖更完整，但会显著扩大范围，且与“新增 AGENTS.md”目标不成比例。

### Decision 3: `AGENTS.md` 使用中文为主

`AGENTS.md` SHALL 使用中文描述规则，路径、命令、包名、类名和 OpenSpec 关键字保留英文。

备选方案是全英文文档。该方案对通用工具友好，但不符合本项目当前协作习惯。

### Decision 4: 验证按改动范围选择

文档实施后默认运行 `openspec validate add-agents-guidelines --strict` 和 `git diff --check`。由于 `AGENTS.md` 不影响运行时代码，默认不运行 Gradle 构建；如果实施过程中触碰 Kotlin、资源、DI、Room 或 Manifest，再按风险补充 `./gradlew testDebugUnitTest`、`./gradlew assembleDebug`、`./gradlew lintDebug` 或设备验证。

备选方案是每次文档变更都跑完整 Android 构建。该方案更重，但对纯文档变更收益有限。

## Risks / Trade-offs

- OpenSpec 初始化可能让后续任务误以为已有完整业务 specs -> 在 proposal 和 design 中明确本次只新增 `agent-guidelines`。
- `AGENTS.md` 规则写得过细会增加维护成本 -> 只记录项目级稳定规则，不写一次性实现细节。
- 当前 dirty worktree 可能混入无关改动 -> 后续实施和提交必须只 stage 明确相关路径。
- 文档验证无法证明 App 行为 -> 明确本次不声明任何运行时行为变化。

## Migration Plan

1. 初始化 `openspec/config.yaml`。
2. 创建 `openspec/changes/add-agents-guidelines/` 并补齐 proposal、design、tasks 和 `agent-guidelines` delta spec。
3. 运行 OpenSpec strict validation 和 whitespace 检查。
4. 用户确认后，实施阶段新增根目录 `AGENTS.md`。
5. 再次运行 `openspec validate add-agents-guidelines --strict` 和 `git diff --check`。
6. 仅在用户明确要求时进入完成提交或归档。

## Open Questions

- 无。默认建议是采用根目录 `AGENTS.md`、中文正文、最小 OpenSpec 初始化、只新增 `agent-guidelines` capability。
