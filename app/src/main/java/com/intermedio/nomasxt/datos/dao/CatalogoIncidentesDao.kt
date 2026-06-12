package com.intermedio.nomasxt.datos.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intermedio.nomasxt.datos.entity.CatalogoIncidentesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogoIncidentesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregaTodos(catalogoIncidentes: List<CatalogoIncidentesEntity>)

    @Query("SELECT COUNT(*) FROM catalogo_incidentes")
    suspend fun contar(): Int

    @Query("SELECT id, incidence FROM catalogo_incidentes")
    fun obtenerCatalogoIncidentes(): Flow<List<CatalogoIncidentesEntity>>
}
