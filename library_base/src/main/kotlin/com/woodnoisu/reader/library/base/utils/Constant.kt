package com.woodnoisu.reader.library.base.utils

import android.graphics.Color
import java.io.File

object Constant {
    const val UserAgent =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36"

    const val SHARED_SEX = "sex"
    const val SHARED_SAVE_BOOK_SORT = "book_sort"
    const val SHARED_SAVE_BILLBOARD = "billboard"
    const val SHARED_CONVERT_TYPE = "convert_type"
    const val SEX_BOY = "boy"
    const val SEX_GIRL = "girl"

    /*URL_BASE*/
    const val API_BASE_URL = "http://api.zhuishushenqi.com"
    const val IMG_BASE_URL = "http://statics.zhuishushenqi.com"

    //book type
    const val BOOK_TYPE_COMMENT = "normal"
    const val BOOK_TYPE_VOTE = "vote"

    //book state
    const val BOOK_STATE_NORMAL = "normal"
    const val BOOK_STATE_DISTILLATE = "distillate"

    //Book Date Convert Format
    const val FORMAT_BOOK_DATE = "yyyy-MM-dd'T'HH:mm:ss"
    const val FORMAT_TIME = "HH:mm"
    const val FORMAT_FILE_DATE = "yyyy-MM-dd"

    //RxBus
    const val MSG_SELECTOR = 1
    const val NIGHT = "NIGHT"
    const val Language = "Language"
    const val BookSort = "BookSort"
    const val Uid = "Uid"
    const val Sex = "Sex"
    const val Type = "Type"
    const val DateType = "DateType"
    const val BookGuide = "BookGuide"  //图书引导是否提示过

    const val COMMENT_SIZE = 10

    const val FeedBackEmail = ""

    const val RESULT_IS_COLLECTED = "result_is_collected"

    /**
     * 百度语音合成
     */
    const val appId = "16826023"
    const val appKey = "vEuU5gIWGwq5hivdTAaKz0P9"
    const val secretKey = "FcWRYUIrOPyE7dy51qfYZmg8Y1ZyP1c4 "

    //BookCachePath (因为getCachePath引用了Context，所以必须是静态变量，不能够是静态常量)
    @kotlin.jvm.JvmField
    var BOOK_CACHE_PATH: String = (FileUtil.getDownloadPath() + File.separator
            + "free_novel" + File.separator)

    @kotlin.jvm.JvmField
    val tagColors = intArrayOf(
        Color.parseColor("#90C5F0"),
        Color.parseColor("#91CED5"),
        Color.parseColor("#F88F55"),
        Color.parseColor("#C0AFD0"),
        Color.parseColor("#E78F8F"),
        Color.parseColor("#67CCB7"),
        Color.parseColor("#F6BC7E"),
        Color.parseColor("#90C5F0"),
        Color.parseColor("#91CED5")
    )

//    //BookCachePath (因为getCachePath引用了Context，所以必须是静态变量，不能够是静态常量)
//    var BOOK_CACHE_PATH: String = (FileUtils.cachePath.toString() + File.separator
//            + "book_cache" + File.separator)
//
//    //文件阅读记录保存的路径
//    var BOOK_RECORD_PATH: String = (FileUtils.cachePath.toString() + File.separator
//            + "book_record" + File.separator)
//    var bookType: Map<String, String> = object : HashMap<String, String>() {
//        init {
//            put("qt", "其他")
//            put(BookType.XHQH.netName, "玄幻奇幻")
//            put(BookType.WXXX.netName, "武侠仙侠")
//            put(BookType.DSYN.netName, "都市异能")
//            put(BookType.LSJS.netName, "历史军事")
//            put(BookType.YXJJ.netName, "游戏竞技")
//            put(BookType.KHLY.netName, "科幻灵异")
//            put(BookType.CYJK.netName, "穿越架空")
//            put(BookType.HMZC.netName, "豪门总裁")
//            put(BookType.XDYQ.netName, "现代言情")
//            put(BookType.GDYQ.netName, "古代言情")
//            put(BookType.HXYQ.netName, "幻想言情")
//            put(BookType.DMTR.netName, "耽美同人")
//        }
//    }

//    //BookType
//    @StringDef(
//        BookType.ALL,
//        BookType.XHQH,
//        BookType.WXXX,
//        BookType.DSYN,
//        BookType.LSJS,
//        BookType.YXJJ,
//        BookType.KHLY,
//        BookType.CYJK,
//        BookType.HMZC,
//        BookType.XDYQ,
//        BookType.GDYQ,
//        BookType.HXYQ,
//        BookType.DMTR
//    )
//    @Retention(
//        RetentionPolicy.SOURCE
//    )
//    annotation class BookType {
//        companion object {
//            var ALL = "all"
//            var XHQH = "xhqh"
//            var WXXX = "wxxx"
//            var DSYN = "dsyn"
//            var LSJS = "lsjs"
//            var YXJJ = "yxjj"
//            var KHLY = "khly"
//            var CYJK = "cyjk"
//            var HMZC = "hmzc"
//            var XDYQ = "xdyq"
//            var GDYQ = "gdyq"
//            var HXYQ = "hxyq"
//            var DMTR = "dmtr"
//        }
//    }
}