package com.example.dtclnh.domain.usecase

import com.example.dtclnh.core.IOResults
import com.example.dtclnh.domain.model.BaseResponse
import com.example.dtclnh.domain.model.ParamLogin
import com.example.dtclnh.domain.model.UserModel
import com.example.dtclnh.domain.reposiory.ILoginRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginUseCase @Inject constructor(private val repository: ILoginRepository) :
    IUseCase<ParamLogin, BaseResponse<UserModel>> {
    override suspend fun execute(input: ParamLogin): Flow<IOResults<BaseResponse<UserModel>>> =
        repository.login(paramLogin = input)
}