package com.intermedio.nomasxt.datos.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intermedio.nomasxt.datos.entity.CatalogoFAQEntity

@Dao
interface CatalogoFAQDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregaTodos(catalogoFAQ: List<CatalogoFAQEntity>)

    @Query("SELECT COUNT(*) FROM catalogo_faq")
    suspend fun contar(): Int

    @Query("SELECT id, question, answer FROM catalogo_faq")
    suspend fun obtenerTodos(): List<CatalogoFAQEntity>
}
