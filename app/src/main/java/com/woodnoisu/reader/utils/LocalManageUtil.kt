package com.woodnoisu.reader.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import android.util.Log
import com.woodnoisu.reader.R
import com.woodnoisu.reader.ui.widget.page.ReadSettingManager
import java.util.*

/**
 * 本地管理帮助类
 */
object LocalManageUtil {
    private const val TAG = "LocalManageUtil"

    /**
     * 获取系统的locale
     *
     * @return Locale对象
     */
    fun getSystemLocale(context: Context?): Locale {
        return SpUtil.systemCurrentLocal
    }

    /**
     * 获取选择的语言
     */
    fun getSelectLanguage(context: Context): String {
        return when (SpUtil.getSelectLanguage()) {
            0 -> context.getString(R.string.a_simple)
            1 -> context.getString(R.string.a_traditional)
            else -> context.getString(R.string.a_simple)
        }
    }

    /**
     * 保存选择语言
     */
    fun saveSelectLanguage(context: Context, select: Int) {
        SpUtil.setSelectLanguage(select) //本地国际化
        ReadSettingManager.getInstance().convertType = select //网络数据国际化
        setApplicationLanguage(context)
    }

    /**
     * 设置本地设置
     */
    fun setLocal(context: Context): Context {
        return updateResources(
            context,
            getSetLanguageLocale(context)
        )
    }

    /**
     * 配置更改
     */
    fun onConfigurationChanged(context: Context) {
        saveSystemCurrentLanguage(context)
        setLocal(context)
        setApplicationLanguage(context)
    }

    /**
     * 更新资源
     */
    private fun updateResources(context: Context, locale: Locale): Context {
        var context = context
        Locale.setDefault(locale)
        val res = context.resources
        val config =
            Configuration(res.configuration)
        if (Build.VERSION.SDK_INT >= 17) {
            config.setLocale(locale)
            context = context.createConfigurationContext(config)
        } else {
            config.locale = locale
            res.updateConfiguration(config, res.displayMetrics)
        }
        return context
    }

    /**
     * 获取选择的语言设置
     * @param context context
     */
    private fun getSetLanguageLocale(context: Context): Locale {
        return when (SpUtil.getSelectLanguage()) {
            0 -> Locale.CHINA
            1 -> Locale.TAIWAN
            2 -> Locale.ENGLISH
            else -> Locale.ENGLISH
        }
    }

    /**
     * 设置语言类型
     */
    fun setApplicationLanguage(context: Context) {
        val resources =
            context.applicationContext.resources
        val dm = resources.displayMetrics
        val config = resources.configuration
        val locale = getSetLanguageLocale(context)
        config.locale = locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            config.setLocales(localeList)
            context.applicationContext.createConfigurationContext(config)
            Locale.setDefault(locale)
        }
        resources.updateConfiguration(config, dm)
    }

    /**
     * 保存系统当前语言
     */
    private fun saveSystemCurrentLanguage(context: Context?) {
        val locale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList.getDefault()[0]
        } else {
            Locale.getDefault()
        }
        Log.d(TAG, locale.language)
        SpUtil.systemCurrentLocal = locale
    }
}