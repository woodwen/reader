package com.woodnoisu.reader.feature.reader.presentation.reader

import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.woodnoisu.reader.feature.reader.domain.model.*
import com.woodnoisu.reader.feature.reader.domain.model.ReaderBookDomainModel
import com.woodnoisu.reader.feature.reader.domain.model.ReaderBookSignDomainModel
import com.woodnoisu.reader.feature.reader.domain.model.ReaderChapterDomainModel
import com.woodnoisu.reader.feature.reader.domain.model.ReaderDomainModel
import com.woodnoisu.reader.feature.reader.domain.model.ReaderRecordDomainModel
import com.woodnoisu.reader.feature.reader.domain.usecase.*
import com.woodnoisu.reader.feature.reader.domain.usecase.AddSignUseCase
import com.woodnoisu.reader.feature.reader.domain.usecase.DeleteSignUseCase
import com.woodnoisu.reader.feature.reader.domain.usecase.GetBookRecordUseCase
import com.woodnoisu.reader.feature.reader.domain.usecase.GetBookUseCase
import com.woodnoisu.reader.feature.reader.domain.usecase.GetChapterContentsUseCase
import com.woodnoisu.reader.feature.reader.presentation.page.ReadSettingManager
import com.woodnoisu.reader.library.base.model.*
import com.woodnoisu.reader.library.base.presentation.navigation.NavManager
import com.woodnoisu.reader.library.base.presentation.viewmodel.BaseAction
import com.woodnoisu.reader.library.base.presentation.viewmodel.BaseViewModel
import com.woodnoisu.reader.library.base.presentation.viewmodel.BaseViewState
import com.woodnoisu.reader.library.base.utils.Constant
import com.woodnoisu.reader.library.base.utils.SpUtil
import com.woodnoisu.reader.library.base.utils.*
import kotlinx.coroutines.launch

internal class ReaderViewModel(
        private val navManager: NavManager,
        private val addSignUseCase: AddSignUseCase,
        private val deleteSignUseCase: DeleteSignUseCase,
        private val getBookRecordUseCase: GetBookRecordUseCase,
        private val getBookUseCase: GetBookUseCase,
        private val getChapterContentsUseCase: GetChapterContentsUseCase,
        private val getChaptersUseCase: GetChaptersUseCase,
        private val getSignsUseCase: GetSignsUseCase,
        private val saveBookRecordUseCase: SaveBookRecordUseCase,
        private val args: ReaderFragmentArgs
): BaseViewModel<ViewState, Action>(ViewState()) {

    // 当前章节
    private var _cChapter:ReaderChapterDomainModel?=null

    // 当前章节索引
    private var _cChapterIndex: Int = 0

    // 开始的章节
    private var _chapterStart: Int = 0

    // 当前模式
    private var _nowMode: Boolean = SpUtil.getBooleanValue(Constant.NIGHT)

    // 当前书籍
    private lateinit var _collBook: ReaderBookDomainModel

    fun setCollBook(book: ReaderBookDomainModel) {
        _collBook = book
    }

    fun moveBackChapterStart(amount: Int) {
        val start = getChapterStart()
        _chapterStart = start + amount
    }

    fun compareNowMode(): Boolean {
        val d = SpUtil.getBooleanValue(Constant.NIGHT) != _nowMode
        _nowMode = SpUtil.getBooleanValue(Constant.NIGHT)
        return d
    }

    fun setCurrentChapter(index: Int, bean: ReaderChapterDomainModel) {
        _cChapterIndex = index
        _cChapter = bean
    }

    fun negateIsNightMode():Boolean {
        val isNightMode = getIsNightMode()
        ReadSettingManager.getInstance().isNightMode = !isNightMode
        return getIsNightMode()
    }

    fun getCollBook() = _collBook

    fun getChapterStart() = _chapterStart

    fun getIsNightMode() = ReadSettingManager.getInstance().isNightMode

    fun getIsFullScreen() = ReadSettingManager.getInstance().isFullScreen

    /**
     * 获取书籍列表
     */
    private fun getBook(bookId: String) {
        viewModelScope.launch {
            getBookUseCase.execute(bookId).also { result ->
                val action = when (result) {
                    is GetBookUseCase.Result.Success ->
                        Action.ReaderBookLoadSuccess(result.data)

                    is GetBookUseCase.Result.Error ->
                        Action.ReaderBookLoadFailure
                }
                sendAction(action)
            }
        }
    }

    /**
     * 获取章节内容（多）
     */
    fun getChapterContents(chapters: List<ReaderChapterDomainModel>){
        viewModelScope.launch {
            getChapterContentsUseCase.execute(chapters).also { result ->
                val action = when (result) {
                    is GetChapterContentsUseCase.Result.Success ->
                        Action.ReaderChapterContentsLoadSuccess(result.data)

                    is GetChapterContentsUseCase.Result.Error ->
                        Action.ReaderChapterContentsLoadFailure
                }
                sendAction(action)
            }
        }
    }

    /**
     * 获取章节列表
     */
    fun getChapters(readerBookDomainModel: ReaderBookDomainModel,
                    start: Int,
                    limit:Int=100,
                    cacheContents:Boolean=false){
        var index = start
        if (cacheContents && start < -1) {
            //如果缓存，且开始设置为-1的话 就用当前章节索引
            index = _cChapterIndex
        }
        viewModelScope.launch {
            getChaptersUseCase.execute(readerBookDomainModel,
                                        index,
                                       limit,
                                       cacheContents).also { result ->
                val action = when (result) {
                    is GetChaptersUseCase.Result.Success ->
                        Action.ReaderChaptersLoadSuccess(result.data)

                    is GetChaptersUseCase.Result.Error ->
                        Action.ReaderChaptersLoadFailure
                }
                sendAction(action)
            }
        }
    }
    /**
     * 获取阅读记录
     */
    fun getBookRecord(){
        val bookUrl = getCollBook().url
        viewModelScope.launch {
            getBookRecordUseCase.execute(bookUrl).also { result ->
                val action = when (result) {
                    is GetBookRecordUseCase.Result.Success ->
                        Action.ReaderBookRecordLoadSuccess(result.data)

                    is GetBookRecordUseCase.Result.Error ->
                        Action.ReaderBookRecordLoadFailure
                }
                sendAction(action)
            }
        }
    }

    /**
     * 获取书签列表
     */
    fun getSigns(){
        val bookUrl = getCollBook().url
        viewModelScope.launch {
            getSignsUseCase.execute(bookUrl).also { result ->
                val action = when (result) {
                    is GetSignsUseCase.Result.Success ->
                        Action.ReaderSignsLoadSuccess(result.data)

                    is GetSignsUseCase.Result.Error ->
                        Action.ReaderSignsLoadFailure
                }
                sendAction(action)
            }
        }
    }

    /**
     * 保存阅读记录
     */
    fun saveBookRecord(readerRecordDomainModel: ReaderRecordDomainModel){
        viewModelScope.launch {
            saveBookRecordUseCase.execute(readerRecordDomainModel).also { result ->
                val action = when (result) {
                    is SaveBookRecordUseCase.Result.Success ->
                        Action.ReaderLoadSuccess

                    is SaveBookRecordUseCase.Result.Error ->
                        Action.ReaderLoadFailure
                }
                sendAction(action)
            }
        }
    }

    /**
     * 添加书签
     */
    fun addSign(){
        val chapterUrl = _cChapter?.url ?: ""
        val chapterName = _cChapter?.name ?: ""
        val bookUrl = getCollBook().url
        viewModelScope.launch {
            addSignUseCase.execute(bookUrl,chapterUrl,chapterName).also { result ->
                val action = when (result) {
                    is AddSignUseCase.Result.Success ->
                        Action.ReaderLoadSuccess

                    is AddSignUseCase.Result.Error ->
                        Action.ReaderLoadFailure
                }
                sendAction(action)
            }
        }
    }

    /**
     * 删除书签
     */
    fun deleteSigns(readerBookSignDomainModels: List<ReaderBookSignDomainModel>){
        viewModelScope.launch {
            deleteSignUseCase.execute(readerBookSignDomainModels).also { result ->
                val action = when (result) {
                    is DeleteSignUseCase.Result.Success ->
                        Action.ReaderLoadSuccess

                    is DeleteSignUseCase.Result.Error ->
                        Action.ReaderLoadFailure
                }
                sendAction(action)
            }
        }
    }

    /**
     * 显示/隐藏工具栏
     */
    fun navigateShow(showBottom: Boolean) {
        navManager.bottomShow(showBottom)
    }

    /**
     * 回退
     */
    fun navigateToBack() {
        navManager.popBack()
    }

    override fun onLoadData() {
        getBook(args.bookId)
    }

    override fun onReduceState(viewAction: Action) = when (viewAction) {
        is Action.ReaderBookRecordLoadSuccess -> state.copy(
                isLoading = false,
                errorMsg = "",
                readerDomainModel = viewAction.readerDomainModel
        )
        is Action.ReaderBookRecordLoadFailure -> state.copy(
                isLoading = false,
                errorMsg = "错误：加载阅读记录失败",
                readerDomainModel = ReaderDomainModel(readerRecordDomainModel=ReaderRecordDomainModel())
        )
        is Action.ReaderBookLoadSuccess -> state.copy(
                isLoading = false,
                errorMsg = "",
                readerDomainModel = viewAction.readerDomainModel
        )
        is Action.ReaderBookLoadFailure -> state.copy(
                isLoading = false,
                errorMsg = "错误：加载书籍失败",
                readerDomainModel = ReaderDomainModel()
        )
        is Action.ReaderChapterContentsLoadSuccess -> state.copy(
                isLoading = false,
                errorMsg = "",
                readerDomainModel = viewAction.readerDomainModel
        )
        is Action.ReaderChapterContentsLoadFailure -> state.copy(
                isLoading = false,
                errorMsg = "错误：加载章节内容列表失败",
                readerDomainModel = ReaderDomainModel(chapterContentsErr = true)
        )
        is Action.ReaderChaptersLoadSuccess -> state.copy(
                isLoading = false,
                errorMsg = "",
                readerDomainModel = viewAction.readerDomainModel
        )
        is Action.ReaderChaptersLoadFailure -> state.copy(
                isLoading = false,
                errorMsg = "错误：加载章节列表失败",
                readerDomainModel = ReaderDomainModel()
        )
        is Action.ReaderSignsLoadSuccess -> state.copy(
                isLoading = false,
                errorMsg = "",
                readerDomainModel = viewAction.readerDomainModel
        )
        is Action.ReaderSignsLoadFailure -> state.copy(
                isLoading = false,
                errorMsg = "错误：加载书签列表失败",
                readerDomainModel = ReaderDomainModel()
        )
        is Action.ReaderLoadSuccess -> state.copy(
                isLoading = false,
                errorMsg = "",
                readerDomainModel = ReaderDomainModel()
        )
        is Action.ReaderLoadFailure -> state.copy(
                isLoading = false,
                errorMsg = "错误：保存阅读记录/添加书签/删除书签，失败",
                readerDomainModel = ReaderDomainModel()
        )
    }
}

internal data class ViewState(
        val isLoading: Boolean = true,
        val errorMsg: String = "",
        val readerDomainModel: ReaderDomainModel = ReaderDomainModel()
) : BaseViewState

internal sealed class Action : BaseAction {
    class ReaderBookRecordLoadSuccess(val readerDomainModel: ReaderDomainModel) : Action()
    object ReaderBookRecordLoadFailure : Action()

    class ReaderBookLoadSuccess(val readerDomainModel: ReaderDomainModel) : Action()
    object ReaderBookLoadFailure : Action()

    class ReaderChapterContentsLoadSuccess(val readerDomainModel: ReaderDomainModel) : Action()
    object ReaderChapterContentsLoadFailure : Action()

    class ReaderChaptersLoadSuccess(val readerDomainModel: ReaderDomainModel) : Action()
    object ReaderChaptersLoadFailure : Action()

    class ReaderSignsLoadSuccess(val readerDomainModel: ReaderDomainModel) : Action()
    object ReaderSignsLoadFailure : Action()

    object ReaderLoadSuccess: Action()
    object ReaderLoadFailure : Action()
}