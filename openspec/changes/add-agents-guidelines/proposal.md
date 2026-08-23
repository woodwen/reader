## Why

当前仓库没有 `AGENTS.md`，后续 Agent 在已有未提交改动的 Android 项目中工作时，缺少统一的项目级协作边界、验证规则和汇报口径。先引入 OpenSpec 并用一个 active change 规划该文档，可以让协作规范本身可审阅、可验证，并避免直接修改实现代码。

## What Changes

- 初始化 OpenSpec root，用 `openspec/config.yaml` 记录 Reader 项目的技术栈、架构边界、语言规则和验证要求。
- 新增 `add-agents-guidelines` OpenSpec change，规划根目录 `AGENTS.md` 的内容和验收标准。
- 后续实施时新增根目录 `AGENTS.md`，作为整个仓库的 Agent 协作规范。
- 本 change 不修改应用源码、Gradle 配置、测试代码或 README。
- 本 change 不提交、不 push、不 archive，除非用户后续明确要求。

## Capabilities

### New Capabilities

- `agent-guidelines`：定义本仓库的 Agent 协作规范，包括项目概览、修改边界、Android 分层约定、验证选择和结果汇报要求。

### Modified Capabilities

- 无。当前仓库此前没有 OpenSpec baseline specs。

## Impact

- `openspec/config.yaml`：新增 OpenSpec 项目上下文和 artifact 规则。
- `openspec/changes/add-agents-guidelines/**`：记录本次文档新增的 proposal、design、tasks 和 delta spec。
- `AGENTS.md`：后续实施阶段新增；当前方案阶段不创建该文件。
- 应用运行时行为、APK 输出、依赖版本和数据库 schema 不受影响。
