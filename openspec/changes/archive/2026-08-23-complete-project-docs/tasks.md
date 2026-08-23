## 1. Evidence Audit

- [x] 1.1 审核现有 `README.md`，标记可保留内容、过时内容和缺失内容。
- [x] 1.2 审核 Gradle 配置、`app/` 目录、截图/APK 文件、OpenSpec artifacts 和近期变更，确认 README/CHANGELOG 可写入的事实。

## 2. README.md

- [x] 2.1 更新根目录 `README.md` 结构，补齐项目概览、功能范围、技术栈、目录结构、本地环境、构建/测试入口、维护规则和参考链接。
- [x] 2.2 校准已有截图、下载链接、单模块说明、书源能力和常见编译问题；保留现有 APK 链接但避免称为“最新”，并避免保留无法确认或明显过时的表述。
- [x] 2.3 确认 README 不声明本次未验证的构建结果、设备交互结果、性能指标、下载量或发布时间。
- [x] 2.4 确认 README 更新不需要配套修改 Android 源码、资源、Gradle、Room schema、Manifest 或测试代码。

## 3. CHANGELOG.md

- [x] 3.1 新增根目录 `CHANGELOG.md`，包含 `Unreleased` 区块和变更分类。
- [x] 3.2 仅在仓库证据可确认时补充历史版本条目；否则保留保守说明，不追溯不可证历史，不虚构发布日期或发布内容。

## 4. Validation

- [x] 4.1 运行 `openspec validate complete-project-docs --strict`。
- [x] 4.2 运行 `git diff --check`。
- [x] 4.3 汇报未运行 Gradle、lint 或设备检查的原因。
