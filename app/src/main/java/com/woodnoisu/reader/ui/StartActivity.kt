package com.woodnoisu.reader.ui

import com.woodnoisu.reader.R
import com.woodnoisu.reader.base.BaseActivity
import com.woodnoisu.reader.ui.main.MainActivity
import kotlinx.coroutines.*

class StartActivity: BaseActivity() {
    /**
     * 获取界面id
     */
    override fun getRLayout():Int{
        return R.layout.activity_start
    }

    /**
     * 初始化界面
     */
    override fun initView(){}

    /**
     * 初始化监听
     */
    override fun initListener(){}

    /**
     * 初始化数据
     */
    override fun initData(){
        // 主线程
        GlobalScope.launch(Dispatchers.Main) {
            delay(500)

            MainActivity.startFromActivity(this@StartActivity)

            delay(500)
            //完成
            finish()
        }
    }
}