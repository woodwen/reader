package com.woodnoisu.reader.ui.square

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.woodnoisu.reader.model.source.SourceOption
import com.woodnoisu.reader.repository.SquareRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SquareViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun startsWithoutFixedSourceFallback() {
        val viewModel = viewModel()

        assertFalse(viewModel.hasShopName())
        assertTrue(viewModel.getSourceOptionsSnapshot().isEmpty())
        assertEquals("书城", viewModel.getShopTitle())
    }

    @Test
    fun clearsSelectionWhenManagedSourcesAreEmpty() {
        val viewModel = viewModel()
        viewModel.fetchShopOption(sourceOption("https://one.example", "源一", canExplore = true))

        val changed = viewModel.selectDefaultSourceIfNeeded(emptyList())

        assertTrue(changed)
        assertFalse(viewModel.hasShopName())
        assertEquals(SquareViewModel.NO_SOURCE_MESSAGE, viewModel.squareMessage.value)
    }

    @Test
    fun keepsCurrentSelectionWhenItStillExists() {
        val viewModel = viewModel()
        val current = sourceOption("https://two.example", "源二", canExplore = true)
        viewModel.fetchShopOption(current)

        val changed = viewModel.selectDefaultSourceIfNeeded(
            listOf(
                sourceOption("https://one.example", "源一", canExplore = true),
                current
            )
        )

        assertFalse(changed)
        assertEquals("https://two.example", viewModel.getShopName())
        assertEquals("源二", viewModel.getShopTitle())
    }

    @Test
    fun switchesToFirstSourceWhenCurrentSelectionIsRemoved() {
        val viewModel = viewModel()
        viewModel.fetchShopOption(sourceOption("https://old.example", "旧源", canExplore = true))

        val changed = viewModel.selectDefaultSourceIfNeeded(
            listOf(sourceOption("https://new.example", "新源", canExplore = true))
        )

        assertTrue(changed)
        assertEquals("https://new.example", viewModel.getShopName())
        assertEquals("新源", viewModel.getShopTitle())
        assertTrue(viewModel.canExplore())
        assertEquals(listOf("发现"), viewModel.getTypes())
    }

    @Test
    fun searchOnlySourceDoesNotTriggerExploreSearchWithoutKeyword() {
        val viewModel = viewModel()
        viewModel.fetchShopOption(sourceOption("https://search.example", "搜索源", canExplore = false))

        viewModel.fetchSearch(1)

        assertEquals(SquareViewModel.SEARCH_ONLY_MESSAGE, viewModel.squareMessage.value)
    }

    private fun sourceOption(key: String, name: String, canExplore: Boolean): SourceOption {
        return SourceOption(
            key = key,
            name = name,
            dynamic = true,
            canExplore = canExplore
        )
    }

    private fun viewModel(): SquareViewModel {
        return SquareViewModel(mock<SquareRepository>())
    }
}
