package com.intermedio.nomasxt.datos.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reportes")
data class ReportesEntity(
    @PrimaryKey
    val id: String,
    val fecha: String,
    val folio: String,
    val etiqueta: String,
    val numeroReportado: String,
    val status: String
)
