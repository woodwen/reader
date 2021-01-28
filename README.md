# reader
免费小说阅读App

参考项目

1.android-showcase 模块化的mvvm开发 [android-showcase](https://github.com/igorwojda/android-showcase)

2.NovelReader 基于"任阅"的改进追书App [NovelReader](https://github.com/newbiechen1024/NovelReader)

3.FreeNovel 基于kotlin的免费Android小说应用[FreeNovel](https://github.com/lxygithub/FreeNovel) 

4.OKBook kotlin + 协程 + MVVM 模式来编写的看小说APP [OKBook](https://gitee.com/xcode_xiao/OKBook)

# 应用简介

在线/本地小说阅读器，用kotlin重写了“任阅”的阅读模块代码，优化，代码逻辑，降低内存使用率。纯kotlin开发，并使用模块化的mvvm模式开发，使得维护和扩展更方便。

目前已有功能：

1.书城

  * 支持书城切换（目前支持，全文阅读网，笔趣阁）。
  * 支持小说分类切换。
  * 支持按书名，作者搜索小说。
  * 支持查看小说简介。
  * 支持小说订阅
  * 支持直接在线阅读
  
2.书架
  
   * 支持取消订阅
   * 支持搜索书架
   * 支持订阅本地书籍（目前只支持.txt）
   * 支持本地阅读
    
3.个人配置
  
  * 清理缓存
  * 跳转github
  
4.阅读

  * 目录（小说目录）
  * 亮度（设置阅读器亮度，日/夜模式）
  * 缓存（下载小说到本地）
  * 设置（字体，字号，翻页模式，背景图片）

准备加入但是目前还没的功能：

1.隐藏书城
2.尝试支持厚墨源
3.书城不再是写死的方式，而是类似于厚墨的安装方式
4.支持语音朗读

**注: 该项目不定时维护更新，练手项目，如有侵权的地方，请告知小弟，立马删除**

# 以下为框架相关

# 项目特点

	• kotlin
	• 架构（动态功能模块，清洁架构，Model-View-ViewModel，Model-View-Intent）
	• Android Jetpack 库
	• 单活动架构（导航组件）
	• Reactive UI
	• CI pipeline（GitHub Actions）
	• Testing（Unit，UI）
	• 静态分析工具
	• 依赖注入
	• Material Design

# 技术栈

最低API级别设置为21，因此该方法适用于超过 85％的运行Android的设备。该项目利用了Android生态系统中许多流行的库和工具。除非有充分的理由使用非稳定依赖关系，否则大多数库都处于稳定版本。
	
	• 技术栈
		○ Kotlin + Coroutines-执行后台操作
		○ Kodein-依赖注入
		○ OKHttp - 网络请求相关
		○ Retrofit - 网络接口相关
		○ Jetpack
	 		§ Navigation - 处理整个应用内导航
	 		§ LiveData - 通知有关数据库更改的视图
	 		§ Lifecycle - 生命周期状态更改时执行操作
	 		§ ViewModel-以生命周期意识的方式存储和管理与UI相关的数据
		○ Coil-使用Kotlin惯用API的图像加载库
		○ Lottie-动画库
		○ Stetho-应用程序调试工具
	
	• 架构
		○ 清洁架构（模块级别）
		○ MVVM + MVI（表示层）
		○ 动态功能模块
		○ Android体系结构组件（ViewModel，LiveData，Navigation，SafeArgs插件）
	
	• 测试
		○ Unit Tests（JUnit）
		○ Mockk
		○ Kluent
	• Gradle
		○ Gradle Kotlin DSL
		○ 自定义任务
		○ 插件（Ktlint，Detekt，Versions，SafeArgs）

# 架构

与功能相关的代码位于功能模块之一中。我们可以将每个功能视为微服务或私有库的等效项。

我们在应用程序中有三种模块：

	• app module
		○ 这是主要模块。它包含将多个模块（依赖注入设置NavHostActivity等）和基本应用程序配置（改造配置，必需的权限设置，自定义应用程序类等）连接在一起的代码。
		
	• helper modules
		○ 独立于应用程序的library_base模块，包含可在其他项目/应用程序中重用的通用代码库（此代码并非特定于此应用程序）。基类，实用程序，自定义委托，扩展。
  		○ library_x一些功能可能依赖的其他特定于应用程序的模块。如果您只想在几个功能模块之间共享某些资产或代码，这将很有帮助（当前应用程序没有此类模块）
			
	• feature modules 
		○ 最常见的模块类型，包含与给定功能相关的所有代码。
  
功能模块结构
	
清洁架构是应用程序的“核心体系结构”，因此每个feature module层都包含自己的一组“清洁”体系结构层（app模块和library_x模块的结构与功能模块的结构略有不同）
 
每个功能模块均包含非层组件和3个层，每个层具有不同的职责集
 
	• presentation

 		该层最接近用户在屏幕上看到的内容。该presentation层是MVVM（JetpackViewModel用于在活动重新启动时保留数据）和 MVI（actions修改common state视图的，然后通过LiveData渲染将新状态编辑到视图）的混合。
 
		common state（针对每个视图）方法源自 单向数据流和Redux原理。
 
 		组件：
  			○ 视图（片段） -在屏幕上显示数据，并将用户交互传递给视图模型。视图很难测试，因此它们应尽可能简单。
  			○ ViewModel-将LiveData状态更改调度（通过）到视图并处理用户交互（这些视图模型不仅仅是POJO类）。
  			○ ViewState-单个视图的通用状态 
  			○ NavManager-单例，有助于处理内部的所有导航事件NavHostActivity（而不是分别在每个视图内部）
	
	• domain
	
  		这是应用程序的核心层。注意，该domain层独立于任何其他层。这允许使域模型和业务逻辑独立于其他层。换句话说，其他层的变化将不会对domain例如层产生影响。理想情况下，更改数据库（data图层）或屏幕UI（presentation图层）不会导致任何代码随domain图层更改。

 		组件：
  			○ UseCase-包含业务逻辑
  			○ DomainModel-定义将在应用程序内使用的数据的核心结构。这是应用程序数据的真实来源。
  			○ 存储库接口-保持domain层独立于data layer（Dependency inversion）所必需。
	
	• data

		管理应用程序数据，并将这些数据源作为存储库公开到该domain层。该层的典型职责是从Internet检索数据，并可以选择在本地缓存该数据。

		组件：
  			○ 存储库将数据公开到该domain层。根据应用程序结构和外部API的质量，存储库还可以合并，过滤和转换数据。这些操作的目的是为该domain层创建高质量的数据源，而不是执行任何业务逻辑（domain层use case职责）。
  			○ 映射器-映射data model到domain model（以使domain层独立于data层）。
  			○ RetrofitService-定义一组API端点。
			○ DataModel-定义从网络检索的数据的结构并包含批注，因此Retrofit（Moshi）了解如何将此网络数据（XML，JSON，Binary ...）解析为对象。
