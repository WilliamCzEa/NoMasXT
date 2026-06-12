package com.intermedio.nomasxt.datos.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalogo_incidentes")
data class CatalogoIncidentesEntity(
    @PrimaryKey
    val id: Int,
    val incidence: String
)
