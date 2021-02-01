package com.woodnoisu.reader.utils

import android.content.Context
import android.content.SharedPreferences
import java.util.*

/**
 * 语言帮助类
 */
object SpUtil {
    // 语言选择
    private const val tagLanguage = "language_select"
    // 当前语言
    var systemCurrentLocal: Locale = Locale.ENGLISH

    // 数据保存对象
    private lateinit var sp: SharedPreferences

    /**
     * 初始化上下文
     */
    fun init(context: Context) {
        sp = context.getSharedPreferences(
            context.packageName,
            Context.MODE_PRIVATE
        )
    }

    /**
     * 获取字符串
     */
    fun getStringValue(key: String?): String? {
        return sp.getString(key, null)
    }

    /**
     * 获取字符串
     */
    fun getStringValue(key: String?, defaultValue: String?): String? {
        return sp.getString(key, defaultValue)
    }

    /**
     * 设置字符串
     */
    fun setStringValue(key: String?, value: String?) {
        val editor = sp.edit()
        editor.putString(key, value)
        editor.apply()
    }

    /**
     * 获取bool值
     */
    fun getBooleanValue(key: String?): Boolean {
        return sp.getBoolean(key, false)
    }

    /**
     * 获取bool值
     */
    fun getBooleanValue(key: String?, value: Boolean): Boolean {
        return sp.getBoolean(key, value)
    }

    /**
     * 获取bool值
     */
    fun setBooleanValue(key: String?, value: Boolean) {
        val editor = sp.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    /**
     * 获取int值
     */
    fun getIntValue(key: String?, def: Int): Int {
        return sp.getInt(key, def)
    }

    /**
     * 设置int值
     */
    fun setIntValue(key: String?, value: Int) {
        val editor = sp.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    /**
     * 设置long值
     */
    fun setLongValue(key: String?, value: Long) {
        val editor = sp.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    /**
     * 获取long值
     */
    fun getLongValue(key: String?): Long {
        return sp.getLong(key, 0)
    }

    /**
     * 获取float值
     */
    fun getFloatValue(key: String?): Float {
        return sp.getFloat(key, 0.0f)
    }

    /**
     * 设置float值
     */
    fun setFloatValue(key: String?, value: Float) {
        val editor = sp.edit()
        editor.putFloat(key, value)
        editor.apply()
    }

    /**
     * 获取当前语言
     */
    fun getSelectLanguage():Int {
        return getIntValue(tagLanguage, 0)
    }

    /**
     * 保存语言
     */
    fun setSelectLanguage(select: Int) {
        setIntValue(tagLanguage, select)
    }

    /**
     * 清理所有值
     */
    fun clearAllValue(context: Context) {
        val sharedData = context.getSharedPreferences(
            context.packageName,
            Context.MODE_PRIVATE
        )
        val editor = sharedData.edit()
        editor.clear()
        editor.apply()
    }
}