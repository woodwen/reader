## Context

Reader 当前是单模块 Android/Kotlin 免费小说阅读器，Gradle 入口为 `./gradlew`，主模块为 `:app`。仓库已有 OpenSpec 配置和 Reader 项目协作规范，要求文档和规范正文默认使用简体中文，并保留路径、命令、capability id、包名和 OpenSpec 结构关键字的英文稳定写法。

当前根目录 `README.md` 已存在，包含项目简介、参考项目、APK 下载链接、截图、功能列表、技术栈和架构图，但存在以下问题：

- 标题和结构较松散，不利于新参与者快速定位构建、测试和维护信息。
- 部分技术描述带有历史痕迹，例如早期依赖注入说明、单模块/多模块切换说明，需要后续实施时用当前仓库文件校准。
- 缺少明确的本地开发环境、常用 Gradle 命令、验证规则和设备验证边界。
- 缺少 CHANGELOG，无法用统一位置记录 `Unreleased` 和可确认历史版本。

## Goals / Non-Goals

**Goals:**

- 补齐根目录 `README.md`，让首次进入仓库的开发者能了解项目定位、功能范围、技术栈、目录结构、构建/测试入口和维护规则。
- 新增根目录 `CHANGELOG.md`，记录后续变更，默认包含 `Unreleased` 区块。
- 保留已有 README 中仍可验证的截图、下载链接、参考项目和项目背景，避免无必要地重写全部内容。
- 对无法从仓库确认的内容保持保守表达，必要时标注为“待确认”或不写入。
- 将文档验证限定为 OpenSpec validation 和 whitespace 检查；不因纯文档变更默认运行 Android 构建。

**Non-Goals:**

- 不修改 Android 应用源码、资源、依赖版本、数据库 schema 或测试代码。
- 不新增完整业务 baseline specs；本次只新增 `project-documentation` capability。
- 不重写项目品牌、图标、截图素材或 APK 发布流程。
- 不补写无法从仓库证据确认的历史发布日期、发布说明或量化指标。
- 不自动提交、push 或 archive。

## Confirmed Defaults

- `README.md` 采用“更新现有文档”的方式，而不是删除后重写；保留可验证且仍有价值的现有信息。
- `CHANGELOG.md` 采用简洁的变更日志结构，包含 `Unreleased`；历史版本只记录仓库中可确认的信息。
- README 保留现有 APK 下载链接，但将“最新应用下载地址”改为“现有 APK 下载链接”等保守表述；除非实施时能证明其最新性，否则不声明最新。
- CHANGELOG 不追溯无法确认的旧版本日期和发布内容；宁可保留“历史未完整重建”的说明，也不补猜测内容。
- README 中的构建命令以仓库事实为准，默认包含 `./gradlew assembleDebug`、`./gradlew testDebugUnitTest` 等项目内可执行入口，但不承诺这些命令在方案阶段已经运行。
- 文档正文使用简体中文；路径、命令、模块名、类名、技术名和版本号保持英文或原文。
- 后续实施默认只修改 `README.md` 和 `CHANGELOG.md`，不触碰 Android 源码、资源、Gradle、Room schema、Manifest 或测试代码。
- 纯文档实施后默认运行 `openspec validate complete-project-docs --strict` 和 `git diff --check`；Gradle、lint 和设备检查默认不运行并说明原因。

## Decisions

### Decision 1: README 更新而非新建替换

根目录已经存在 `README.md`，后续实施 SHALL 在现有基础上整理结构、补齐缺口并校准事实。这样能保留已有下载链接、截图、参考项目和历史背景，同时降低无关改动范围。

备选方案是完全重写 README。该方案更容易得到干净结构，但会丢失原作者已有上下文和参考信息，且与“补上 README”的最小变更目标不成比例，默认不采用。

### Decision 2: CHANGELOG 使用保守历史

根目录新增 `CHANGELOG.md` SHALL 至少包含 `Unreleased` 区块。若仓库 tag、APK 文件、README 下载链接或提交历史能确认历史版本，可加入对应版本；否则不得虚构发布日期和发布内容。

备选方案是根据记忆或推断补完整历史。该方案看起来完整，但可信度不足，默认不采用。

### Decision 3: 文档内容以当前仓库证据为准

README 和 CHANGELOG SHALL 优先引用当前仓库中的 Gradle 配置、源码目录、OpenSpec artifacts、截图/APK 文件和本地命令输出。对于近期功能，例如动态书源管理、远程搜索结果展示等，实施时必须区分已实现、已规划、已归档和待确认状态。

### Decision 4: 纯文档验证不扩大到 Android 构建

本 change 只规划根目录文档变更。后续实施如果只修改 `README.md` 和 `CHANGELOG.md`，默认不运行 Gradle 构建、lint 或设备验证；如果实施过程中发现文档需要配套修改代码、资源或配置，则应先更新方案或另开 change。

### Decision 5: 下载链接保留但不声明最新性

现有 README 中的 APK 外链 SHALL 保留为可用的历史入口或现有下载入口，但后续实施 SHALL 把标题从“最新应用下载地址”调整为“现有 APK 下载链接”等保守表述。除非实施时用仓库 tag、release、APK 文件或其他证据证明该链接确为最新，否则不得继续声明“最新”。

备选方案是删除 APK 链接。该方案避免最新性风险，但会移除用户已有安装入口，默认不采用。

### Decision 6: CHANGELOG 不重建不可证历史

`CHANGELOG.md` SHALL 从 `Unreleased` 开始记录后续变更。旧版本只有在仓库证据明确时才补入；如果只能确认存在某个 APK 文件或 README 链接，但无法确认发布时间和完整内容，则只做保守说明，不编写看似完整的历史 release note。

备选方案是根据当前功能列表推断旧版本内容。该方案不可审计，默认不采用。

## Risks / Trade-offs

- README 旧内容可能包含已过时技术说明 -> 实施时需要用当前 Gradle、源码和 OpenSpec 校准，不能直接复制旧说法。
- CHANGELOG 历史可能证据不足 -> 默认只写 `Unreleased` 和可确认版本，宁缺毋滥。
- 文档命令如果未实际运行可能误导读者 -> 实施汇报必须区分“文档列出命令”和“本次已运行命令”。
- README 过长会降低可读性 -> 结构应聚焦项目入口、常用命令、功能范围、维护规则和链接，避免写成完整内部设计文档。

## Migration Plan

1. 审核 `README.md`、Gradle 配置、`app/` 目录、截图/APK 文件、OpenSpec artifacts 和近期变更，列出可确认事实。
2. 整理 README 结构：项目简介、功能状态、技术栈、目录结构、本地环境、构建/测试、书源/阅读能力、截图下载、常见问题、参考项目和维护说明。
3. 新增 CHANGELOG，包含 `Unreleased`，并仅补入可确认的历史版本条目。
4. 运行 `openspec validate complete-project-docs --strict` 和 `git diff --check`。
5. 汇报文档变更范围、验证结果，以及未运行 Gradle/lint/设备检查的原因。

## Open Questions

无。默认决策已确认：保留现有 APK 链接但不声明最新性；CHANGELOG 不追溯不可证历史；后续实施只修改 `README.md` 和 `CHANGELOG.md`。
