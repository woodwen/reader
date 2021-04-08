package com.woodnoisu.reader.utils

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import com.woodnoisu.reader.base.ContextProvider
import java.io.*
import java.text.DecimalFormat
import java.util.*
import java.util.regex.Pattern

/**
 * 文件帮助类
 */
object FileUtil {
    /**
     * 编码类型
     */
    enum class Charset constructor(var code: String) {
        UTF8("UTF-8"),
        UTF16LE("UTF-16LE"),
        UTF16BE("UTF-16BE"),
        GBK("GBK");

        companion object {
            const val BLANK: Byte = 0x0a
        }
    }

    /**
     * 获取文件夹
     */
    fun getFolder(filePath: String?): File {
        val file = File(filePath)
        //如果文件夹不存在，就创建它
        if (!file.exists()) {
            file.mkdirs()
        }
        return file
    }

    /**
     * 获取文件
     */
    @Synchronized
    fun getFile(filePath: String?): File {
        val file = File(filePath)
        try {
            if (!file.exists()) {
                //创建父类文件夹
                getFolder(file.parent)
                //创建文件
                file.createNewFile()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }

    /**
     * 获取文件夹数量
     */
    fun getDirSize(file: File): Long {
        //判断文件是否存在
        return if (file.exists()) {
            //如果是目录则递归计算其内容的总大小
            if (file.isDirectory) {
                val children = file.listFiles()
                var size: Long = 0
                for (f in children) size += getDirSize(f)
                size
            } else {
                file.length()
            }
        } else {
            0
        }
    }

    /**
     * 获取文件大小
     */
    fun getFileSize(size: Long): String {
        if (size <= 0) return "0"
        val units = arrayOf("b", "kb", "M", "G", "T")
        //计算单位的，原理是利用lg,公式是 lg(1024^n) = nlg(1024)，最后 nlg(1024)/lg(1024) = n。
        val digitGroups =
            (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        //计算原理是，size/单位值。单位值指的是:比如说b = 1024,KB = 1024^2
        return DecimalFormat("#,##0.##")
            .format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }

    /**
     * 本来是获取File的内容的。但是为了解决文本缩进、换行的问题
     * 这个方法就是专门用来获取书籍的...
     *
     *
     * 应该放在BookRepository中。。。
     *
     * @param file
     * @return
     */
    fun getFileContent(file: File?): String {
        var reader: Reader? = null
        var str: String? = null
        val sb = StringBuilder()
        try {
            reader = FileReader(file)
            val br = BufferedReader(reader)
            while (br.readLine().also { str = it } != null) {
                //过滤空语句
                if (str != "") {
                    //由于sb会自动过滤\n,所以需要加上去
                    sb.append("    $str\n")
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            reader?.close()
        }
        return sb.toString()
    }

    /**
     * 递归删除文件夹下的数据
     */
    @Synchronized
    fun deleteFile(filePath: String?) {
        val file = File(filePath)
        if (!file.exists()) return
        if (file.isDirectory) {
            val files = file.listFiles()
            for (subFile in files) {
                val path = subFile.path
                deleteFile(path)
            }
        }
        //删除文件
        file.delete()
    }

    /**
     * 由于递归的耗时问题，取巧只遍历内部三层
     * 获取txt文件
     */
    fun getTxtFiles(filePath: String?, layer: Int): List<File> {
        val txtFiles: MutableList<File> = ArrayList()
        val file = File(filePath)

        //如果层级为 3，则直接返回
        if (layer == 3) {
            return txtFiles
        }

        //获取文件夹
        val dirs = file.listFiles { pathname ->
            if (pathname.isDirectory && !pathname.name.startsWith(".")) {
                true
            } else if (pathname.name.endsWith(".txt")) {
                txtFiles.add(pathname)
                false
            } else {
                false
            }
        }
        //遍历文件夹
        for (dir in dirs) {
            //递归遍历txt文件
            txtFiles.addAll(getTxtFiles(dir.path, layer + 1))
        }
        return txtFiles
    }

    /**
     * 获取编码格式
     */
    fun getCharset(fileName: String?): String {
        var bis: BufferedInputStream? = null
        var charset = Charset.GBK
        val first3Bytes = ByteArray(3)
        try {
            var checked = false
            bis = BufferedInputStream(FileInputStream(fileName))
            bis.mark(0)
            var read = bis.read(first3Bytes, 0, 3)
            if (read == -1) return charset.code
            if (first3Bytes[0] == 0xEF.toByte() && first3Bytes[1] == 0xBB.toByte() && first3Bytes[2] == 0xBF.toByte()
            ) {
                charset = Charset.UTF8
                checked = true
            }
            bis.mark(0)
            if (!checked) {
                while (bis.read().also { read = it } != -1) {
                    if (read >= 0xF0) break
                    if (0x80 <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK
                        break
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read()
                        if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF)
                        // (0x80 - 0xBF),也可能在GB编码内
                            continue else break
                    } else if (0xE0 <= read) { // 也有可能出错，但是几率较小
                        read = bis.read()
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read()
                            if (0x80 <= read && read <= 0xBF) {
                                charset = Charset.UTF8
                                break
                            } else break
                        } else break
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            bis?.close()
        }
        return charset.code
    }

    /**
     * 获取缓存文件夹
     */
    fun getDownloadPath():String{
        return ContextProvider.mContext?.cacheDir?.absolutePath ?:""
    }

    fun uriToName(uri: Uri, context: Context):String{
        val filename = when(uri.scheme){
            ContentResolver.SCHEME_FILE -> uri.toFile().name
            ContentResolver.SCHEME_CONTENT->{
                val cursor = context.contentResolver.query(uri, null, null, null, null, null)
                cursor?.let {
                    it.moveToFirst()
                    val displayName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    cursor.close()
                    displayName
                }?:"${System.currentTimeMillis()}.${MimeTypeMap.getSingleton().getExtensionFromMimeType(context.contentResolver.getType(uri))}}"
            }
            else -> "${System.currentTimeMillis()}.${MimeTypeMap.getSingleton().getExtensionFromMimeType(context.contentResolver.getType(uri))}}"
        }
        return filename.split('.')[0]
    }

    fun getFilePathForN(
        uri: Uri,
        context: Context
    ): String? {
        try {
            val returnCursor: Cursor? =
                context.contentResolver.query(uri, null, null, null, null)
            if(returnCursor==null) return  null
            val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            val name: String = returnCursor.getString(nameIndex)
            val file = File(context.filesDir, name)
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            var read = 0
            val maxBufferSize = 1 * 1024 * 1024
            if(inputStream==null) return  null
            val bytesAvailable: Int = inputStream.available()
            val bufferSize = Math.min(bytesAvailable, maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream.read(buffers).also({ read = it }) != -1) {
                outputStream.write(buffers, 0, read)
            }
            returnCursor.close()
            inputStream.close()
            outputStream.close()
            return file.getPath()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}