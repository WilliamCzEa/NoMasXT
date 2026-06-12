package com.intermedio.nomasxt.datos.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intermedio.nomasxt.datos.entity.PerfilEntity

@Dao
interface PerfilDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregarPerfil(perfil: PerfilEntity)

    @Query("SELECT id, ageRange, state, gender, eMail FROM perfil WHERE eMail = :eMail")
    suspend fun buscarPerfil(eMail: String): PerfilEntity

    @Query("SELECT id, ageRange, state, gender, eMail FROM perfil LIMIT 1")
    suspend fun obtenerPerfilGuardado(): PerfilEntity?
}
