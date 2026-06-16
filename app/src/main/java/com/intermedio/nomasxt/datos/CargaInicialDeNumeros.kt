package com.intermedio.nomasxt.datos

import android.content.Context
import android.util.Log
import com.intermedio.nomasxt.datos.dao.NumerosDao
import com.intermedio.nomasxt.datos.entity.NumerosEntity
import com.intermedio.nomasxt.datos.remoto.dto.NumerosJson
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CargaInicialDeNumeros @Inject constructor(
    @ApplicationContext private val context: Context,
    private val numerosDao: NumerosDao,
    private val moshi: Moshi
) {

    suspend fun cargarNumerosDeJson(archivo: String) = withContext(Dispatchers.IO) {
        try {
            val totalNumeros = numerosDao.cuentaNumeros()
            val jsonString = context.assets.open(archivo).bufferedReader().use { it.readText() }

            val adaptador = moshi.adapter(NumerosJson::class.java)
            val numerosJson = adaptador.fromJson(jsonString)

            numerosJson?.numeros?.let { numerosList ->
                val entidades = numerosList.map { NumerosEntity(numero = it) }

                // El DAO usa OnConflictStrategy.IGNORE, asi que esto agrega faltantes sin duplicar.
                numerosDao.insertaTodos(entidades)

                Log.d(
                    "NoMasXT",
                    "CargaInicialDeNumeros | Tabla tenia $totalNumeros numeros antes de sincronizar assets"
                )
            }
        } catch (e: Exception) {
            Log.d("NoMasXT", "CargaInicialDeNumeros | Error al cargar numeros: ${e.message}")
        }
    }
}
