package com.intermedio.nomasxt.datos.remoto.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppInfoDto (
    @Json(name = "os")
    val os: String,
    @Json(name = "versionOs")
    val versionOs: String,
    @Json(name = "versionApp")
    val versionApp: String,
    @Json(name = "skuApp")
    val skuApp: String
)
