package com.woodnoisu.reader.feature.favourite.presentation.favourite

import androidx.lifecycle.viewModelScope
import com.woodnoisu.reader.feature.favourite.domain.model.FavouriteBookDomainModel
import com.woodnoisu.reader.feature.favourite.domain.model.FavouriteDomainModel
import com.woodnoisu.reader.feature.favourite.domain.usecase.DeleteBookUseCase
import com.woodnoisu.reader.feature.favourite.domain.usecase.GetBookListUseCase
import com.woodnoisu.reader.feature.favourite.domain.usecase.InsertBookUseCase
import com.woodnoisu.reader.library.base.presentation.navigation.NavManager
import com.woodnoisu.reader.library.base.presentation.viewmodel.BaseAction
import com.woodnoisu.reader.library.base.presentation.viewmodel.BaseViewModel
import com.woodnoisu.reader.library.base.presentation.viewmodel.BaseViewState
import kotlinx.coroutines.launch

internal class FavouriteViewModel (
    private val navManager: NavManager,
    private val getBookListUseCase: GetBookListUseCase,
    private val insertBookUseCase: InsertBookUseCase,
    private val deleteBookUseCase: DeleteBookUseCase
) : BaseViewModel<ViewState, Action>(ViewState()) {
    /**
     * 获取书籍列表
     */
    fun getBookList(keyword: String) {
        viewModelScope.launch {
            getBookListUseCase.execute(keyword).also { result ->
                val action = when (result) {
                    is GetBookListUseCase.Result.Success ->
                        Action.BookListLoadingSuccess(result.data)

                    is GetBookListUseCase.Result.Error ->
                        Action.BookListLoadingFailure
                }
                sendAction(action)
            }
        }
    }

    /**
     * 添加书籍入书架
     */
    fun insertBook(favouriteBookDomainModel: FavouriteBookDomainModel) {
        viewModelScope.launch {
            insertBookUseCase.execute(favouriteBookDomainModel).also { result ->
                val action = when (result) {
                    is InsertBookUseCase.Result.Success ->
                        Action.InsertedBookSuccess

                    is InsertBookUseCase.Result.Error ->
                        Action.InsertedBookFailure
                }
                sendAction(action)
            }
        }
    }

    /**
     * 删除书架书籍
     */
    fun deleteBook(favouriteBookDomainModel: FavouriteBookDomainModel) {
        viewModelScope.launch {
            deleteBookUseCase.execute(favouriteBookDomainModel).also { result ->
                val action = when (result) {
                    is DeleteBookUseCase.Result.Success ->
                        Action.DeletedBookSuccess

                    is DeleteBookUseCase.Result.Error ->
                        Action.DeletedBookFailure
                }
                sendAction(action)
            }
        }
    }

    /**
     * 跳转到阅读页
     */
    fun navigateToReader(bookId:String) {
        val navDirections = FavouriteFragmentDirections.actionFavoriteToReader(bookId)
        navManager.navigate(navDirections)
    }

    override fun onLoadData() {
        getBookList("")
    }

    override fun onReduceState(viewAction: Action) = when (viewAction) {

        is Action.BookListLoadingSuccess -> state.copy(
            isLoading = false,
            errorMsg = "",
            favouriteDomainModel = viewAction.favouriteDomainModel
        )
        is Action.BookListLoadingFailure -> state.copy(
            isLoading = false,
            errorMsg = "错误：加载书籍列表失败",
            favouriteDomainModel = FavouriteDomainModel()
        )

        is Action.InsertedBookSuccess -> state.copy(
            isLoading = false,
            errorMsg = ""
        )
        is Action.InsertedBookFailure -> state.copy(
            isLoading = false,
            errorMsg = "错误：书架加入书籍失败"
        )

        is Action.DeletedBookSuccess -> state.copy(
            isLoading = false,
            errorMsg = ""
        )
        is Action.DeletedBookFailure -> state.copy(
            isLoading = false,
            errorMsg = "错误：书架删除书籍失败"
        )
    }
}

internal data class ViewState(
        val isLoading: Boolean = true,
        val errorMsg: String = "",
        val favouriteDomainModel: FavouriteDomainModel = FavouriteDomainModel()
) : BaseViewState

internal sealed class Action : BaseAction {
    class BookListLoadingSuccess(val favouriteDomainModel: FavouriteDomainModel) : Action()
    object BookListLoadingFailure : Action()

    object InsertedBookSuccess : Action()
    object InsertedBookFailure : Action()

    object DeletedBookSuccess : Action()
    object DeletedBookFailure : Action()
}