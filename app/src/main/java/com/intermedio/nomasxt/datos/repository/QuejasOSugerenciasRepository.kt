package com.intermedio.nomasxt.datos.repository

import android.util.Log
import com.intermedio.nomasxt.datos.dao.CatalogoIncidentesDao
import com.intermedio.nomasxt.datos.entity.CatalogoIncidentesEntity
import com.intermedio.nomasxt.datos.remoto.ApiService
import com.intermedio.nomasxt.datos.remoto.dto.QuejaOSugerenciaRequestDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuejasOSugerenciasRepository @Inject constructor(
    private val apiService: ApiService,
    private val catalogoIncidentesDao: CatalogoIncidentesDao
) {
    fun obtenerCatalogoIncidencias(): Flow<List<CatalogoIncidentesEntity>> {
        return catalogoIncidentesDao.obtenerCatalogoIncidentes()
    }

    suspend fun enviarQuejaOSugerencia(
        quejaOSugerenciaRequestDto: QuejaOSugerenciaRequestDto
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // El backend responde como {"result": {...}}; si Retrofit falla aqui, evitamos cerrar la app.
                val respuesta = apiService.quejaOSugerencia(quejaOSugerenciaRequestDto)
                if (respuesta.isSuccessful) {
                    val result = respuesta.body()?.result
                    if (result?.resultCode == 200) {
                        Log.d("nomasxt", "QuejasOSugerenciasRepository | Se pudo enviar correctamente la informacion")
                        Result.success("Solicitud enviada exitosamente")
                    } else {
                        Log.d("nomasxt", "QuejasOSugerenciasRepository | Los datos no se guardaron en el servidor")
                        Result.failure(Exception("Error al enviar la solicitud: ${result?.resultMessage}"))
                    }
                } else {
                    Log.d(
                        "nomasxt",
                        "QuejasOSugerenciasRepository | Error HTTP ${respuesta.code()}: ${respuesta.errorBody()?.string()}"
                    )
                    Result.failure(Exception("Error al enviar la solicitud, intente mas tarde."))
                }
            } catch (e: Exception) {
                Log.d("nomasxt", "QuejasOSugerenciasRepository | Ocurrio un error: ${e.message}")
                Result.failure(Exception("Error de red o desconocido"))
            }
        }
    }
}
