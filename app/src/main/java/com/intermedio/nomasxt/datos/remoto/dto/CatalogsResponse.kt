package com.intermedio.nomasxt.datos.remoto.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CatalogsResponse(
    @Json(name = "result")
    val result: ResultDto,
    @Json(name = "ageCatalog")
    val ageCatalog: List<AgeCatalogDto>?,
    @Json(name = "incidencesCatalog")
    val incidencesCatalog: List<IncidenceCatalogDto>?,
    @Json(name = "questionsCatalog")
    val questionsCatalog: List<QuestionCatalogDto>?,
    @Json(name = "stateCatalog")
    val stateCatalog: List<StateCatalogDto>?
)
