package com.woodnoisu.reader.library.base.presentation.activity

import android.os.Bundle
import android.os.PersistableBundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import com.woodnoisu.reader.library.base.BuildConfig
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.KodeinTrigger
import org.kodein.di.android.kodein
import org.kodein.di.android.retainedKodein
import org.kodein.di.generic.kcontext

abstract class InjectionActivity(@LayoutRes contentLayoutId: Int) : AppCompatActivity(contentLayoutId), KodeinAware {

    private val parentKodein by kodein()

    @SuppressWarnings("LeakingThisInConstructor")
    final override val kodeinContext = kcontext<AppCompatActivity>(this)

    // Using retainedKodein will not recreate Kodein when the Activity restarts
    final override val kodein: Kodein by retainedKodein {
        extend(parentKodein)
    }

    /*
    调试版本的依赖关系解析：
     通过定义kodeinTrigger，我们可以热切地检索onCreate方法中的所有依赖项。 这使我们可以确定
     已经正确检索了所有依赖关系（没有未声明的依赖关系，也没有依赖关系
     循环）

     发布版本的依赖关系解析：
     通过不使用kodeinTrigger，所有的依赖关系将被延迟解决。 这样可以节省一些资源并加快速度
     仅在需要/使用依赖项时才检索依赖项。

    More:
    https://github.com/Kodein-Framework/Kodein-DI/blob/master/doc/android.adoc#using-a-trigger
    http://kodein.org/Kodein-DI/?latest/android#_using_a_trigger

     */
    final override val kodeinTrigger: KodeinTrigger? by lazy {
        if (BuildConfig.DEBUG) KodeinTrigger() else super.kodeinTrigger
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        kodeinTrigger?.trigger()
    }
}
