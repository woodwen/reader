package com.woodnoisu.reader.library.base.presentation.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.LayoutRes
import timber.log.Timber

abstract class BaseActivity(@LayoutRes contentLayoutId: Int) : InjectionActivity(contentLayoutId) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        // The window will not be resized when virtual keyboard is shown (bottom navigation bar will be
        // hidden under virtual keyboard)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        Timber.v("onCreate ${javaClass.simpleName}")

        initView()
        initListener()
        initData()
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
