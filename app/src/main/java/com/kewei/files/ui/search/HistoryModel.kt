package com.kewei.files.ui.search

import android.net.Uri

data class HistoryModel(
    val id: Long,
    val displayName: String,
    val dateModified: Long,
    val mineType: String,
    val extension: Any? = null,
    val contentUri: Uri
)

data class HistoryImageModel(val width: Int, val height: Int)

data class HistoryVideoModel(val width: Int, val height: Int)