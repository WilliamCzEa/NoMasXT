package com.intermedio.nomasxt.datos.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "numeros")
data class NumerosEntity(
    @PrimaryKey
    val numero: String
)
