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
        sms.forEach {
            Log.e("AMBE1203 ", "${it.toString()}")
        }
        getAllSmsInDb().collect {
            val receivedAts = it.map { sms ->
                sms.receivedAt
            }.toList()

            val receivedAtsInbox = sms.map { s ->
                s.receivedAt
            }
            deleteNonExistingEntities(receivedAts, receivedAtsInbox)
        }
    }

    override suspend fun getAllSmsForBackup(): Flow<MutableList<SmsModel>> =
        database.smsDao().loadSmsByBackupStatus(backupStatus = BackupStatus.FAIL)

    override suspend fun getAllSmsInDb(): Flow<MutableList<SmsModel>> =
        database.smsDao().loadAllSMSInDb()

    override suspend fun deleteNonExistingEntities(
        receivedAts: List<String>,
        receivedAtsInbox: List<String>,
    ) {
        val nonExistingIds = receivedAtsInbox.filter { id -> !receivedAts.contains(id) }
        database.smsDao().deleteNonExistingEntities(nonExistingIds, BackupStatus.SUCCESS)
    }

    override suspend fun backup(
        sms: SmsDataWrapper,
    ): Flow<IOResults<BaseResponse<SmsModel>>> = performSafeNetworkApiCall {
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