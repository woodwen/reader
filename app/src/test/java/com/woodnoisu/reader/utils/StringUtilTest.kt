package com.woodnoisu.reader.utils

import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.jvm.Throws

class StringUtilTest {

    @Before
    @Throws(java.lang.Exception::class)
    fun setUp() {
        println("测试开始！")
    }

    @After
    @Throws(java.lang.Exception::class)
    fun tearDown() {
        println("测试结束！")
    }


    @Test
    @Throws(java.lang.Exception::class)
    fun parseCnToIntTest() {
        val str ="一百一十五"
        val num= StringUtil.parseCnToInt(str)
        println("${str}:${num}")
    }
}