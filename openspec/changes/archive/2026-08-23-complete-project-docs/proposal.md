## Why

当前仓库根目录已有 `README.md`，但内容偏早期，缺少清晰的本地构建入口、当前单模块 Android/Kotlin 项目结构、验证命令、维护边界和与现有书源能力匹配的说明。根目录未发现 `CHANGELOG.md`，后续用户和维护者难以判断最近变更、待发布内容和历史版本依据。

本 change 先规划项目文档补齐，不修改实现代码。目标是在后续实施阶段补齐可维护、基于仓库事实的 README 和 CHANGELOG，让新参与者可以快速理解项目、运行基本验证，并追踪版本变更。

## What Changes

- 更新根目录 `README.md`，保留已有项目说明、参考项目、截图和下载信息中仍有价值的内容，并补齐当前项目概览、功能范围、目录结构、环境要求、构建/测试命令、书源能力、常见问题和维护说明。
- README 中保留现有 APK 下载链接时 SHALL 使用保守表述，例如“现有 APK 下载链接”，不得在未验证最新性的情况下继续称为“最新应用下载地址”。
- 新增根目录 `CHANGELOG.md`，记录 `Unreleased` 变更和可确认的历史版本信息；无法确认日期或内容的历史版本不追溯补写。
- README 和 CHANGELOG SHALL 基于仓库文件、已归档/active OpenSpec change、可见 APK/截图和本地命令结果，不编造性能指标、下载量、发布时间或无法确认的发布历史。
- 文档变更不调整 Android 源码、Gradle 配置、Room schema、资源文件或测试代码。
- 本 change 不提交、不 push、不 archive，除非用户后续明确要求。

## Capabilities

### New Capabilities

- `project-documentation`：定义 Reader 仓库根目录 README 和 CHANGELOG 的内容边界、可信来源、结构要求和验证要求。

### Modified Capabilities

- 无。当前仓库没有项目文档相关 baseline spec。

## Impact

- `openspec/changes/complete-project-docs/**`：记录本次方案、设计、任务和 delta spec。
- 后续实施预计涉及：
  - `README.md`
  - `CHANGELOG.md`
- 当前方案阶段不修改 `README.md`、不新增 `CHANGELOG.md`，也不修改 Android 源码、Gradle 配置、测试代码或 OpenSpec baseline specs。后续实施默认也只触碰 `README.md` 和 `CHANGELOG.md`。
