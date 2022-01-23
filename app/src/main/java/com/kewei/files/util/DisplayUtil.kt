package com.kewei.files.util

import android.util.TypedValue

object DisplayUtil {

    fun Int.dpToPx() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, toFloat(), RuntimeContext.appContext.resources.displayMetrics
    )
}