package com.woodnoisu.reader.ui.square

import android.graphics.drawable.Drawable
import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.woodnoisu.reader.base.BaseViewModel
import com.woodnoisu.reader.model.*
import com.woodnoisu.reader.repository.SquareRepository
import com.woodnoisu.reader.utils.LogUtil
import dagger.assisted.AssistedInject

class SquareViewModel @AssistedInject constructor(
    private val squareRepository: SquareRepository
) : BaseViewModel() {
    private val searchTypeFetching: MutableLiveData<RequestSearchPageByType> = MutableLiveData()
    val searchType: LiveData<ResponseSearchPageByType>

    private val searchKeyWordFetching: MutableLiveData<RequestSearchPageByKeyword> = MutableLiveData()
    val searchKeyWord: LiveData<ResponseSearchPageByKeyword>

    private val bookInserting: MutableLiveData<BookBean> = MutableLiveData()
    val bookInserted: LiveData<BookBean>

    private val bookInfoFetching: MutableLiveData<RequestBookInfo> = MutableLiveData()
    val bookInfo: LiveData<ResponseBookInfo>

    private val _shopName: MutableLiveData<String> = MutableLiveData()
    private val _book: MutableLiveData<BookBean> = MutableLiveData()
    //private val _remoteBookList: MutableLiveData<ArrayList<BookBean>> = MutableLiveData()
    private val _currentPage: MutableLiveData<Int> = MutableLiveData()
    private val _totalPage: MutableLiveData<Int> = MutableLiveData()
    private val _keyWord: MutableLiveData<String> = MutableLiveData()
    private val _type: MutableLiveData<String> = MutableLiveData()

    init {
        LogUtil.i("init SquareViewModel")

        // 初始化当前页
        _currentPage.value = 1
        // 初始化总页数
        _totalPage.value = 1

        // 初始化远程书籍容器
        //_remoteBookList.value = ArrayList()

        // 根据类型搜索
        searchType = searchTypeFetching.switchMap {
            _isLoading.postValue(true)
            launchOnViewModelScope {
                squareRepository.fetchSearchType(
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

        // 根据关键字搜索
        searchKeyWord = searchKeyWordFetching.switchMap {
            _isLoading.postValue(true)
            launchOnViewModelScope {
                squareRepository.fetchSearchKeyWord(
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

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(): SquareViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create() as T
            }
        }
    }

    @MainThread
    fun fetchSearch(page:Int=-1) {
        var p = 1
        var keyword = ""
        var type = ""
        if (page==-1&&_currentPage.value != null) p = _currentPage.value!!

        if (_keyWord.value != null) keyword = _keyWord.value!!
        if (_type.value != null) type = _type.value!!

        if (keyword.isNotBlank()) {
            fetchSearchKeyWord(keyword, p)
        } else {
            fetchSearchType(type, p)
        }
    }

    @MainThread
    fun fetchSearchType(typeName: String, page: Int) {
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
        return _shopName.value!!
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
        return squareRepository.getTypes(getShopName())
    }

    @MainThread
    fun getParses():List<String>{
        return squareRepository.getParses()
    }

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
    fun fetchShopName(shopName:String){
        _shopName.value=shopName
    }

    @MainThread
    fun fetchBook(book: BookBean?){
        if(book!=null){
            _book.postValue(book)
        }
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