package com.woodnoisu.reader.ui.source

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.woodnoisu.reader.databinding.ItemBookSourceBinding
import com.woodnoisu.reader.model.source.BookSource

class BookSourceAdapter(
    private val callback: Callback
) : RecyclerView.Adapter<BookSourceAdapter.ViewHolder>() {
    private val items = arrayListOf<BookSource>()

    class ViewHolder(val binding: ItemBookSourceBinding) : RecyclerView.ViewHolder(binding.root)

    interface Callback {
        fun edit(source: BookSource)
        fun delete(source: BookSource)
        fun updateEnabled(source: BookSource, enabled: Boolean)
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
            switchEnabled.setOnCheckedChangeListener(null)
            switchEnabled.isChecked = source.enabled
            switchEnabled.setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView.isPressed) {
                    callback.updateEnabled(source, isChecked)
                }
            }
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
}
