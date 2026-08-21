package com.woodnoisu.reader.source

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.woodnoisu.reader.R
import com.woodnoisu.reader.ui.main.MainActivity
import com.woodnoisu.reader.ui.novelRead.NovelReadActivity
import com.woodnoisu.reader.ui.source.BookSourceActivity
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.startsWith
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URLEncoder

@RunWith(AndroidJUnit4::class)
class BookSourceDeviceTest {

    @Test
    fun importEditSearchAndOpenDynamicSource() {
        grantStoragePermission()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val server = MockWebServer()
        server.start()
        try {
            enqueueBookPages(server)
            val baseUrl = server.url("/").toString()
            val sourceName = "真机自动源"
            val bookName = "真机自动书"
            val sourceJson = """
                {
                  "bookSourceName": "$sourceName",
                  "bookSourceGroup": "真机",
                  "bookSourceUrl": "$baseUrl",
                  "enabled": true,
                  "searchUrl": "${baseUrl}search?q={{key}}&page={{page}}",
                  "ruleSearch": {
                    "bookList": ".result",
                    "name": ".title@text",
                    "author": ".author@text",
                    "bookUrl": ".title@href"
                  },
                  "ruleBookInfo": {
                    "name": "h1@text",
                    "author": ".author@text",
                    "tocUrl": ".toc@href"
                  },
                  "ruleToc": {
                    "chapterList": ".chapter",
                    "chapterName": "text",
                    "chapterUrl": "href"
                  },
                  "ruleContent": {
                    "content": ".content@html"
                  }
                }
            """.trimIndent()

            val importUri = "yuedu://booksource/importonline?src=" +
                URLEncoder.encode(sourceJson, "UTF-8")
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(importUri))
                    .setClass(context, BookSourceActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            waitForText(startsWith("$sourceName (真机)"))

            onView(withText("编辑")).perform(click())
            waitForText("编辑书源")
            onView(withId(R.id.et_source_json)).check(matches(withText(containsString(sourceName))))
            pressBack()

            context.startActivity(
                Intent(context, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            waitForText("书城")
            onView(withId(R.id.navigation_square)).perform(click())
            waitForText("全文阅读")

            onView(withId(R.id.tv_search_title)).perform(click())
            waitForText(sourceName)
            onView(withText(sourceName)).perform(click())
            waitForText("动态书源请先输入书名或作者搜索")

            onView(withId(R.id.tv_search_search)).perform(click())
            waitForText("搜索小说")
            onView(withId(android.R.id.input)).perform(typeText("测试"), closeSoftKeyboard())
            onView(withText("搜索")).perform(click())
            waitForText(bookName)

            val monitor = instrumentation.addMonitor(NovelReadActivity::class.java.name, null, false)
            onView(withText(bookName)).perform(click())
            waitForText("开始阅读")
            onView(withText("开始阅读")).perform(click())
            val activity = instrumentation.waitForMonitorWithTimeout(monitor, 5000)
            assertNotNull(activity)
            activity?.finish()
        } finally {
            server.shutdown()
        }
    }

    private fun enqueueBookPages(server: MockWebServer) {
        server.enqueue(
            MockResponse().setBody(
                """
                <html><body>
                  <div class="result">
                    <a class="title" href="/book/auto">真机自动书</a>
                    <span class="author">作者：测试作者</span>
                  </div>
                </body></html>
                """.trimIndent()
            )
        )
        server.enqueue(
            MockResponse().setBody(
                """
                <html><body>
                  <h1>真机自动书</h1>
                  <span class="author">作者：测试作者</span>
                  <a class="toc" href="/toc/auto">目录</a>
                </body></html>
                """.trimIndent()
            )
        )
        server.enqueue(
            MockResponse().setBody(
                """
                <html><body>
                  <a class="chapter" href="/chapter/1">第一章</a>
                </body></html>
                """.trimIndent()
            )
        )
        server.enqueue(
            MockResponse().setBody(
                """
                <html><body>
                  <div class="content"><p>真机正文第一段</p></div>
                </body></html>
                """.trimIndent()
            )
        )
    }

    private fun grantStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            kotlin.runCatching {
                val instrumentation = InstrumentationRegistry.getInstrumentation()
                instrumentation.uiAutomation.grantRuntimePermission(
                    instrumentation.targetContext.packageName,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
        }
    }

    private fun waitForText(matcher: org.hamcrest.Matcher<String>, timeout: Long = 8000) {
        waitUntil(timeout) {
            onView(withText(matcher)).check(matches(isDisplayed()))
        }
    }

    private fun waitForText(text: String, timeout: Long = 8000) {
        waitUntil(timeout) {
            onView(withText(text)).check(matches(isDisplayed()))
        }
    }

    private fun waitUntil(timeout: Long, assertion: () -> Unit) {
        val startTime = SystemClock.elapsedRealtime()
        var lastError: Throwable? = null
        while (SystemClock.elapsedRealtime() - startTime < timeout) {
            try {
                assertion()
                return
            } catch (error: Throwable) {
                lastError = error
                SystemClock.sleep(250)
            }
        }
        throw AssertionError("Timed out waiting for UI condition", lastError)
    }
}
