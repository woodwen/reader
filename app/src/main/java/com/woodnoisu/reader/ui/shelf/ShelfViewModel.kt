package com.woodnoisu.reader.ui.shelf

import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.woodnoisu.reader.base.BaseViewModel
import com.woodnoisu.reader.model.BookBean
import com.woodnoisu.reader.repository.ShelfRepository
import com.woodnoisu.reader.utils.LogUtil
import dagger.assisted.AssistedInject


class ShelfViewModel @AssistedInject constructor(
    shelfRepository: ShelfRepository
) : BaseViewModel() {
    private val bookListFetching: MutableLiveData<String> = MutableLiveData()
    val bookList: LiveData<List<BookBean>>

    private val bookInserting: MutableLiveData<BookBean> = MutableLiveData()
    val bookInserted: LiveData<BookBean>

    private val bookDeleting: MutableLiveData<BookBean> = MutableLiveData()
    val bookDeleted: LiveData<BookBean>


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
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(): ShelfViewModel
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
}