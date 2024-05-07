package com.example.dtclnh.domain.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

data class SmsParam(
    @SerializedName("smsId") val smsId: String,
    @SerializedName("clientId") val clientId: String,
    @SerializedName("sender") val sender: String,
    @SerializedName("content") val content: String,
    @SerializedName("receivedAt") val receivedAt: String
)