package com.example.dtclnh.presentation.base.ext

import android.os.Build
import androidx.annotation.RequiresApi
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.regex.Pattern

fun Long.toDateTimeString(): String {
    val date = Date(this)
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.getDefault())
    return dateFormat.format(date)
}

fun String.toDateTimeLong(): Long {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.getDefault())

    val date = dateFormat.parse(this)
    return date?.time ?: 0
}

fun isLink(inputString: String): Boolean {
    val pattern = Pattern.compile("^https?://\\S+")
    val matcher = pattern.matcher(inputString)
    return matcher.matches()
}

fun convertSecondsToMMSS(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

fun generateUniqueID(time: Long, senderId: String): String {
    val combinedString = "$time$senderId"
    val bytes = combinedString.toByteArray(Charsets.UTF_8)

    val md = MessageDigest.getInstance("SHA-256")
    val hashBytes = md.digest(bytes)

    val hexString = StringBuffer()
    for (byte in hashBytes) {
        val hex = Integer.toHexString(0xff and byte.toInt())
        if (hex.length == 1) hexString.append('0')
        hexString.append(hex)
    }

    return hexString.toString()
}
