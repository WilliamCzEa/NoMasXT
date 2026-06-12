package com.intermedio.nomasxt.datos.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "perfil")
data class PerfilEntity(
    @PrimaryKey
    val id: String,
    val ageRange: Int,
    val state: Int,
    val gender: Int,
    val eMail: String
)
