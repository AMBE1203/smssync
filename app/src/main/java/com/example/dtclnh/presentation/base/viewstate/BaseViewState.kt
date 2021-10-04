package com.example.dtclnh.presentation.base.viewstate

open class BaseViewState<out T : Any>(
    val isLoading: Boolean,
    val throwable: Throwable?,
    val result: T?
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseViewState<*>
        if (isLoading != other.isLoading) return false
        if (throwable != other.throwable) return false
        if (result != other.result) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isLoading.hashCode()
        result = 31 * result + (throwable?.hashCode() ?: 0)
        result = 31 * result + (result.hashCode() ?: 0)
        return result
    }
}