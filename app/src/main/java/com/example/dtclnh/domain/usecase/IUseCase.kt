package com.example.dtclnh.domain.usecase

import com.example.dtclnh.core.IOResults
import kotlinx.coroutines.flow.Flow

interface IUseCase<in I : Any, out O : Any> {
    suspend fun execute(input: I): Flow<IOResults<O>>
}