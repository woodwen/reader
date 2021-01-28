package com.woodnoisu.reader.feature.album.presentation.album

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.woodnoisu.reader.feature.album.domain.model.AlbumBookDomainModel
import com.woodnoisu.reader.feature.album.domain.model.AlbumDomainModel
import com.woodnoisu.reader.feature.album.domain.usecase.GetBookListByKeywordUseCase
import com.woodnoisu.reader.feature.album.domain.usecase.GetBookListByTypeUseCase
import com.woodnoisu.reader.feature.album.domain.usecase.GetBookUseCase
import com.woodnoisu.reader.feature.album.domain.usecase.GetUseCase
import com.woodnoisu.reader.feature.album.domain.usecase.InsertBookUseCase
import com.woodnoisu.reader.library.base.presentation.navigation.NavManager
import com.woodnoisu.reader.library.base.presentation.viewmodel.BaseAction
import com.woodnoisu.reader.library.base.presentation.viewmodel.BaseViewModel
import com.woodnoisu.reader.library.base.presentation.viewmodel.BaseViewState
import kotlinx.coroutines.launch

internal class AlbumViewModel (
        private val navManager: NavManager,
        private val getBookListByKeywordUseCase: GetBookListByKeywordUseCase,
        private val getBookListByTypeUseCase: GetBookListByTypeUseCase,
        private val getBookUseCase: GetBookUseCase,
        private val insertBookUseCase: InsertBookUseCase,
        private val getUseCase: GetUseCase
) : BaseViewModel<ViewState, Action>(ViewState()) {
    private var shopName = ""
    private var currentPage = 1
    private var totalPage = 1
    private var keyword = ""
    private var typeName = ""

    fun getShopName() = shopName

    fun setShopName(shopName: String) {
        this.shopName = shopName
    }

    fun setPage(currentPage: Int, totalPage: Int) {
        this.currentPage = currentPage
        this.totalPage = totalPage
    }

    fun isLastPage() = currentPage >= totalPage

    fun getKeywordOrTypeName() = if (typeName.isNotBlank()) typeName else keyword

    /**
     * 获取下一页
     */
    fun getBookList() {
        if (keyword.isNotBlank()) {
            getBookListByKeyWord(keyword, currentPage)
        } else {
            getBookListByType(typeName, currentPage)
        }
    }

    /**
     * 根据类型搜索
     */
    fun getBookListByType(typeName: String, page: Int) {
        this.currentPage = page
        this.keyword = ""
        if (typeName.isNotBlank()) this.typeName = typeName
        viewModelScope.launch {
            getBookListByTypeUseCase.execute(shopName, typeName, page).also { result ->
                val action = when (result) {
                    is GetBookListByTypeUseCase.Result.Success ->
                        if (result.data.albumBookDomainModels.isNullOrEmpty()) {
                            Action.BookListLoadingFailure
                        } else {
                            Action.BookListLoadingSuccess(result.data)
                        }

                    is GetBookListByTypeUseCase.Result.Error ->
                        Action.BookListLoadingFailure
                }
                sendAction(action)
            }
        }
    }

    /**
     * 根据关键字搜索
     */
    fun getBookListByKeyWord(keyword: String, page: Int) {
        this.currentPage = page
        this.typeName = ""
        if (keyword.isNotBlank()) this.keyword = keyword
        viewModelScope.launch {
            getBookListByKeywordUseCase.execute(shopName, keyword, page).also { result ->
                val action = when (result) {
                    is GetBookListByKeywordUseCase.Result.Success ->
                        if (result.data.albumBookDomainModels.isNullOrEmpty()) {
                            Action.BookListLoadingFailure
                        } else {
                            Action.BookListLoadingSuccess(result.data)
                        }

                    is GetBookListByKeywordUseCase.Result.Error ->
                        Action.BookListLoadingFailure
                }
                sendAction(action)
            }
        }
    }

    /**
     * 从本地数据库中搜索书籍
     */
    fun getBook(bookUrl: String) {
        viewModelScope.launch {
            getBookUseCase.execute(shopName, bookUrl).also { result ->
                val action = when (result) {
                    is GetBookUseCase.Result.Success ->
                        if (result.data == null) {
                            Action.BookLoadingFailure
                        } else {
                            Action.BookLoadingSuccess(result.data)
                        }

                    is GetBookUseCase.Result.Error ->
                        Action.BookLoadingFailure
                }
                sendAction(action)
            }
        }
    }

    /**
     * 加入书架
     */
    fun insertBook(albumBook: AlbumBookDomainModel, openBook: Boolean = false) {
        viewModelScope.launch {
            insertBookUseCase.execute(albumBook).also { result ->
                val action = when (result) {
                    is InsertBookUseCase.Result.Success ->
                        Action.InsertedBookSuccess

                    is InsertBookUseCase.Result.Error ->
                        Action.InsertedBookFailure
                }
                sendAction(action)
            }
            if (openBook) {
                //打开书籍
                navigateToReader(albumBook.id.toString())
            }
        }
    }

    /**
     * 获取类型列表
     */
    fun getTypes(): List<String> = getUseCase.getTypes(shopName)

    /**
     * 获取书城列表
     */
    fun getParses(): List<String> = getUseCase.getParses()

    /**
     * 跳转到阅读页
     */
    private fun navigateToReader(bookId: String) {
        val navDirections = AlbumFragmentDirections.actionAlbumToReader(bookId)
        navManager.navigate(navDirections)
    }

    override fun onLoadData() {
        setShopName(getParses()[0])
        getBookListByType(getTypes()[0], 1)
    }

    override fun onReduceState(viewAction: Action) = when (viewAction) {
        is Action.BookListLoadingSuccess -> state.copy(
                isLoading = false,
                errorMsg = "",
                albumDomainModel = viewAction.albumDomainModel
        )
        is Action.BookListLoadingFailure -> state.copy(
                isLoading = false,
                errorMsg = "错误：加载书籍列表失败",
                albumDomainModel = AlbumDomainModel(albumBookDomainModels = listOf())
        )
        is Action.BookLoadingSuccess -> state.copy(
                isLoading = false,
                errorMsg = "",
                albumDomainModel = viewAction.albumDomainModel
        )
        is Action.BookLoadingFailure -> state.copy(
                isLoading = false,
                errorMsg = "错误：加载书籍失败",
                albumDomainModel = AlbumDomainModel()
        )
        is Action.InsertedBookSuccess -> state.copy(
                isLoading = false,
                errorMsg = "",
                albumDomainModel = AlbumDomainModel()
        )
        is Action.InsertedBookFailure -> state.copy(
                isLoading = false,
                errorMsg = "错误：书架加入书籍失败",
                albumDomainModel = AlbumDomainModel()
        )
    }
}

internal data class ViewState(
        val isLoading: Boolean = true,
        val errorMsg: String = "",
        val albumDomainModel: AlbumDomainModel = AlbumDomainModel()
) : BaseViewState

internal sealed class Action : BaseAction {
    class BookListLoadingSuccess(val albumDomainModel: AlbumDomainModel) : Action()
    object BookListLoadingFailure : Action()

    class BookLoadingSuccess(val albumDomainModel: AlbumDomainModel) : Action()
    object BookLoadingFailure : Action()

    object InsertedBookSuccess : Action()
    object InsertedBookFailure : Action()
}