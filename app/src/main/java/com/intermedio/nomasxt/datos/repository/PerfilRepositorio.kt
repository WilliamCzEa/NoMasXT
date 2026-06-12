package com.intermedio.nomasxt.datos.repository

import com.intermedio.nomasxt.datos.dao.PerfilDao
import com.intermedio.nomasxt.datos.entity.PerfilEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerfilRepositorio @Inject constructor(
    private val perfilDao: PerfilDao
) {
    suspend fun agregarPerfil(perfil: PerfilEntity) = withContext(Dispatchers.IO) {
        perfilDao.agregarPerfil(perfil)
    }

    suspend fun obtenerPerfil(): PerfilEntity? = withContext(Dispatchers.IO) {
        perfilDao.obtenerPerfilGuardado()
    }
}
