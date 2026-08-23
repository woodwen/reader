package com.woodnoisu.reader.model.source

data class SourceOption(
    val key: String,
    val name: String,
    val dynamic: Boolean,
    val canExplore: Boolean = false
)
