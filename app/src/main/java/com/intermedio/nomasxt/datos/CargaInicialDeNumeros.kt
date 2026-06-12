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
        //Verificación de tamaño de tabla
        val totalNumeros = numerosDao.cuentaNumeros()

        if(totalNumeros == 0) {
            try {
                val jsonString = context.assets.open(archivo).bufferedReader().use { it.readText() }

                //Obtiene el adaptador Moshi para la estructura del JSON
                val adaptador = moshi.adapter(NumerosJson::class.java)
                val numerosJson = adaptador.fromJson(jsonString)

                numerosJson?.numeros?.let { numerosList ->
                    //Mapea la lista de Strings a la lista de Entities
                    val entidades = numerosList.map { NumerosEntity(numero = it) }
                    //Inserta los datos en la base de datos
                    numerosDao.insertaTodos(entidades)

                }


            } catch (e: Exception) {
                Log.d("NoMasXT", "Ocurrió el error ${e.message}")

            }
        }
    }
}