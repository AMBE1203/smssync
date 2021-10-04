package com.example.dtclnh.presentation.page.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.dtclnh.core.Errors
import com.example.dtclnh.core.getViewStateFlowForNetworkCall
import com.example.dtclnh.domain.model.BaseResponse
import com.example.dtclnh.domain.model.ParamLogin
import com.example.dtclnh.domain.model.UserModel
import com.example.dtclnh.domain.usecase.LoginUseCase
import com.example.dtclnh.presentation.base.ext.postNext
import com.example.dtclnh.presentation.base.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val loginUseCase: LoginUseCase) : BaseViewModel() {

    private val _loginLiveData: MutableLiveData<LoginViewState<BaseResponse<UserModel>>> =
        MutableLiveData(
            LoginViewState<BaseResponse<UserModel>>(
                isShowLoading = false,
                isError = null,
                isSuccess = null,
                showErrorValidEmail = false
            )
        )

    val loginLiveData: LiveData<LoginViewState<BaseResponse<UserModel>>> = _loginLiveData


    fun login(userName: String?, password: String?) {
        when (userName.isNullOrEmpty() || password.isNullOrEmpty()) {
            false -> viewModelScope.launch {
                getViewStateFlowForNetworkCall {
                    loginUseCase.execute(ParamLogin(userName, password))
                }.collect {
                    _loginLiveData.postNext { state ->
                        state.copy(
                            isError = it.throwable,
                            isShowLoading = it.isLoading,
                            isSuccess = null,
                            showErrorValidEmail = false
                        )
                    }
                }

            }
            true -> _loginLiveData.postNext { state ->
                state.copy(
                    isError = Errors.EmptyInputError,
                    isShowLoading = false,
                    isSuccess = null,
                    showErrorValidEmail = true
                )
            }
        }
    }


}