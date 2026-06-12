package com.intermedio.nomasxt.datos.remoto.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BlockedNumberDto(
    @Json(name = "date")
    val date: String?,
    @Json(name = "folio")
    val folio: String?,
    @Json(name = "label")
    val label: String?,
    @Json(name = "phoneNumber")
    val phoneNumber: String?,
    @Json(name = "status")
    val status: String?
)
