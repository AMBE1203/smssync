package com.example.dtclnh.domain.model

data class BaseResponse<T>(
    var status: Int,
    var message: String,
    var data: T?,
    var devMessage: String,
)