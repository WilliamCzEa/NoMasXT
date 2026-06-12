package com.intermedio.nomasxt.datos.remoto.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PerfilDto (
    @Json(name = "appInfo")
    val appInfo: AppInfoDto,
    @Json(name = "userId")
    val userId: String?,
    @Json(name = "stateId")
    val stateId: Int,
    @Json(name = "ageId")
    val ageId: Int,
    @Json(name = "gender")
    val gender: Int,
    @Json(name = "email")
    val email: String?,
    @Json(name = "token")
    val token: String?
)
