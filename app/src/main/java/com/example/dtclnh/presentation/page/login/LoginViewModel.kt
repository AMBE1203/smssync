package com.example.dtclnh.presentation.page.login

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.Telephony
import android.provider.Telephony.Sms
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val application: Application
) : BaseViewModel() {
    private val context: Context = application.applicationContext

    private val _loginLiveData: MutableLiveData<LoginViewState<BaseResponse<UserModel>>> =
        MutableLiveData(
            LoginViewState(
                isShowLoading = false,
                isError = null,
                isSuccess = null,
                showErrorValidEmail = false
            )
        )

    val loginLiveData: LiveData<LoginViewState<BaseResponse<UserModel>>> = _loginLiveData

    fun readAllSMS() {
        val contentResolver: ContentResolver = context.contentResolver
        val smsURI: Uri = Uri.parse("content://sms/")
        val cursor = contentResolver.query(smsURI, null, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val personId =
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.PERSON))

                    val id: String = cursor.getString(cursor.getColumnIndexOrThrow("_id"))
                    val address: String = cursor.getString(cursor.getColumnIndexOrThrow("address"))
                    val body: String = cursor.getString(cursor.getColumnIndexOrThrow("body"))
                    val date: Long = cursor.getLong(cursor.getColumnIndexOrThrow("date"))
                    val read: String = cursor.getString(cursor.getColumnIndexOrThrow("read"))
                    var type: String
                    if (cursor.getString(cursor.getColumnIndexOrThrow("type")).contains("1")) {
                        type = "inbox"
                    } else {
                        type = "sent"
                    }
                    var serviceSenter: String

                    if (cursor.getString(cursor.getColumnIndexOrThrow("service_center")) != null) {
                        serviceSenter =
                            cursor.getString(cursor.getColumnIndexOrThrow("service_center"));
                    } else {
                        serviceSenter = "";
                    }
                    Log.e(
                        "AMBE1203",
                        " id = $id \n personId = $personId \n address = $address \n body = $body \n date = $date \n read = $read \n type = $type  \n serviceSenter = $serviceSenter"
                    )


                } while (cursor.moveToNext())
            }
            cursor.close()
        }
    }


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