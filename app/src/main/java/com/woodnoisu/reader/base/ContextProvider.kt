package com.woodnoisu.reader.base

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.woodnoisu.reader.utils.LocalManageUtil
import com.woodnoisu.reader.utils.SpUtil

/**
 * 上下文
 */
class ContextProvider : ContentProvider() {

    /**
     * 静态对象
     */
    companion object {
        lateinit var mContext: Context
    }

    /**
     * 创建事件
     */
    override fun onCreate(): Boolean {
        mContext = context!!
        SpUtil.init(context!!)
        LocalManageUtil.setApplicationLanguage(context!!)
        setNight()
        return false
    }

    /**
     * 设置暗夜模式
     */
    private fun setNight() {


    }

    /**
     * 查询
     */
    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        return null
    }

    /**
     * 获取类型
     */
    override fun getType(uri: Uri): String? {
        return null
    }

    /**
     * 插入
     */
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    /**
     * 删除
     */
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    /**
     * 更新
     */
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }
}