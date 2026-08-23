package com.woodnoisu.reader.ui.square

import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.woodnoisu.reader.base.BaseViewModel
import com.woodnoisu.reader.model.*
import com.woodnoisu.reader.model.source.SourceOption
import com.woodnoisu.reader.repository.SquareRepository
import com.woodnoisu.reader.utils.LogUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SquareViewModel @Inject constructor(
    private val squareRepository: SquareRepository
) : BaseViewModel() {
    companion object {
        const val NO_SOURCE_MESSAGE = "暂无可用书源，请先到书源管理导入或启用书源"
        const val SEARCH_ONLY_MESSAGE = "当前书源请先输入书名或作者搜索"
    }

    private val searchTypeFetching: MutableLiveData<RequestSearchPageByType> = MutableLiveData()
    val searchType: LiveData<ResponseSearchPageByType>

    private val searchKeyWordFetching: MutableLiveData<RequestSearchPageByKeyword> = MutableLiveData()
    val searchKeyWord: LiveData<ResponseSearchPageByKeyword>

    private val bookInserting: MutableLiveData<BookBean> = MutableLiveData()
    val bookInserted: LiveData<BookBean>

    private val bookInfoFetching: MutableLiveData<RequestBookInfo> = MutableLiveData()
    val bookInfo: LiveData<ResponseBookInfo>

    private val _shopName: MutableLiveData<String> = MutableLiveData()
    private val _shopTitle: MutableLiveData<String> = MutableLiveData()
    private val _shopExplore: MutableLiveData<Boolean> = MutableLiveData()
    private val _book: MutableLiveData<BookBean> = MutableLiveData()
    //private val _remoteBookList: MutableLiveData<ArrayList<BookBean>> = MutableLiveData()
    private val _currentPage: MutableLiveData<Int> = MutableLiveData()
    private val _totalPage: MutableLiveData<Int> = MutableLiveData()
    private val _keyWord: MutableLiveData<String> = MutableLiveData()
    private val _type: MutableLiveData<String> = MutableLiveData()
    private val _squareMessage: MutableLiveData<String?> = MutableLiveData()
    val squareMessage: LiveData<String?> get() = _squareMessage
    private val sourceOptionsFetching: MutableLiveData<Unit> = MutableLiveData()
    val sourceOptions: LiveData<List<SourceOption>>
    private val _sourceOptions: MutableLiveData<List<SourceOption>> = MutableLiveData()

    init {
        LogUtil.i("init SquareViewModel")

        // 初始化当前页
        _currentPage.value = 1
        // 初始化总页数
        _totalPage.value = 1

        // 初始化远程书籍容器
        //_remoteBookList.value = ArrayList()
        _sourceOptions.value = emptyList()

        sourceOptions = sourceOptionsFetching.switchMap {
            launchOnViewModelScope {
                squareRepository.fetchSourceOptions().asLiveData()
            }
        }

        // 根据类型搜索
        searchType = searchTypeFetching.switchMap { request ->
            _isLoading.postValue(true)
            if (request.page == 1) {
                _squareMessage.postValue(null)
            }
            launchOnViewModelScope {
                squareRepository.fetchSearchType(
                    request = request,
                    onSuccess = {
                        _isLoading.postValue(false)
                        _squareMessage.postValue(null)
                    },
                    onError = { message ->
                        _isLoading.postValue(false)
                        if (request.page == 1) {
                            _squareMessage.postValue(message)
                        }
                        _toast.postValue(message)
                    }
                ).asLiveData()
            }
        }

        // 根据关键字搜索
        searchKeyWord = searchKeyWordFetching.switchMap { request ->
            _isLoading.postValue(true)
            if (request.page == 1) {
                _squareMessage.postValue(null)
            }
            launchOnViewModelScope {
                squareRepository.fetchSearchKeyWord(
                    request = request,
                    onSuccess = {
                        _isLoading.postValue(false)
                        _squareMessage.postValue(null)
                    },
                    onError = { message ->
                        _isLoading.postValue(false)
                        if (request.page == 1) {
                            _squareMessage.postValue(message)
                        }
                        _toast.postValue(message)
                    }
                ).asLiveData()
            }
        }

        //填充书籍
        bookInfo = bookInfoFetching.switchMap {
            _isLoading.postValue(true)
            launchOnViewModelScope {
                squareRepository.fetchBookInfo(
                    request = it,
                    onSuccess = {
                        _isLoading.postValue(false)
                    },
                    onError = {
                        _isLoading.postValue(false)
                        _toast.postValue(it)
                    }
                ).asLiveData()
            }
        }

        //新增书籍
        bookInserted = bookInserting.switchMap {
            _isLoading.postValue(true)
            launchOnViewModelScope {
                squareRepository.insertBook(
                    book = it,
                    onSuccess = {
                        _isLoading.postValue(false)
                        _toast.postValue(it)
                    },
                    onError = {
                        _isLoading.postValue(false)
                        _toast.postValue(it)
                    }
                ).asLiveData()
            }
        }
    }

    @MainThread
    fun fetchSearch(page:Int=-1) {
        if (!hasShopName()) {
            showNoSourceMessage()
            return
        }
        var p = 1
        var keyword = ""
        var type = ""
        if (page==-1&&_currentPage.value != null) p = _currentPage.value!!

        if (_keyWord.value != null) keyword = _keyWord.value!!
        if (_type.value != null) type = _type.value!!

        if (keyword.isNotBlank()) {
            fetchSearchKeyWord(keyword, p)
        } else if (!canExplore()) {
            showSearchOnlyMessage(true)
        } else {
            fetchSearchType(type, p)
        }
    }

    @MainThread
    fun fetchSearchType(typeName: String, page: Int) {
        if (!hasShopName()) {
            showNoSourceMessage()
            return
        }
        if (!canExplore()) {
            showSearchOnlyMessage(true)
            return
        }
        _currentPage.value = page
        _keyWord.value = ""
        if (typeName.isNotBlank()) _type.value = typeName
        searchTypeFetching.value = RequestSearchPageByType(
            _shopName.value!!,
            typeName,
            page
        )
    }

    @MainThread
    fun fetchSearchKeyWord(keyword: String, page: Int) {
        if (!hasShopName()) {
            showNoSourceMessage()
            return
        }
        _currentPage.value = page
        if (keyword.isNotBlank()) _keyWord.value = keyword
        searchKeyWordFetching.postValue(
            RequestSearchPageByKeyword(
                _shopName.value!!,
                keyword,
                page
            )
        )
    }

    @MainThread
    fun fetchBookInfo(bookUrl:String){
        if (!hasShopName()) {
            showNoSourceMessage()
            return
        }
        bookInfoFetching.value = RequestBookInfo(_shopName.value!!,bookUrl)
    }

    @MainThread
    fun insertBook() {
        if (_book.value != null) {
            bookInserting.value = _book.value
        }
    }

    @MainThread
    fun getShopName(): String {
        return _shopName.value.orEmpty()
    }

    @MainThread
    fun getShopTitle(): String {
        return _shopTitle.value ?: getShopName().ifBlank { "书城" }
    }

    @MainThread
    fun hasShopName(): Boolean {
        return !_shopName.value.isNullOrBlank()
    }

    @MainThread
    fun getBookBean(): BookBean? {
        return _book.value
    }

    @MainThread
    fun getTotalPage():Int{
        var totalPage = 1
        if(_totalPage.value !=null)
            totalPage = _totalPage.value!!
        return totalPage
    }

    @MainThread
    fun getTypes():List<String>{
        return if (canExplore()) listOf("发现") else listOf("仅搜索")
    }

    @MainThread
    fun getSourceOptionsSnapshot():List<SourceOption>{
        return _sourceOptions.value.orEmpty()
    }

    @MainThread
    fun canExplore(): Boolean = _shopExplore.value == true

    @MainThread
    fun isLastPage():Boolean {
        var current = 1
        var total = 1
        if (_currentPage.value != null) {
            current = _currentPage.value!!
        }
        if (_totalPage.value != null) {
            total = _totalPage.value!!
        }
        return current >= total
    }

    @MainThread
    fun isLoading(): Boolean {
        return if (_isLoading.value != null)
            _isLoading.value!!
        else
            false
    }

    @MainThread
    fun fetchPage(currentPage:Int,totalPage:Int) {
        _currentPage.value = currentPage
        _totalPage.value = totalPage
    }

    @MainThread
    fun fetchShopOption(sourceOption: SourceOption) {
        _shopName.value = sourceOption.key
        _shopTitle.value = sourceOption.name
        _shopExplore.value = sourceOption.canExplore
    }

    @MainThread
    fun selectShopOption(sourceOption: SourceOption) {
        fetchShopOption(sourceOption)
        clearSearchState()
    }

    @MainThread
    fun fetchSourceOptions() {
        sourceOptionsFetching.value = Unit
    }

    @MainThread
    fun updateSourceOptions(options: List<SourceOption>) {
        _sourceOptions.value = options
    }

    @MainThread
    fun selectDefaultSourceIfNeeded(options: List<SourceOption>): Boolean {
        val shopName = _shopName.value
        if (options.isEmpty()) {
            clearShop()
            return true
        }
        val selected = options.firstOrNull { it.key == shopName } ?: options[0]
        val changed = selected.key != shopName ||
            selected.name != _shopTitle.value ||
            selected.canExplore != _shopExplore.value
        fetchShopOption(selected)
        if (changed) {
            clearSearchState()
        }
        if (_squareMessage.value == NO_SOURCE_MESSAGE) {
            _squareMessage.value = null
        }
        return changed
    }

    @MainThread
    fun fetchBook(book: BookBean?){
        if(book!=null){
            _book.postValue(book)
        }
    }

    @MainThread
    fun showNoSourceMessage() {
        _isLoading.value = false
        _squareMessage.value = NO_SOURCE_MESSAGE
    }

    @MainThread
    fun showSearchOnlyMessage(toast: Boolean = false) {
        _isLoading.value = false
        _squareMessage.value = SEARCH_ONLY_MESSAGE
        if (toast) {
            _toast.value = "当前书源请先搜索"
        }
    }

    private fun clearShop() {
        _shopName.value = ""
        _shopTitle.value = "书城"
        _shopExplore.value = false
        clearSearchState()
        showNoSourceMessage()
    }

    private fun clearSearchState() {
        _keyWord.value = ""
        _type.value = ""
        resetPaging()
    }

    private fun resetPaging() {
        _currentPage.value = 1
        _totalPage.value = 1
    }

//    private fun fetchRemoteBookList(bookList: List<BookBean>) {
//        if (!bookList.isNullOrEmpty() && _remoteBookList.value != null) {
//            if(_currentPage.value==1){
//                _remoteBookList.value?.clear()
//            }
//            _remoteBookList.value?.addAll(bookList)
//        }
//    }
}
