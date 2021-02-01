package com.woodnoisu.reader.base

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.ProgressBar
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.woodnoisu.reader.R
import com.woodnoisu.reader.utils.LogUtil
import com.woodnoisu.reader.utils.StatusBarUtil
import com.woodnoisu.reader.utils.UtilDialog


abstract class BaseActivity : AppCompatActivity() {
  // 标签
  protected var TAG: String = lazy { this.javaClass.simpleName }.value

  // 进度框
  private var mProgressDialog: MaterialDialog? = null

  // 容器
  private var mFragmentList: MutableList<Fragment> = mutableListOf()

  /**
   * 初始化事件
   */
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    LogUtil.i(TAG, "onCreate", savedInstanceState)
    setContentView(getRLayout())
    StatusBarUtil.setWindowStatusBarColor(this, R.color.main_color)

    initView()
    initListener()
    initData()
  }

  /**
   * 恢复保存状态
   */
  override fun onRestoreInstanceState(savedInstanceState: Bundle) {
    super.onRestoreInstanceState(savedInstanceState)
    LogUtil.i(TAG, "onRestoreInstanceState1", savedInstanceState)
  }

  /**
   * 恢复保存状态
   */
  override fun onRestoreInstanceState(
    savedInstanceState: Bundle?,
    persistentState: PersistableBundle?
  ) {
    super.onRestoreInstanceState(savedInstanceState, persistentState)
    LogUtil.i(TAG, "onRestoreInstanceState2", savedInstanceState, persistentState)
  }

  /**
   * 活动启动完成时调用
   */
  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    LogUtil.i(TAG, "onPostCreate1", savedInstanceState)
  }

  /**
   * 活动启动完成时调用
   */
  override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
    super.onPostCreate(savedInstanceState, persistentState)
    LogUtil.i(TAG, "onPostCreate2", savedInstanceState, persistentState)
  }

  /**
   * 设置主容器
   */
  override fun setContentView(@LayoutRes layoutResID: Int) {
    super.setContentView(layoutResID)
    LogUtil.i(TAG, "setContentView", layoutResID)
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
   * 存储状态
   */
  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    LogUtil.i(TAG, "onSaveInstanceState1", outState)
  }

  /**
   * 存储状态
   */
  override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
    super.onSaveInstanceState(outState, outPersistentState)
    LogUtil.i(TAG, "onSaveInstanceState2", outState, outPersistentState)
  }

  /**
   * 销毁
   */
  override fun onDestroy() {
    super.onDestroy()
    LogUtil.i(TAG, "onDestroy")
    mProgressDialog?.dismiss()
  }

  /**
   * 按照attach顺序从后往前遍历所有子fragment，调用子fragment的onFragmentBackPressed，直到某个子fragment返回true
   * 如果子fragment都没有消费掉回退事件，则调用activity默认的回退方法
   */
  override fun onBackPressed() {
    LogUtil.i(TAG, "onBackPressed", mFragmentList)
    val fragments = mFragmentList.reversed()
    val consumer = fragments.firstOrNull {
      (it as? BaseFragment)?.onFragmentBackPressed() ?: false
    }
    LogUtil.i(TAG, "onBackPressed fragments = $fragments , consumer = $consumer")
    if (consumer == null) super.onBackPressed()
  }

  open fun getRLayout(): Int {
    return 0
  }

  open fun initView() {}
  open fun initListener() {}
  open fun initData() {}

  /**
   * 显示/隐藏进度条
   */
  fun setLoading(isLoading: Boolean, msg:String?=null,title: String? = null) {
    val ctx = this
    val newTitle = title ?: "正在加载"
    var dialog = mProgressDialog
    if (!isLoading) {
      mProgressDialog?.dismiss()
      mProgressDialog = null
    } else if (dialog == null) {
      dialog = UtilDialog.showDialog(context = ctx, title = newTitle, message = msg, customView = ProgressBar(ctx))
      mProgressDialog = dialog
    } else {
      dialog.title(text = newTitle)
      dialog.show()
    }
  }

  //  protected inline fun <reified T : ViewDataBinding> binding(
//    @LayoutRes resId: Int
//  ): Lazy<T> = lazy { DataBindingUtil.setContentView<T>(this, resId) }
}
