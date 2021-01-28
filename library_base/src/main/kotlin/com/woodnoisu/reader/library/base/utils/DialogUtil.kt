package com.woodnoisu.reader.library.base.utils

import android.text.InputType
import androidx.fragment.app.FragmentActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.listItems

object DialogUtil {
    fun showDialog(
        activity: FragmentActivity,
        title: String = "",
        message: String = "",
        positiveButtonText: String = "",
        negativeButtonText: String = "",
        list: List<String> = listOf(),
        hint: String = "",
        onPositiveClick: () -> Unit = {},
        onNegativeClick: () -> Unit = {},
        onListItemClick: (String) -> Unit = {},
        onInput: (String) -> Unit = {}
    ) {
        MaterialDialog(activity).show {
            if (title.isNotBlank()) {
                title(text = title)
            }
            if (message.isNotBlank()) {
                message(text = message)
            }
            if (positiveButtonText.isNotBlank()) {
                positiveButton(text = positiveButtonText) {
                    onPositiveClick()
                }
            }
            if (negativeButtonText.isNotBlank()) {
                negativeButton(text = negativeButtonText) {
                    onNegativeClick()
                }
            }
            if (list.isNotEmpty()) {
                listItems(items = list) { _, _, text ->
                    onListItemClick(text.toString())
                }
            }
            if (hint.isNotBlank()) {
                input(
                    hint = hint,
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
                ) { _, text ->
                    onInput(text.toString())
                }
            }
            lifecycleOwner(activity)
        }
    }
}