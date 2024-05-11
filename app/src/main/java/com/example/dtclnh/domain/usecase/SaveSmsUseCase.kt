package com.example.dtclnh.domain.usecase

import com.example.dtclnh.domain.model.SmsModel
import com.example.dtclnh.domain.reposiory.ISmsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaveSmsUseCase @Inject constructor(private val repository: ISmsRepository) {
    suspend fun execute(sms: MutableList<SmsModel>, smsCome: Boolean) = repository.saveSmsToLocal(sms = sms, smsCome)

}