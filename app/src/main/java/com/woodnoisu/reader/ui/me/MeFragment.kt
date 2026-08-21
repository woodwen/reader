package com.woodnoisu.reader.ui.me

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import com.woodnoisu.reader.R
import com.woodnoisu.reader.base.BaseFragment
import com.woodnoisu.reader.constant.Constant
import com.woodnoisu.reader.databinding.FragmentMeBinding
import com.woodnoisu.reader.ui.source.BookSourceActivity
import com.woodnoisu.reader.utils.FileUtil
import com.woodnoisu.reader.utils.SpUtil
import java.io.File


/**
 * 我的窗口
 */

class MeFragment : BaseFragment() {
    private var _binding: FragmentMeBinding? = null
    private val binding get() = _binding!!

    /**
     * 获取界面id
     */
    override fun getRLayout():Int = R.layout.fragment_me

    /**
     * 初始化界面
     */
    override fun initView(){
        _binding = FragmentMeBinding.bind(requireView())
    }

    /**
     * 初始化监听
     */
    override fun initListener(){
        // 音量键控制事件
        binding.switchVolume.setOnCheckedChangeListener { buttonView, isChecked ->
            SpUtil.setBooleanValue("volume_turn_page", isChecked)
        }
        // 清空缓存事件
        binding.clearCache.setOnClickListener { v ->
            AlertDialog.Builder(activity)
                .setMessage("确定要清除缓存么(将会删除所有已缓存章节)？").setNegativeButton("取消", null)
                .setPositiveButton("确定") { _, _ ->
                    FileUtil.deleteFile(Constant.BOOK_CACHE_PATH)
                    binding.tvCache.text = "0kb"
                }.show()
        }
        binding.tvBookSource.setOnClickListener {
            startActivity(Intent(requireContext(), BookSourceActivity::class.java))
        }
        // 个人主页
        binding.tvAbout.setOnClickListener {
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
        binding.switchVolume.isChecked = SpUtil.getBooleanValue("volume_turn_page", true)
        // 获取缓存文件大小
        val cacheSize = FileUtil.getDirSize(File(Constant.BOOK_CACHE_PATH)) / 1024
        //初始化缓存文件大小单位
        val unit: String = if (cacheSize in (0..1024)) {
            "kb"
        } else {
            "MB"
        }
        //附值
        binding.tvCache.text = "$cacheSize$unit"
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
