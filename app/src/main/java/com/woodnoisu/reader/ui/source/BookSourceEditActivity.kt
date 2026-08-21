package com.woodnoisu.reader.ui.source

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import com.woodnoisu.reader.R
import com.woodnoisu.reader.base.BaseActivity
import com.woodnoisu.reader.databinding.ActivityBookSourceEditBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookSourceEditActivity : BaseActivity() {
    private lateinit var binding: ActivityBookSourceEditBinding
    private val viewModel: BookSourceEditViewModel by viewModels()
    private var sourceKey: String? = null

    override fun getRLayout(): Int = R.layout.activity_book_source_edit

    override fun initView() {
        binding = ActivityBookSourceEditBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0))
    }

    override fun initListener() {
        binding.tvBack.setOnClickListener { finish() }
        binding.tvPaste.setOnClickListener {
            getClipText()?.let { binding.etSourceJson.setText(it) }
        }
        binding.tvSave.setOnClickListener {
            viewModel.saveSource(binding.etSourceJson.text?.toString().orEmpty(), sourceKey)
        }
        viewModel.sourceJson.observe(this) {
            binding.etSourceJson.setText(it)
        }
        viewModel.saved.observe(this) {
            setResult(RESULT_OK)
            finish()
        }
        viewModel.toast.observe(this) {
            if (!it.isNullOrBlank()) Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    override fun initData() {
        sourceKey = intent.getStringExtra(EXTRA_SOURCE_KEY)
        binding.tvTitle.text = if (sourceKey.isNullOrBlank()) "新增书源" else "编辑书源"
        viewModel.loadSource(sourceKey)
    }

    private fun getClipText(): String? {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboard.primaryClip ?: return null
        if (clipData.itemCount == 0) return null
        return clipData.getItemAt(0).text?.toString()
    }

    companion object {
        private const val EXTRA_SOURCE_KEY = "sourceKey"

        fun start(context: Context, sourceKey: String?) {
            val intent = Intent(context, BookSourceEditActivity::class.java)
            intent.putExtra(EXTRA_SOURCE_KEY, sourceKey)
            context.startActivity(intent)
        }
    }
}
