package com.woodnoisu.reader.utils

import android.content.Context
import android.view.View
import com.afollestad.materialdialogs.DialogCallback
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onCancel
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.callbacks.onPreShow
import com.afollestad.materialdialogs.callbacks.onShow
import com.afollestad.materialdialogs.customview.customView


object UtilDialog {
    fun showDialog(
        context: Context,
        title: String? = null,
        message: String? = null,
        positiveText: String? = null,
        negativeText: String? = null,
        neutralText: String? = null,
        positiveClick: DialogCallback = { it.dismiss() },
        negativeClick: DialogCallback = { it.dismiss() },
        neutralClick: DialogCallback = { it.dismiss() },
        onPreShow: DialogCallback = { },
        onShow: DialogCallback = { },
        onDismiss: DialogCallback = { },
        onCancel: DialogCallback = { },

        customView: View? = null,
        scrollable: Boolean = false,
        noVerticalPadding: Boolean = false,
        horizontalPadding: Boolean = false,
        dialogWrapContent: Boolean = false,

        showDialogNow: Boolean = true,
        cancelable: Boolean = true,
        cancelOnTouchOutside: Boolean = true,
        cornerRadius: Float = 0f
    ): MaterialDialog {
        val dialog = MaterialDialog(context)
            .title(text = title ?: "")
            .message(text = message ?: "")
            .onPreShow(onPreShow)
            .onShow(onShow)
            .onDismiss(onDismiss)
            .onCancel(onCancel)
        dialog.setCancelable(cancelable)
        dialog.setCanceledOnTouchOutside(cancelOnTouchOutside)
        dialog.cornerRadius(cornerRadius)
        if (!positiveText.isNullOrBlank()) {
            dialog.positiveButton(text = positiveText, click = positiveClick)
        }
        if (!negativeText.isNullOrBlank()) {
            dialog.negativeButton(text = negativeText, click = negativeClick)
        }
        if (!neutralText.isNullOrBlank()) {
            dialog.neutralButton(text = neutralText, click = neutralClick)
        }
        if (customView != null) {
            dialog.customView(view = customView, scrollable = scrollable, noVerticalPadding = noVerticalPadding,
                horizontalPadding = horizontalPadding, dialogWrapContent = dialogWrapContent)
        }
        if (showDialogNow) {
            dialog.show()
        }
        return dialog
    }


}