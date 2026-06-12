package com.intermedio.nomasxt.datos.remoto.dto

import com.squareup.moshi.Json

data class NumerosJson (
    @Json(name = "numeros")
    val numeros: List<String>
)
