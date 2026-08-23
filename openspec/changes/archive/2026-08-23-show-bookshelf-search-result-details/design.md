## Context

当前书架页由 `ShelfFragment` 管理：

- 本地书架展示使用 `ShelfAdapter` 和 `item_shelf.xml`，搜索框输入时过滤本地收藏。
- 点击“搜书源”或键盘搜索后，`ShelfViewModel.fetchRemoteSearch()` 调用 `ShelfRepository.fetchRemoteSearch()` 聚合已启用动态书源。
- 远程搜索结果复用 `SquareAdapter` 和 `item_book.xml`，当前只渲染书名、作者和简介。
- `ShelfRepository.fetchRemoteSearch()` 通过 `HtmlClient.getDynamicSearchSourceOptions()` 获取书源，按 `source.key` 调用解析，并用 `${book.shopName}|${book.url}` 去重。
- 动态书源内部 key 当前是 `bookSourceUrl`；用户可读名称来自 `BookSource.displayName()`。
- `SearchRule` 已有 `intro`、`kind`、`lastChapter`、`updateTime`、`wordCount` 等字段，但 `RuleBookParse.parseBookList()` 目前没有映射 `wordCount`，并把 `lastChapter` 填入 `BookBean.status`，导致“最新章节”和“连载/完结”语义混淆。

## Goals / Non-Goals

**Goals:**

- 书架页远程搜索结果列表直接显示：书源提供的字数字段、书源显示名、最新章节、简介、分类、连载/完结状态。
- 保留内部书源 key，确保点击结果后仍能加载详情、目录和正文。
- 对动态书源搜索规则中可解析的元信息做最小映射，不为缺失字段发起额外重型请求。
- 让多个书源返回同一本书时，用户能看到来源差异，并保留按书源区分的结果。
- 补充聚合搜索和元信息映射相关单元测试。

**Non-Goals:**

- 不重做书源管理、导入规则编辑、二维码导入或书城 Tab 的整体交互。
- 不为列表展示去抓取章节正文来计算真实单章字数；这会显著拖慢多书源并发搜索。
- 不改变本地书架网格卡片布局，除非后续用户明确要求本地收藏也展示同样字段。
- 不做完整业务 baseline specs；本次只新增 `bookshelf-search` capability。
- 不在方案阶段修改实现代码、提交或归档。

## Confirmed Defaults

- “单章字数”按书源搜索规则提供的 `wordCount` 字段展示为字数信息，不额外抓取章节正文计算真实字数。
- 改动范围只覆盖书架页远程“搜书源”结果列表，不改本地书架网格。
- 数据来源向用户展示 `BookSource.displayName()`，内部继续使用 `bookSourceUrl` / `shopName` 作为详情、目录和阅读路由 key。
- 最新章节和连载/完结状态强制分离；无法保守识别状态时隐藏或显示“未知”，不猜测。
- 简介为空时隐藏；分类、状态、字数、最新章节为空时隐藏或显示“未知”。
- UI 实现优先选择最小影响路径：如果增强 `SquareAdapter` 会影响书城列表，则新增书架远程搜索专用 adapter/layout。

## Decisions

### Decision 1: 范围限定在书架页远程“搜书源”结果

“书架搜索结果”确认解释为书架页点击“搜书源”后展示的远程书源搜索结果。该解释和“数据来源（哪个书源的）”要求一致，因为本地书架过滤结果通常已经是用户收藏书籍，不一定需要在网格卡片上显示来源。

备选方案是同时改本地书架网格和远程结果列表。该方案覆盖更广，但会影响收藏书籍密集布局，和本次“搜索结果元信息”目标不成比例，默认不采用。

### Decision 2: 展示用书源名和路由用书源 key 分离

结果列表 SHALL 展示 `BookSource.displayName()`，而详情、目录和正文加载 SHALL 继续使用内部 `shopName`/source key。动态书源 key 仍为 `bookSourceUrl`，避免用可编辑显示名做网络路由。

备选方案是直接展示 `shopName`。该方案实现简单，但动态书源下 `shopName` 是 URL，不适合用户快速识别，默认不采用。

### Decision 3: 最新章节和连载状态必须分离

搜索结果 SHALL 把“最新章节”和“连载/完结状态”作为不同展示项。后续实施时可以用 transient display 字段或书架搜索专用 UI model 承载，不应继续把 `SearchRule.lastChapter` 填进 `BookBean.status` 后同时当状态展示。

如果书源没有提供明确连载状态，只从 `kind`、详情状态文本或其他已解析文本中做保守识别：包含“完结”“完本”“已完结”视为完结，包含“连载”“连载中”视为连载；无法识别时显示“未知”或隐藏状态，不猜测。

### Decision 4: 单章字数使用书源提供的字段，不额外抓正文计算

“单章字数”存在数据来源歧义：现有动态书源规则里可用字段名是 `wordCount`，很多书源也可能把它表示为总字数、章节字数或自由文本。确认方案是把书源搜索结果提供的 `wordCount` 作为列表上的“字数”展示文本保留，不强转为整数，也不抓取章节正文计算真实单章字数。

如果后续确认必须展示“最新章节正文实际字数”，需要新增详情/章节内容采样流程、超时和缓存策略，建议单独开 change。

### Decision 5: 缺失字段不阻断搜索结果

搜索结果 SHALL 以书名和可打开链接为最低可展示条件。简介、分类、最新章节、状态、字数字段缺失时，列表仍展示结果并保留点击详情能力。UI 对空简介隐藏；对空分类、最新章节、状态或字数字段隐藏或显示“未知”，但不得生成看似真实的数据。

### Decision 6: 默认新增书架远程搜索专用展示面

后续实施 SHALL 先评估 `SquareAdapter` 是否可兼容增强。如果增强会改变书城列表密度或语义，则新增书架远程搜索专用 adapter/layout；默认不为了书架搜索结果改坏书城列表。

## Risks / Trade-offs

- 动态书源规则质量不一致，部分字段可能为空或语义不标准 -> UI 和测试必须覆盖缺失字段场景。
- 复用 `SquareAdapter` 会影响书城列表展示 -> 后续实施需选择“只增强 adapter 且兼容书城”或新增书架远程搜索专用 adapter，默认优先避免破坏书城已有布局。
- `BookBean` 是 Room entity，直接新增持久字段会触发 schema migration -> 默认优先使用 transient display 字段或专用 UI model，除非确有持久化需求。
- 多书源并发搜索已经逐步 emit 结果，新增字段映射不能延长单源超时或让失败源阻塞其他结果。

## Migration Plan

1. 梳理 `RuleBookParse`、`ShelfRepository`、`SquareAdapter` 和 `item_book.xml` 的现有字段使用，确认是否需要书架远程搜索专用 adapter/layout。
2. 确定展示模型：优先选择不触发 Room migration 的 transient display 字段或书架远程搜索专用 UI model。
3. 补齐动态书源搜索解析：映射 `wordCount`、`lastChapter`、`kind`、`intro` 等已有规则字段，并区分最新章节和状态。
4. 在书架远程搜索结果聚合时补充书源显示名，同时保留 source key。
5. 更新列表 UI，展示完整元信息并处理字段缺失。
6. 补充单元测试，覆盖多书源聚合、书源显示名、最新章节/状态分离、字数字段和缺失字段。
7. 运行 OpenSpec validation、diff 检查和相关 Android 单元测试；如资源布局风险较高，补充 `assembleDebug` 或截图/设备验证。

## Open Questions

无。默认建议已确认并写入本方案。
