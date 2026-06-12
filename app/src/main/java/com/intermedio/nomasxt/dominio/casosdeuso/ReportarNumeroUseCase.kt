package com.intermedio.nomasxt.dominio.casosdeuso

import android.util.Log
import com.intermedio.nomasxt.datos.remoto.dto.AppInfoDto
import com.intermedio.nomasxt.datos.remoto.dto.ReportesDto
import com.intermedio.nomasxt.datos.repository.PerfilRepositorio
import com.intermedio.nomasxt.datos.repository.ReportesRepository

import javax.inject.Inject

class ReportarNumeroUseCase @Inject constructor (
    private val reportesRepository: ReportesRepository,
    private val perfilRepositorio: PerfilRepositorio
) {
    /*
     * Ejecuta la lógica para reportar un número.
     * Siempre intenta guardar el número en la base de datos local 'numeros'.
     * Luego, intenta enviar el reporte al API y, si es exitoso, guarda los reportes en 'reportes'.
     * @param phoneNumber El número del teléfono a reportar.
     * @param category La categoría del reporte.
     * @param
     */
    private val APP_INFO_OS = "AND"
    private val APP_INFO_VERSION_OS = "25"
    private val APP_INFO_VERSION_APP = "2.8"
    private val APP_INFO_SKU_APP = "no+xt_and"

    private val REPORT_LABEL = "Reportar como extorsión"

    suspend fun reportar(phoneNumber: String): String {//Boolean {
        if(phoneNumber.isBlank()) {
            Log.d("nomasxt", "ReportarNumeroUseCase | Error en la longitud del número o número vacío")
            return "Error en la longitud del número o número vacío"//false
        }
        val perfil = perfilRepositorio.obtenerPerfil()
        val userId = perfil?.id

        //Construir appInfo con los valores constantes
        val appInfo = AppInfoDto(
            os = APP_INFO_OS,
            versionOs = APP_INFO_VERSION_OS,
            versionApp = APP_INFO_VERSION_APP,
            skuApp = APP_INFO_SKU_APP
        )

        //Construir ReportesDto
        val reportesDto = ReportesDto(
            appInfo = appInfo,
            userId = userId,
            reportedMsisdn = phoneNumber,
            label = REPORT_LABEL
        )

        return reportesRepository.reportarNumero(reportesDto)
    }
}