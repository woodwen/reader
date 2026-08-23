## 1. Source Option Flow

- [x] 1.1 调整书城源选项获取路径，只返回书源管理中已启用且支持发现的 `BookSource`。
- [x] 1.2 确保书城 `SourceOption.key` 使用 `bookSourceUrl`，展示名使用 `BookSource.displayName()`。
- [x] 1.3 去除书城选项对 `getFixedSourceOptions()` 或固定 `parseMap` 列表的初始化/兜底依赖。
- [x] 1.4 移除 `SquareRepository` 和 `SquareViewModel` 中固定源获取、初始化和兜底相关 API。

## 2. Bookstore State and UI

- [x] 2.1 调整 `SquareViewModel` 初始化逻辑，等待书源管理源列表加载后再选择默认源。
- [x] 2.2 当前源仍启用时保留选择；当前源被禁用或删除时切换到第一个可用源；无可用源时清空选择。
- [x] 2.3 无可用源时清空书城列表，展示导入或启用书源的空态提示，并阻止刷新、分类和加载更多触发固定源请求。
- [x] 2.4 保持支持发现的源可进入“发现”，仅支持搜索的源不在书城源列表展示。
- [x] 2.5 在无源空态增加轻量“书源管理”跳转入口。
- [x] 2.6 确认新安装或无书源状态下不自动创建、内置或导入默认源。

## 3. Fixed Source Removal

- [x] 3.1 删除 `HtmlClient` 中固定 `parseMap` 分支、固定源识别和固定源列表暴露方法。
- [x] 3.2 删除只服务 `全文阅读`、`笔趣阁` 的旧解析类，例如 `QWYDParse` 和 `BQGParse`。
- [x] 3.3 检查 `HtmlParse` 是否仍作为 `RuleBookParse` 通用基类需要保留；若保留，确保其中没有固定源行为。
- [x] 3.4 全仓库搜索并移除固定源解析相关未使用引用、导入和测试期望。

## 4. Tests

- [x] 4.1 补充 `HtmlClient` 或 Repository 单元测试，证明书城源列表不包含固定源兜底，只包含支持发现的可用管理源。
- [x] 4.2 补充 `SquareViewModel` 行为测试，覆盖初始无源、当前源保留、当前源失效后切换/清空。
- [x] 4.3 补充搜索/发现能力测试，覆盖仅搜索源不进入书城，支持发现源可发起发现请求。
- [x] 4.4 补充固定源删除测试或断言，证明 `全文阅读`、`笔趣阁` 不再由 `HtmlClient` 特判为可用源。
- [x] 4.5 保持现有书源导入、规则解析、书架搜索和阅读相关测试通过。

## 5. Validation

- [x] 5.1 运行 `openspec validate use-managed-bookstore-sources --strict`。
- [x] 5.2 运行 `git diff --check`。
- [x] 5.3 运行 `./gradlew testDebugUnitTest`。
- [x] 5.4 若触碰 UI 资源、启动路径或 DI，补充 `./gradlew assembleDebug` 或 `./gradlew lintDebug`。
- [x] 5.5 真机验证：设备 `bf3ba0d2`（`24117RK2CC`，Android 16）安装 `app-debug.apk` 成功并冷启动成功；普通 `adb shell input tap` 被系统拦截（缺少 `INJECT_EVENTS`），已改用 instrumentation 交互验证。`BookSourceDeviceTest#squareManagedSourceCanSearchOpenDetailAndStartReading` 在真机通过，覆盖导入排序更靠前的仅搜索托管源、导入支持发现的托管源、书城源列表排除仅搜索源、书城加载支持发现源、展示发现列表、打开详情并点击开始阅读后显示 `read_pv_page`。
