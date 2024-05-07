package com.example.dtclnh.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SmsParam(
    @Json(name = "smsId") val smsId: String,
    @Json(name = "clientId") val clientId: String,
    @Json(name = "sender") val sender: String,
    @Json(name = "content") val content: String,
    @Json(name = "receivedAt") val receivedAt: String
)