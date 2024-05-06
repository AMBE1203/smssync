package com.example.dtclnh.data.repository

import com.example.dtclnh.core.IOResults
import com.example.dtclnh.core.performSafeNetworkApiCall
import com.example.dtclnh.data.source.local.AppDatabase
import com.example.dtclnh.data.source.remote.IBackUpApi
import com.example.dtclnh.domain.model.BackupStatus
import com.example.dtclnh.domain.model.BaseResponse
import com.example.dtclnh.domain.model.SmsModel
import com.example.dtclnh.domain.reposiory.ISmsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val iBackUpApi: IBackUpApi

) : ISmsRepository {

    override suspend fun saveSmsToLocal(sms: MutableList<SmsModel>) {
        sms.forEach {
            if (!messageExists(it.receivedAt, it.sender)) {
                database.smsDao().insert(it)
            }
        }
    }

    override suspend fun getAllSmsForBackup(): Flow<MutableList<SmsModel>> =
        database.smsDao().loadSmsByBackupStatus(backupStatus = BackupStatus.FAIL)

    override suspend fun backup(
        sms: MutableList<SmsModel>,
    ): Flow<IOResults<BaseResponse<SmsModel>>> = performSafeNetworkApiCall {
        iBackUpApi.backUp(sms)
    }


    private fun messageExists(receivedAt: String, sender: String): Boolean {
        val messageCount = database.smsDao().getMessageCountByDateTimeAndSender(receivedAt, sender)
        return messageCount > 0
    }
}