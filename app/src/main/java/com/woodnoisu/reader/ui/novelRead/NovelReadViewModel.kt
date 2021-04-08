package com.woodnoisu.reader.ui.novelRead

import androidx.annotation.MainThread
import androidx.hilt.Assisted
import androidx.lifecycle.*
import com.woodnoisu.reader.base.BaseViewModel
import com.woodnoisu.reader.model.*
import com.woodnoisu.reader.repository.NovelReadRepository
import com.woodnoisu.reader.ui.widget.page.ReadSettingManager
import com.woodnoisu.reader.constant.Constant
import com.woodnoisu.reader.utils.LogUtil
import com.woodnoisu.reader.utils.SpUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NovelReadViewModel @Inject constructor(
    novelReadRepository: NovelReadRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : BaseViewModel() {
    // 当前书籍
    private var _collBook:MutableLiveData<BookBean> = MutableLiveData()
    // 当前章节
    private val _cChapter:MutableLiveData<ChapterBean> = MutableLiveData()
    // 当前章节索引
    private val _cChapterIndex:MutableLiveData<Int> = MutableLiveData()
    // 是否暗夜模式
    private val _isNightMode : MutableLiveData<Boolean> = MutableLiveData()
    // 是否全屏
    private val _isFullScreen : MutableLiveData<Boolean> = MutableLiveData()
    // 开始的章节
    private val _chapterStart : MutableLiveData<Int> = MutableLiveData()
    // 当前模式
    private val _nowMode: MutableLiveData<Boolean> = MutableLiveData()

    private val _chapterContentsFetchingErr: MutableLiveData<String> = MutableLiveData()
    val chapterContentsFetchingErr: LiveData<String> get() = _chapterContentsFetchingErr
    private val chapterContentsFetching: MutableLiveData<List<ChapterBean>> =
        MutableLiveData()
    val chapterContents: LiveData<ArrayList<ChapterBean>>

    private val _refreshChapter: MutableLiveData<Int> = MutableLiveData()
    val refreshChapter: LiveData<Int> get() = _refreshChapter

    private val chaptersFetching: MutableLiveData<RequestChapter> =
        MutableLiveData()
    val chapters: LiveData<ResponseChapter>

    private val _saveBookRecord: MutableLiveData<ReadRecordBean> =
        MutableLiveData()
    val saveBookRecord: LiveData<String>

    private val _getBookRecord: MutableLiveData<String> = MutableLiveData()
    val getBookRecord: LiveData<ReadRecordBean>

    private val _addSign: MutableLiveData<RequestAddSign> = MutableLiveData()
    val addSign: LiveData<MutableList<BookSignBean>>

    private val _getSign: MutableLiveData<String> = MutableLiveData()
    val getSign: LiveData<List<BookSignBean>>

    private val _deleteSignBean: MutableLiveData<List<BookSignBean>> = MutableLiveData()
    val deleteSign: LiveData<String>

    init {
        LogUtil.i("init NovelReadViewModel")
        // 填充章节列表
        chapters = chaptersFetching.switchMap {
            _isLoading.postValue(true)
            launchOnViewModelScope {
                novelReadRepository.getChapterBeans(
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

        // 填充章节详情
        chapterContents = chapterContentsFetching.switchMap {
            _isLoading.postValue(true)
            launchOnViewModelScope {
                novelReadRepository.getChapterContents(
                    chapters = it,
                    onNext = {_refreshChapter.postValue(it)},
                    onSuccess = {
                        _isLoading.postValue(false)
                    },
                    onError = {
                        _isLoading.postValue(false)
                        _toast.postValue(it)
                        _chapterContentsFetchingErr.postValue(it)
                    }
                ).asLiveData()
            }
        }

        // 保存阅读记录
        saveBookRecord = _saveBookRecord.switchMap {
            _isLoading.postValue(true)
            launchOnViewModelScope {
                novelReadRepository.saveBookRecord(
                    mBookRecord = it,
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
        getBookRecord = _getBookRecord.switchMap {
            _isLoading.postValue(true)
            launchOnViewModelScope {
                novelReadRepository.getBookRecord(
                    bookUrl = it,
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
        addSign = _addSign.switchMap {
            _isLoading.postValue(true)
            launchOnViewModelScope {
                novelReadRepository.addSign(
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
        getSign = _getSign.switchMap {
            _isLoading.postValue(true)
            launchOnViewModelScope {
                novelReadRepository.getSigns(
                    bookUrl = it,
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
        deleteSign = _deleteSignBean.switchMap {
            _isLoading.postValue(true)
            launchOnViewModelScope {
                novelReadRepository.deleteSign(
                    bookSignBean = it,
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
    fun setCollBook(collBook: BookBean) {
        _collBook.value = collBook
    }

    @MainThread
    fun getCollBook(): BookBean {
        return _collBook.value!!
    }

    @MainThread
    fun negateIsNightMode():Boolean {
        val isNightMode = getIsNightMode()
        _isNightMode.postValue(!isNightMode)
        return !isNightMode
    }

    @MainThread
    fun getIsNightMode():Boolean {
        if (_isNightMode.value == null) {
            _isNightMode.value  = ReadSettingManager.getInstance().isNightMode
        }
        return _isNightMode.value!!
    }

    @MainThread
    fun getIsFullScreen():Boolean {
        if (_isFullScreen.value == null) {
            _isFullScreen.value  = ReadSettingManager.getInstance().isFullScreen
        }
        return _isFullScreen.value!!
    }

    @MainThread
    fun moveBackChapterStart(amount: Int) {
        val start = getChapterStart()
        _chapterStart.value = start + amount
    }

    @MainThread
    fun getChapterStart():Int {
        if (_chapterStart.value == null) {
            _chapterStart.value  = 0
        }
        return _chapterStart.value!!
    }

    @MainThread
    fun compareNowMode():Boolean {
        val mNowMode = getNowMode()
        return SpUtil.getBooleanValue(Constant.NIGHT) != mNowMode
    }

    @MainThread
    fun setCurrentChapter(index:Int,bean: ChapterBean){
        _cChapterIndex.value = index
        _cChapter.value = bean
    }

    @MainThread
    fun fetchChapterContents(requestChapters: List<ChapterBean>) {
        chapterContentsFetching.value = requestChapters
    }

    @MainThread
    fun fetchChapters(
        start: Int = 0,
        limit: Int = 100,
        cacheContents: Boolean = false
    ) {
        var index = start
        if (cacheContents && start < -1) {
            //如果缓存，且开始设置为-1的话 就用当前章节索引
            index = getCurrentChapterIndex()
        }
        chaptersFetching.value =
            RequestChapter(
                getCollBook(),
                index,
                limit,
                cacheContents
            )
    }

    @MainThread
    fun saveBookRecord( bean: ReadRecordBean?) {
        if (bean != null) {
            _saveBookRecord.postValue(bean)
        }
    }

    @MainThread
    fun getBookRecord() {
        _getBookRecord.value = getCollBook().url
    }

    @MainThread
    fun addBookSign() {
        val chapterUrl = _cChapter.value?.url ?: ""
        val chapterName = _cChapter.value?.name ?: ""
        val mBookUrl = getCollBook().url
        _addSign.postValue(
            RequestAddSign(
                mBookUrl,
                chapterUrl,
                chapterName
            )
        )
    }

    @MainThread
    fun fetchBookSign() {
        val mBookUrl = getCollBook().url
        _getSign.postValue(mBookUrl)
    }

    @MainThread
    fun deleteBookSign(bookSignBeans: List<BookSignBean>) {
        _deleteSignBean.postValue(bookSignBeans)
    }

    @MainThread
    fun canTurnPageByVolume():Boolean {
        return SpUtil.getBooleanValue("volume_turn_page", true)
    }

    @MainThread
    private fun getCurrentChapterIndex():Int {
        if(_cChapterIndex.value==null){
            _cChapterIndex.value = 0
        }
        return _cChapterIndex.value!!
    }

    @MainThread
    private fun getNowMode():Boolean {
        if (_nowMode.value == null) {
            _nowMode.value  = SpUtil.getBooleanValue(Constant.NIGHT)
        }
        return _nowMode.value!!
    }
}