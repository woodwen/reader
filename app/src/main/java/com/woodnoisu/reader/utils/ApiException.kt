package com.woodnoisu.reader.utils

/**
 * 接口异常，即接口返回的code != 0
 *
 * 原因：1，后台接口有问题；或者，2，请求参数有问题。
 * @param errorCode 接口返回的code(非0)
 * @param msg 错误提示信息
 */
class ApiException(val errorCode: Int,val msg: String): Throwable(msg)