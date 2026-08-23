## Why

当前书城仍会初始化并展示内置固定源，例如 `全文阅读` 和 `笔趣阁`。这些源写死在客户端代码中，和用户通过“书源管理”导入、启停、维护的源不是同一个入口，导致书城实际使用范围和用户管理状态不一致。

本 change 目标是让书城以“书源管理”中的已启用源作为唯一用户可选来源，去掉书城入口对原固定源列表的依赖，让新增、禁用、删除书源后能直接影响书城。

## What Changes

- 书城源列表改为只读取书源管理中已启用、且支持发现的 `BookSource`；仅支持搜索的源不在书城展示。
- 书城不再向用户展示写死的固定源；除非用户在书源管理中导入并启用了同名源，否则 `全文阅读`、`笔趣阁` 不应作为书城默认选项出现。
- 书城选源的内部 key 继续使用 `bookSourceUrl`，展示名使用 `BookSource.displayName()`。
- 没有可用书源时，书城展示空态提示，并引导用户先到书源管理导入或启用书源；不再回退到固定源。
- 已启用书源变更后，书城返回前台时刷新源列表，并处理当前选中源被禁用或删除的情况。
- 动态源能力保持边界清晰：书城只展示支持发现的源；仅支持搜索的源继续保留在书源管理和书架搜索等搜索路径中，不进入书城源列表。
- 无可用书源空态增加轻量“书源管理”跳转入口。
- 不自动创建、内置或导入默认源；书城可用性完全来自用户在书源管理中启用的源。
- 删除旧固定源入口和只服务固定源的解析实现，包括 `HtmlClient` 中的固定 `parseMap`、固定源暴露 API，以及 `全文阅读`、`笔趣阁` 对应的旧解析类。
- 本 change 仅规划方案；当前阶段不修改实现代码、不提交、不 archive。

## Capabilities

### New Capabilities

- `bookstore-source-integration`：定义书城与书源管理之间的来源同步、选源、空态、搜索/发现能力和兼容边界。

### Modified Capabilities

- 无。当前仓库没有书城或书源管理的 baseline OpenSpec spec。

## Impact

- `openspec/changes/use-managed-bookstore-sources/**`：记录本次方案、设计、任务和 delta spec。
- 后续实施预计涉及：
  - `app/src/main/java/com/woodnoisu/reader/network/HtmlClient.kt`
  - `app/src/main/java/com/woodnoisu/reader/repository/SquareRepository.kt`
  - `app/src/main/java/com/woodnoisu/reader/ui/square/SquareViewModel.kt`
  - `app/src/main/java/com/woodnoisu/reader/ui/square/SquareFragment.kt`
  - `app/src/main/java/com/woodnoisu/reader/network/parse/QWYDParse.kt`
  - `app/src/main/java/com/woodnoisu/reader/network/parse/BQGParse.kt`
  - `app/src/main/java/com/woodnoisu/reader/network/parse/HtmlParse.kt`（如仍作为 `RuleBookParse` 基类则保留通用基类，不保留固定源行为）
  - `app/src/test/java/com/woodnoisu/reader/network/HtmlClientTest.kt`
  - 视实现需要补充书城 ViewModel 或 UI 行为测试
- 不改数据库 schema；继续复用已有 `book_sources` 表、`BookSourceDao.getAllEnabled()` 和规则解析能力。
- 不影响书源管理的导入、编辑、启停、删除、二维码导入入口。
- 删除固定解析后，历史书架数据中仍带旧固定 `shopName` 的书籍不做自动迁移；如需继续可读，应通过书源管理导入对应规则源。
