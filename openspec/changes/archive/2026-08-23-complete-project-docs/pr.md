# M-6(docs): 补齐项目文档与变更日志

OpenSpec Change: complete-project-docs

背景:
- 根目录 `README.md` 内容偏早期，缺少当前单模块 Android/Kotlin 项目结构、本地环境、构建测试入口、OpenSpec 工作流和维护边界说明。
- 仓库此前没有 `CHANGELOG.md`，近期文档、书源管理和书架搜索相关变更缺少统一记录位置。
- 本次按 OpenSpec change `complete-project-docs` 完成规划、实施和归档，范围限定为文档与 OpenSpec artifacts。

方案概述:
- 更新现有 `README.md`，保留原有项目背景、参考项目、截图外链和 APK 外链，同时用当前仓库事实校准技术栈、目录结构和验证说明。
- 新增 `CHANGELOG.md`，记录 `Unreleased` 和可确认的 `1.0.2` 信息，不追溯不可证历史、不编造发布日期或发布内容。
- 归档 `complete-project-docs`，新增 `project-documentation` baseline spec。

实现改动:
- `README.md` 补齐项目概览、当前功能、技术栈、本地环境、常用命令、OpenSpec 工作流、APK 下载、截图、参考项目、历史说明和免责声明。
- `CHANGELOG.md` 新增 `Unreleased` 分类条目，覆盖文档、书源管理、书架远程搜索展示和兼容性修复等可确认近期变更。
- `openspec/specs/project-documentation/spec.md` 定义 README/CHANGELOG 的可信来源、结构要求和文档变更验证边界。
- `openspec/changes/archive/2026-08-23-complete-project-docs/` 保留本次 proposal、design、tasks、delta spec 和 PR markdown。

测试计划(UT):
- `openspec validate complete-project-docs --strict`（归档前）：通过。
- `openspec validate --all --strict`（归档前）：通过。
- `git diff --check`（归档前）：通过。
- `openspec archive complete-project-docs --yes`：完成，生成 `2026-08-23-complete-project-docs` archive。
- `openspec validate --all --strict`（归档后）：通过。
- `git diff --check`（归档后）：通过。
- 未运行 Gradle、lint 或设备检查：本次只修改根目录文档和 OpenSpec artifacts，未触碰 Android 源码、资源、Gradle、Room schema、Manifest 或启动路径。

影响范围(建议手动测试范围):
- 影响范围为项目文档、变更日志和 OpenSpec 文档规范。
- 不影响 App 编译产物、运行时行为、依赖版本、数据库 schema 或设备交互。
- 无需手动设备验证；后续若修改 Android 源码、资源或启动路径，再按风险补充构建、lint、单元测试或真机验证。

风险与后续:
- README 中的 APK 和部分截图保留为原 README 远程外链；本次未验证其最新性或远程可用性，因此文档使用保守表述。
- CHANGELOG 只记录当前仓库可确认内容；更早版本历史未重建。
