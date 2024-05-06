package com.example.dtclnh.domain.model

sealed class SyncStatus {
    object SYNCING : SyncStatus()
    object SUCCESS : SyncStatus()
    object ERROR : SyncStatus()
}