# Changelog

本文件记录 Reader 仓库中可从当前本地代码、OpenSpec artifacts 或提交记录确认的变更。无法确认日期或内容的历史版本不追溯补写。

## Unreleased

### Added

- 新增项目级 OpenSpec 和 `AGENTS.md` 协作规范，明确单模块 Android/Kotlin 项目边界、OpenSpec 工作流、修改边界和验证规则。
- 增加书源管理能力，覆盖书源列表、启停、编辑、删除、二维码导入和 `yuedu://booksource/importonline` 在线导入路径。
- 书城改用书源管理中的启用发现源，不再展示写死固定源或仅搜索源。
- 书架远程“搜书源”结果展示书源来源、分类、连载/完结状态、最新章节、简介和书源提供的字数字段。
- 更新 `README.md` 项目入口信息，并新增本 `CHANGELOG.md`。

### Changed

- 优化书架搜索和远程搜索展示链路，使本地书架过滤与远程书源搜索职责更清晰。
- 动态书源搜索结果保留内部 source key 用于详情、目录和正文加载，同时向用户展示可读书源名。
- README 中的 APK 链接改为保守的现有下载链接表述，不再声明未验证的最新性。

### Build

- 升级当前应用配置版本为 `versionName 1.0.3`、`versionCode 3`。

### Fixed

- 修复新版 Android Studio/legacy Gradle 基线下的构建与真机启动兼容问题。
- 修正书架远程搜索结果中“最新章节”和“连载/完结状态”混用的问题。

### Docs

- 补齐项目概览、目录结构、本地环境、构建/测试命令、OpenSpec 工作流、APK 链接说明和历史说明。

## 1.0.2 - 日期未确认

### Known

- 当前 `dependencies.gradle` 中配置 `versionName` 为 `1.0.2`，`versionCode` 为 `2`。
- 原 README 保留了 `reader_v1.0.2` APK 外链。

### Notes

- 当前本地仓库未发现 tag 或 release metadata 可确认该版本发布日期和完整发布内容。
- 更早版本历史未在本次文档更新中重建。
