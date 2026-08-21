# M-1(fix): 修复新版 Android Studio 构建与真机启动

背景:
- 更新到 Android Studio Quail 3 后，旧 Gradle/AGP/Kotlin/Hilt 组合无法稳定 Sync/构建，使用新版 IDE 自带 JBR 会触发兼容问题。
- 迁移后真机启动 `MainActivity` 时出现 `CreationExtras must have a value by SAVED_STATE_REGISTRY_OWNER_KEY` 崩溃。
- 仓库中还跟踪了本地 `local.properties`，不利于不同开发机协作。

方案概述:
- 采用低风险升级路线，固定项目可在 JDK 11 下构建，避免直接跨到 Gradle 9/AGP 9。
- 移除废弃的 `kotlin-android-extensions`，迁移到 `kotlin-parcelize` 与 ViewBinding。
- 对齐 Hilt、Lifecycle、Activity/Fragment KTX 版本，修复真机启动崩溃。
- 清理测试目录中过期的 Dagger generated 源码，让测试编译依赖 kapt 正常生成。

实现改动:
- 升级 Gradle Wrapper、AGP、Kotlin、Room、Hilt、Lifecycle、Activity KTX、Fragment KTX 等构建依赖。
- 添加 `namespace`，保留 `applicationId`，并将 `compileSdk` 提到 31、`targetSdk` 保持 30。
- 将 Parcelable 注解切换到 `kotlinx.parcelize.Parcelize`。
- 将 Activity、Fragment、阅读设置弹窗中的 synthetic view 访问迁移为 ViewBinding。
- 将 Hilt ViewModel 从旧 assisted 注入迁移为 `@HiltViewModel` + `@Inject`。
- 调整书城网络失败和空结果处理，避免空 HTML 导致解析链路异常，并在页面显示可见错误状态。
- 从 Git 跟踪中移除 `local.properties`，并忽略 `.DS_Store`、`/docs/spec/` 和本地配置文件。

测试计划(UT):
- `JAVA_HOME=/Library/Java/JavaVirtualMachines/microsoft-11.jdk/Contents/Home ./gradlew :app:assembleDebug --no-daemon --stacktrace`
- `JAVA_HOME=/Library/Java/JavaVirtualMachines/microsoft-11.jdk/Contents/Home ./gradlew :app:testDebugUnitTest --no-daemon --stacktrace`
- `git diff --check`
- 真机安装并冷启动 `com.woodnoisu.reader/.ui.StartActivity`，确认 `MainActivity` 前台存活且 crash buffer 为空。

影响范围(建议手动测试范围):
- Android Studio Sync、debug 构建、真机安装启动。
- 首页书城、书架、我的三个主入口页面。
- 搜索、分类切换、下拉刷新、加入书架、阅读设置弹窗。

调查结论:
- 新版 Hilt/Lifecycle 需要 Activity/Fragment KTX 与 Lifecycle 2.5.x 时代的 `CreationExtras` 支持；旧 `activity-ktx`/`fragment-ktx` 会导致真机启动崩溃。
- Android Studio Quail 3 自带 JBR 25 不适合当前低风险构建栈，默认仍使用 JDK 11。

风险与后续:
- 构建仍有 Gradle 8 兼容性弃用警告，后续若要支持 JBR 25，需要另起 Gradle 9/AGP 9 大版本迁移。
- 当前真机禁止 shell 注入点击事件，因此自动化只验证到安装、冷启动、主界面存活和截图渲染。
