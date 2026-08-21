package com.woodnoisu.reader.model.source.rule

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContentRule(
    var content: String? = null,
    var nextContentUrl: String? = null,
    var webJs: String? = null,
    var sourceRegex: String? = null,
    var replaceRegex: String? = null,
    var imageStyle: String? = null
) : Parcelable
