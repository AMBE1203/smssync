package com.example.dtclnh.core

sealed class IOResults<out T : Any> {
    data class OnFailed(val error: Throwable) : IOResults<Nothing>()
    data class OnSuccess<out T : Any>(val data: T) : IOResults<T>()
}