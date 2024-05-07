package com.example.dtclnh.domain.reposiory


import com.example.dtclnh.core.IOResults
import com.example.dtclnh.domain.model.BackupStatus
import com.example.dtclnh.domain.model.BaseResponse
import com.example.dtclnh.domain.model.SmsDataWrapper
import com.example.dtclnh.domain.model.SmsModel
import kotlinx.coroutines.flow.Flow


interface ISmsRepository {
    suspend fun saveSmsToLocal(sms: MutableList<SmsModel>)

    suspend fun getAllSmsForBackup(): Flow<MutableList<SmsModel>>

    suspend fun getAllSmsInDb(): Flow<MutableList<SmsModel>>
    suspend fun deleteNonExistingEntities(
        receivedAts: List<String>,
        receivedAtsInbox: List<String>,
    )

    suspend fun backup(sms: SmsDataWrapper): Flow<IOResults<BaseResponse<SmsModel>>>


    suspend fun findAndUpdateStatus(receivedAts: List<String>)
    suspend fun countMessageNotBackUp(): Flow<Int>


}