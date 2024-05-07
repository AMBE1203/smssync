package com.example.dtclnh.data.repository

import android.util.Log
import com.example.dtclnh.core.IOResults
import com.example.dtclnh.core.performSafeNetworkApiCall
import com.example.dtclnh.data.source.local.AppDatabase
import com.example.dtclnh.data.source.remote.IBackUpApi
import com.example.dtclnh.domain.model.BackupStatus
import com.example.dtclnh.domain.model.BaseResponse
import com.example.dtclnh.domain.model.SmsDataWrapper
import com.example.dtclnh.domain.model.SmsModel
import com.example.dtclnh.domain.reposiory.ISmsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
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
        kotlin.run {
            deleteNonExistingEntities(sms.map { it.receivedAt }.toList())
        }

    }

    override suspend fun getAllSmsForBackup(): Flow<MutableList<SmsModel>> =
        database.smsDao().loadSmsByBackupStatus(backupStatus = BackupStatus.FAIL)

    override suspend fun getAllSmsInDb(): Flow<MutableList<SmsModel>> =
        database.smsDao().loadAllSMSInDb()

    override suspend fun deleteNonExistingEntities(
        receivedAtsInbox: List<String>,
    ) {
        database.smsDao().loadSmsByBackupStatus(backupStatus = BackupStatus.SUCCESS).collect { i ->
            val idsInDb = i.map { it.receivedAt }
            val idsToDelete = idsInDb.filterNot { receivedAtsInbox.contains(it) }
            if (idsToDelete.isNotEmpty()) {
                database.smsDao().deleteNonExistingEntities(idsToDelete)
            }
        }

    }

    override suspend fun backup(
        sms: SmsDataWrapper,
    ): Flow<IOResults<BaseResponse<List<SmsModel>>>> = performSafeNetworkApiCall {
        iBackUpApi.backUp(sms)
    }

    override suspend fun findAndUpdateStatus(receivedAts: List<String>) {
        val entities = database.smsDao().findByReceivedAt(receivedAts)
        if (entities.isNotEmpty()) {
            val idsToUpdate = entities.map { it.receivedAt }
            database.smsDao().updateStatusByReceivedAt(idsToUpdate, BackupStatus.SUCCESS)
        }
    }

    override suspend fun countMessageNotBackUp(): Flow<Int> =
        database.smsDao().countMessageNotBackUp(backupStatus = BackupStatus.FAIL)


    private fun messageExists(receivedAt: String, sender: String): Boolean {
        val messageCount = database.smsDao().getMessageCountByDateTimeAndSender(receivedAt, sender)
        return messageCount > 0
    }
}