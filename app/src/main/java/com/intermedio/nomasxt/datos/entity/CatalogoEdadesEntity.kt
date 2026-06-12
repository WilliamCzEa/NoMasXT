package com.intermedio.nomasxt.datos.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalogo_edades")
data class CatalogoEdadesEntity (

    @PrimaryKey
    val id: Int,
    val ageRange: String

)

