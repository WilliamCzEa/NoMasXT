package com.intermedio.nomasxt.datos.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalogo_faq")
data class CatalogoFAQEntity(
    @PrimaryKey
    val id: Int,
    val question: String,
    val answer: String
)
