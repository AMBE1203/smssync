package com.example.dtclnh.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.dtclnh.data.source.local.dao.SmsDao
import com.example.dtclnh.domain.model.Converters
import com.example.dtclnh.domain.model.SmsModel

@Database(entities = [SmsModel::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        const val DB_NAME = "MyDatabase.db"
    }
    abstract fun smsDao(): SmsDao


}