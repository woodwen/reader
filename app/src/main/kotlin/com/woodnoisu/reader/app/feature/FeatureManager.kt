package com.woodnoisu.reader.app.feature

import com.woodnoisu.reader.BuildConfig

//动态功能模块需要反向依赖（动态功能模块取决于应用程序模块）
//这意味着我们必须使用反射来访问模块内容
//参见：https：//medium.com/mindorks/dynamic-feature-modules-the-future-4bee124c0f1
@Suppress("detekt.UnsafeCast")
object FeatureManager {

    private const val featurePackagePrefix = "com.woodnoisu.reader.feature"

    val kodeinModules = BuildConfig.FEATURE_MODULE_NAMES
        .map { "$featurePackagePrefix.$it.FeatureKodeinModule" }
        .map {
            try {
                Class.forName(it).kotlin.objectInstance as KodeinModuleProvider
            } catch (e: ClassNotFoundException) {
                throw ClassNotFoundException("Kodein module class not found $it")
            }
        }
        .map { it.kodeinModule }
}
