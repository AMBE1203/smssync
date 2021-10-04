package com.example.dtclnh.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.dtclnh.data.source.local.dao.UserDao
import com.example.dtclnh.domain.model.UserModel

@Database(entities = [UserModel::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    companion object {
        const val DB_NAME = "MyDatabase.db"
    }

    abstract fun userDao(): UserDao


}