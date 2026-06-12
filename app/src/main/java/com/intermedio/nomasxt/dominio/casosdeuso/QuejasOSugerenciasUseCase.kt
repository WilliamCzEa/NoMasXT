package com.intermedio.nomasxt.dominio.casosdeuso

import com.intermedio.nomasxt.datos.entity.CatalogoIncidentesEntity
import com.intermedio.nomasxt.datos.remoto.dto.AppInfoDto
import com.intermedio.nomasxt.datos.remoto.dto.QuejaOSugerenciaRequestDto
import com.intermedio.nomasxt.datos.repository.PerfilRepositorio
import com.intermedio.nomasxt.datos.repository.QuejasOSugerenciasRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class QuejasOSugerenciasUseCase @Inject constructor(
    private val quejasOSugerenciasRepository: QuejasOSugerenciasRepository,
    private val perfilRepositorio: PerfilRepositorio
) {

    private val APP_INFO_OS = "AND"
    private val APP_INFO_VERSION_OS = "26"
    private val APP_INFO_VERSION_APP = "2.8"
    private val APP_INFO_SKU_APP = "no+xt_and"

    //Constantes para el tipo de solicitud
    companion object {
        const val TIPO_QUEJA = 1
        const val TIPO_SUGERENCIA = 2
    }

    fun obtenerCatalogoIncidentes(): Flow<List<CatalogoIncidentesEntity>> {
        return quejasOSugerenciasRepository.obtenerCatalogoIncidencias()
    }

    suspend fun enviarQuejaOSugerencia(
        tipo: Int,
        idIncidencia: Int? = null,
        email: String,
        comments: String
        ): Result<String>
    {

        if(email.isBlank() || comments.isBlank()){
            return Result.failure(Exception("Email y comentarios son requeridos."))
        }
        if(tipo != TIPO_QUEJA && idIncidencia != null) {
            return Result.failure(Exception("Debe seleccionar una incidencia para una queja."))
        }
        if(tipo != TIPO_QUEJA && tipo != TIPO_SUGERENCIA) {
            return Result.failure(Exception("Tipo de solicitud no válido."))
        }


        val perfil = perfilRepositorio.obtenerPerfil()
        val userId = perfil?.id

        //Construir AppInfo con los valores constantes
        val appInfo = AppInfoDto(
            os = APP_INFO_OS,
            versionOs = APP_INFO_VERSION_OS,
            versionApp = APP_INFO_VERSION_APP,
            skuApp = APP_INFO_SKU_APP
        )

        //Construir QuejasOSugerenciasRequestDto
        val quejaOSugerenciaRequestDto = QuejaOSugerenciaRequestDto(
            appInfo = appInfo,
            userId = userId,
            type = tipo,
            idIncidence = idIncidencia,
            email = email,
            comments = comments
        )

        return quejasOSugerenciasRepository.enviarQuejaOSugerencia(quejaOSugerenciaRequestDto)
    }

}