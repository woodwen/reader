package com.woodnoisu.reader.constant

import com.woodnoisu.reader.utils.FileUtil
import java.io.File
import java.util.regex.Pattern

object Constant {
    const val UserAgent =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36"

    /*URL_BASE*/
    const val API_BASE_URL = "http://api.zhuishushenqi.com"

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

    //采用自己的格式去设置文件，防止文件被系统文件查询到
    const val SUFFIX_NB = ".zlj"
    const val SUFFIX_TXT = ".txt"
    const val SUFFIX_EPUB = ".epub"
    const val SUFFIX_PDF = ".pdf"

    //默认从文件中获取数据的长度
    const val BUFFER_SIZE = 512 * 1024

    //没有标题的时候，每个章节的最大长度
    const val MAX_LENGTH_WITH_NO_CHAPTER = 10 * 1024

    //BookCachePath (因为getCachePath引用了Context，所以必须是静态变量，不能够是静态常量)
    @kotlin.jvm.JvmField
    var BOOK_CACHE_PATH: String = (FileUtil.getDownloadPath() + File.separator
            + "free_novel" + File.separator)

    // "序(章)|前言"
    @kotlin.jvm.JvmField
    val mPreChapterPattern: Pattern = Pattern.compile(
        "^(\\s{0,10})((\u5e8f[\u7ae0\u8a00]?)|(\u524d\u8a00)|(\u6954\u5b50))(\\s{0,10})$",
        Pattern.MULTILINE
    )

    //正则表达式章节匹配模式
    // "(第)([0-9零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]{1,10})([章节回集卷])(.*)"
    @kotlin.jvm.JvmField
    val CHAPTER_PATTERNS = arrayOf(
        "^(.{0,8})(\u7b2c)([0-9\u96f6\u4e00\u4e8c\u4e24\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341\u767e\u5343\u4e07\u58f9\u8d30\u53c1\u8086\u4f0d\u9646\u67d2\u634c\u7396\u62fe\u4f70\u4edf]{1,10})([\u7ae0\u8282\u56de\u96c6\u5377])(.{0,30})$",
        "^(\\s{0,4})([\\(\u3010\u300a]?(\u5377)?)([0-9\u96f6\u4e00\u4e8c\u4e24\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341\u767e\u5343\u4e07\u58f9\u8d30\u53c1\u8086\u4f0d\u9646\u67d2\u634c\u7396\u62fe\u4f70\u4edf]{1,10})([\\.:\uff1a\u0020\\f\t])(.{0,30})$",
        "^(\\s{0,4})([\\(\uff08\u3010\u300a])(.{0,30})([\\)\uff09\u3011\u300b])(\\s{0,2})$",
        "^(\\s{0,4})(\u6b63\u6587)(.{0,20})$",
        "^(.{0,4})(Chapter|chapter)(\\s{0,4})([0-9]{1,4})(.{0,30})$"
    )
}