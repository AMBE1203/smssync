package com.example.dtclnh.domain.usecase

import com.example.dtclnh.core.IOResults
import com.example.dtclnh.domain.model.BaseResponse
import com.example.dtclnh.domain.model.SmsModel
import com.example.dtclnh.domain.reposiory.ISmsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class BackUpUseCase @Inject constructor(private val repository: ISmsRepository) :
    IUseCase<MutableList<SmsModel>, BaseResponse<SmsModel>> {
    override suspend fun execute(input: MutableList<SmsModel>): Flow<IOResults<BaseResponse<SmsModel>>> =
        repository.backup(sms = input)


}