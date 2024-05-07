package com.example.dtclnh.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SmsDataWrapper(
    @Json(name = "data") val data: List<SmsParam>
)