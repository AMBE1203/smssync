package com.example.dtclnh.di

import com.example.dtclnh.domain.model.SyncEvent
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Singleton
    @Provides
    fun provideSyncEventFlow(): SharedFlow<SyncEvent> {
        return MutableSharedFlow()
    }
}