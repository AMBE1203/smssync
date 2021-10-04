package com.example.dtclnh.data.source.remote

import com.example.dtclnh.domain.model.BaseResponse
import com.example.dtclnh.domain.model.ParamLogin
import com.example.dtclnh.domain.model.UserModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import javax.inject.Singleton

@Singleton
interface ILoginApi {

    @POST("auth/login")
    suspend fun login(@Body paramLogin: ParamLogin?): Response<BaseResponse<UserModel>>

}