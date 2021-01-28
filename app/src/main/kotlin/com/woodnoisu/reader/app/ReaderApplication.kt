package com.woodnoisu.reader.app

import android.content.Context
import com.facebook.stetho.Stetho
import com.google.android.play.core.splitcompat.SplitCompatApplication
import com.woodnoisu.reader.BuildConfig
import com.woodnoisu.reader.app.feature.FeatureManager
import com.woodnoisu.reader.app.kodein.FragmentArgsExternalSource
import com.woodnoisu.reader.appModule
import com.woodnoisu.reader.library.base.baseModule
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import timber.log.Timber

/*
 * 在AndroidManifest.xml文件中引用的自定义Android应用程序类的误报“未使用符号”:
 * https://youtrack.jetbrains.net/issue/KT-27971
 */
class ReaderApplication : SplitCompatApplication(), KodeinAware {
    override val kodein = Kodein.lazy {
        import(androidXModule(this@ReaderApplication))
        import(baseModule)
        import(appModule)
        importAll(FeatureManager.kodeinModules)

        externalSources.add(FragmentArgsExternalSource())
    }

    private lateinit var context: Context

    override fun onCreate() {
        super.onCreate()

        context = this

        initTimber()
        initStetho()
    }

    private fun initStetho() {
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
        }
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
