package com.woodnoisu.reader.ui.source

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import com.woodnoisu.reader.model.source.BookSource
import com.woodnoisu.reader.repository.source.BookSourceRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class, sdk = [28])
class BookSourceAdapterTest {

    @Test
    fun bindsFailureStatusAndCheckClick() {
        val callback = FakeCallback()
        val adapter = BookSourceAdapter(callback)
        val source = BookSource(
            bookSourceName = "失效源",
            bookSourceUrl = "https://source.example",
            bookSourceGroup = BookSourceRepository.FAILURE_GROUP,
            bookSourceComment = "${BookSourceRepository.DETECTION_ERROR_PREFIX}正文：正文内容为空"
        )
        adapter.submitList(listOf(source))
        adapter.setCheckingSource(source.bookSourceUrl)

        val holder = adapter.onCreateViewHolder(parent(), 0)
        adapter.onBindViewHolder(holder, 0)

        assertEquals(View.VISIBLE, holder.binding.tvStatus.visibility)
        assertEquals("检测失败：正文：正文内容为空", holder.binding.tvStatus.text.toString())
        assertEquals("检测中", holder.binding.tvCheck.text.toString())
        assertFalse(holder.binding.tvCheck.isEnabled)

        adapter.setCheckingSource("")
        adapter.onBindViewHolder(holder, 0)
        holder.binding.tvCheck.performClick()

        assertSame(source, callback.checked)
    }

    private fun parent(): FrameLayout {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return FrameLayout(context)
    }

    private class FakeCallback : BookSourceAdapter.Callback {
        var checked: BookSource? = null

        override fun edit(source: BookSource) {}

        override fun delete(source: BookSource) {}

        override fun updateEnabled(source: BookSource, enabled: Boolean) {}

        override fun check(source: BookSource) {
            checked = source
        }
    }
}
