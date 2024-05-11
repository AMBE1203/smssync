package com.example.dtclnh.domain.usecase

import com.example.dtclnh.domain.model.BackupStatus
import com.example.dtclnh.domain.reposiory.ISmsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CountMessageByBackUpStatusUseCase @Inject constructor(private val repository: ISmsRepository) {
    suspend fun execute(backupStatus: BackupStatus): Int =
        repository.countMessageByBackUpStatus(backupStatus)

}