# AGENTS.md

本文件约束 Agent 在本仓库内的默认工作方式。适用范围是整个 `reader` 项目。

## 项目概览

- 这是单模块 Android/Kotlin 免费小说阅读器，主模块为 `:app`。
- Gradle 入口是 `./gradlew`，项目结构保持单模块，不要主动拆模块。
- 主要技术栈包括 Kotlin、AndroidX、Coroutines、Flow、ViewModel、LiveData、Room、Hilt、Retrofit、OkHttp、Jsoup、Moshi。
- 单元测试位于 `app/src/test`，设备测试位于 `app/src/androidTest`。

## OpenSpec 工作流

- 用户要求“先出方案”“调整方案”“实施方案”“review”“完成提交”“归档”时，优先按 OpenSpec change 工作流处理。
- 新用户可见行为、架构约束或工作流规则变更默认先创建或更新 `openspec/changes/<change-id>/`。
- OpenSpec artifact 正文默认使用简体中文；`ADDED Requirements`、`MODIFIED Requirements`、`Requirement`、`Scenario`、`WHEN`、`THEN` 等结构关键字保持英文。
- 未经用户明确要求，不提交、不 push、不 archive。

## 修改边界

- 编码前先读现状和相关 OpenSpec artifacts，不要假设。
- 有歧义时说明假设、权衡和默认建议；无法安全判断时先询问。
- 只修改当前任务必须修改的文件，不做无关重构、格式化或清理。
- 工作区已有未提交改动时，默认视为用户改动；不要回退、覆盖、整理、stage 或删除无关文件。
- 如果本次改动产生了无用导入、变量或函数，只清理本次引入的无用内容。

## 编码原则

- 简洁优先，用最少代码解决当前问题，不为未要求的扩展创建抽象。
- 匹配现有代码风格，即使存在个人偏好差异。
- 注释只用于解释非显而易见的约束或复杂逻辑，避免重复代码字面含义。
- 修改必须能追溯到用户请求；发现无关问题可以汇报，但不要顺手修改。

## Android 分层约定

- UI 层位于 `app/src/main/java/com/woodnoisu/reader/ui/**`，负责渲染状态、收集用户输入和展示错误。
- ViewModel 负责状态持有、流程编排和用户意图转换。
- Repository 负责数据读取、缓存、网络/数据库协调和来源切换。
- Room DAO 与数据库定义位于 `app/src/main/java/com/woodnoisu/reader/persistence/**`。
- 网络请求、HTML 解析和规则解析位于 `app/src/main/java/com/woodnoisu/reader/network/**`。
- 书源管理相关模型、仓库和解析逻辑应保持边界清晰，避免 UI 直接耦合网络、数据库或解析细节。

## 验证规则

- OpenSpec change 更新后运行 `openspec validate <change-id> --strict`。
- OpenSpec root 或多 change 状态需要确认时运行 `openspec validate --all --strict`。
- 文档或规范变更至少运行 `git diff --check`。
- Kotlin/Android 单元行为变更优先运行 `./gradlew testDebugUnitTest`。
- UI、资源、Manifest、DI、Room schema 或启动路径变更按风险补充 `./gradlew assembleDebug`、`./gradlew lintDebug` 或设备验证。
- 设备验证必须区分 build、install、cold start、日志观察和真实交互；安装或启动成功不等于完整功能验证。

## 汇报规则

- 汇报时区分已验证事实、未验证推断和未运行检查。
- 如果没有运行 Gradle、lint 或设备检查，说明原因。
- 涉及设备时，明确说明设备型号或序列、执行命令、观察到的日志或交互证据。
- 对当前任务之外的 dirty worktree 改动只做提示，不纳入本次成果。
