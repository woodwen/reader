package com.woodnoisu.reader.ui.widget.page

import android.content.Context
import com.woodnoisu.reader.model.BookBean
import com.woodnoisu.reader.model.ChapterBean
import com.woodnoisu.reader.utils.*
import java.io.*
import kotlin.jvm.Throws

/**
 * 本地书籍加载器
 */
class LocalPageLoader(pageView: PageView,
                      collBook: BookBean
    ) : PageLoader(pageView, collBook) {

    //获取书本的文件
    private var mBookFile: File? = null

    //编码类型
    private var mCharset: String = ""

    //上下文
    private var mContext: Context

    /**
     * 初始化
     */
    init {
        mStatus = STATUS_PARING
        mContext = pageView.context
    }

    /**
     * 刷新章节
     */
    override fun refreshChapterList() {
        // 对于文件是否存在，或者为空的判断，不作处理。 ==> 在文件打开前处理过了。
        val mb = File(mCollBook.bookFilePath)
        mBookFile = mb
        //获取文件编码
        mCharset = FileUtil.getCharset(mb.absolutePath)

        // 判断文件是否已经加载过，并具有缓存
        if (mCollBook.chapters.isNotEmpty()) {
            mChapterList.clear()
            mChapterList.addAll(mCollBook.chapters)
            isChapterListPrepare = true
            //提示目录加载完成
            mPageChangeListener?.onChaptersFinished(mChapterList)
            // 加载并显示当前章节
            openChapter()
        }
    }

    /**
     * 打开指定章节
     */
    override fun openSpecifyChapter(specifyChapter: Int) {}

    /**
     * 获取章节阅读器
     */
    @Throws(Exception::class)
    override fun getChapterReader(chapter: ChapterBean): BufferedReader {
        //从文件中获取数据
        val content = getChapterContent(chapter)
        val bis = ByteArrayInputStream(content)
        return BufferedReader(InputStreamReader(bis, mCharset))
    }

    /**
     * 判断是否有章节数据
     */
    override fun hasChapterData(chapter: ChapterBean): Boolean {
        return true
    }

    /**
     * 从文件中提取一章的内容
     *
     * @param chapter
     * @return
     */
    private fun getChapterContent(chapter: ChapterBean): ByteArray? {
        var bookStream: RandomAccessFile? = null
        try {
            bookStream = RandomAccessFile(mBookFile, "r")
            bookStream.seek(chapter.start)
            val extent = (chapter.end - chapter.start).toInt()
            val content = ByteArray(extent)
            bookStream.read(content, 0, extent)
            return content
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            bookStream?.close()
        }
        return ByteArray(0)
    }
}