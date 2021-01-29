package com.woodnoisu.reader.feature.reader.presentation.reader

import androidx.lifecycle.*
import com.woodnoisu.reader.feature.reader.domain.model.*
import com.woodnoisu.reader.feature.reader.domain.repository.ReaderRepository
import com.woodnoisu.reader.library.base.presentation.navigation.NavManager
import com.woodnoisu.reader.library.base.presentation.viewmodel.BaseAction
import com.woodnoisu.reader.library.base.presentation.viewmodel.BaseViewModel
import com.woodnoisu.reader.library.base.presentation.viewmodel.BaseViewState
import com.woodnoisu.reader.library.base.utils.Constant
import com.woodnoisu.reader.library.base.utils.SpUtil

internal class ReaderViewModel(
    private val navManager: NavManager,
    private val readerRepository: ReaderRepository,
    private val args: ReaderFragmentArgs
): BaseViewModel<ViewState, Action>(ViewState()) {
    // 当前书籍
    private var cBook:ReaderBookDomainModel? = null
    // 当前章节
    private var cChapter:ReaderChapterDomainModel? = null
    // 当前章节页面索引
    private var cChapterIndex:Int = 0
    // 开始的章节索引
    private var chapterStart : Int = 0
    // 当前模式
    private var nowMode: Boolean = SpUtil.getBooleanValue(Constant.NIGHT)

    // 获取书籍
    private val bookFetching: MutableLiveData<RequestGetBookDomainModel> = MutableLiveData()
    val bookLiveData: LiveData<ResponseGetBookDomainModel>

    // 获取章节内容列表
    private val chapterContentsFetching: MutableLiveData<RequestGetChapterContentsDomainModel> = MutableLiveData()
    val chapterContentsLiveData: LiveData<ResponseGetChapterContentsDomainModel>
    private val refreshChapterFetching: MutableLiveData<Int> = MutableLiveData()
    val refreshChapterLiveData: LiveData<Int> get() = refreshChapterFetching
    private val chapterContentErrFetching: MutableLiveData<String> = MutableLiveData()
    val chapterContentErrLiveData: LiveData<String> get() = chapterContentErrFetching

    // 获取章节列表
    private val chaptersFetching: MutableLiveData<RequestGetChaptersDomainModel> = MutableLiveData()
    val chaptersLiveData: LiveData<ResponseGetChaptersDomainModel>

    // 获取阅读记录
    private val bookRecordFetching: MutableLiveData<RequestGetBookRecordDomainModel> = MutableLiveData()
    val bookRecordLiveData: LiveData<ResponseGetBookRecordDomainModel>

    // 获取书签列表
    private val signsFetching: MutableLiveData<RequestGetSignsDomainModel> = MutableLiveData()
    val signsLiveData: LiveData<ResponseGetSignsDomainModel>

    // 保存阅读记录
    private val saveBookRecordFetching: MutableLiveData<RequestSetBookRecordDomainModel> = MutableLiveData()
    val saveBookRecordLiveData: LiveData<ResponseSetBookRecordDomainModel>

    // 添加书签
    private val addSignFetching: MutableLiveData<RequestAddSignDomainModel> = MutableLiveData()
    val addSignLiveData: LiveData<ResponseAddSignDomainModel>

    // 删除书签列表
    private val deleteSignsFetching: MutableLiveData<RequestDeleteSignsDomainModel> = MutableLiveData()
    val deleteSignsLiveData: LiveData<ResponseDeleteSignsDomainModel>

    /////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 获取数据
     */
    private fun fetchBook(bookId:String) {
        bookFetching.value = RequestGetBookDomainModel(bookId)
    }

    /**
     * 设置书籍
     */
    fun setBook(book: ReaderBookDomainModel) {
        cBook = book
    }

    /**
     * 获取书籍
     */
    fun getBook() = cBook!!

    /**
     * 获取章节内容列表
     */
    fun fetchChapterContents(chapters: List<ReaderChapterDomainModel>) {
        chapterContentsFetching.value = RequestGetChapterContentsDomainModel(chapters)
    }

    /**
     * 获取章节列表
     */
    fun fetchChapters(start: Int = 0, limit: Int = 100, cacheContents: Boolean = false) {
        var index = start
        if (cacheContents && start < -1) {
            //如果缓存，且开始设置为-1的话 就用当前章节索引
            index = cChapterIndex
        }
        chaptersFetching.value =
            RequestGetChaptersDomainModel(
                getBook(),
                index,
                limit,
                cacheContents)
    }

    /**
     * 获取阅读记录
     */
    fun fetchBookRecord() {
        bookRecordFetching.value =RequestGetBookRecordDomainModel(getBook().url)
    }

    /**
     * 获取书签列表
     */
    fun fetchBookSigns() {
        signsFetching.value =RequestGetSignsDomainModel(getBook().url)
    }

    /**
     * 保存阅读记录
     */
    fun fetchSaveBookRecord(bean: ReaderRecordDomainModel?) {
        if (bean != null) {
            saveBookRecordFetching.value =  RequestSetBookRecordDomainModel(bean)
        }
    }

    /**
     * 添加书签
     */
    fun fetchAddSign() {
        val chapterUrl = cChapter?.url ?: ""
        val chapterName = cChapter?.name ?: ""
        val mBookUrl = getBook().url
        addSignFetching.value = RequestAddSignDomainModel(mBookUrl, chapterUrl, chapterName)
    }

    /**
     * 删除书签列表
     */
    fun fetchDeleteSigns(bookSignBeans: List<ReaderBookSignDomainModel>) {
        deleteSignsFetching.value = RequestDeleteSignsDomainModel(bookSignBeans)
    }

    /**
     * 移动当前章节位置
     */
    fun moveBackChapterStart(amount: Int) {
        chapterStart += amount
    }

    /**
     * 获取当前章节位置
     */
    fun getChapterStart() = chapterStart

    /**
     * 比较当前模式
     */
    fun compareNowMode():Boolean {
        val mNowMode = nowMode
        return SpUtil.getBooleanValue(Constant.NIGHT) != mNowMode
    }

    /**
     * 设置当前章节
     */
    fun setCurrentChapter(index:Int,chapter: ReaderChapterDomainModel) {
        cChapterIndex = index
        cChapter = chapter
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

    /**
     * 初始化
     */
    init {
        // 填充书籍
        bookLiveData = bookFetching.switchMap {
            _isLoading.postValue(true)
            launchOnViewModelScope {
                readerRepository.getBook(
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

        // 填充章节内容
        chapterContentsLiveData = chapterContentsFetching.switchMap {
            _isLoading.postValue(true)
            launchOnViewModelScope {
                readerRepository.getChapterContents(
                    request = it,
                    onNext = {refreshChapterFetching.postValue(it)},
                    onSuccess = {
                        _isLoading.postValue(false)
                    },
                    onError = {
                        _isLoading.postValue(false)
                        _toast.postValue(it)
                        chapterContentErrFetching.postValue(it)
                    }
                ).asLiveData()
            }
        }

        // 填充章节列表
        chaptersLiveData = chaptersFetching.switchMap {
            _isLoading.postValue(true)
            launchOnViewModelScope {
                readerRepository.getChapters(
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

        // 获取阅读记录
        bookRecordLiveData = bookRecordFetching.switchMap {
            _isLoading.postValue(true)
            launchOnViewModelScope {
                readerRepository.getBookRecord(
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

        // 获取书签
        signsLiveData = signsFetching.switchMap {
            _isLoading.postValue(true)
            launchOnViewModelScope {
                readerRepository.getSigns(
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

        // 保存阅读记录
        saveBookRecordLiveData = saveBookRecordFetching.switchMap {
            _isLoading.postValue(true)
            launchOnViewModelScope{
                readerRepository.setBookRecord(
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

        // 添加书签
        addSignLiveData = addSignFetching.switchMap {
            _isLoading.postValue(true)
            launchOnViewModelScope {
                readerRepository.addSign(
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

        // 删除书签
        deleteSignsLiveData = deleteSignsFetching.switchMap {
            _isLoading.postValue(true)
            launchOnViewModelScope {
                readerRepository.deleteSigns(
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

    /**
     * 起始加载
     */
    override fun onLoadData() {
        fetchBook(args.bookId)
    }

    override fun onReduceState(viewAction: Action) = ViewState()
}

internal class ViewState : BaseViewState

internal sealed class Action : BaseAction