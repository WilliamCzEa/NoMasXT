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
    private companion object {
        const val STATUS_PENDIENTE = "PENDIENTE"
        const val FOLIO_LOCAL = "LOCAL"
    }

    /*
     * Flujo offline-first:
     * 1. Guarda el numero en Room para que Mis reportes y la alerta local funcionen.
     * 2. Intenta enviarlo al servidor.
     * 3. Si el servidor falla, el reporte queda como PENDIENTE y se reintenta despues.
     */
    suspend fun reportarNumero(reporteDto: ReportesDto): String {
        return withContext(Dispatchers.IO) {
            val numeroReportado = reporteDto.reportedMsisdn.orEmpty()
            val mensajeNumeroGuardado = "Numero registrado correctamente en su lista de bloqueo."

            try {
                guardarReporteLocalPendiente(reporteDto)
                Log.d("nomasxt", "ReportesRepository | Numero $numeroReportado guardado localmente")
            } catch (e: Exception) {
                Log.d("nomasxt", "ReportesRepository | Error guardando reporte local $numeroReportado: ${e.message}")
                return@withContext "No se pudo registrar el numero reportado localmente, intente mas tarde."
            }

            val enviado = enviarReporteAlServidor(reporteDto)

            if (enviado) {
                "$mensajeNumeroGuardado\nReporte enviado al servidor correctamente."
            } else {
                "$mensajeNumeroGuardado\nServidor no disponible. El reporte queda pendiente por enviar."
            }
        }
    }

    private suspend fun guardarReporteLocalPendiente(reporteDto: ReportesDto) {
        val numeroReportado = reporteDto.reportedMsisdn.orEmpty()
        numeroDao.insertaNumero(NumerosEntity(numero = numeroReportado))

        val reporteLocal = ReportesEntity(
            id = numeroReportado,
            fecha = fechaActual(),
            folio = FOLIO_LOCAL,
            etiqueta = reporteDto.label ?: "Reporte local",
            numeroReportado = numeroReportado,
            status = STATUS_PENDIENTE
        )
        reportesDao.agregarReporte(reporteLocal)
    }

    private suspend fun enviarReporteAlServidor(reporteDto: ReportesDto): Boolean {
        return try {
            val respuesta = apiService.reportarNumero(reporteDto)
            if (!respuesta.isSuccessful) {
                Log.d("nomasxt", "ReportesRepository | Error HTTP: ${respuesta.code()}: ${respuesta.errorBody()?.string()}")
                return false
            }

            val body = respuesta.body()
            if (body == null || body.result.resultCode != 200) {
                Log.d("nomasxt", "ReportesRepository | API no acepto reporte: ${body?.result?.resultMessage}")
                return false
            }

            val blockedNumberDto: BlockedNumberDto = body.blockedNumber ?: run {
                Log.d("nomasxt", "ReportesRepository | API acepto reporte pero no regreso blockedNumber")
                return false
            }

            guardarReporteApi(blockedNumberDto, reporteDto.label ?: "Reporte local")
            Log.d("nomasxt", "ReportesRepository | Reporte enviado y guardado desde API.")
            true
        } catch (e: Exception) {
            Log.d("nomasxt", "ReportesRepository | No se pudo enviar reporte a API: ${e.message}")
            false
        }
    }

    private suspend fun guardarReporteApi(blockedNumberDto: BlockedNumberDto, etiquetaFallback: String) {
        val numeroReportado = blockedNumberDto.phoneNumber.orEmpty()
        if (numeroReportado.isBlank()) return

        val reporteEntity = ReportesEntity(
            id = blockedNumberDto.folio ?: numeroReportado,
            fecha = blockedNumberDto.date ?: fechaActual(),
            folio = blockedNumberDto.folio ?: FOLIO_LOCAL,
            etiqueta = blockedNumberDto.label ?: etiquetaFallback,
            numeroReportado = numeroReportado,
            status = blockedNumberDto.status ?: "1"
        )
        reportesDao.eliminarReportePorNumero(reporteEntity.numeroReportado)
        reportesDao.agregarReporte(reporteEntity)
    }

    private fun fechaActual(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }

    fun obtenerTodoslosReportesLocales(): Flow<List<ReportesEntity>> {
        return reportesDao.obtenerTodosLosReportes()
    }

    suspend fun obtenerReportesPendientes(): List<ReportesEntity> = withContext(Dispatchers.IO) {
        reportesDao.obtenerReportesPendientes()
    }

    suspend fun sincronizarReportePendiente(reporteDto: ReportesDto): Boolean = withContext(Dispatchers.IO) {
        enviarReporteAlServidor(reporteDto)
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
