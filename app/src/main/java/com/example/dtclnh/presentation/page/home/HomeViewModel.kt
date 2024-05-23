package com.example.dtclnh.presentation.page.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.dtclnh.domain.model.SmsModel
import com.example.dtclnh.domain.usecase.FetchAllSmsForBackUpUseCase
import com.example.dtclnh.presentation.base.ext.postNext
import com.example.dtclnh.presentation.base.viewmodel.BaseViewModel
import com.example.dtclnh.presentation.page.login.LoginViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fetchAllSmsForBackUpUseCase: FetchAllSmsForBackUpUseCase,

    ) : BaseViewModel() {

    private val state: HomeViewState<MutableList<SmsModel>> = HomeViewState()


    private val _stateLiveData: MutableLiveData<HomeViewState<MutableList<SmsModel>>> =
        MutableLiveData(state)

    val stateLiveData: LiveData<HomeViewState<MutableList<SmsModel>>> = _stateLiveData

    fun fetchSms() {
        viewModelScope.launch {
            val listSms = fetchAllSmsForBackUpUseCase.execute()
            _stateLiveData.postNext { state ->
                state.copy(
                    isSuccess = listSms,

                    )
            }

        }
    }

}