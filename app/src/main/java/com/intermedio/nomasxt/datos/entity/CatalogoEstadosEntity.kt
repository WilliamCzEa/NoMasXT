package com.intermedio.nomasxt.datos.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalogo_estados")
data class CatalogoEstadosEntity(
    @PrimaryKey
    val id: Int,
    val name: String
)
