package com.woodnoisu.reader.ui.source

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.woodnoisu.reader.base.BaseViewModel
import com.woodnoisu.reader.model.source.BookSource
import com.woodnoisu.reader.repository.source.BookSourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookSourceViewModel @Inject constructor(
    private val repository: BookSourceRepository
) : BaseViewModel() {
    private val searchKey = MutableLiveData("")
    val sources: LiveData<List<BookSource>> = searchKey.switchMap {
        repository.liveData(it)
    }

    private val _importResult = MutableLiveData<Int>()
    val importResult: LiveData<Int> get() = _importResult

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
            }.onFailure {
                _toast.postValue(it.localizedMessage ?: "更新失败")
            }
        }
    }
}
