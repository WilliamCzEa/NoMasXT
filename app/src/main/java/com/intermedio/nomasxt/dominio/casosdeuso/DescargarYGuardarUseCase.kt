package com.intermedio.nomasxt.dominio.casosdeuso

import android.util.Log
import com.intermedio.nomasxt.datos.repository.CatalogosRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class DescargarYGuardarUseCase @Inject constructor(
    private val repositorio: CatalogosRepository
) {
    suspend operator fun invoke(): Boolean? = withContext(Dispatchers.IO) {
        //Verificar si ya están descargados
        val yaDescargados = repositorio.estanDescargadosLosCatalogos()

        if(yaDescargados) {
            Log.d("NoMasXT", "DescargarYGuardarUseCase | Ya se habían descargado los catálogos")
            return@withContext false
        }

        //Si seguimos por aquí es que no se habían descargado los catálogos
        Log.d("NoMasXT", "DescargarYGuardarUseCase | Catálogos no descargados, consumiendo API...")

        try {
            val respuesta = repositorio.obtenerCatalogosDeLaRed()

            //Validar la respuesta Http
            if(respuesta.isSuccessful && respuesta.body() != null ) {
                val catalogosRespuesta = respuesta.body()!!

                //Validar resultCode del Json
                if(catalogosRespuesta.result.resultCode == 200) {
                    Log.d("NoMasXT", "DescargarYGuardarUseCase | Respuesta de Api exitosa, guardando en base de datos...")
                    repositorio.guardarCatalogosABd(catalogosRespuesta)
                    Log.d("NoMasXT", "DescargarYGuardarUseCase | Catálogos guardados exitosamente")
                    return@withContext true
                } else {
                    //Api devolvió un mensaje de Error
                    Log.d("NoMasXT", "DescargarYGuardarUseCase | API devolvió resultCode ${catalogosRespuesta.result.resultCode}: ${catalogosRespuesta.result.resultMessage}")
                    return@withContext false
                }

            } else {
                //Error Http (ej 404, 500)
                Log.d("NoMasXT", "DescargarYGuardarUseCase | Error HTTP ${respuesta.code()}: ${respuesta.message()}")
                return@withContext null
            }

        } catch(e: IOException) {
            //Error de red (sin conexión, timeout, etc)
            Log.d("NoMasXT","DescargarYGuardarUseCase | Error de red al descargar ${e.message}")
            return@withContext null
        } catch(e: HttpException) {
            Log.d("NoMasXT","DescargarYGuardarUseCase | Error de http inesperado al descargar catálogos: ${e.code()}: ${e.message()}")
            return@withContext null
        } catch(e: Exception) {
            Log.d("NoMasXT","DescargarYGuardarUseCase | Error inesperado al descargar/guardar catálogos: ${e.message}")
            return@withContext null
        }
    }
}
