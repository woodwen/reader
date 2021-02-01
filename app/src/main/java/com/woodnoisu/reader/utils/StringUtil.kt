package com.woodnoisu.reader.utils

import android.text.TextUtils
import com.woodnoisu.reader.ui.widget.page.ReadSettingManager.Companion.SHARED_READ_CONVERT_TYPE

/**
 * 字符串帮助类
 */
object StringUtil {
    /**
     * 将文本中的半角字符，转换成全角字符
     */
    fun halfToFull(input: String): String {
        val text = deleteImgs(input)
        val c = text.toCharArray()
        for (i in c.indices) {
            if (c[i].toInt() == 32)
            //半角空格
            {
                c[i] = 12288.toChar()
                continue
            }
            //根据实际情况，过滤不需要转换的符号
            //if (c[i] == 46) //半角点号，不转换
            // continue;

            if (c[i].toInt() in 33..126)
            //其他符号都转换为全角
                c[i] = (c[i].toInt() + 65248).toChar()
        }
        return String(c)
    }

    /**
     * 删除160
     */
    fun delete160(des: String): String {
        var text = des
        text = text.replace("&#160;".toRegex(), "")
        text = text.replace("&amp;#160;".toRegex(), "")
        text = text.replace("\\s*".toRegex(), "")
        text = text.trim { it <= ' ' }
        return text
    }

    /**
     * 繁簡轉換
     */
    fun convertCC(input: String): String {
        val convertType = SpUtil.getIntValue(SHARED_READ_CONVERT_TYPE, 0)

        if (input.isEmpty())
            return ""

        return input
    }

    /**
     * 将中文数字转换阿拉伯数字
     */
    fun parseCnToInt(cNum: String):Long {
        val num = cNum.replace(Regex("\\s+"), "")
        var firstUnit: Long = 1//一级单位
        var secondUnit: Long = 1//二级单位
        var result: Long = 0//结果
        val arr = num.toCharArray()
        //从低到高位依次处理
        for (i in arr.size - 1 downTo 0) {
            val c = arr[i]
            //临时单位变量
            var tmpUnit = charToUnit(c)
            //判断此位是数字还是单位
            if (tmpUnit > firstUnit){
                //是的话就赋值,以备下次循环使用
                firstUnit = tmpUnit
                secondUnit = 1
                //处理如果是"十","十一"这样的开头的
                if (i == 0){
                    result += firstUnit * secondUnit
                }
                //结束本次循环
                continue
            }
            if (tmpUnit > secondUnit) {
                secondUnit = tmpUnit
                continue
            }
            result += firstUnit * secondUnit * charToNumber(c);//如果是数字,则和单位想乘然后存到结果里
        }
        return result
    }

    /**
     * 转换单位
     */
    private fun charToUnit(c:Char):Long{
        return when (c) {
            '十' -> 10
            '百' -> 100
            '千' -> 1000
            '万' -> 10000
            '亿' -> 100000000
            else -> 1
        }
    }

    /**
     * 转换数字
     */
    private fun charToNumber(c: Char):Long {
        return when (c) {
            '一' -> 1
            '二' -> 2
            '三' -> 3
            '四' -> 4
            '五' -> 5
            '六' -> 6
            '七' -> 7
            '八' -> 8
            '九' -> 9
            '零' -> 0
            else -> 0
        }
    }

    /**
     * 删除图片
     */
    private fun deleteImgs(content: String?): String {
        return if (content != null && !TextUtils.isEmpty(content)) {
            // 去掉所有html元素,
            var str =
                content.replace("&[a-zA-Z]{1,10};".toRegex(), "").replace("<[^>]*>".toRegex(), "")
            str = str.replace("[(/>)<]".toRegex(), "")
            str
        } else {
            ""
        }
    }
}
