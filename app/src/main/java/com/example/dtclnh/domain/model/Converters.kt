package com.example.dtclnh.domain.model

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromStatus(status: BackupStatus): String {
        return status.name
    }

    @TypeConverter
    fun toStatus(statusString: String): BackupStatus {
        return BackupStatus.valueOf(statusString)
    }
}