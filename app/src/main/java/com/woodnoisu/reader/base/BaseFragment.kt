package com.woodnoisu.reader.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.afollestad.materialdialogs.MaterialDialog
import com.woodnoisu.reader.utils.LogUtil
import com.woodnoisu.reader.utils.UtilDialog

abstract class BaseFragment: Fragment() {
    // 标签
    var TAG: String = lazy { this.toString() }.value

    // 默认状态
    private val LOADING_TITLE_DEFAULT = "正在加载"

    // 子容器
    private var mFragmentList: MutableList<Fragment> = mutableListOf()

    // 生命周期相关
    private val fragmentLifecycleCallback = object : FragmentManager.FragmentLifecycleCallbacks() {

        override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
            LogUtil.i(TAG, "fragmentLifecycleCallback onFragmentAttached", fm, f)
        }

        override fun onFragmentCreated(
            fm: FragmentManager,
            f: Fragment,
            savedInstanceState: Bundle?
        ) {
            LogUtil.i(TAG, "fragmentLifecycleCallback onFragmentCreated", fm, f)
            mFragmentList.add(f)
        }

        override fun onFragmentViewCreated(
            fm: FragmentManager,
            f: Fragment,
            v: View,
            savedInstanceState: Bundle?
        ) {
            LogUtil.i(TAG, "fragmentLifecycleCallback onFragmentViewCreated", fm, f)
        }

        override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
            LogUtil.i(TAG, "fragmentLifecycleCallback onFragmentDestroyed", fm, f)
            mFragmentList.remove(f)
        }

        override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
            LogUtil.i(TAG, "fragmentLifecycleCallback onFragmentDetached", fm, f)
        }
    }

    // 进度条弹出框
    private var mProgressDialog: MaterialDialog? = null

//    // 网络弹出框
//    private var mNetworkDialog: MaterialDialog? = null
//
//    // 打印弹出看
//    private var mPrintErrorDialog: MaterialDialog? = null

    /**
     * 初始化事件1
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogUtil.i(TAG, "onCreate", savedInstanceState)
        childFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallback, false)
    }

    /**
     * 初始化事件2
     */
    override fun onCreateView(
        inflater: LayoutInflater
        , container: ViewGroup?
        , savedInstanceState: Bundle?
    ): View? {
        LogUtil.i(TAG, "onCreateView", inflater, container, savedInstanceState)
        return layoutInflater.inflate(getRLayout(), container, false)
    }

    /**
     * 创建完成事件3
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LogUtil.i(TAG, "onViewCreated", view, savedInstanceState)
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
        //mCompositeDisposables.clear()
        mProgressDialog?.dismiss()
//        mNetworkDialog?.dismiss()
//        mPrintErrorDialog?.dismiss()
    }

    /**
     * 获取界面id
     */
    open fun getRLayout(): Int {
        return 0
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

    /**
     * activity的onBackPressed被调用时，会遍历所有子fragment，并执行子fragment的onFragmentBackPressed()方法
     * 如果fragment想拦截事件，需要返回true
     */
    open fun onFragmentBackPressed(): Boolean {
        LogUtil.i(TAG, "onFragmentBackPressed", mFragmentList)
        val fragments = mFragmentList.reversed()
        val consumer = fragments.firstOrNull {
            (it as? BaseFragment)?.onFragmentBackPressed() ?: false
        }
        LogUtil.i(TAG, "onFragmentBackPressed", consumer)
        if (consumer == null) return false
        return true
    }

    /**
     * 显示/隐藏进度条
     */
    fun setLoading(isLoading: Boolean, msg: String? = null, title: String? = null) {
        val ctx = context ?: return
        val newTitle = title ?: "正在加载"
        var dialog = mProgressDialog
        if (!isLoading) {
            mProgressDialog?.dismiss()
            mProgressDialog = null
        } else if (dialog == null) {
            dialog = UtilDialog.showDialog(
                context = ctx,
                title = newTitle,
                message = msg,
                customView = ProgressBar(ctx)
            )
            mProgressDialog = dialog
        } else {
            dialog.title(text = newTitle)
            dialog.show()
        }
    }
}