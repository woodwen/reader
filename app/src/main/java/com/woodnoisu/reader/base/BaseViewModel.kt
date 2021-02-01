package com.woodnoisu.reader.base

import androidx.annotation.MainThread
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers

abstract class BaseViewModel : ViewModel() {

  inline fun <T> launchOnViewModelScope(crossinline block: suspend () -> LiveData<T>): LiveData<T> {
    return liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
      emitSource(block())
    }
  }

  protected val _toast: MutableLiveData<String> = MutableLiveData()
  val toast: LiveData<String> get() = _toast

  protected val _isLoading: MutableLiveData<Boolean> = MutableLiveData()
  val isLoading: LiveData<Boolean> get() = _isLoading

  @MainThread
  fun toastMsg(msg: String) {
    _toast.value = msg
  }
}
