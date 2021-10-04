package com.example.dtclnh.util

import android.content.Context
import android.widget.Toast
import com.example.dtclnh.MainApplication

fun Context.toast(value: String) = toast { value }

inline fun Context.toast(value: () -> String) =
    Toast.makeText(this, value(), Toast.LENGTH_SHORT).show()


inline fun showToast(value: () -> String): Unit = MainApplication.INSTANCE.toast(value)