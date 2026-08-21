package com.woodnoisu.reader.ui.source

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.woodnoisu.reader.R
import com.woodnoisu.reader.base.BaseActivity
import com.woodnoisu.reader.databinding.ActivityBookSourceBinding
import com.woodnoisu.reader.model.source.BookSource
import com.woodnoisu.reader.ui.qrcode.QrCodeResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookSourceActivity : BaseActivity(), BookSourceAdapter.Callback {
    private lateinit var binding: ActivityBookSourceBinding
    private lateinit var adapter: BookSourceAdapter
    private val viewModel: BookSourceViewModel by viewModels()

    private val openSourceFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            importSource(readText(it))
        }
    }

    private val scanQrCode = registerForActivityResult(QrCodeResult()) { text ->
        importSource(text)
    }

    override fun getRLayout(): Int = R.layout.activity_book_source

    override fun initView() {
        binding = ActivityBookSourceBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0))
        adapter = BookSourceAdapter(this)
        binding.rvSources.layoutManager = LinearLayoutManager(this)
        binding.rvSources.adapter = adapter
    }

    override fun initListener() {
        binding.tvBack.setOnClickListener { finish() }
        binding.tvAdd.setOnClickListener { BookSourceEditActivity.start(this, null) }
        binding.tvImport.setOnClickListener { showImportMenu() }
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.search(s?.toString().orEmpty())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        viewModel.sources.observe(this) {
            adapter.submitList(it)
            binding.tvEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.toast.observe(this) {
            if (!it.isNullOrBlank()) Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
        viewModel.importResult.observe(this) {
            Toast.makeText(this, "导入 $it 个书源", Toast.LENGTH_SHORT).show()
        }
    }

    override fun initData() {
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(it) }
    }

    override fun edit(source: BookSource) {
        BookSourceEditActivity.start(this, source.bookSourceUrl)
    }

    override fun delete(source: BookSource) {
        AlertDialog.Builder(this)
            .setMessage("确定删除 ${source.displayName()}？")
            .setNegativeButton("取消", null)
            .setPositiveButton("删除") { _, _ -> viewModel.delete(source) }
            .show()
    }

    override fun updateEnabled(source: BookSource, enabled: Boolean) {
        viewModel.updateEnabled(source, enabled)
    }

    private fun showImportMenu() {
        val popupMenu = PopupMenu(this, binding.tvImport)
        popupMenu.menu.add("粘贴导入")
        popupMenu.menu.add("文件导入")
        popupMenu.menu.add("在线 URL")
        popupMenu.menu.add("二维码")
        popupMenu.setOnMenuItemClickListener {
            when (it.title.toString()) {
                "粘贴导入" -> importSource(getClipText())
                "文件导入" -> openSourceFile.launch(arrayOf("text/*", "application/json"))
                "在线 URL" -> showUrlImportDialog()
                "二维码" -> scanQrCode.launch(null)
            }
            true
        }
        popupMenu.show()
    }

    private fun showUrlImportDialog() {
        val editText = EditText(this).apply {
            hint = "输入书源 URL"
            setSingleLine(true)
            setPadding(32, 16, 32, 16)
        }
        AlertDialog.Builder(this)
            .setTitle("在线导入")
            .setView(editText)
            .setNegativeButton("取消", null)
            .setPositiveButton("导入") { _, _ ->
                importSource(editText.text?.toString())
            }
            .show()
    }

    private fun importSource(text: String?) {
        viewModel.importSource(text)
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_VIEW -> intent.data?.toString()?.let { importSource(it) }
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)?.let { importSource(it) }
        }
    }

    private fun getClipText(): String? {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData: ClipData = clipboard.primaryClip ?: return null
        if (clipData.itemCount == 0) return null
        return clipData.getItemAt(0).text?.toString()
    }

    private fun readText(uri: Uri): String? {
        return kotlin.runCatching {
            contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        }.getOrNull()
    }
}
