package com.example.dtclnh.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.example.dtclnh.core.IOResults
import com.example.dtclnh.core.performSafeNetworkApiCall
import com.example.dtclnh.data.source.local.AppDatabase
import com.example.dtclnh.data.source.remote.IBackUpApi
import com.example.dtclnh.domain.model.BackupStatus
import com.example.dtclnh.domain.model.BaseResponse
import com.example.dtclnh.domain.model.SmsDataWrapper
import com.example.dtclnh.domain.model.SmsModel
import com.example.dtclnh.domain.reposiory.ISmsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val iBackUpApi: IBackUpApi,
    @ApplicationContext private val context: Context

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

    override suspend fun getAllSmsForBackup(): MutableList<SmsModel> =
        database.smsDao().loadSmsByBackupStatus(backupStatus = BackupStatus.FAIL)

    override suspend fun getAllSmsInDb(): Flow<MutableList<SmsModel>> =
        database.smsDao().loadAllSMSInDb()

    override suspend fun getAllSmsInInbox(): MutableList<SmsModel> {
        val contentResolver: ContentResolver = context.contentResolver
        val smsURI: Uri = Uri.parse("content://sms/inbox")
        val cursor = contentResolver.query(smsURI, null, null, null, null)
        val listSms: MutableList<SmsModel> = mutableListOf()
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {


                    val id: String = cursor.getString(cursor.getColumnIndexOrThrow("_id"))
                    val address: String =
                        cursor.getString(cursor.getColumnIndexOrThrow("address"))
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

        }
        return listSms
    }

    override suspend fun deleteNonExistingEntities(
        receivedAtsInbox: List<String>,
    ) {
        val i = database.smsDao().loadSmsByBackupStatus(backupStatus = BackupStatus.SUCCESS)
        val idsInDb = i.map { it.receivedAt }
        val idsToDelete = idsInDb.filterNot { receivedAtsInbox.contains(it) }
        if (idsToDelete.isNotEmpty()) {
            database.smsDao().deleteNonExistingEntities(idsToDelete)
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

    override suspend fun countMessageByBackUpStatus(backupStatus: BackupStatus): Flow<Int> =
        database.smsDao().countMessageByBackUpStatus(backupStatus = backupStatus)


    private fun messageExists(receivedAt: String, sender: String): Boolean {
        val messageCount = database.smsDao().getMessageCountByDateTimeAndSender(receivedAt, sender)
        return messageCount > 0
    }
}