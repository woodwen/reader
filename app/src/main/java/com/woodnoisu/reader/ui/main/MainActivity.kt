package com.woodnoisu.reader.ui.main

import android.Manifest
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.woodnoisu.reader.R
import com.woodnoisu.reader.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint
class MainActivity : BaseActivity() {
  @VisibleForTesting
  val viewModel: MainViewModel by viewModels()

  /**
   * 获取界面id
   */
  override fun getRLayout():Int{
    return R.layout.activity_main
  }

  /**
   * 初始化界面
   */
  override fun initView(){
    viewPager.adapter =
        MainAdapter(this@MainActivity)
    //申请权限
    requestPermission()
  }

  /**
   * 初始化监听
   */
  override fun initListener() {
    // 设置切换标签事件
    bottom_navigation.setOnNavigationItemSelectedListener {
      when (it.itemId) {
        R.id.navigation_square -> {
          if (viewPager.currentItem != 0) {
            viewPager.currentItem = 0
          }
          return@setOnNavigationItemSelectedListener true
        }
        R.id.navigation_shelf -> {
          if (viewPager.currentItem != 1) {
            viewPager.currentItem = 1
          }
          return@setOnNavigationItemSelectedListener true
        }
        R.id.navigation_me -> {
          if (viewPager.currentItem != 2) {
            viewPager.currentItem = 2
          }
          return@setOnNavigationItemSelectedListener true
        }
      }
      false
    }

    //中间区域注册事件
    viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
      override fun onPageSelected(position: Int) {
        val menuItem = bottom_navigation.menu.getItem(position)
        if (!menuItem.isChecked) {
          menuItem.isChecked = true
        }
      }
    })

    //错误通知事件
    viewModel.toast.observe(this, Observer<String> {
      if (!it.isNullOrBlank()) {
        Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
      }
    })
  }

  /**
   * 初始化数据
   */
  override fun initData(){}

  /**
   * 申请权限
   */
  private fun requestPermission(){
    //权限要求
    val permission = registerForActivityResult(ActivityResultContracts.RequestPermission()){
      if(!it) {
        viewModel.toastMsg("请开通相关权限，否则无法正常使用本应用！")
      }
    }

    //申请权限
    permission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
  }

  /**
   * 静态内容
   */
  companion object {

    fun startFromActivity(activity: Activity) {
      //设置配置
      val options = ActivityOptions.makeSceneTransitionAnimation(activity).toBundle()
      //设置启动窗体
      val intent = Intent(activity, MainActivity::class.java)
      //启动窗体
      activity.startActivity(intent,options)

    }

    fun startFromFragment(activity: FragmentActivity?) {
      //设置配置
      val options = ActivityOptions.makeSceneTransitionAnimation(activity).toBundle()
      //设置启动窗体
      val intent = Intent(activity, MainActivity::class.java)
      //启动窗体
      activity?.startActivity(intent,options)
    }
  }
}
