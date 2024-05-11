package com.example.dtclnh.domain.reposiory


import com.example.dtclnh.core.IOResults
import com.example.dtclnh.domain.model.BackupStatus
import com.example.dtclnh.domain.model.BaseResponse
import com.example.dtclnh.domain.model.SmsDataWrapper
import com.example.dtclnh.domain.model.SmsModel
import kotlinx.coroutines.flow.Flow


interface ISmsRepository {
    suspend fun saveSmsToLocal(sms: MutableList<SmsModel>, smsCome: Boolean)

    suspend fun getAllSmsForBackup(): MutableList<SmsModel>

    suspend fun getAllSmsInDb(): MutableList<SmsModel>
    suspend fun getAllSmsInInbox(): MutableList<SmsModel>
    suspend fun deleteNonExistingEntities(
        receivedAtsInbox: List<String>,
    )

    suspend fun backup(sms: SmsDataWrapper): Flow<IOResults<BaseResponse<List<SmsModel>>>>


    suspend fun findAndUpdateStatus(receivedAts: List<String>)
    suspend fun countMessageByBackUpStatus(backupStatus: BackupStatus): Int


}