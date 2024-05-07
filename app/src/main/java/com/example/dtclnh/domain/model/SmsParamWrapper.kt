package com.example.dtclnh.domain.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

data class SmsDataWrapper(
    @SerializedName( "data") val data: List<SmsParam>
)