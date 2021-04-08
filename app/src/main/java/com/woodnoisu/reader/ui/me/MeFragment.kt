package com.woodnoisu.reader.ui.me

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import com.woodnoisu.reader.R
import com.woodnoisu.reader.base.BaseFragment
import com.woodnoisu.reader.constant.Constant
import com.woodnoisu.reader.utils.FileUtil
import com.woodnoisu.reader.utils.SpUtil
import kotlinx.android.synthetic.main.fragment_me.*
import java.io.File


/**
 * 我的窗口
 */

class MeFragment : BaseFragment() {
    /**
     * 获取界面id
     */
    override fun getRLayout():Int = R.layout.fragment_me

    /**
     * 初始化界面
     */
    override fun initView(){}

    /**
     * 初始化监听
     */
    override fun initListener(){
        // 音量键控制事件
        switch_volume.setOnCheckedChangeListener { buttonView, isChecked ->
            SpUtil.setBooleanValue("volume_turn_page", isChecked)
        }
        // 清空缓存事件
        clear_cache.setOnClickListener { v ->
            AlertDialog.Builder(activity)
                .setMessage("确定要清除缓存么(将会删除所有已缓存章节)？").setNegativeButton("取消", null)
                .setPositiveButton("确定") { _, _ ->
                    FileUtil.deleteFile(Constant.BOOK_CACHE_PATH)
                    tv_cache.text = "0kb"
                }.show()
        }
        // 个人主页
        tv_about.setOnClickListener {
            // 跳转到作者的github
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/woodwen/reader")
                )
            )
        }

    }

    /**
     * 初始化数据
     */
    override fun initData(){
        // 是否音量键控制翻页
        switch_volume.isChecked = SpUtil.getBooleanValue("volume_turn_page", true)
        // 获取缓存文件大小
        val cacheSize = FileUtil.getDirSize(File(Constant.BOOK_CACHE_PATH)) / 1024
        //初始化缓存文件大小单位
        val unit: String = if (cacheSize in (0..1024)) {
            "kb"
        } else {
            "MB"
        }
        //附值
        tv_cache.text = "$cacheSize$unit"
    }
}