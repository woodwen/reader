package com.woodnoisu.reader.ui.shelf

import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.woodnoisu.reader.base.BaseViewModel
import com.woodnoisu.reader.model.BookBean
import com.woodnoisu.reader.model.RequestBookInfo
import com.woodnoisu.reader.model.RequestSearchPageByKeyword
import com.woodnoisu.reader.model.ResponseBookInfo
import com.woodnoisu.reader.model.ResponseSearchPageByKeyword
import com.woodnoisu.reader.repository.ShelfRepository
import com.woodnoisu.reader.utils.LogUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class ShelfViewModel @Inject constructor(
    shelfRepository: ShelfRepository
) : BaseViewModel() {
    private val bookListFetching: MutableLiveData<String> = MutableLiveData()
    val bookList: LiveData<List<BookBean>>

    private val bookInserting: MutableLiveData<BookBean> = MutableLiveData()
    val bookInserted: LiveData<BookBean>

    private val bookDeleting: MutableLiveData<BookBean> = MutableLiveData()
    val bookDeleted: LiveData<BookBean>

    private val remoteSearchFetching: MutableLiveData<RequestSearchPageByKeyword> = MutableLiveData()
    val remoteSearch: LiveData<ResponseSearchPageByKeyword>

    private val bookInfoFetching: MutableLiveData<RequestBookInfo> = MutableLiveData()
    val bookInfo: LiveData<ResponseBookInfo>

    private val _remoteKeyword: MutableLiveData<String> = MutableLiveData()
    private val _remoteBook: MutableLiveData<BookBean> = MutableLiveData()

    init {
        LogUtil.i("init ShelfViewModel")

        //获取本地书籍
        bookList = bookListFetching.switchMap {
            _isLoading.postValue(true)
            launchOnViewModelScope {
                shelfRepository.fetchBookList(
                    keyword = it,
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
                shelfRepository.insertBook(
                    book = it,
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

        //删除书籍
        bookDeleted = bookDeleting.switchMap {
            _isLoading.postValue(true)
            launchOnViewModelScope {
                shelfRepository.deleteBook(
                    book = it,
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

        remoteSearch = remoteSearchFetching.switchMap {
            _isLoading.postValue(true)
            launchOnViewModelScope {
                shelfRepository.fetchRemoteSearch(
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

        bookInfo = bookInfoFetching.switchMap {
            _isLoading.postValue(true)
            launchOnViewModelScope {
                shelfRepository.fetchBookInfo(
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
    }

    @MainThread
    fun insertBook(book: BookBean) {
        bookInserting.value = book
    }

    @MainThread
    fun deleteBook(book: BookBean) {
        bookDeleting.value = book
    }

    @MainThread
    fun fetchBookList(keyword: String) {
        bookListFetching.postValue(keyword)
    }

    @MainThread
    fun fetchRemoteSearch(keyword: String, page: Int = 1) {
        val key = keyword.trim()
        if (key.isBlank()) {
            _toast.value = "关键字不能为空"
            return
        }
        _remoteKeyword.value = key
        remoteSearchFetching.value = RequestSearchPageByKeyword(keyword = key, page = page, shopName = "")
    }

    @MainThread
    fun refreshRemoteSearch(page: Int = 1): Boolean {
        val keyword = _remoteKeyword.value.orEmpty()
        if (keyword.isBlank()) return false
        fetchRemoteSearch(keyword, page)
        return true
    }

    @MainThread
    fun fetchBookInfo(book: BookBean) {
        if (book.shopName.isBlank()) {
            _toast.value = "获取失败，书源为空"
            return
        }
        bookInfoFetching.value = RequestBookInfo(book.shopName, book.url)
    }

    @MainThread
    fun fetchBook(book: BookBean?) {
        if (book != null) {
            _remoteBook.postValue(book)
        }
    }

    @MainThread
    fun getBookBean(): BookBean? {
        return _remoteBook.value
    }
}
