package com.intermedio.nomasxt.datos.remoto

import com.intermedio.nomasxt.datos.remoto.dto.CatalogsResponse
import com.intermedio.nomasxt.datos.remoto.dto.DeleteReportedNumberDto
import com.intermedio.nomasxt.datos.remoto.dto.DeleteReportedNumberResponseDto
import com.intermedio.nomasxt.datos.remoto.dto.HelpResponseDto
import com.intermedio.nomasxt.datos.remoto.dto.PerfilDto
import com.intermedio.nomasxt.datos.remoto.dto.PerfilResponse
import com.intermedio.nomasxt.datos.remoto.dto.PerfilEditResponse
import com.intermedio.nomasxt.datos.remoto.dto.QuejaOSugerenciaRequestDto
import com.intermedio.nomasxt.datos.remoto.dto.ReportesDto
import com.intermedio.nomasxt.datos.remoto.dto.ReportesResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("/services/1.0/extortion/properties")
    suspend fun obtenerCatalogos(): Response<CatalogsResponse>

    @POST("/services/1.0/extortion/userRegistry")
    suspend fun registraUsuario(@Body body: PerfilDto): Response<PerfilResponse>

    @POST("/services/1.0/extortion/editProfile")
    suspend fun actualizaUsuario(@Body body: PerfilDto): Response<PerfilEditResponse>

    @POST("/services/1.0/extortion/ReporterNumber")
    suspend fun reportarNumero(@Body body: ReportesDto): Response<ReportesResponseDto>

    @POST("/services/1.0/extortion/deleteReporterNumber")
    suspend fun eliminarNumeroReportado(@Body body: DeleteReportedNumberDto): Response<DeleteReportedNumberResponseDto>

    @POST("/services/1.0/extortion/helpService")
    suspend fun quejaOSugerencia(@Body body: QuejaOSugerenciaRequestDto): Response<HelpResponseDto>
}
