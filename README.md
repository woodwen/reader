# reader
免费小说阅读App

[主页](https://github.com/woodwen/reader/tree/main)

[多模块版](https://github.com/woodwen/reader/tree/dev-multiple)

# 可能遇到的编译问题

如果是从多项目版本切换过来的，会在单项目版本中多很多多余的文件夹（buildSrc文件夹，feature系列文件夹，library系列文件夹，各种build文件夹），删除后再编译

# 以下为框架相关

# 项目特点

	* 基于现代Android应用程序技术堆栈和MVVM架构的小型应用程序。
	* 该项目的重点是实现依赖注入的新库Hilt。
	* 还可以从网络中获取数据，并通过存储库模式将持久性数据集成到数据库中

# 技术栈

   	• 最低SDK级别21

   	• 基于Kotlin，Coroutines + Flow用于异步。

   	• Hilt（alpha）用于依赖项注入。

	• JetPack
	   	○ LiveData-将域层数据通知视图。
	   	○ Lifecycle-当生命周期状态改变时，丢弃观察数据。
	   	○ ViewModel-与UI相关的数据持有者，具有生命周期意识。
	   	○ Room Persistence-使用抽象层构建数据库。

   	• 结构
	   	○ MVVM体系结构（视图-数据绑定-ViewModel-模型）
	   	○ 储存库模式

   	• Retrofit2和OkHttp3-构造REST API和分页网络数据。

   	• Moshi -Kotlin和Java的现代JSON库。

   	• Coil -使用Kotlin惯用API的图像加载库

   	• Bundler -Android Intent和Bundle扩展，可优雅地插入和检索值。

   	• Material-Components-材质设计组件，例如波纹动画，cardView。

   	• 自定义视图
		○ Rainbow-适用于Android的渐变和着色的简单方法。
	   	○ AndroidRibbon-一种在Android上通过闪烁实现漂亮的功能区的简单方法。
	   	○ ProgressView-优美灵活的ProgressView，可完全通过动画进行自定义。

# 架构

   基于MVVM体系结构和存储库模式
   
   ![](https://github.com/woodwen/reader/blob/main/screenshot/mvvm.png)
