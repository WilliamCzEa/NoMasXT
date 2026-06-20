package com.intermedio.nomasxt.datos.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intermedio.nomasxt.datos.entity.ReportesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregarReporte(reporte: ReportesEntity)

    @Query("SELECT id, fecha, folio, etiqueta, numeroReportado, status FROM reportes")
    fun obtenerTodosLosReportes(): Flow<List<ReportesEntity>>

    @Query("SELECT id, fecha, folio, etiqueta, numeroReportado, status FROM reportes WHERE status IN ('PENDIENTE', 'LOCAL')")
    suspend fun obtenerReportesPendientes(): List<ReportesEntity>

    @Query("DELETE FROM reportes WHERE numeroReportado = :numero")
    suspend fun eliminarReportePorNumero(numero: String)
}
