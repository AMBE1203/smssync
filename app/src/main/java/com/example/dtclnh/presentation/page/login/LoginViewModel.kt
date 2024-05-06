package com.example.dtclnh.presentation.page.login

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.dtclnh.core.Constants.API_KEY_KEY
import com.example.dtclnh.core.Constants.API_URL_KEY
import com.example.dtclnh.core.Constants.CLIENT_ID_KEY
import com.example.dtclnh.domain.model.BackupStatus
import com.example.dtclnh.domain.model.SmsModel
import com.example.dtclnh.domain.usecase.FetchAllSmsUseCase
import com.example.dtclnh.domain.usecase.SaveSmsUseCase
import com.example.dtclnh.presentation.base.ext.postNext
import com.example.dtclnh.presentation.base.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val fetchAllSmsUseCase: FetchAllSmsUseCase,
    private val saveSmsUseCase: SaveSmsUseCase,
    private val application: Application,
    private val sharedPreferences: SharedPreferences
) : BaseViewModel() {
    private val context: Context = application.applicationContext
    private val state: LoginViewState<MutableList<SmsModel>> = LoginViewState()

    private val _stateLiveData: MutableLiveData<LoginViewState<MutableList<SmsModel>>> =
        MutableLiveData(state)

    val stateLiveData: LiveData<LoginViewState<MutableList<SmsModel>>> = _stateLiveData

    fun initData() {
        viewModelScope.launch {
            _stateLiveData.postNext { state ->
                state.copy(apiUrl = getApiUrl(), apiKey = getApiKey(), clientId = getClientId())
            }
        }
    }

    fun readAllSMS() {
        val contentResolver: ContentResolver = context.contentResolver
        val smsURI: Uri = Uri.parse("content://sms/inbox")
        val cursor = contentResolver.query(smsURI, null, null, null, null)
        val listSms: MutableList<SmsModel> = mutableListOf()
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {


                    val id: String = cursor.getString(cursor.getColumnIndexOrThrow("_id"))
                    val address: String = cursor.getString(cursor.getColumnIndexOrThrow("address"))
                    val body: String = cursor.getString(cursor.getColumnIndexOrThrow("body"))
                    val date: Long = cursor.getLong(cursor.getColumnIndexOrThrow("date"))
                    val read: String = cursor.getString(cursor.getColumnIndexOrThrow("read"))
                    val sms: SmsModel = SmsModel(
                        smsId = id,
                        sender = address,
                        content = body,
                        receivedAt = date.toString(),
                        status = read,
                        backupStatus = BackupStatus.FAIL
                    )

                    listSms.add(sms)


                } while (cursor.moveToNext())
            }
            cursor.close()

            if (listSms.isNotEmpty()) {
                viewModelScope.launch {
                    saveSmsUseCase.execute(listSms)
                }
            }
        }
    }

    fun fetchAllSmsFromLocal() {
        viewModelScope.launch {
            fetchAllSmsUseCase.execute().collect {
                _stateLiveData.postNext { state ->
                    state.copy(
                        isSuccess = it
                    )
                }

            }
        }
    }

    fun setClientId(clientId: String) {
        viewModelScope.launch {
            _stateLiveData.postNext { state ->
                state.copy(clientId = clientId)
            }
        }

    }

    fun setApiKey(apiKey: String) {
        viewModelScope.launch {
            _stateLiveData.postNext { state ->
                state.copy(apiKey = apiKey)
            }
        }

    }

    fun setApiUrl(apiUrl: String) {
        viewModelScope.launch {
            _stateLiveData.postNext { state ->
                state.copy(apiUrl = apiUrl)
            }
        }
    }

    fun saveApiKey(apiKey: String?) {
        viewModelScope.launch {
            sharedPreferences.edit().putString(API_KEY_KEY, apiKey ?: _stateLiveData.value?.apiKey)
                .apply()

        }
    }

    fun saveApiUrl(apiUrl: String?) {
        viewModelScope.launch {
            sharedPreferences.edit().putString(API_URL_KEY, apiUrl ?: _stateLiveData.value?.apiUrl)
                .apply()
        }
    }

    fun saveClientId(clientId: String?) {
        viewModelScope.launch {

            sharedPreferences.edit()
                .putString(CLIENT_ID_KEY, clientId ?: _stateLiveData.value?.clientId)
                .apply()
        }
    }

    fun getClientId(): String? {
        return sharedPreferences.getString(CLIENT_ID_KEY, "")
    }

    fun getApiKey(): String? {
        return sharedPreferences.getString(API_KEY_KEY, "")
    }

    fun getApiUrl(): String? {
        return sharedPreferences.getString(API_URL_KEY, "")
    }


}