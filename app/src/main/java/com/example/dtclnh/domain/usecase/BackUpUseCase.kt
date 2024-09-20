package com.example.dtclnh.domain.usecase

import com.example.dtclnh.core.IOResults
import com.example.dtclnh.domain.model.BackupResponse
import com.example.dtclnh.domain.model.BaseResponse
import com.example.dtclnh.domain.model.SmsDataWrapper
import com.example.dtclnh.domain.model.SmsModel
import com.example.dtclnh.domain.model.SmsParam
import com.example.dtclnh.domain.reposiory.ISmsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class BackUpUseCase @Inject constructor(private val repository: ISmsRepository) :
    IUseCase<SmsParam, Any> {
    override suspend fun execute(input: SmsParam): Flow<IOResults<Any>> =
        repository.backup(sms = input)


}