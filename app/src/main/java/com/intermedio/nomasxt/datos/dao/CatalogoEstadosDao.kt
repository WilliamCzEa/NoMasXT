package com.intermedio.nomasxt.datos.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intermedio.nomasxt.datos.entity.CatalogoEstadosEntity

@Dao
interface CatalogoEstadosDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregaTodos(catalogoEstados: List<CatalogoEstadosEntity>)

    @Query("SELECT COUNT(*) FROM catalogo_estados")
    suspend fun contar(): Int

    @Query("SELECT id, name FROM catalogo_estados")
    suspend fun obtenerTodos(): List<CatalogoEstadosEntity>
}
