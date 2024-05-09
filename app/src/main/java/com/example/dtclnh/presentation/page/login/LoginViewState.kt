package com.example.dtclnh.presentation.page.login

import com.example.dtclnh.core.Constants.CLIENT_ID
import com.example.dtclnh.presentation.base.viewstate.BaseViewState


data class LoginViewState<T : Any>(
    val isShowLoading: Boolean = false,
    val isError: Throwable? = null,
    val isSuccess: T? = null,
    val showErrorValidEmail: Boolean = false,
    val clientId: String? = CLIENT_ID,
    val apiKey: String? = null,
    val apiUrl: String? = null,
    val numberSmsNotBackUp: Int? = 0,
    val numberSmsBackUpSuccess: Int? = 0,
) : BaseViewState<T>(isShowLoading, isError, isSuccess)