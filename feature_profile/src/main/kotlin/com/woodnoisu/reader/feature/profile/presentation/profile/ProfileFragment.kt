package com.woodnoisu.reader.feature.profile.presentation.profile

import android.content.Intent
import android.net.Uri
import com.zaihui.squirrel.feature.profile.R
import com.woodnoisu.reader.library.base.presentation.fragment.InjectionFragment
import com.woodnoisu.reader.library.base.utils.DialogUtil
import kotlinx.android.synthetic.main.fragment_profile.*
import org.kodein.di.generic.instance

class ProfileFragment : InjectionFragment(R.layout.fragment_profile) {

    private val viewModel: ProfileViewModel by instance()

    /**
     * 初始化监听
     */
    override fun initListener(){
        // 清空缓存事件
        clear_cache.setOnClickListener { _ ->
            DialogUtil.showDialog(
                activity = requireActivity(),
                title = "警告",
                message = "确定要清除缓存么(将会删除所有已缓存章节)？",
                positiveButtonText = "确定",
                negativeButtonText = "取消",
                onPositiveClick = {
                    viewModel.clearCaches()
                    tv_cache.text = "0kb"
                }
            )
        }
        // 个人主页
        tv_about.setOnClickListener {
            // 跳转到作者的github
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(viewModel.getHome())
                )
            )
        }
    }

    /**
     * 初始化数据
     */
    override fun initData(){
        // 缓存文件大小
        tv_cache.text = viewModel.getCachesSize()
    }
}
