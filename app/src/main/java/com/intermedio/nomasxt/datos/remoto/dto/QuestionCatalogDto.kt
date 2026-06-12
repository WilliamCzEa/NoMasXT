package com.intermedio.nomasxt.datos.remoto.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QuestionCatalogDto(
    @Json(name = "id")
    val id: Int,
    @Json(name = "question")
    val question: String,
    @Json(name = "answer")
    val answer: String
)
