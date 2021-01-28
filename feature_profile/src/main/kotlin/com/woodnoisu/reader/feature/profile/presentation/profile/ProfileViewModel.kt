package com.woodnoisu.reader.feature.profile.presentation.profile

import com.woodnoisu.reader.library.base.presentation.viewmodel.BaseAction
import com.woodnoisu.reader.library.base.presentation.viewmodel.BaseViewModel
import com.woodnoisu.reader.library.base.presentation.viewmodel.BaseViewState
import com.woodnoisu.reader.library.base.utils.Constant
import com.woodnoisu.reader.library.base.utils.FileUtil
import com.woodnoisu.reader.library.base.utils.SpUtil
import java.io.File

internal class ProfileViewModel(
): BaseViewModel<ViewState, Action>(ViewState()) {

    fun getCachesSize():String {
        // 获取缓存文件大小
        val cacheSize = FileUtil.getDirSize(File(Constant.BOOK_CACHE_PATH)) / 1024
        //初始化缓存文件大小单位
        val unit: String = if (cacheSize in (0..1024)) {
            "kb"
        } else {
            "MB"
        }
        return "$cacheSize$unit"
    }

    fun getHome() :String = "https://github.com/woodwen/reader"

    fun clearCaches() {
        FileUtil.deleteFile(Constant.BOOK_CACHE_PATH)
    }

    override fun onReduceState(viewAction: Action) =
        ViewState(isLoading = false, isError = false)
}

internal data class ViewState(
        val isLoading: Boolean = false,
        val isError: Boolean = false
) : BaseViewState

internal sealed class Action : BaseAction