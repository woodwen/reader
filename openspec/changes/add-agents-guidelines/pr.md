# M-3(docs): 新增 Agent 协作规范

背景:
- 当前仓库此前没有 `AGENTS.md`，后续 Agent 在已有 dirty worktree 中工作时缺少统一的项目级协作边界、验证规则和汇报口径。
- 本次已先通过 OpenSpec change `add-agents-guidelines` 完成规划，并保持不修改应用运行时代码。

方案概述:
- 初始化 OpenSpec root，用 `openspec/config.yaml` 固定 Reader 项目的技术栈、架构边界、OpenSpec 语言规则和验证要求。
- 新增根目录 `AGENTS.md`，作为整个仓库的 Agent 协作规范。
- 保持 OpenSpec change active，不执行 archive。

实现改动:
- 新增 `AGENTS.md`，覆盖项目概览、OpenSpec 工作流、修改边界、编码原则、Android 分层约定、验证规则和汇报规则。
- 新增 `openspec/changes/add-agents-guidelines/`，包含 proposal、design、tasks、delta spec 和本地 PR markdown。
- 更新 `tasks.md`，将 OpenSpec setup、`AGENTS.md` implementation 和 validation 任务标记完成。

测试计划(UT):
- `openspec validate add-agents-guidelines --strict`
- `openspec validate --all --strict`
- `git diff --check`

影响范围(建议手动测试范围):
- 仅影响仓库协作文档和 OpenSpec 规划 artifacts。
- 不影响 App 编译、运行、APK 输出、依赖版本或数据库 schema。
- 本次不需要手动设备验证；后续触碰 Kotlin、资源、DI、Room、Manifest 或启动路径时再按范围补充。

风险与后续:
- 当前工作区仍有与本次无关的 `app/**` 既有未提交改动，本次提交应只 stage `AGENTS.md` 和 `openspec/**`。
- OpenSpec change 暂不 archive；如需归档，需要后续明确执行归档流程。

OpenSpec Change: add-agents-guidelines
