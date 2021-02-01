package com.woodnoisu.reader.ui.main

import androidx.hilt.Assisted
import androidx.lifecycle.*
import com.woodnoisu.reader.base.BaseViewModel
import com.woodnoisu.reader.utils.LogUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @Assisted private val savedStateHandle: SavedStateHandle
) : BaseViewModel() {
    init {
        LogUtil.i("init MainViewModel")
    }
}