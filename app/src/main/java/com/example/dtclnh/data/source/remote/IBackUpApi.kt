package com.example.dtclnh.data.source.remote

import com.example.dtclnh.domain.model.BaseResponse
import com.example.dtclnh.domain.model.SmsModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import javax.inject.Singleton

@Singleton
interface IBackUpApi {
    @POST("/post")
    suspend fun backUp(@Body sms: MutableList<SmsModel>): Response<BaseResponse<SmsModel>>
}