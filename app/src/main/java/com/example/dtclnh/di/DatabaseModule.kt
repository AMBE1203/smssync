package com.example.dtclnh.di

import android.app.Application
import androidx.room.Room.databaseBuilder
import com.example.dtclnh.data.source.local.AppDatabase
import com.example.dtclnh.data.source.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
    @Provides
    @Singleton
    internal fun provideAppDatabase(application: Application): AppDatabase {
        return databaseBuilder(
            application,
            AppDatabase::class.java,
            AppDatabase.DB_NAME
        ).allowMainThreadQueries().build()
    }


    @Provides
    internal fun providePhotoDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }
}