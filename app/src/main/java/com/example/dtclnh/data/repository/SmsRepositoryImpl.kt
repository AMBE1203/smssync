package com.example.dtclnh.data.repository

import com.example.dtclnh.data.source.local.AppDatabase
import com.example.dtclnh.domain.model.BackupStatus
import com.example.dtclnh.domain.model.SmsModel
import com.example.dtclnh.domain.reposiory.ISmsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
) : ISmsRepository {

    override suspend fun saveSmsToLocal(sms: MutableList<SmsModel>) {
        sms.forEach {
            if (!messageExists(it.receivedAt, it.sender)) {
                database.smsDao().insert(it)
            }
        }
    }

    override suspend fun getAllSms(): Flow<MutableList<SmsModel>> = database.smsDao().loadAll(backupStatus = BackupStatus.FAIL)

    private fun messageExists(receivedAt: String, sender: String): Boolean {
        val messageCount = database.smsDao().getMessageCountByDateTimeAndSender(receivedAt, sender)
        return messageCount > 0
    }
}