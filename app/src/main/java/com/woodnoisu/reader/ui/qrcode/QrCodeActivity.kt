package com.woodnoisu.reader.ui.qrcode

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import com.woodnoisu.reader.R
import com.woodnoisu.reader.base.BaseActivity
import com.woodnoisu.reader.databinding.ActivityQrcodeCaptureBinding

class QrCodeActivity : BaseActivity() {
    private lateinit var binding: ActivityQrcodeCaptureBinding

    private val requestCamera = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
            showScanner()
        } else {
            Toast.makeText(this, "扫描二维码需要相机权限", Toast.LENGTH_SHORT).show()
        }
    }

    private val selectQrImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@registerForActivityResult
        val text = kotlin.runCatching {
            contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input)?.let { parseBitmap(it) }
            }
        }.getOrNull()
        if (text.isNullOrBlank()) {
            Toast.makeText(this, "未识别到二维码", Toast.LENGTH_SHORT).show()
        } else {
            finishWithResult(text)
        }
    }

    override fun getRLayout(): Int = R.layout.activity_qrcode_capture

    override fun initView() {
        binding = ActivityQrcodeCaptureBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0))
    }

    override fun initListener() {
        binding.tvBack.setOnClickListener { finish() }
        binding.tvGallery.setOnClickListener { selectQrImage.launch("image/*") }
    }

    override fun initData() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            showScanner()
        } else {
            requestCamera.launch(Manifest.permission.CAMERA)
        }
    }

    fun finishWithResult(text: String?) {
        val resultText = text ?: return
        val intent = Intent()
        intent.putExtra(QrCodeResult.EXTRA_RESULT, resultText)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun showScanner() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_content, QrCodeFragment())
            .commitAllowingStateLoss()
    }

    private fun parseBitmap(bitmap: Bitmap): String? {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val source = RGBLuminanceSource(bitmap.width, bitmap.height, pixels)
        val reader = MultiFormatReader()
        return kotlin.runCatching {
            reader.decode(BinaryBitmap(HybridBinarizer(source))).text
        }.recoverCatching {
            reader.decode(BinaryBitmap(GlobalHistogramBinarizer(source))).text
        }.getOrNull().also {
            reader.reset()
        }
    }
}
