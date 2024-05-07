package com.example.dtclnh.presentation.base.ext

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toDateTimeString(): String {
    val date = Date(this)
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
    return dateFormat.format(date)
}