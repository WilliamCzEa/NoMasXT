package com.intermedio.nomasxt.datos.remoto.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReportesDto (
    @Json(name = "appInfo")
    val appInfo: AppInfoDto,
    @Json(name = "userId")
    val userId: String?,
    @Json(name = "reportedMsisdn")
    val reportedMsisdn: String?,
    @Json(name = "label")
    val label: String?
)
