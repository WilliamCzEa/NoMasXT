package com.intermedio.nomasxt.datos.repository

import com.intermedio.nomasxt.datos.dao.NumerosDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NumeroRepository @Inject constructor(
    private val numerosDao: NumerosDao
) {
    suspend fun verificarNumeroEnBD(numero: String): Boolean {
        return numerosDao.numeroALocalizar(numero) != null
    }
}