package com.alexvinov.movingpics.utils

import android.content.Context

fun Int.toDp(context: Context): Int {
    val density = context.resources.displayMetrics.density
    return (this * density).toInt()
}