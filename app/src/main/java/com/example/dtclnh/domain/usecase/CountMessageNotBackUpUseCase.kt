package com.example.dtclnh.domain.usecase

import com.example.dtclnh.domain.reposiory.ISmsRepository
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CountMessageNotBackUpUseCase @Inject constructor(private val repository: ISmsRepository) {
    fun execute(): Int = repository.countMessageNotBackUp()

}