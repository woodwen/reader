package com.woodnoisu.reader.ui.widget.page

import com.woodnoisu.reader.constant.Constant
import com.woodnoisu.reader.model.BookBean
import com.woodnoisu.reader.model.ChapterBean
import com.woodnoisu.reader.utils.BookUtil
import com.woodnoisu.reader.utils.FileUtil
import com.woodnoisu.reader.utils.MD5Util.strToMd5By16
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.Reader
import kotlin.jvm.Throws


class NetPageLoader(pageView: PageView,
                    collBook: BookBean
) : PageLoader(pageView, collBook){

    /**
     * 刷新章节列表
     */
    override fun refreshChapterList() {
        if (mCollBook.chapters.isEmpty()) return
        mChapterList.clear()
        mChapterList.addAll(mCollBook.chapters)
        isChapterListPrepare = true

        // 目录加载完成，执行回调操作。
        mPageChangeListener?.onChaptersFinished(mChapterList)

        // 如果章节未打开
        if (!isChapterOpen()) {
            // 打开章节
            openChapter()
        }
    }

    /**
     * 打开指定章节
     */
    override fun openSpecifyChapter(specifyChapter: Int) {
        // mCurChapterPos = specifyChapter;
        // refreshChapterList();
    }

    /**
     * 获取章节阅读器
     */
    @Throws(Exception::class)
    override fun getChapterReader(chapter: ChapterBean): BufferedReader? {
        val file = File(
            Constant.BOOK_CACHE_PATH + strToMd5By16(mCollBook.url)
                    + File.separator + chapter.name + Constant.SUFFIX_NB
        )
        if (!file.exists()) return null
        val reader: Reader = FileReader(file)
        return BufferedReader(reader)
    }

    /**
     * 是否有章节数据
     */
    override fun hasChapterData(chapter: ChapterBean): Boolean {
        return BookUtil.isChapterCached(strToMd5By16(mCollBook.url), chapter.name)
    }

    /**
     * 装载上一章节的内容
     */
    override fun parsePrevChapterExt() {
        if (mStatus == STATUS_FINISH) {
            loadPrevChapter()
        } else if (mStatus == STATUS_LOADING) {
            loadCurrentChapter()
        }
    }

    /**
     * 装载当前章内容。
     */
    override fun parseCurChapterExt() {
        if (mStatus == STATUS_LOADING) {
            loadCurrentChapter()
        }
    }

    /**
     * 装载下一章节的内容
     */
    override fun parseNextChapterExt() {
        if (mStatus == STATUS_FINISH) {
            loadNextChapter()
        } else if (mStatus == STATUS_LOADING) {
            loadCurrentChapter()
        }
    }

    /**
     * 加载当前页的前面两个章节
     */
    private fun loadPrevChapter() {
        if (mPageChangeListener != null) {
            val end = mCurChapterPos
            var begin = end - 2
            if (begin < 0) {
                begin = 0
            }
            requestChapters(begin, end)
        }
    }

    /**
     * 加载前一页，当前页，后一页。
     */
    private fun loadCurrentChapter() {
        if (mPageChangeListener != null) {
            var begin = mCurChapterPos
            var end = mCurChapterPos

            // 是否当前不是最后一章
            if (end < mChapterList.size) {
                end += 1
                if (end >= mChapterList.size) {
                    end = mChapterList.size - 1
                }
            }

            // 如果当前不是第一章
            if (begin != 0) {
                begin -= 1
                if (begin < 0) {
                    begin = 0
                }
            }
            requestChapters(begin, end)
        }
    }

    /**
     * 加载当前页的后两个章节
     */
    private fun loadNextChapter() {
        if (mPageChangeListener != null) {

            // 提示加载后两章
            val begin = mCurChapterPos + 1
            var end = begin + 1

            // 判断是否大于最后一章
            if (begin >= mChapterList.size) {
                // 如果下一章超出目录了，就没有必要加载了
                return
            }
            if (end > mChapterList.size) {
                end = mChapterList.size - 1
            }
            requestChapters(begin, end)
        }
    }

    /**
     * 请求章节
     */
    private fun requestChapters(st: Int, ed: Int) {
        // 检验输入值
        var start = st
        var end = ed
        if (start < 0) {
            start = 0
        }
        if (end >= mChapterList.size) {
            end = mChapterList.size - 1
        }
        val chapters: MutableList<ChapterBean> = ArrayList()

        // 过滤，哪些数据已经加载了
        for (i in start..end) {
            val txtChapter = mChapterList[i]
            if (!hasChapterData(txtChapter)) {
                chapters.add(txtChapter)
            }
        }
        if (chapters.isNotEmpty()) {
            mPageChangeListener?.chapterContents(chapters)
        }
    }
}