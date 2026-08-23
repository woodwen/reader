# M-5(feat): 完善书架书源搜索结果信息

OpenSpec Change: show-bookshelf-search-result-details

背景:
- 书架页远程“搜书源”结果此前主要展示书名、作者和简介，用户难以在多个书源结果之间判断来源、分类、最新章节、状态和字数信息。
- 动态书源搜索规则已有 `intro`、`kind`、`lastChapter`、`wordCount` 等字段，但搜索结果展示和解析映射没有完整利用这些元信息。

方案概述:
- 在不改变本地书架网格、不新增 Room schema migration 的前提下，为书架远程搜索结果补充非持久展示字段和专用列表 item。
- 列表展示书源提供的字数、数据来源、最新章节、简介、分类、连载/完结状态；缺失字段不编造数据。
- 数据来源展示使用 `BookSource.displayName()`，内部仍保留 `bookSourceUrl` / `shopName` 作为详情、目录和阅读路由 key。

实现改动:
- 在 `BookBean` 增加 `@Ignore` 字段 `sourceDisplayName`、`latestChapter`、`wordCountText`，仅用于搜索结果展示。
- 在 `RuleBookParse` 补齐 `wordCount`、`lastChapter`、`kind`、`intro` 映射，并将最新章节和连载/完结状态分离。
- 在 `HtmlClient` 和 `ShelfRepository` 补充书源显示名传递，保持聚合搜索按 source key + URL 区分结果。
- 新增 `RemoteSearchAdapter` 和 `item_remote_search_book.xml`，书架远程搜索不再复用书城 `SquareAdapter`，避免影响书城列表密度。
- 更新 `ShelfFragment` 的远程搜索列表和详情弹窗展示语义。
- 归档 OpenSpec change，生成 `openspec/specs/bookshelf-search/spec.md`。

测试计划(UT):
- `openspec validate show-bookshelf-search-result-details --strict`
- `openspec validate --all --strict`
- `git diff --check`
- `./gradlew :app:testDebugUnitTest --tests com.woodnoisu.reader.network.rule.RuleBookParseTest --tests com.woodnoisu.reader.repository.ShelfRepositoryTest`
- `./gradlew :app:testDebugUnitTest`
- `./gradlew :app:assembleDebug`

影响范围(建议手动测试范围):
- 书架页搜索框输入关键词后点击“搜书源”，检查结果列表是否展示来源、分类、状态、字数、最新章节和简介。
- 点击远程搜索结果进入详情弹窗，检查详情、开始阅读和加入书架流程是否仍使用正确书源。
- 检查本地书架网格展示和书城列表展示是否保持原有布局。

风险与后续:
- 字数字段只展示书源搜索规则提供的 `wordCount` 原始文本，不抓章节正文计算真实单章字数。
- 未做真机交互验证；后续可在可安装/可操作设备上补充书架远程搜索到详情和阅读的完整手动验证。
