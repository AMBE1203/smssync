package com.example.dtclnh.domain.reposiory


import com.example.dtclnh.domain.model.SmsModel
import kotlinx.coroutines.flow.Flow


interface ISmsRepository {
    suspend fun saveSmsToLocal(sms: MutableList<SmsModel>)

    suspend fun getAllSms(): Flow<MutableList<SmsModel>>


}