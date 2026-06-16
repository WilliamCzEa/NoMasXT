package com.intermedio.nomasxt.datos.remoto.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeleteReportedNumberResponseDto(
    @Json(name = "result")
    val result: ResultDto
)
