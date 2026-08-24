package com.woodnoisu.reader.ui.source

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.woodnoisu.reader.base.BaseViewModel
import com.woodnoisu.reader.model.source.BookSource
import com.woodnoisu.reader.repository.source.BookSourceCheckResult
import com.woodnoisu.reader.repository.source.BookSourceImportResult
import com.woodnoisu.reader.repository.source.BookSourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookSourceCheckUiState(
    val running: Boolean = false,
    val total: Int = 0,
    val completed: Int = 0,
    val success: Int = 0,
    val failed: Int = 0,
    val disabled: Int = 0,
    val cancelled: Int = 0,
    val currentSourceUrl: String = "",
    val currentSourceName: String = "",
    val summary: String = ""
) {
    fun progressText(): String {
        if (!running) return summary
        val name = currentSourceName.ifBlank { "准备检测" }
        return "检测 $completed/$total：$name"
    }
}

@HiltViewModel
class BookSourceViewModel @Inject constructor(
    private val repository: BookSourceRepository
) : BaseViewModel() {
    internal var ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    private var checkJob: Job? = null

    private val searchKey = MutableLiveData("")
    val sources: LiveData<List<BookSource>> = searchKey.switchMap {
        repository.liveData(it)
    }

    private val _importResult = MutableLiveData<BookSourceImportResult>()
    val importResult: LiveData<BookSourceImportResult> get() = _importResult

    private val _checkState = MutableLiveData(BookSourceCheckUiState())
    val checkState: LiveData<BookSourceCheckUiState> get() = _checkState

    private val _checkResult = MutableLiveData<BookSourceCheckResult>()
    val checkResult: LiveData<BookSourceCheckResult> get() = _checkResult

    fun search(key: String) {
        searchKey.value = key
    }

    fun importSource(text: String?) {
        val sourceText = text?.trim().orEmpty()
        if (sourceText.isBlank()) {
            _toast.value = "导入内容为空"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                repository.importSource(sourceText)
            }.onSuccess {
                _importResult.postValue(it)
            }.onFailure {
                _toast.postValue(it.localizedMessage ?: "导入失败")
            }
        }
    }

    fun delete(source: BookSource) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                repository.delete(source)
            }.onFailure {
                _toast.postValue(it.localizedMessage ?: "删除失败")
            }
        }
    }

    fun updateEnabled(source: BookSource, enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                repository.updateEnabled(source, enabled)
            }.onSuccess {
                if (enabled && !it) {
                    _toast.postValue("书源不可用，已保持禁用")
                }
            }.onFailure {
                _toast.postValue(it.localizedMessage ?: "更新失败")
            }
        }
    }

    fun checkSource(source: BookSource, keyword: String) {
        checkSources(listOf(source), keyword)
    }

    fun checkSources(sources: List<BookSource>, keyword: String) {
        if (checkJob?.isActive == true) {
            _toast.value = "已有书源正在检测"
            return
        }
        val targets = sources
            .filter { it.bookSourceUrl.isNotBlank() }
            .distinctBy { it.bookSourceUrl }
        if (targets.isEmpty()) {
            _toast.value = "当前没有可检测书源"
            return
        }
        val checkKeyword = keyword.trim().ifBlank { BookSourceRepository.DEFAULT_CHECK_KEYWORD }
        _checkState.value = BookSourceCheckUiState(running = true, total = targets.size)
        checkJob = viewModelScope.launch(ioDispatcher) {
            var completed = 0
            var success = 0
            var failed = 0
            var disabled = 0
            var cancelledByUser = false
            try {
                targets.forEach { source ->
                    if (!isActive) throw CancellationException()
                    _checkState.postValue(
                        BookSourceCheckUiState(
                            running = true,
                            total = targets.size,
                            completed = completed,
                            success = success,
                            failed = failed,
                            disabled = disabled,
                            currentSourceUrl = source.bookSourceUrl,
                            currentSourceName = source.displayName()
                        )
                    )
                    val result = repository.checkSourceAvailability(source, checkKeyword)
                    completed++
                    if (result.available) {
                        success++
                    } else {
                        failed++
                    }
                    if (result.disabled) {
                        disabled++
                    }
                    _checkResult.postValue(result)
                    _checkState.postValue(
                        BookSourceCheckUiState(
                            running = true,
                            total = targets.size,
                            completed = completed,
                            success = success,
                            failed = failed,
                            disabled = disabled,
                            currentSourceUrl = source.bookSourceUrl,
                            currentSourceName = source.displayName()
                        )
                    )
                }
            } catch (e: CancellationException) {
                cancelledByUser = true
            }
            val cancelled = targets.size - completed
            val summary = buildSummary(completed, success, failed, disabled, cancelled, cancelledByUser)
            _checkState.postValue(
                BookSourceCheckUiState(
                    running = false,
                    total = targets.size,
                    completed = completed,
                    success = success,
                    failed = failed,
                    disabled = disabled,
                    cancelled = cancelled,
                    summary = summary
                )
            )
        }
    }

    fun cancelCheck() {
        checkJob?.cancel()
    }

    private fun buildSummary(
        completed: Int,
        success: Int,
        failed: Int,
        disabled: Int,
        cancelled: Int,
        cancelledByUser: Boolean
    ): String {
        val prefix = if (cancelledByUser) "检测已取消" else "检测完成"
        return "$prefix，完成 $completed 个，可用 $success 个，失效 $failed 个，自动禁用 $disabled 个，取消 $cancelled 个"
    }
}
