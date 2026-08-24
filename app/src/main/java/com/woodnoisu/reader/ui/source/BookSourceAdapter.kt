package com.woodnoisu.reader.ui.source

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.woodnoisu.reader.databinding.ItemBookSourceBinding
import com.woodnoisu.reader.model.source.BookSource
import com.woodnoisu.reader.repository.source.BookSourceRepository

class BookSourceAdapter(
    private val callback: Callback
) : RecyclerView.Adapter<BookSourceAdapter.ViewHolder>() {
    private val items = arrayListOf<BookSource>()
    private var checkingSourceUrl: String = ""

    class ViewHolder(val binding: ItemBookSourceBinding) : RecyclerView.ViewHolder(binding.root)

    interface Callback {
        fun edit(source: BookSource)
        fun delete(source: BookSource)
        fun updateEnabled(source: BookSource, enabled: Boolean)
        fun check(source: BookSource)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemBookSourceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val source = items[position]
        holder.binding.apply {
            tvName.text = if (source.bookSourceGroup.isNullOrBlank()) {
                source.displayName()
            } else {
                "${source.displayName()} (${source.bookSourceGroup})"
            }
            tvDesc.text = source.bookSourceUrl
            val status = source.failureStatusText()
            tvStatus.text = status
            tvStatus.visibility = if (status.isBlank()) View.GONE else View.VISIBLE
            switchEnabled.setOnCheckedChangeListener(null)
            switchEnabled.isChecked = source.enabled
            switchEnabled.setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView.isPressed) {
                    callback.updateEnabled(source, isChecked)
                }
            }
            val checking = source.bookSourceUrl == checkingSourceUrl
            tvCheck.text = if (checking) "检测中" else "检测"
            tvCheck.isEnabled = !checking
            tvCheck.setOnClickListener { callback.check(source) }
            tvEdit.setOnClickListener { callback.edit(source) }
            tvDelete.setOnClickListener { callback.delete(source) }
            root.setOnClickListener { callback.edit(source) }
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<BookSource>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun getItemsSnapshot(): List<BookSource> = items.toList()

    fun setCheckingSource(sourceUrl: String) {
        if (checkingSourceUrl == sourceUrl) return
        val oldUrl = checkingSourceUrl
        checkingSourceUrl = sourceUrl
        notifySourceChanged(oldUrl)
        notifySourceChanged(sourceUrl)
    }

    private fun notifySourceChanged(sourceUrl: String) {
        if (sourceUrl.isBlank()) return
        val index = items.indexOfFirst { it.bookSourceUrl == sourceUrl }
        if (index >= 0) {
            notifyItemChanged(index)
        }
    }

    private fun BookSource.failureStatusText(): String {
        val detectionError = bookSourceComment.orEmpty()
            .lineSequence()
            .firstOrNull { it.startsWith(BookSourceRepository.DETECTION_ERROR_PREFIX) }
            .orEmpty()
        if (detectionError.isNotBlank()) return detectionError
        val failed = bookSourceGroup.orEmpty()
            .split(Regex("[,;，；]"))
            .any { it.trim() == BookSourceRepository.FAILURE_GROUP }
        return if (failed) "检测失败" else ""
    }
}
