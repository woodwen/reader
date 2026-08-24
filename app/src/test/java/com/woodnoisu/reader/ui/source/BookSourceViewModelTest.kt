package com.woodnoisu.reader.ui.source

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.woodnoisu.reader.model.source.BookSource
import com.woodnoisu.reader.network.HtmlService
import com.woodnoisu.reader.persistence.BookSourceDao
import com.woodnoisu.reader.repository.source.BookSourceCheckResult
import com.woodnoisu.reader.repository.source.BookSourceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BookSourceViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dispatcher = TestCoroutineDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        dispatcher.cleanupTestCoroutines()
    }

    @Test
    fun checkSourceUsesDefaultKeywordAndPublishesSummary() {
        val repository = FakeRepository().apply {
            results.add(checkResult("https://source.example", available = true))
        }
        val viewModel = viewModel(repository)
        val source = bookSource("https://source.example")

        viewModel.checkSource(source, " ")
        dispatcher.advanceUntilIdle()

        assertEquals(listOf("https://source.example" to BookSourceRepository.DEFAULT_CHECK_KEYWORD), repository.calls)
        val state = viewModel.checkState.value!!
        assertFalse(state.running)
        assertEquals(1, state.completed)
        assertEquals(1, state.success)
        assertTrue(state.summary.contains("检测完成"))
    }

    @Test
    fun checkSourcesReportsEmptyList() {
        val viewModel = viewModel(FakeRepository())

        viewModel.checkSources(emptyList(), "关键词")

        assertEquals("当前没有可检测书源", viewModel.toast.value)
    }

    @Test
    fun cancelCheckStopsRemainingSources() {
        val repository = FakeRepository().apply {
            delayMs = 1_000L
            results.add(checkResult("https://one.example", available = true))
            results.add(checkResult("https://two.example", available = true))
        }
        val viewModel = viewModel(repository)

        viewModel.checkSources(
            listOf(bookSource("https://one.example"), bookSource("https://two.example")),
            "关键词"
        )
        dispatcher.advanceTimeBy(100L)
        viewModel.cancelCheck()
        dispatcher.advanceUntilIdle()

        val state = viewModel.checkState.value!!
        assertFalse(state.running)
        assertTrue(state.cancelled > 0)
        assertTrue(state.summary.contains("检测已取消"))
    }

    private fun viewModel(repository: FakeRepository): BookSourceViewModel {
        return BookSourceViewModel(repository).apply {
            ioDispatcher = dispatcher
        }
    }

    private fun bookSource(url: String): BookSource {
        return BookSource(
            bookSourceName = "测试源",
            bookSourceUrl = url
        )
    }

    private fun checkResult(url: String, available: Boolean): BookSourceCheckResult {
        return BookSourceCheckResult(
            bookSourceUrl = url,
            displayName = "测试源",
            available = available,
            stage = "完成",
            message = "书源可用"
        )
    }

    private class FakeRepository : BookSourceRepository(FakeBookSourceDao(), HtmlService()) {
        val calls = arrayListOf<Pair<String, String>>()
        val results = ArrayDeque<BookSourceCheckResult>()
        var delayMs: Long = 0L

        override fun liveData(searchKey: String?): LiveData<List<BookSource>> {
            return MutableLiveData(emptyList())
        }

        override suspend fun checkSourceAvailability(
            source: BookSource,
            keyword: String
        ): BookSourceCheckResult {
            calls.add(source.bookSourceUrl to keyword)
            if (delayMs > 0) {
                delay(delayMs)
            }
            return results.removeFirst()
        }
    }

    private class FakeBookSourceDao : BookSourceDao {
        override fun liveDataAll(): LiveData<List<BookSource>> = MutableLiveData(emptyList())

        override fun liveDataSearch(key: String): LiveData<List<BookSource>> = MutableLiveData(emptyList())

        override suspend fun getAllEnabled(): List<BookSource> = emptyList()

        override suspend fun getBookSource(key: String): BookSource? = null

        override suspend fun count(): Int = 0

        override suspend fun maxOrder(): Int = 0

        override suspend fun insert(vararg bookSource: BookSource) {}

        override suspend fun update(vararg bookSource: BookSource) {}

        override suspend fun delete(vararg bookSource: BookSource) {}

        override suspend fun delete(key: String) {}
    }
}
