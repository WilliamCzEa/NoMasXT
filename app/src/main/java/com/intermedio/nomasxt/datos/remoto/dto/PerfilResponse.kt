package com.intermedio.nomasxt.datos.remoto.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PerfilResponse(
    @Json(name = "result")
    val result: ResultDto,
    @Json(name = "userId")
    val userId: String,
    @Json(name = "reportedNumbers")
    val reportedNumbers: List<NumerosReportadosDto>?
)
