package com.woodnoisu.reader.library.base.model

data class ResponsePage(val currentPage:Int=1, val totalPage:Int=1, val bookBeans: List<BookBean> = ArrayList())