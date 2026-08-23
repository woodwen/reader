# M-7(feat): 书城改用书源管理源

OpenSpec Change: use-managed-bookstore-sources

## 背景:
- 书城原来仍包含 `全文阅读`、`笔趣阁` 等客户端写死源，和用户在书源管理中导入、启停、删除的状态不一致。
- 用户要求去除原写死源，并让书城只展示书源管理里非仅搜索的书源。

## 方案概述:
- 书城源列表统一从书源管理的已启用书源读取，只保留支持发现能力的源。
- 移除固定源解析入口和只服务固定源的旧解析类，不再做固定源兜底。
- 无可用发现源时进入书城空态，并提供进入书源管理的入口。

## 实现改动:
- `HtmlClient.getSourceOptions()` 改为只返回 `supportsExplore()` 的启用 `BookSource`，搜索型源继续由书架搜索路径使用。
- `SquareRepository`、`SquareViewModel`、`SquareFragment` 去除固定源初始化和兜底逻辑，处理无源空态、返回前台刷新、当前源失效切换。
- 删除 `QWYDParse`、`BQGParse` 旧固定源解析类，保留通用 `HtmlParse` 作为动态规则解析基类。
- 补充 `HtmlClientTest`、`SquareViewModelTest` 和真机 instrumentation，覆盖仅搜索源不进入书城、无固定源回退、书城发现/详情/阅读链路。
- 归档 OpenSpec change，生成 `bookstore-source-integration` 主规格，并更新 `CHANGELOG.md`。

## 测试计划(UT):
- `openspec validate use-managed-bookstore-sources --strict`
- `openspec validate --all --strict`
- `git diff --check`
- `./gradlew testDebugUnitTest`
- `./gradlew assembleDebug`
- `./gradlew :app:assembleDebugAndroidTest`
- 真机 `bf3ba0d2` 执行 `BookSourceDeviceTest#squareManagedSourceCanSearchOpenDetailAndStartReading` 通过，覆盖导入仅搜索源、导入发现源、书城排除仅搜索源、打开详情并进入阅读页。

## 影响范围(建议手动测试范围):
- 书城首次进入、返回前台刷新、无可用发现源空态、点击空态进入书源管理。
- 导入支持发现的书源后，书城源选择、发现列表、搜索、详情、加入书架和开始阅读。
- 仅搜索书源应继续可在书源管理维护和书架搜索中使用，但不应出现在书城源列表。
- 历史书架中旧固定 `shopName` 数据不会自动迁移；如需继续使用对应来源，需要导入规则源。

## 风险与后续:
- 无书源或仅有搜索源时，书城会展示空态，短期可用性取决于用户是否导入支持发现的书源。
- 旧固定源解析删除后，不再承诺通过 `全文阅读`、`笔趣阁` 固定标识继续获取详情、目录或正文。
