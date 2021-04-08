package com.woodnoisu.reader.utils

import com.woodnoisu.reader.constant.Constant
import java.io.File

object BookUtil {
    /**
     * 获取书籍大小
     */
    fun getBookSize(folderName: String): Long {
        return FileUtil.getDirSize(
            FileUtil
                .getFolder(Constant.BOOK_CACHE_PATH + folderName)
        )
    }

    /**
     * 创建或获取存储文件
     * @param folderName 文件夹
     * @param fileName 文件
     * @return 文件
     */
    fun getBookFile(folderName: String?, fileName: String?): File {
        return FileUtil.getFile(
            Constant.BOOK_CACHE_PATH + folderName
                    + File.separator + fileName + Constant.SUFFIX_NB
        )
    }

    /**
     * 根据文件名判断是否被缓存过 (因为可能数据库显示被缓存过，但是文件中却没有的情况，所以需要根据文件判断是否被缓存
     * 过)
     * @param folderName : bookId
     * @param fileName: chapterName
     * @return 是否被缓存过
     */
    fun isChapterCached(folderName: String, fileName: String): Boolean {
        val file = File(
            Constant.BOOK_CACHE_PATH + folderName
                    + File.separator + fileName + Constant.SUFFIX_NB
        )
        return file.exists()
    }
}