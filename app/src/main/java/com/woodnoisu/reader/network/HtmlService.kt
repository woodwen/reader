package com.woodnoisu.reader.network

import com.woodnoisu.reader.constant.Constant.UserAgent
import com.woodnoisu.reader.utils.LogUtil
import com.woodnoisu.reader.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request


class HtmlService {

    /**
     * 获取网页
     */
    suspend fun getHtml(url: String, head: Map<String,String>): String? {
        try {
            return withContext(Dispatchers.IO) { get(url,head) }
        } catch (e: java.io.IOException) {
            LogUtil.e(e.toString())
            showToast(e.toString())
        }
        return null
    }

    /**
     * 获取网页
     */
    suspend fun postHtml(url: String,head: Map<String,String>,body: Map<String,String>): String? {
        try {
            return withContext(Dispatchers.IO) { post(url,head,body) }
        } catch (e: java.io.IOException) {
            LogUtil.e(e.toString())
            showToast(e.toString())
        }
        return null
    }


    /**
     * 请求
     */
    private fun get(url: String,head: Map<String,String>): String? {
        val client = OkHttpClient()
        val requestBuilder = Request.Builder()
            .removeHeader("User-Agent")
            .addHeader("User-Agent", UserAgent)
        for (hd in head){
            requestBuilder.addHeader(hd.key, hd.value)
        }
        val request = requestBuilder.url(url)
            .build()
        val response = client.newCall(request).execute()
        return response.body?.string()
    }

    /**
     * 请求
     */
    private fun post(url: String,head: Map<String,String>,body: Map<String,String>): String? {
        val client = OkHttpClient()
        val requestBuilder = Request.Builder()
            .removeHeader("User-Agent")
            .addHeader("User-Agent", UserAgent)
        for (hd in head){
            requestBuilder.addHeader(hd.key, hd.value)
        }
        val formBodyBuilder = FormBody.Builder()
        for (bd in body){
            formBodyBuilder.add(bd.key, bd.value)
        }
        val request = requestBuilder.url(url)
            .post(formBodyBuilder.build())
            .build()
        val response = client.newCall(request).execute()
        return response.body?.string()
    }
}