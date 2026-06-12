package com.intermedio.nomasxt.datos.remoto.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QuejaOSugerenciaRequestDto(
    @Json(name = "appInfo")
    val appInfo: AppInfoDto,
    @Json(name = "userId")
    val userId: String?,
    @Json(name = "type")
    val type: Int,
    @Json(name = "idIncidence")
    val idIncidence: Int?,
    @Json(name = "email")
    val email: String,
    @Json(name = "comments")
    val comments: String
)
