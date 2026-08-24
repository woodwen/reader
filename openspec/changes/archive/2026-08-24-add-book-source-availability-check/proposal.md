## Why

当前书源管理已经支持导入、编辑、启停和删除书源，并在导入、保存或启用时做了基础可用性校验。但用户缺少一个主动检测入口，无法在管理页直接判断已导入书源是否真的能完成搜索或发现、打开详情、加载目录并读取正文。

参考 `/Users/mac/code/AndroidStudioProjects/story` 中的书源校验能力，本 change 目标是在 reader 的书源管理中新增用户可见的“书源可用检测”，让用户能批量或单独识别失效源，并看到检测进度和失败原因。

## What Changes

- 在书源管理页面新增显式检测入口，支持检测单个书源，以及检测当前列表范围内的多个书源。
- 检测逻辑不只判断 HTTP 响应非空，而是按 `story` 的思路执行可阅读链路：搜索或发现书籍、加载详情、加载目录、加载首章正文。
- 检测过程中展示当前进度、正在检测的书源和可取消操作，避免长时间网络请求让用户无法判断状态。
- 检测成功时清除该书源此前由检测写入的失效标记；检测失败时保留书源记录，自动禁用该书源，并写入可见失败状态和错误摘要。
- 检测失败不会自动删除书源；检测成功也不会自动重新启用原本禁用的书源，用户仍可手动启用。
- 检测逻辑复用现有 `BookSource`、`BookSourceRepository`、`HtmlService` 和 `RuleBookParse` 边界，UI 不直接耦合网络或解析细节。
- 不新增书源市场、远程推荐源下载、规则编辑器调试器或完整后台常驻检测服务。
- 本 change 仅规划方案；当前阶段不修改实现代码、不提交、不 archive。

## Capabilities

### New Capabilities

- `book-source-management`：定义书源管理中的导入、启停、编辑、检测、失效标记和用户可见反馈要求。

### Modified Capabilities

- 无。当前仓库还没有独立的书源管理 baseline spec；已有 `bookstore-source-integration` 只覆盖书城如何消费管理源。

## Impact

- `openspec/changes/add-book-source-availability-check/**`：记录本次方案、设计、任务和 delta spec。
- 后续实施预计涉及：
  - `app/src/main/java/com/woodnoisu/reader/repository/source/BookSourceRepository.kt`
  - `app/src/main/java/com/woodnoisu/reader/ui/source/BookSourceViewModel.kt`
  - `app/src/main/java/com/woodnoisu/reader/ui/source/BookSourceActivity.kt`
  - `app/src/main/java/com/woodnoisu/reader/ui/source/BookSourceAdapter.kt`
  - `app/src/main/res/layout/activity_book_source.xml`
  - `app/src/main/res/layout/item_book_source.xml`
  - `app/src/test/java/com/woodnoisu/reader/repository/source/BookSourceRepositoryTest.kt`
  - 视实现需要新增 `BookSourceViewModel` 或 UI 行为测试
- 不改数据库 schema；优先复用已有 `book_sources` 表、`enabled`、`bookSourceGroup` 和 `bookSourceComment` 表达检测结果。
- 不改变书城只消费已启用且支持发现书源的既有边界。
- 不影响二维码导入、文件导入、在线 URL 导入和编辑页保存入口。
