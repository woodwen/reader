## 1. OpenSpec Setup

- [x] 1.1 初始化 `openspec/config.yaml`，写入 Reader 项目上下文、协作规则和验证规则。
- [x] 1.2 创建 active change `add-agents-guidelines`。
- [x] 1.3 补齐 `proposal.md`、`design.md`、`tasks.md` 和 `specs/agent-guidelines/spec.md`。

## 2. AGENTS.md Implementation

- [x] 2.1 新增根目录 `AGENTS.md`，作为整个仓库的 Agent 协作规范。
- [x] 2.2 在 `AGENTS.md` 中覆盖项目概览、修改边界、编码原则、Android 分层约定、验证规则和结果汇报规则。
- [x] 2.3 确认实施阶段不修改应用源码、Gradle 配置、测试代码或 README。

## 3. Validation

- [x] 3.1 运行 `openspec validate add-agents-guidelines --strict`。
- [x] 3.2 运行 `git diff --check`。
- [x] 3.3 汇报变更路径、validation 结果、未运行的 Gradle/设备检查及原因。
