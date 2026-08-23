## 1. Data Mapping

- [x] 1.1 梳理书架远程搜索结果当前从 `SearchRule`、`BookSource` 到列表 UI 的字段流向，确认不引入不必要的 Room schema migration。
- [x] 1.2 补齐动态书源搜索结果元信息映射，覆盖 `intro`、`kind`、`lastChapter`、`updateTime`、`wordCount` 等可用字段；`wordCount` 作为书源提供的字数展示文本，不抓正文计算。
- [x] 1.3 将 `BookSource.displayName()` 加入远程搜索结果展示数据，同时保留内部 `bookSourceUrl` / `shopName` 用于详情、目录和正文加载。
- [x] 1.4 分离“最新章节”和“连载/完结状态”展示语义，避免继续把最新章节当作状态显示。

## 2. UI

- [x] 2.1 为书架页远程搜索结果选择最小 UI 改动路径；若增强 `SquareAdapter` 会影响书城列表，则新增书架远程搜索专用 adapter/layout。
- [x] 2.2 在搜索结果 item 中展示书源提供的字数、数据来源、最新章节、简介、分类、连载/完结状态，并保证空字段不会造成布局异常。
- [x] 2.3 对缺失字段采用默认展示策略：简介为空隐藏；分类、状态、字数、最新章节为空时隐藏或显示“未知”，不得编造数据。
- [x] 2.4 保持点击结果进入详情、开始阅读和加入书架流程不变，并让详情弹窗中的分类/状态/简介与列表语义一致。

## 3. Tests / Validation

- [x] 3.1 补充或更新 `ShelfRepositoryTest`，验证多书源聚合结果保留 source key、展示书源名，并携带新增元信息。
- [x] 3.2 补充解析层单元测试，验证 `SearchRule.wordCount`、`lastChapter`、`kind`、`intro` 缺失或存在时的映射行为。
- [x] 3.3 运行 `openspec validate show-bookshelf-search-result-details --strict`。
- [x] 3.4 运行 `git diff --check`。
- [x] 3.5 实施阶段如触碰 Kotlin 或资源布局，运行 `./gradlew testDebugUnitTest`；如布局或启动路径风险较高，补充 `./gradlew assembleDebug` 或设备/截图验证。
