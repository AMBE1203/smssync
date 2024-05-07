package com.example.dtclnh.domain.usecase

import com.example.dtclnh.domain.reposiory.ISmsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FindAndUpdateStatusUseCase @Inject constructor(private val repository: ISmsRepository) {
    suspend fun execute(receivedAts: List<String>) =
        repository.findAndUpdateStatus(receivedAts = receivedAts)

}