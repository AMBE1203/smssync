package com.example.dtclnh.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey


enum class BackupStatus {
    SUCCESS,
    FAIL,
}

@Entity(tableName = "SMS")
data class SmsModel(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var smsId: String,
    var sender: String,
    var content: String,
    var receivedAt: String,
    var status: String,
    var backupStatus: BackupStatus
)
