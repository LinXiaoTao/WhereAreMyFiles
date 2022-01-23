package com.kewei.files.util

import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtil {

    private val simpleDateFormat: SimpleDateFormat by lazy {
        return@lazy SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    }

    fun Long.toSimpleDateFormat(): String = simpleDateFormat.format(Date(this))
}