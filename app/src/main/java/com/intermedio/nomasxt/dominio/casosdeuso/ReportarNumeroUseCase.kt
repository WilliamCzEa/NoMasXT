package com.intermedio.nomasxt.dominio.casosdeuso

import android.util.Log
import com.intermedio.nomasxt.datos.entity.PerfilEntity
import com.intermedio.nomasxt.datos.remoto.ApiService
import com.intermedio.nomasxt.datos.remoto.dto.AppInfoDto
import com.intermedio.nomasxt.datos.remoto.dto.DeleteReportedNumberDto
import com.intermedio.nomasxt.datos.remoto.dto.PerfilDto
import com.intermedio.nomasxt.datos.remoto.dto.ReportesDto
import com.intermedio.nomasxt.datos.repository.PerfilRepositorio
import com.intermedio.nomasxt.datos.repository.ReportesRepository
import java.util.UUID
import javax.inject.Inject

class ReportarNumeroUseCase @Inject constructor(
    private val reportesRepository: ReportesRepository,
    private val perfilRepositorio: PerfilRepositorio,
    private val apiService: ApiService
) {
    private val APP_INFO_OS = "AND"
    private val APP_INFO_VERSION_OS = "25"
    private val APP_INFO_VERSION_APP = "2.8"
    private val APP_INFO_SKU_APP = "no+xt_and"

    // El backend productivo busca la llave tecnica del catalogo TAG_MAP.
    // Si mandamos el texto visible ("Reportar como extorsion"), no encuentra el tag y falla.
    private val REPORT_LABEL = "EXTORSION"

    // Perfil anonimo tecnico: permite reportar sin pedir registro/login al usuario.
    private val ANONYMOUS_STATE_ID = 7
    private val ANONYMOUS_AGE_ID = 3
    private val ANONYMOUS_GENDER = 1

    private fun crearAppInfo(): AppInfoDto {
        return AppInfoDto(
            os = APP_INFO_OS,
            versionOs = APP_INFO_VERSION_OS,
            versionApp = APP_INFO_VERSION_APP,
            skuApp = APP_INFO_SKU_APP
        )
    }

    suspend fun reportar(phoneNumber: String): String {
        if (phoneNumber.isBlank()) {
            Log.d("nomasxt", "ReportarNumeroUseCase | Error en la longitud del numero o numero vacio")
            return "Error en la longitud del numero o numero vacio"
        }

        val perfil = obtenerOCrearPerfilAnonimo()
        val reportesDto = ReportesDto(
            appInfo = crearAppInfo(),
            userId = perfil?.id,
            reportedMsisdn = phoneNumber,
            label = REPORT_LABEL
        )
        return reportesRepository.reportarNumero(reportesDto)
    }

    private suspend fun obtenerOCrearPerfilAnonimo(): PerfilEntity? {
        val perfilExistente = perfilRepositorio.obtenerPerfil()
        if (perfilExistente != null) return perfilExistente

        return try {
            val emailAnonimo = "anon-${UUID.randomUUID()}@nomasxt.local"
            val perfilDto = PerfilDto(
                appInfo = crearAppInfo(),
                userId = null,
                stateId = ANONYMOUS_STATE_ID,
                ageId = ANONYMOUS_AGE_ID,
                gender = ANONYMOUS_GENDER,
                email = emailAnonimo,
                token = null
            )

            val respuesta = apiService.registraUsuario(perfilDto)
            val userId = respuesta.body()?.userId
            if (!respuesta.isSuccessful || userId.isNullOrBlank()) {
                Log.d("nomasxt", "ReportarNumeroUseCase | No se pudo crear perfil anonimo: ${respuesta.code()}")
                return null
            }

            val perfilAnonimo = PerfilEntity(
                id = userId,
                ageRange = ANONYMOUS_AGE_ID,
                state = ANONYMOUS_STATE_ID,
                gender = ANONYMOUS_GENDER,
                eMail = emailAnonimo
            )
            perfilRepositorio.agregarPerfil(perfilAnonimo)
            Log.d("nomasxt", "ReportarNumeroUseCase | Perfil anonimo creado con userId=$userId")
            perfilAnonimo
        } catch (e: Exception) {
            Log.d("nomasxt", "ReportarNumeroUseCase | Error creando perfil anonimo: ${e.message}")
            null
        }
    }

    suspend fun eliminarNumeroReportado(phoneNumber: String) {
        if (phoneNumber.isBlank()) {
            Log.d("nomasxt", "ReportarNumeroUseCase | No se puede eliminar un numero vacio")
            return
        }

        val perfil = perfilRepositorio.obtenerPerfil()
        val deleteDto = DeleteReportedNumberDto(
            appInfo = crearAppInfo(),
            userId = perfil?.id,
            reportedMsisdn = phoneNumber
        )
        reportesRepository.eliminarNumeroReportado(deleteDto)
    }

    suspend fun sincronizarReportesPendientes() {
        val perfil = obtenerOCrearPerfilAnonimo()
        val userId = perfil?.id
        val pendientes = reportesRepository.obtenerReportesPendientes()

        pendientes.forEach { reporte ->
            val reportesDto = ReportesDto(
                appInfo = crearAppInfo(),
                userId = userId,
                reportedMsisdn = reporte.numeroReportado,
                // Reenviamos pendientes con la llave tecnica aceptada por produccion.
                label = REPORT_LABEL
            )
            val sincronizado = reportesRepository.sincronizarReportePendiente(reportesDto)
            if (sincronizado) {
                Log.d("nomasxt", "ReportarNumeroUseCase | Reporte pendiente ${reporte.numeroReportado} sincronizado")
            }
        }
    }
}
