package com.woodnoisu.reader.ui.source

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.woodnoisu.reader.base.BaseViewModel
import com.woodnoisu.reader.model.source.BookSource
import com.woodnoisu.reader.repository.source.BookSourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookSourceEditViewModel @Inject constructor(
    private val repository: BookSourceRepository
) : BaseViewModel() {
    private val _sourceJson = MutableLiveData<String>()
    val sourceJson: LiveData<String> get() = _sourceJson

    private val _saved = MutableLiveData<Boolean>()
    val saved: LiveData<Boolean> get() = _saved

    fun loadSource(key: String?) {
        if (key.isNullOrBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                repository.getSource(key)?.let { repository.toJson(it) }
            }.onSuccess {
                it?.let { json -> _sourceJson.postValue(json) }
            }.onFailure {
                _toast.postValue(it.localizedMessage ?: "读取书源失败")
            }
        }
    }

    fun saveSource(json: String, oldKey: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val sources = repository.parseSources(json)
                if (sources.size != 1) {
                    throw IllegalArgumentException("编辑页只能保存单个书源")
                }
                repository.saveSource(sources[0], oldKey)
            }.onSuccess {
                _saved.postValue(true)
            }.onFailure {
                _toast.postValue(it.localizedMessage ?: "保存失败")
            }
        }
    }
}
