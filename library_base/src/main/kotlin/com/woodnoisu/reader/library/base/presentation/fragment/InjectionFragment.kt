package com.woodnoisu.reader.library.base.presentation.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.woodnoisu.reader.library.base.BuildConfig
import com.woodnoisu.reader.library.base.utils.LogUtil
import org.kodein.di.KodeinAware
import org.kodein.di.KodeinTrigger
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.kcontext

/*
 * 请参阅InjectionActivity类中的描述
 */
abstract class InjectionFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId), KodeinAware {
    // 标签
    var TAG: String = lazy { this.toString() }.value

    @SuppressWarnings("LeakingThisInConstructor")
    final override val kodeinContext = kcontext<Fragment>(this)

    final override val kodein by kodein()

    final override val kodeinTrigger: KodeinTrigger? by lazy {
        if (BuildConfig.DEBUG) KodeinTrigger() else super.kodeinTrigger
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        kodeinTrigger?.trigger()
        LogUtil.i(TAG, "onViewCreated")
        initView()
        initListener()
        initData()
    }
    /**
     * 恢复
     */
    override fun onResume() {
        super.onResume()
        LogUtil.i(TAG, "onResume")
    }

    /**
     * 保存状态
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        LogUtil.i(TAG, "onSaveInstanceState", outState)
    }

    /**
     * 暂停
     */
    override fun onPause() {
        super.onPause()
        LogUtil.i(TAG, "onPause")
    }

    /**
     * 停止
     */
    override fun onStop() {
        super.onStop()
        LogUtil.i(TAG, "onStop")
    }

    /**
     * 销毁
     */
    override fun onDestroy() {
        super.onDestroy()
        LogUtil.i(TAG, "onDestroy")
    }

    /**
     * 初始化界面
     */
    open fun initView() {}

    /**
     * 初始化监听
     */
    open fun initListener() {}

    /**
     * 初始化数据
     */
    open fun initData() {}
}
