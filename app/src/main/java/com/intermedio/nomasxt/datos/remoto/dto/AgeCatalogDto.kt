package com.intermedio.nomasxt.datos.remoto.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AgeCatalogDto(
    @Json(name = "id")
    val id: Int,
    @Json(name = "ageRange")
    val ageRange: String
)
