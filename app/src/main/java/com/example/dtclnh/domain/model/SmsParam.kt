package com.example.dtclnh.domain.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

data class SmsParam(
    @SerializedName("phone") val phone: String,
    @SerializedName("message") val message: String,
    @SerializedName("timeSent") val timeSent: String,
)