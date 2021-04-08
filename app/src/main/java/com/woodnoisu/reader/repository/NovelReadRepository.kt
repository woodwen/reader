package com.woodnoisu.reader.repository

import android.util.Log
import androidx.annotation.WorkerThread
import com.woodnoisu.reader.constant.Constant
import com.woodnoisu.reader.model.*
import com.woodnoisu.reader.network.HtmlClient
import com.woodnoisu.reader.persistence.BookDao
import com.woodnoisu.reader.persistence.BookSignDao
import com.woodnoisu.reader.persistence.ChapterDao
import com.woodnoisu.reader.persistence.ReadRecordDao
import com.woodnoisu.reader.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.jvm.Throws

/**
 * 搜索存储器
 */
class NovelReadRepository @Inject constructor(
    private val htmlClient: HtmlClient,
    private val bookDao: BookDao,
    private val bookSignDao: BookSignDao,
    private val chapterDao: ChapterDao,
    private val readRecordDao: ReadRecordDao,
):Repository {

    /**
     * 保存阅读记录
     */
    @WorkerThread
    suspend fun saveBookRecord(
        mBookRecord: ReadRecordBean,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) = flow {
        try {
            val md5 = MD5Util.strToMd5By16(mBookRecord.bookUrl)
            if (!md5.isNullOrBlank()) {
                mBookRecord.bookMd5 = md5
                readRecordDao.insertSome(mBookRecord)
                emit("保存阅读记录成功")
                onSuccess("保存阅读记录成功")
            } else {
                onError("计算md5码错误")
            }
        } catch (e: Exception) {
            onError(e.toString())
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 获取章节内容（多）
     */
    @WorkerThread
    suspend fun getChapterContents(
        chapters:List<ChapterBean>,
        onNext: (Int) -> Unit,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) = flow {
        try {
            val newChapters = arrayListOf<ChapterBean>()
            for (bean in chapters) {
                val chapterBean = getChapterContent(bean)
                newChapters.add(chapterBean)
                //存储章节内容到本地文件
                if (chapterBean.content.isNotBlank()) {
                    saveChapterInfo(
                        MD5Util.strToMd5By16(chapterBean.bookUrl),
                        chapterBean.name,
                        chapterBean.content
                    )
                    onNext(chapterBean.id)
                }
            }
            emit(newChapters)
            onSuccess("请求章节内容成功")
        } catch (e: Exception) {
            onError(e.toString())
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 获取章节列表
     */
    @WorkerThread
    suspend fun getChapterBeans(
        request: RequestChapter,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) = flow {
        try {
            val mCollBook = request.mCollBook
            val bookFilePath = mCollBook.bookFilePath
            val start = request.start
            val limit = request.limit
            val cacheContents = request.cacheContents
            if (bookFilePath.isNullOrBlank()) {
                // 获取本地缓存章节数量
                val count = chapterDao.getListCountByBookUrl(mCollBook.url)
                if(count==0||start >= count) {
                    // 如果本地没有缓存则从网络获取,如果本地有数据，获取最新章节
                    val remoteChapters =
                        htmlClient.getChapterList(mCollBook.shopName,mCollBook.url,mCollBook.chaptersUrl,start,limit)
                    if (remoteChapters.isNotEmpty()) {
                        // 如果获取成功则缓存到数据库
                        chapterDao.insertList(remoteChapters)
                    }
                }
                // 获取网络章节列表的解析
                val chaptersList = chapterDao
                    .getListByBookUrl(mCollBook.url, start = start, limit = limit)

                emit(
                    ResponseChapter(
                        chaptersList,
                        cacheContents
                    )
                )
            } else {
                Log.i("测试", "当前章节:"+start.toString())
                if(start==0){
                    // 本地书籍的章节列表解析
                    val chaptersList = loadChapters(bookFilePath)
                    emit(
                        ResponseChapter(
                            chaptersList,
                            cacheContents
                        )
                    )
                }
            }
            onSuccess("获取章节列表成功")
        } catch (e: Exception) {
            onError(e.toString())
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 获取阅读记录
     */
    @WorkerThread
    suspend fun getBookRecord(
        bookUrl: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) = flow {
        try {
            val md5 = MD5Util.strToMd5By16(bookUrl)
            if (!md5.isNullOrBlank()) {
                var bean = readRecordDao.getByMd5(md5)
                if (bean == null) {
                    bean = ReadRecordBean()
                }
                emit(bean!!)
                onSuccess("获取阅读记录成功")
            }
        } catch (e: Exception) {
            onError(e.toString())
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 获取书签列表
     */
    @WorkerThread
    suspend fun getSigns(
        bookUrl: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) = flow {
        try {
            val bookSigns = bookSignDao.getListByBookUrl(bookUrl)
            emit(bookSigns.toList())
            onSuccess("获取书签列表成功")
        } catch (e: Exception) {
            onError(e.toString())
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 添加书签
     */
    @WorkerThread
    suspend fun addSign(
        request: RequestAddSign,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) = flow {
        try {
            val bookSign = BookSignBean()
            bookSign.bookUrl = request.mBookUrl
            bookSign.chapterUrl = request.chapterUrl
            bookSign.chapterName = request.chapterName
            val bookSignModel = bookSignDao.getByChapterUrl(bookSign.chapterUrl)
            if (bookSignModel == null) {
                bookSignDao.insertSome(bookSign)
                emit(mutableListOf(bookSign))
                onSuccess("添加书签成功")
            } else {
                onError("本章节书签已经存在")
            }
        } catch (e: Exception) {
            onError(e.toString())
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 删除书签
     */
    @WorkerThread
    suspend fun deleteSign(
        bookSignBean: List<BookSignBean>,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) = flow {
        try {
            bookSignDao.deleteList(bookSignBean)
            emit("删除书签成功")
            onSuccess("删除书签成功")
        } catch (e: Exception) {
            onError(e.toString())
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 获取章节内容（单）
     */
    @WorkerThread
   private suspend fun getChapterContent(
        chapterBean:ChapterBean
    ): ChapterBean {
        try {
            // 从数据库中获取
            val url = chapterBean?.url
            if (!url.isNullOrBlank()) {
                val chapterContent = chapterDao.getContentByUrl(chapterBean.url)
                if (chapterContent.isNullOrBlank()) {
                    //如果未从数据库中获取，则从网络获取
                    val content = htmlClient.getChapterContent(chapterBean.shopName,chapterBean.url)
                    if (!content.isNullOrBlank()) {
                        chapterBean.content = content
                        // 存取到数据库中
                        val chapterModel =
                            ChapterBean()
                        chapterModel.id = chapterBean.id
                        chapterModel.name = chapterBean.name
                        chapterModel.url = chapterBean.url
                        chapterModel.index = chapterBean.index
                        chapterModel.content = chapterBean.content
                        //chapterModel.bookName = chapterBean.bookName
                        chapterModel.bookUrl = chapterBean.bookUrl
                        chapterDao.update(chapterModel)
                    }
                } else {
                    chapterBean.content = chapterContent
                }
            }
        } catch (e: Exception) {
            LogUtil.e("NovelRepository", "获取章节内容失败", e)
            //onError(e.toString())
        }
        return chapterBean
    }

    /**
     * 从文件中获取章节
     * 未完成的部分:
     * 1. 序章的添加
     * 2. 章节存在的书本的虚拟分章效果
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun loadChapters(bookFilePath: String): MutableList<ChapterBean> {
        val chapters: MutableList<ChapterBean> = ArrayList()
        // 对于文件是否存在，或者为空的判断，不作处理。 ==> 在文件打开前处理过了。
        val mBookFile = File(bookFilePath)
        //获取文件编码
        val mCharset = FileUtil.getCharset(mBookFile.absolutePath)
        var mChapterPattern: Pattern? = null
        //获取文件流
        val bookStream = RandomAccessFile(mBookFile, "r")
        //寻找匹配文章标题的正则表达式，判断是否存在章节名
        var hasChapter: Boolean = false
        //首先获取128k的数据
        val buffer0 = ByteArray(Constant.BUFFER_SIZE / 4)
        val length0 = bookStream.read(buffer0, 0, buffer0.size)
        //进行章节匹配
        for (str in Constant.CHAPTER_PATTERNS) {
            val pattern =
                Pattern.compile(str, Pattern.MULTILINE)
            val matcher =
                pattern.matcher(String(buffer0, 0, length0, charset(mCharset)))
            //如果匹配存在，那么就表示当前章节使用这种匹配方式
            if (matcher.find()) {
                mChapterPattern = pattern
                //重置指针位置
                bookStream.seek(0)
                hasChapter = true
                break
            }
        }
        if(!hasChapter){
            //重置指针位置
            bookStream.seek(0)
        }

        //加载章节
        val buffer = ByteArray(Constant.BUFFER_SIZE)
        //获取到的块起始点，在文件中的位置
        var curOffset: Long = 0
        //block的个数
        var blockPos = 0
        //读取的长度
        var length: Int

        //获取文件中的数据到buffer，直到没有数据为止
        while (bookStream.read(buffer, 0, buffer.size).also { length = it } > 0) {
            ++blockPos
            //如果存在Chapter
            if (hasChapter) {
                //将数据转换成String
                val blockContent = String(buffer, 0, length, charset(mCharset))
                //当前Block下使过的String的指针
                var seekPos = 0
                //进行正则匹配
                val matcher: Matcher = mChapterPattern!!.matcher(blockContent)
                //如果存在相应章节
                while (matcher.find()) {
                    //获取匹配到的字符在字符串中的起始位置
                    val chapterStart: Int = matcher.start()

                    //如果 seekPos == 0 && nextChapterPos != 0 表示当前block处前面有一段内容
                    //第一种情况一定是序章 第二种情况可能是上一个章节的内容
                    if (seekPos == 0 && chapterStart != 0) {
                        //获取当前章节的内容
                        val chapterContent =
                            blockContent.substring(seekPos, chapterStart)
                        //设置指针偏移
                        seekPos += chapterContent.length

                        //如果当前对整个文件的偏移位置为0的话，那么就是序章
                        if (curOffset == 0L) {
                            //创建序章
                            val preChapter =
                                ChapterBean()
                            preChapter.name = "序章"
                            preChapter.start = 0
                            preChapter.end =
                                chapterContent.toByteArray(charset(mCharset)).size.toLong() //获取String的byte值,作为最终值

                            //如果序章大小大于30才添加进去
                            if (preChapter.end - preChapter.start > 30) {
                                chapters.add(preChapter)
                            }

                            //创建当前章节
                            val curChapter =
                                ChapterBean()
                            curChapter.name = matcher.group().trim()
                            curChapter.start = preChapter.end
                            chapters.add(curChapter)
                        } else {
                            //获取上一章节
                            val lastChapter = chapters[chapters.size - 1]
                            //将当前段落添加上一章去
                            lastChapter.end =
                                lastChapter.end + chapterContent.toByteArray(charset(mCharset)).size

                            //如果章节内容太小，则移除
                            if (lastChapter.end - lastChapter.start < 30) {
                                chapters.remove(lastChapter)
                            }

                            //创建当前章节
                            val curChapter =
                                ChapterBean()
                            curChapter.name = matcher.group().trim()
                            curChapter.start = lastChapter.end
                            chapters.add(curChapter)
                        }
                    } else {
                        //是否存在章节
                        if (chapters.size != 0) {
                            //获取章节内容
                            val chapterContent =
                                blockContent.substring(seekPos, matcher.start())
                            seekPos += chapterContent.length

                            //获取上一章节
                            val lastChapter = chapters[chapters.size - 1]
                            lastChapter.end =
                                lastChapter.start + chapterContent.toByteArray(charset(mCharset)).size

                            //如果章节内容太小，则移除
                            if (lastChapter.end - lastChapter.start < 30) {
                                chapters.remove(lastChapter)
                            }

                            //创建当前章节
                            val curChapter =
                                ChapterBean()
                            curChapter.name = matcher.group().trim()
                            curChapter.start = lastChapter.end
                            chapters.add(curChapter)
                        } else {
                            val curChapter =
                                ChapterBean()
                            curChapter.name = matcher.group().trim()
                            curChapter.start = 0
                            chapters.add(curChapter)
                        }
                    }
                }
            } else {
                //章节在buffer的偏移量
                var chapterOffset = 0
                //当前剩余可分配的长度
                var strLength = length
                //分章的位置
                var chapterPos = 0
                while (strLength > 0) {
                    ++chapterPos
                    //是否长度超过一章
                    if (strLength > Constant.MAX_LENGTH_WITH_NO_CHAPTER) {
                        //在buffer中一章的终止点
                        var end = length
                        //寻找换行符作为终止点
                        for (i in chapterOffset + Constant.MAX_LENGTH_WITH_NO_CHAPTER until length) {
                            if (buffer[i] == FileUtil.Charset.BLANK) {
                                end = i
                                break
                            }
                        }
                        val chapter =
                            ChapterBean()
                        chapter.name = "第" + blockPos + "章" + "(" + chapterPos + ")"
                        chapter.start = curOffset + chapterOffset + 1
                        chapter.end = curOffset + end
                        chapters.add(chapter)
                        //减去已经被分配的长度
                        strLength -= (end - chapterOffset)
                        //设置偏移的位置
                        chapterOffset = end
                    } else {
                        val chapter = ChapterBean()
                        chapter.name = "第" + blockPos + "章" + "(" + chapterPos + ")"
                        chapter.start = curOffset + chapterOffset + 1
                        chapter.end = curOffset + length
                        chapters.add(chapter)
                        strLength = 0
                    }
                }
            }

            //block的偏移点
            curOffset += length.toLong()
            if (hasChapter) {
                //设置上一章的结尾
                val lastChapter = chapters[chapters.size - 1]
                lastChapter.end = curOffset
            }

            //当添加的block太多的时候，执行GC
            if (blockPos % 15 == 0) {
                System.gc()
                System.runFinalization()
            }
        }

        // 计算章节的md5
        for (i in 0 until chapters.size) {
            val chapter: ChapterBean = chapters[i]
//            val md5 = MD5Util.strToMd5By16(
//                mBookFile?.absolutePath ?: ""
//                + File.separator + chapter.name
//            )
//            chapter.md5 = md5
            chapter.index = i
            chapter.bookUrl = bookFilePath
        }

        bookStream.close()
        System.gc()
        System.runFinalization()

        return chapters
    }

    /**
     * 存储章节
     *
     * @param folderName
     * @param fileName
     * @param content
     */
    @WorkerThread
    private fun saveChapterInfo(folderName: String, fileName: String, content: String) {
        val filePath = (Constant.BOOK_CACHE_PATH + folderName
                + File.separator + fileName + Constant.SUFFIX_NB)
        if (File(filePath).exists()) {
            return
        }
        val str = content.replace("\\\\n\\\\n".toRegex(), "\n")
        val file = BookUtil.getBookFile(folderName, fileName)
        //获取流并存储
        var writer: Writer? = null
        try {
            writer = BufferedWriter(FileWriter(file))
            writer.write(str)
            writer.flush()
        } catch (e: IOException) {
            e.printStackTrace()
            writer?.close()
        }
    }
}