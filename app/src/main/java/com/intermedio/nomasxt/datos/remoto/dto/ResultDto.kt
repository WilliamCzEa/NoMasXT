package com.intermedio.nomasxt.datos.remoto.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ResultDto(
    @Json(name = "resultCode")
    val resultCode: Int,
    @Json(name = "resultMessage")
    val resultMessage: String
)
