# M-9(feat): 新增书源可用检测

OpenSpec Change: add-book-source-availability-check

背景:
- 书源管理已有导入、编辑、启停和删除能力，但缺少主动检测入口，用户无法直接确认已导入书源是否能完成搜索或发现、详情、目录和正文读取链路。
- 用户确认检测失败时自动禁用失效源，其余策略沿用方案默认值。

方案概述:
- 在书源管理页新增单源检测和当前列表批量检测入口，检测前提供默认关键词“我的”。
- Repository 复用现有 `HtmlService` 和 `RuleBookParse` 执行可阅读链路检测，失败时持久化“失效”标记和检测错误摘要，并自动置 `enabled=false`。
- ViewModel 管理检测 job、进度、取消和最终摘要，UI 只负责触发和展示状态。

实现改动:
- 新增 `BookSourceCheckResult` 与 `BookSourceRepository.checkSourceAvailability()`，按搜索/发现、详情、目录、首章正文判定书源可用性。
- `BookSourceActivity`、`BookSourceViewModel` 和 `BookSourceAdapter` 增加检测入口、进度条、取消入口、检测中状态和失败状态展示。
- 更新 `activity_book_source.xml` 和 `item_book_source.xml`，补充批量检测、检测进度和列表项检测按钮。
- 补充 Repository、ViewModel、Adapter 单元测试，以及真机 instrumentation 测试 `BookSourceDeviceTest#sourceAvailabilityCheckDisablesFailedSource`。
- 归档 OpenSpec change，并生成 `book-source-management` 主规格。

测试计划(UT):
- `openspec validate add-book-source-availability-check --strict`
- `openspec validate --all --strict`
- `openspec archive add-book-source-availability-check --yes`
- `openspec validate --all --strict`
- `git diff --check`
- `./gradlew testDebugUnitTest assembleDebug lintDebug`
- `./gradlew installDebug installDebugAndroidTest`
- `adb -s bf3ba0d2 shell am instrument -w -r -e class com.woodnoisu.reader.source.BookSourceDeviceTest#sourceAvailabilityCheckDisablesFailedSource com.woodnoisu.reader.test/com.woodnoisu.reader.AppTestRunner`
- `adb -s bf3ba0d2 logcat -d -t 300 AndroidRuntime:E '*:S'`

影响范围(建议手动测试范围):
- 书源管理页的新增、导入、搜索筛选、单源检测、批量检测、取消检测、启停、编辑和删除。
- 检测失败后书城和书架远程搜索只消费仍启用书源的路径。
- 原有二维码导入、文件导入、在线 URL 导入和编辑页保存路径。

风险与后续:
- 真实站点限流、反爬或临时网络异常可能导致检测失败并自动禁用，用户可手动重新启用后再次检测。
- 当前实现为页面内检测任务，未引入前台 Service；如后续需要离开页面继续检测，应单独建 change。
