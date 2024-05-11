package com.example.dtclnh.data.source.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dtclnh.domain.model.BackupStatus
import com.example.dtclnh.domain.model.SmsModel
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sms: SmsModel): Long

    @Query("SELECT COUNT(*) FROM SMS WHERE receivedAt = :receivedAt AND sender = :sender AND content = :body")
    fun getMessageCountByDateTimeAndSender(
        receivedAt: String,
        sender: String,
        body: String,
    ): Int

    @Query("SELECT COUNT(*) FROM SMS WHERE backupStatus = :backupStatus AND sender = :sender AND content = :body")
    fun getMessageCountByContentAndSender(
        sender: String,
        body: String,
        backupStatus: BackupStatus
    ): Int

    @Query("SELECT COUNT(*) FROM SMS WHERE backupStatus = :backupStatus")
    fun countMessageByBackUpStatus(backupStatus: BackupStatus): Int

    @Query("SELECT * FROM SMS WHERE backupStatus = :backupStatus")
    fun loadSmsByBackupStatus(backupStatus: BackupStatus): MutableList<SmsModel>

    @Query("SELECT * FROM SMS")
    fun loadAllSMSInDb(): MutableList<SmsModel>

    @Delete
    suspend fun delete(smsModel: SmsModel)

    @Query("DELETE FROM SMS WHERE receivedAt NOT IN (:receivedAts)")
    suspend fun deleteNonExistingEntities(
        receivedAts: List<String>,
    )

    @Query("SELECT * FROM SMS WHERE receivedAt IN (:receivedAts)")
    suspend fun findByReceivedAt(receivedAts: List<String>): List<SmsModel>

    @Query("UPDATE SMS SET backupStatus = :backupStatus WHERE receivedAt IN (:receivedAts)")
    suspend fun updateStatusByReceivedAt(receivedAts: List<String>, backupStatus: BackupStatus)

    @Query("DELETE FROM SMS")
    suspend fun deleteAll()
}