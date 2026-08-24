## 1. Source Review and API Shape

- [x] 1.1 对照 `story` 的 `CheckSourceService`、`CheckSource` 和书源管理触发入口，确认 reader 需要保留的检测链路、进度和失效标记行为。
- [x] 1.2 梳理 reader 现有 `BookSourceRepository.validateSource()`、导入、保存、启用逻辑，确定哪些基础校验可以复用，哪些需要扩展为深度检测。
- [x] 1.3 定义检测结果模型，至少包含 `bookSourceUrl`、显示名、成功/失败、失败阶段、错误摘要和进度展示所需字段。

## 2. Repository Detection Flow

- [x] 2.1 在 Repository 层新增单源检测 API，按搜索或发现、详情、目录、首章正文的可阅读链路判定可用性。
- [x] 2.2 支持搜索为空时回退发现；仅支持发现的源直接走发现；缺少必要阅读规则时返回明确失败原因。
- [x] 2.3 为单源检测增加超时、异常捕获和阶段化错误摘要，避免异常泄漏到 UI。
- [x] 2.4 持久化检测结果：失败时添加或更新“失效”标记和检测错误摘要，并自动将 `enabled` 置为 `false`；成功时清理检测写入的失效信息，但不自动重新启用原本禁用的书源。
- [x] 2.5 确认导入、保存、启用阶段继续保留基础校验；如复用检测代码，不降低现有保护行为。

## 3. ViewModel and UI

- [x] 3.1 在 `BookSourceViewModel` 中管理检测 job、进度、当前检测源、结果列表和取消入口。
- [x] 3.2 在书源管理页面增加显式检测入口：列表项支持检测单源，页面操作区支持检测当前筛选结果；搜索框为空时检测全部书源。
- [x] 3.3 检测前提供关键词输入或确认，默认关键词使用“我的”，用户输入为空时保持默认值。
- [x] 3.4 检测过程中展示进度、当前源和取消操作；检测完成后展示成功、失败、自动禁用和取消数量摘要。
- [x] 3.5 列表项展示检测后的失效状态或错误摘要，不与启停开关、编辑、删除操作互相遮挡。

## 4. Tests

- [x] 4.1 补充 `BookSourceRepositoryTest`，覆盖搜索成功并加载正文、搜索为空回退发现、缺少必要规则、详情/目录/正文失败、检测成功清理失效标记、检测失败保留用户注释并自动禁用。
- [x] 4.2 补充 ViewModel 测试，覆盖单源检测、批量检测进度、取消、空列表和错误 toast/state。
- [x] 4.3 补充 UI 或 adapter 测试，覆盖检测入口触发、检测中状态和失败状态展示。
- [x] 4.4 保持现有导入、启用、书城源列表、书架远程搜索和阅读解析相关测试通过。

## 5. Validation

- [x] 5.1 运行 `openspec validate add-book-source-availability-check --strict`。
- [x] 5.2 运行 `openspec validate --all --strict`。
- [x] 5.3 运行 `git diff --check`。
- [x] 5.4 运行 `./gradlew testDebugUnitTest`。
- [x] 5.5 若触碰 Android UI 资源、Manifest、DI 或启动路径，补充 `./gradlew assembleDebug` 和按风险选择 `./gradlew lintDebug`。
- [x] 5.6 若可用真机环境允许，补充书源管理页面的设备验证，分别说明 build、install、cold start、日志观察和真实交互结果。
  - 已完成：`bf3ba0d2` 上完成 `installDebug` / `installDebugAndroidTest`，`BookSourceActivity` cold start 成功，进程存在，logcat 未见 reader 崩溃。
  - 已完成：新增并运行 `BookSourceDeviceTest#sourceAvailabilityCheckDisablesFailedSource`，覆盖导入测试书源、点击检测入口、确认检测弹窗、检测失败状态展示和自动禁用。
