package com.intermedio.nomasxt.datos.remoto.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NumerosReportadosDto (
    @Json(name = "reportDate")
    val reportDate: String,
    @Json(name = "folio")
    val folio: String,
    @Json(name = "msisdn")
    val msisdn: String
)
