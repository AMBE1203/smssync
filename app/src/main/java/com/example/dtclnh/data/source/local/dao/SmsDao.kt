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

    @Query("SELECT COUNT(*) FROM SMS WHERE receivedAt = :receivedAt AND sender = :sender")
    fun getMessageCountByDateTimeAndSender(receivedAt: String, sender: String): Int


    @Query("SELECT * FROM SMS WHERE backupStatus = :backupStatus")
    fun loadSmsByBackupStatus(backupStatus: BackupStatus): Flow<MutableList<SmsModel>>

    @Query("SELECT * FROM SMS")
    fun loadAllSMSInDb(): Flow<MutableList<SmsModel>>

    @Delete
    suspend fun delete(smsModel: SmsModel)

    @Query("DELETE FROM SMS WHERE receivedAt NOT IN (:receivedAts) AND backupStatus = :backupStatus")
    suspend fun deleteNonExistingEntities(
        receivedAts: List<String>,
        backupStatus: BackupStatus
    )

    @Query("DELETE FROM SMS")
    suspend fun deleteAll()
}