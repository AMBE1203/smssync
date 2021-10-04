package com.example.dtclnh.presentation.page.login

import com.example.dtclnh.presentation.base.viewstate.BaseViewState


data class LoginViewState<T : Any>(
    val isShowLoading: Boolean,
    val isError: Throwable?,
    val isSuccess: T?,
    val showErrorValidEmail: Boolean = false
) : BaseViewState<T>(isShowLoading, isError, isSuccess)