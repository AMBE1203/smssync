package com.example.dtclnh.presentation.page.home

import com.example.dtclnh.core.Constants
import com.example.dtclnh.presentation.base.viewstate.BaseViewState


data class HomeViewState<T : Any>(
    val isShowLoading: Boolean = false,
    val isError: Throwable? = null,
    val isSuccess: T? = null,
) : BaseViewState<T>(isShowLoading, isError, isSuccess)