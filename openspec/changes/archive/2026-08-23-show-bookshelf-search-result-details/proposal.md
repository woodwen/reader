## Why

书架页已经支持在搜索框输入关键词后通过“搜书源”聚合已启用书源的远程搜索结果，但结果列表当前主要展示书名、作者和简介。用户在多个书源返回相同或相似书籍时，无法直接判断结果来自哪个书源、是否连载、最新章节是什么、分类是否匹配，以及章节字数信息是否符合预期。

本 change 先规划书架远程搜索结果的信息展示，不修改实现代码。目标是让用户在进入详情或开始阅读前，能在列表中完成更可靠的结果筛选。

## What Changes

- 书架页“搜书源”结果列表展示更完整的书籍元信息：书源提供的字数字段、数据来源、最新章节、简介、分类、连载/完结状态。
- 数据来源展示使用用户可理解的书源显示名；内部仍保留书源 key 用于详情、目录和阅读路由。
- 搜索源没有返回某项元信息时，列表不编造数据，保持结果可点击；简介为空时隐藏，分类、状态、最新章节或字数为空时隐藏或以“未知”等弱提示处理。
- 解析层需要补齐搜索规则中已有元信息字段的映射，避免把“最新章节”和“连载/完结”混在同一个字段中展示。
- 字数展示以书源搜索规则返回的 `wordCount` 为准，不额外抓取章节正文计算真实单章字数。
- 本 change 不调整书源管理、二维码导入、阅读器分页、目录加载或书架本地网格展示。
- 本 change 不提交、不 push、不 archive，除非用户后续明确要求。

## Capabilities

### New Capabilities

- `bookshelf-search`：定义书架远程书源搜索结果的信息展示、书源身份映射、缺失数据兜底和验证要求。

### Modified Capabilities

- 无。当前仓库没有业务 baseline specs，本 change 只新增 `bookshelf-search` delta spec。

## Impact

- `openspec/changes/show-bookshelf-search-result-details/**`：记录本次方案、设计、任务和 delta spec。
- 后续实施预计涉及：
  - `app/src/main/java/com/woodnoisu/reader/network/rule/RuleBookParse.kt`
  - `app/src/main/java/com/woodnoisu/reader/repository/ShelfRepository.kt`
  - `app/src/main/java/com/woodnoisu/reader/ui/square/SquareAdapter.kt` 或书架专用远程搜索结果 adapter
  - `app/src/main/res/layout/item_book.xml` 或新的远程搜索结果 item layout
  - 相关单元测试
- 当前方案阶段不修改 Android 源码、资源、Gradle 配置、数据库 schema 或测试代码。
