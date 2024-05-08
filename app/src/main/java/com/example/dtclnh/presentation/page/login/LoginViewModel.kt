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
import com.example.dtclnh.domain.model.SyncStatus
import com.example.dtclnh.domain.usecase.*
import com.example.dtclnh.presentation.base.ext.postNext
import com.example.dtclnh.presentation.base.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.wait
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val fetchAllSmsForBackUpUseCase: FetchAllSmsForBackUpUseCase,
    private val saveSmsUseCase: SaveSmsUseCase,
    private val application: Application,
    private val sharedPreferences: SharedPreferences,
    private val countMessageNotBackUpUseCase: CountMessageNotBackUpUseCase,
    private val loadAllSmsInDbUseCase: LoadAllSmsInDbUseCase,
    private val loadAllSmsInInboxUseCase: LoadAllSmsInInboxUseCase,


    ) : BaseViewModel() {


    private val context: Context = application.applicationContext
    private val state: LoginViewState<MutableList<SmsModel>> = LoginViewState()

    private val _stateLiveData: MutableLiveData<LoginViewState<MutableList<SmsModel>>> =
        MutableLiveData(state)

    val stateLiveData: LiveData<LoginViewState<MutableList<SmsModel>>> = _stateLiveData

    init {
        viewModelScope.launch {

            loadAllSmsInDbUseCase.execute().collect {
                it.forEach { s ->
                    if (s.backupStatus == BackupStatus.SUCCESS) {
                        Log.e("AMBE1203", "${s.sender} ${s.content}")
                    }
                }

            }
        }

    }

    fun initData() {
        viewModelScope.launch {
            _stateLiveData.postNext { state ->
                state.copy(apiUrl = getApiUrl(), apiKey = getApiKey(), clientId = getClientId())
            }
        }
    }


    fun readAllSMS() {
        viewModelScope.launch {
            val listSms = loadAllSmsInInboxUseCase.execute()
            if (listSms.isNotEmpty()) {
                saveSmsUseCase.execute(listSms)
            }
        }

    }

    init {
        countNumberSmsForBackUp()
    }

    fun countNumberSmsForBackUp() {
        viewModelScope.launch {
            countMessageNotBackUpUseCase.execute().collect {
                _stateLiveData.postNext { state ->
                    state.copy(
                        numberSmsNotBackUp = it
                    )
                }
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