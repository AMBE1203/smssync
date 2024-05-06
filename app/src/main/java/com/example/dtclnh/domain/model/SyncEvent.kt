package com.example.dtclnh.domain.model

import javax.inject.Singleton

@Singleton
sealed class SyncEvent {
    object SyncStarted : SyncEvent()
    data class SyncSuccess(val message: String) : SyncEvent()
    data class SyncError(val error: Throwable) : SyncEvent()
}