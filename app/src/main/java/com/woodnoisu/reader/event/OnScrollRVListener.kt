package com.woodnoisu.reader.event

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class RVOScrollListener constructor(var layoutManager: LinearLayoutManager) : RecyclerView.OnScrollListener() {

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        val visibleItemCount: Int = layoutManager.childCount
        val totalItemCount: Int = layoutManager.itemCount
        val firstVisibleItemPosition: Int = layoutManager.findFirstVisibleItemPosition()

        if (newState == RecyclerView.SCROLL_STATE_IDLE && !isLoading() && !isLastPage()
                && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                && firstVisibleItemPosition >= 0) {
            loadMoreItems()
        }
    }

    abstract fun loadMoreItems()

    abstract fun totalPageCount(): Int

    abstract fun isLastPage(): Boolean
    abstract fun isLoading(): Boolean
}