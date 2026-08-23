# M-8(build): 升级应用版本至 1.0.3

背景:
- 当前应用配置版本仍为 `versionName 1.0.2`、`versionCode 2`。
- 本次提交只处理软件版本升级，不调整依赖版本、SDK 基线、数据库 schema 或下载外链。

方案概述:
- 将 Android 应用集中版本配置升级到 `versionName 1.0.3`、`versionCode 3`。
- 同步 README 当前版本说明，并在 CHANGELOG 的 `Unreleased / Build` 记录该版本配置变更。

实现改动:
- 更新 `dependencies.gradle` 中的 `versionCode` 和 `versionName`。
- 更新 `README.md` 中当前仓库配置版本。
- 更新 `CHANGELOG.md`，记录本次版本升级。

测试计划(UT):
- `git diff --check`
- `./gradlew :app:assembleDebug`

影响范围(建议手动测试范围):
- 影响后续 APK 构建产物的展示版本与安装升级判断。
- 不涉及业务逻辑、UI 流程、网络解析、Room schema 或设备交互变更。
