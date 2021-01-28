package com.woodnoisu.reader.app.data.retrofit

import okhttp3.Interceptor
import okhttp3.Response

/*
    *将User-Agent标头添加到请求中。 标头采用以下格式：
    * <AppName> / <版本> Dalvik / <版本>（Linux; U; Android <android版本>; <设备ID> Build / <buildtag>）
    *
    *有用的链接：
    *移动应用中的用户代理：https://www.scientiamobile.com/correctly-form-user-agents-for-mobile-apps
    *测试用户代理：https：//faisalman.github.io/ua-parser-js/
*/
class UserAgentInterceptor : Interceptor {
    private val userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_1_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36"

    override fun intercept(chain: Interceptor.Chain): Response = chain
        .request()
        .newBuilder()
        .header("User-Agent", userAgent)
        .build()
        .let { chain.proceed(it) }
}
