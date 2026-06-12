package com.intermedio.nomasxt.datos.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intermedio.nomasxt.datos.entity.NumerosEntity

@Dao
interface NumerosDao {

    @Query("SELECT COUNT(*) FROM numeros")
    suspend fun cuentaNumeros(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertaTodos(numeros: List<NumerosEntity>)

    @Query("SELECT numero FROM numeros WHERE numero = :numero")
    suspend fun numeroALocalizar(numero: String): String?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertaNumero(numero: NumerosEntity)
}
