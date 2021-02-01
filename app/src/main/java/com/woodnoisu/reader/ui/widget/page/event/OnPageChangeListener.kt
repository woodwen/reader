package com.woodnoisu.reader.ui.widget.page.event

import com.woodnoisu.reader.model.ChapterBean

interface OnPageChangeListener {
    /**
     * 作用：章节切换的时候进行回调
     *
     * @param pos:切换章节的序号
     */
    fun onChapterChange(pos: Int)

    /**
     * 作用：请求加载章节内容
     *
     * @param requestChapters:需要下载的章节列表
     */
    fun chapterContents(requestChapters: MutableList<ChapterBean>)

    /**
     * 作用：章节目录加载完成时候回调
     *
     * @param chapters：返回章节目录
     */
    fun onChaptersFinished(chapters: MutableList<ChapterBean>)

    /**
     * 作用：章节页码数量改变之后的回调。==> 字体大小的调整，或者是否关闭虚拟按钮功能都会改变页面的数量。
     *
     * @param count:页面的数量
     */
    fun onPageCountChange(count: Int)

    /**
     * 作用：当页面改变的时候回调
     *
     * @param pos:当前的页面的序号
     */
    fun onPageChange(pos: Int)
}