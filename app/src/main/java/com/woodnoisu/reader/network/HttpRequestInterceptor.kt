package com.woodnoisu.reader.network

import com.woodnoisu.reader.constant.Constant.UserAgent
import com.woodnoisu.reader.utils.LogUtil
import okhttp3.Interceptor
import okhttp3.Response

  /**
   * 拦截器
   */
  class HttpRequestInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
      val request = chain.request()
        .newBuilder()
        .removeHeader("User-Agent")
        .addHeader("User-Agent", UserAgent)
        .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
        .addHeader("Accept-Encoding", "gzip, deflate")
        .addHeader("Connection", "keep-alive")
        .addHeader("Accept", "*/*")
        .build()
      LogUtil.i(request.toString())
      return chain.proceed(request)
    }
  }
