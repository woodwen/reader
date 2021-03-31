# reader
免费小说阅读App

参考项目

1.Pokedex 单项目 mvvm，flow [Pokedex](https://github.com/skydoves/Pokedex)

2.NovelReader 基于"任阅"的改进追书App [NovelReader](https://github.com/newbiechen1024/NovelReader)

3.FreeNovel 基于kotlin的免费Android小说应用[FreeNovel](https://github.com/lxygithub/FreeNovel) 

4.OKBook kotlin + 协程 + MVVM 模式来编写的看小说APP [OKBook](https://gitee.com/xcode_xiao/OKBook)

# 最新应用下载地址
[reader_v1.0.2](https://raw.githubusercontent.com/woodwen/reader/main/apk/reader_v1.0.2.apk)

# 应用展示

![](https://github.com/woodwen/reader/blob/main/screenshot/1.jpeg)
![](https://github.com/woodwen/reader/blob/main/screenshot/2.jpeg)
![](https://github.com/woodwen/reader/blob/main/screenshot/3.jpeg)
![](https://github.com/woodwen/reader/blob/main/screenshot/4.jpeg)
![](https://github.com/woodwen/reader/blob/main/screenshot/5.jpeg)
![](https://github.com/woodwen/reader/blob/main/screenshot/6.jpeg)


# 应用简介

小说阅读器（模块化开发/单项目开发，基于Kotlin+MVVM+Kodein/Hilt+Retrofit+Jsoup+Moshi+Coroutines+Flow+Jetpack+Coil+Room+Mockk等架构实现），用kotlin重写了“任阅”的阅读模块代码，优化，代码逻辑，降低内存使用率。

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

**注: 该项目不定时维护更新，如有侵权的地方，请告知小弟，立马删除**

# 以下为框架相关（开辟了两个分支，一个是单项目的，一个是模块化的）

   [单模块版本](https://github.com/woodwen/reader/tree/dev-single)

   [多模块版本](https://github.com/woodwen/reader/tree/dev-multiple)


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
