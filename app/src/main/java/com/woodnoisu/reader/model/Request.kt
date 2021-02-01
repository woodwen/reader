package com.woodnoisu.reader.model


data class RequestSearchPageByKeyword(val shopName:String,val keyword:String="", val page:Int=1)

data class RequestSearchPageByType(val shopName:String,val typeName:String="", val page:Int=1)

data class RequestBookInfo(val shopName:String, val bookUrl: String)

data class RequestAddSign(val mBookUrl: String, val chapterUrl: String, val chapterName: String)

data class RequestChapter(val mCollBook: BookBean, val start: Int, val limit:Int=100, val cacheContents:Boolean=false)