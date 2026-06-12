package com.intermedio.nomasxt.datos.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intermedio.nomasxt.datos.entity.CatalogoEdadesEntity

@Dao
interface CatalogoEdadesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregaTodos(catalogoEdades: List<CatalogoEdadesEntity>)

    @Query("SELECT COUNT(*) FROM catalogo_edades")
    suspend fun contar(): Int

    @Query("SELECT id, ageRange FROM catalogo_edades")
    suspend fun obtenerTodas(): List<CatalogoEdadesEntity>
}
