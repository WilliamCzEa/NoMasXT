package com.intermedio.nomasxt.datos.repository

import android.util.Log
import com.intermedio.nomasxt.datos.dao.NumerosDao
import com.intermedio.nomasxt.datos.dao.ReportesDao
import com.intermedio.nomasxt.datos.entity.NumerosEntity
import com.intermedio.nomasxt.datos.entity.ReportesEntity
import com.intermedio.nomasxt.datos.remoto.ApiService
import com.intermedio.nomasxt.datos.remoto.dto.BlockedNumberDto
import com.intermedio.nomasxt.datos.remoto.dto.DeleteReportedNumberDto
import com.intermedio.nomasxt.datos.remoto.dto.ReportesDto
import com.intermedio.nomasxt.datos.remoto.dto.ReportesResponseDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportesRepository @Inject constructor(
    private val apiService: ApiService,
    private val numeroDao: NumerosDao,
    private val reportesDao: ReportesDao
) {

    /*
     * Reporta un número:
     * 1. Guarda el número en la tabla 'números' (siempre).
     * 2. Intenta enviarlo a la API
     * 3. Si la API responde exitosamente, guarda los reportes recibidos en la tabla 'reportes'.
     * @param numeroDto El DTO con la información del número a reportar (que se utilizará para la API y NumerosEntity)
     * @return true si la operación principal (guardar número localmente) fue exitosa, false si hubo un fallo fatal.
     */
    suspend fun reportarNumero(reporteDto: ReportesDto): String { // Boolean {
        return withContext(Dispatchers.IO) {      //Ejecutar en un hilo I/O
            try {
                // 1. Guardar el número en la tabla 'numeros' (siempre)
                val numeroEntity = NumerosEntity(
                    numero = reporteDto.reportedMsisdn.toString()
                )
                numeroDao.insertaNumero(numeroEntity)
                val numeroReportado = reporteDto.reportedMsisdn.toString()
                val reporteLocal = ReportesEntity(
                    id = numeroReportado,
                    fecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                    folio = "LOCAL",
                    etiqueta = reporteDto.label ?: "Reporte local",
                    numeroReportado = numeroReportado,
                    status = "LOCAL"
                )
                reportesDao.agregarReporte(reporteLocal)
                val mensajeNumeroGuardado = "Número registrado correctamente en su lista de bloqueo."

                Log.d("nomasxt", "ReportesRepository | Número ${reporteDto.reportedMsisdn} guardado en la tabla 'numeros'")
                // 2. Intentar enviar a la API
                val respuesta = apiService.reportarNumero(reporteDto)

                if(respuesta.isSuccessful) {
                    val body = respuesta.body()
                    if(body != null && body.result.resultCode == 200 ) {
                        val blockedNumberDto: BlockedNumberDto? = body.blockedNumber
                        if(blockedNumberDto != null) {
                            //Convertir DTOs a Entities para la tabla 'reportes'
                            //Nuevamente, revisar como debe estar mapeada la información
                            val reporteEntity= ReportesEntity (
                                //Mapeo de BlockedNumberDto a ReportesEntity
                                id = blockedNumberDto.folio!!,
                                fecha = blockedNumberDto.date!!,
                                folio = blockedNumberDto.folio!!,
                                etiqueta = blockedNumberDto.label!!,
                                numeroReportado =  blockedNumberDto.phoneNumber!!,
                                status = blockedNumberDto.status!!
                            )
                            reportesDao.eliminarReportePorNumero(reporteEntity.numeroReportado)
                            reportesDao.agregarReporte(reporteEntity)
                            Log.d("nomasxt", "ReportesRepository | Reportes de la API guardados en la tabla 'reportes'.")
                            val mensajeReporteGuardado = "Reporte de la API guardado en la tabla 'reportes'"
                            return@withContext "$mensajeNumeroGuardado\n$mensajeReporteGuardado"    //Éxito completo, guardado en ambas tablas
                        } else {
                            Log.d("nomasxt", "ReportesRepository | API reportarnúmero - Éxito HTTP, result code es 200 o similares pero no hay valores de regreso ")
                            val mensajeReporteNulo = "Error al enviar el reporte, intentar más tarde."
                            return@withContext "$mensajeNumeroGuardado\n$mensajeReporteNulo"
                        }
                    } else {
                        Log.d("nomasxt", "ReportesRepository | Éxito HTTP, result code no es 200: ${body?.result?.resultMessage}")
                        val mensajeCodigoApiError = "Error al enviar el reporte, intentar más tarde."
                        return@withContext "$mensajeNumeroGuardado\n$mensajeCodigoApiError"
                    }
                } else {
                    Log.d("nomasxt", "ReportesRepository | Error HTTP: ${respuesta.code()}: ${respuesta.errorBody()?.string()}")
                    val mensajeCodigoHTTPError = "Error al enviar el reporte, intentar más tarde."
                    return@withContext "$mensajeNumeroGuardado\n$mensajeCodigoHTTPError"
                }
                //Si llegamos aquí significa que el número se guardó localmente,
                //pero la llamada al API no fue completamente exitosa o no trajo reportes para guardar.
                //Aún así, la operación local de guardado fue exitosa.
                return@withContext "$mensajeNumeroGuardado"
            } catch(e: Exception) {
                //Manejar excepciones de red o parsing
                Log.d("nomasxt", "ReportesRepository | Excepción ${e.message}")
                e.printStackTrace()  //Para depuración
                val mensajeErrorGeneral = "No se pudo registrar el número reportado, ocurrió un error, intente más tarde."
                return@withContext "$mensajeErrorGeneral"  //Fallo total (no se pudo guardar localmente al menos)
            }
        }
    }

    /*
     * Obtiene todos los números reportados localmente desde la tabla 'reportes'.
     */
    fun obtenerTodoslosReportesLocales(): Flow<List<ReportesEntity>> {
        return reportesDao.obtenerTodosLosReportes()
    }

    suspend fun eliminarNumeroReportado(reporteDto: DeleteReportedNumberDto) = withContext(Dispatchers.IO) {
        val numero = reporteDto.reportedMsisdn.orEmpty()

        // Borrado local inmediato: permite quitar el numero de Mis reportes y de la alerta.
        reportesDao.eliminarReportePorNumero(numero)
        numeroDao.eliminarNumero(numero)

        try {
            // La API no borra fisicamente: marca el reporte como status=0 en MySQL.
            val respuesta = apiService.eliminarNumeroReportado(reporteDto)
            if (respuesta.isSuccessful && respuesta.body()?.result?.resultCode == 200) {
                Log.d("nomasxt", "ReportesRepository | Reporte $numero marcado como eliminado en la API.")
            } else {
                Log.d("nomasxt", "ReportesRepository | Error al eliminar reporte en API: ${respuesta.code()}: ${respuesta.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.d("nomasxt", "ReportesRepository | Excepcion al eliminar reporte en API: ${e.message}")
        }
    }
}
