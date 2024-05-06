package com.example.dtclnh.domain.usecase

import com.example.dtclnh.core.IOResults

import com.example.dtclnh.domain.model.SmsModel
import com.example.dtclnh.domain.reposiory.ISmsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FetchAllSmsUseCase @Inject constructor(private val repository: ISmsRepository) {
    suspend fun execute(): Flow<MutableList<SmsModel>> = repository.getAllSms();

}