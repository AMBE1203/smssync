package com.example.dtclnh.data.repository

import com.example.dtclnh.core.IOResults
import com.example.dtclnh.core.performSafeNetworkApiCall
import com.example.dtclnh.data.source.local.AppDatabase
import com.example.dtclnh.data.source.remote.ILoginApi
import com.example.dtclnh.domain.model.BaseResponse
import com.example.dtclnh.domain.model.ParamLogin
import com.example.dtclnh.domain.model.UserModel
import com.example.dtclnh.domain.reposiory.ILoginRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val iLoginApi: ILoginApi
) : ILoginRepository {
    override suspend fun login(paramLogin: ParamLogin):
            Flow<IOResults<BaseResponse<UserModel>>> =
        performSafeNetworkApiCall {
            iLoginApi.login(paramLogin)
        }
}