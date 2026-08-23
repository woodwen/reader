package com.woodnoisu.reader.source

import android.Manifest
import android.os.Build
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.woodnoisu.reader.R
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.Matcher
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.FileInputStream
import java.net.URLEncoder

@RunWith(AndroidJUnit4::class)
class BookSourceDeviceTest {

    @Test
    fun shelfSearchShowsRealResultsForDouluo() {
        openShelfPage()

        searchRealDouluoSourceResults()
    }

    @Test
    fun shelfSearchResultCanOpenNovelReaderForDouluo() {
        openShelfPage()
        searchRealDouluoSourceResults()

        openSearchResultDialog("斗罗")
        onView(withText("开始阅读")).perform(performViewClick())
        waitUntil(30000) {
            onView(withId(R.id.read_pv_page)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun importEditAndShowMeOrder() {
        grantStoragePermission()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val server = MockWebServer()
        server.start()
        try {
            enqueueBookPages(server)
            val baseUrl = server.url("/").toString()
            val sourceName = "真机自动源${SystemClock.elapsedRealtime()}"
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
            startActivityByShell(
                "am start -W -a android.intent.action.VIEW -d $importUri " +
                    "-n ${context.packageName}/.ui.source.BookSourceActivity"
            )
            onView(withId(R.id.et_search)).perform(replaceText(sourceName), closeSoftKeyboard())
            waitForText(startsWith("$sourceName (真机)"))

            onView(withText("编辑")).perform(performViewClick())
            waitForText("编辑书源")
            onView(withId(R.id.et_source_json)).check(matches(withText(containsString(sourceName))))

            startActivityByShell(
                "am start -W -a android.intent.action.MAIN -c android.intent.category.LAUNCHER " +
                    "-n ${context.packageName}/.ui.StartActivity"
            )
            waitForText("全文阅读")

            onView(withId(R.id.navigation_me)).perform(performViewClick())
            waitForText("书源管理")
            waitForText("音量键翻页")
            assertViewAbove(R.id.tv_book_source, R.id.switch_volume)
        } finally {
            server.shutdown()
        }
    }

    private fun enqueueBookPages(server: MockWebServer) {
        enqueueSearchPage(server)
        enqueueSearchPage(server)
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

    private fun enqueueSearchPage(server: MockWebServer) {
        server.enqueue(
            MockResponse().setBody(searchPage("真机自动书", "作者：测试作者", "/book/auto"))
        )
    }

    private fun searchPage(name: String, author: String, bookUrl: String): String {
        return """
            <html><body>
              <div class="result">
                <a class="title" href="$bookUrl">$name</a>
                <span class="author">$author</span>
              </div>
            </body></html>
        """.trimIndent()
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

    private fun startActivityByShell(command: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val descriptor = instrumentation.uiAutomation.executeShellCommand(command)
        FileInputStream(descriptor.fileDescriptor).bufferedReader().use { it.readText() }
        descriptor.close()
    }

    private fun performViewClick(): ViewAction = object : ViewAction {
        override fun getConstraints(): Matcher<View> = isDisplayed()

        override fun getDescription(): String = "perform view click without input injection"

        override fun perform(uiController: UiController, view: View) {
            view.performClick()
            uiController.loopMainThreadUntilIdle()
        }
    }

    private fun assertViewAbove(aboveId: Int, belowId: Int) {
        var aboveTop = 0
        var belowTop = 0
        onView(withId(aboveId)).check { view, noViewFoundException ->
            noViewFoundException?.let { throw it }
            aboveTop = view.top
        }
        onView(withId(belowId)).check { view, noViewFoundException ->
            noViewFoundException?.let { throw it }
            belowTop = view.top
        }
        assertTrue("Expected first view to be above second view", aboveTop < belowTop)
    }

    private fun openShelfPage() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        startActivityByShell(
            "am start -W -a android.intent.action.MAIN -c android.intent.category.LAUNCHER " +
                "-n ${context.packageName}/.ui.StartActivity"
        )
        waitForText("全文阅读")
        onView(withId(R.id.navigation_shelf)).perform(performViewClick())
        onView(withId(R.id.tv_source_search)).check(matches(isDisplayed()))
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

    private fun tryWaitForText(text: String, timeout: Long = 8000): Boolean {
        return kotlin.runCatching {
            waitForText(text, timeout)
        }.isSuccess
    }

    private fun tryWaitForRecyclerTextContains(text: String, timeout: Long = 8000): Boolean {
        return kotlin.runCatching {
            waitUntil(timeout) {
                onView(withId(R.id.rv_shelf)).perform(scrollUntilTextContaining(text))
            }
        }.isSuccess
    }

    private fun searchRealDouluoSourceResults() {
        searchShelfSources("斗罗大陆")
        if (!tryWaitForRecyclerTextContains("斗罗", 30000)) {
            searchShelfSources("斗罗")
            assertTrue(
                "Expected real source search to return a result containing 斗罗",
                tryWaitForRecyclerTextContains("斗罗", 30000)
            )
        }
    }

    private fun openSearchResultDialog(text: String) {
        repeat(3) { index ->
            val clicked = kotlin.runCatching {
                onView(withId(R.id.rv_shelf)).perform(clickRecyclerItemContaining(text, index))
            }.isSuccess
            if (!clicked) {
                throw AssertionError("No clickable search result containing value: $text")
            }
            if (tryWaitForText("开始阅读", 20000)) {
                return
            }
        }
        throw AssertionError("Search result did not open a readable book detail dialog")
    }

    private fun searchShelfSources(keyword: String) {
        onView(withId(R.id.et_search)).perform(replaceText(keyword), closeSoftKeyboard())
        onView(withText("搜书源")).perform(performViewClick())
    }

    private fun clickRecyclerItemContaining(text: String, matchIndex: Int): ViewAction = object : ViewAction {
        override fun getConstraints(): Matcher<View> = isAssignableFrom(RecyclerView::class.java)

        override fun getDescription(): String = "click RecyclerView item containing: $text"

        override fun perform(uiController: UiController, view: View) {
            val recyclerView = view as RecyclerView
            val adapter = recyclerView.adapter ?: throw AssertionError("RecyclerView has no adapter")
            var matched = 0
            for (position in 0 until adapter.itemCount) {
                recyclerView.scrollToPosition(position)
                uiController.loopMainThreadForAtLeast(100)
                val itemView = recyclerView.findViewHolderForAdapterPosition(position)?.itemView
                if (itemView != null && containsTextPart(itemView, text)) {
                    if (matched == matchIndex) {
                        itemView.performClick()
                        uiController.loopMainThreadUntilIdle()
                        return
                    }
                    matched++
                }
            }
            throw AssertionError("Text containing value not found in RecyclerView: $text")
        }
    }

    private fun scrollUntilTextContaining(text: String): ViewAction = object : ViewAction {
        override fun getConstraints(): Matcher<View> = isAssignableFrom(RecyclerView::class.java)

        override fun getDescription(): String = "scroll RecyclerView until text contains: $text"

        override fun perform(uiController: UiController, view: View) {
            val recyclerView = view as RecyclerView
            val adapter = recyclerView.adapter ?: throw AssertionError("RecyclerView has no adapter")
            for (position in 0 until adapter.itemCount) {
                recyclerView.scrollToPosition(position)
                uiController.loopMainThreadForAtLeast(100)
                if (containsTextPart(recyclerView, text)) {
                    return
                }
            }
            throw AssertionError("Text containing value not found in RecyclerView: $text")
        }
    }

    private fun containsTextPart(view: View, text: String): Boolean {
        if (view is TextView && view.text?.toString()?.contains(text) == true) {
            return true
        }
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                if (containsTextPart(view.getChildAt(index), text)) {
                    return true
                }
            }
        }
        return false
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
