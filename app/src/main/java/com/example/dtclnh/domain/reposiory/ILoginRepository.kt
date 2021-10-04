package com.example.dtclnh.domain.reposiory

import com.example.dtclnh.core.IOResults
import com.example.dtclnh.domain.model.BaseResponse
import com.example.dtclnh.domain.model.ParamLogin
import com.example.dtclnh.domain.model.UserModel
import kotlinx.coroutines.flow.Flow

interface ILoginRepository {
    suspend fun login(paramLogin: ParamLogin): Flow<IOResults<BaseResponse<UserModel>>>
}