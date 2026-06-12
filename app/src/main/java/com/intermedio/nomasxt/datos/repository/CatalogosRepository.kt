package com.intermedio.nomasxt.datos.repository

import android.util.Log
import com.intermedio.nomasxt.datos.dao.CatalogoEdadesDao
import com.intermedio.nomasxt.datos.dao.CatalogoEstadosDao
import com.intermedio.nomasxt.datos.dao.CatalogoFAQDao
import com.intermedio.nomasxt.datos.dao.CatalogoIncidentesDao
import com.intermedio.nomasxt.datos.entity.CatalogoEdadesEntity
import com.intermedio.nomasxt.datos.entity.CatalogoEstadosEntity
import com.intermedio.nomasxt.datos.entity.CatalogoFAQEntity
import com.intermedio.nomasxt.datos.entity.CatalogoIncidentesEntity
import com.intermedio.nomasxt.datos.remoto.ApiService
import com.intermedio.nomasxt.datos.remoto.dto.AgeCatalogDto
import com.intermedio.nomasxt.datos.remoto.dto.CatalogsResponse
import com.intermedio.nomasxt.datos.remoto.dto.IncidenceCatalogDto
import com.intermedio.nomasxt.datos.remoto.dto.QuestionCatalogDto
import com.intermedio.nomasxt.datos.remoto.dto.StateCatalogDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.Response

@Singleton
class CatalogosRepository @Inject constructor(
    private val apiService: ApiService,
    private val edadesDao: CatalogoEdadesDao,
    private val incidentesDao: CatalogoIncidentesDao,
    private val faqDao: CatalogoFAQDao,
    private val estadosDao: CatalogoEstadosDao
){
    //Método para verificar si existe información al menos en una tabla
    suspend fun estanDescargadosLosCatalogos(): Boolean {
        return edadesDao.contar() > 0
    }

    //Método para descargar catálogos desde la red
    suspend fun obtenerCatalogosDeLaRed(): Response <CatalogsResponse> {
        return apiService.obtenerCatalogos()
    }

    //Método para guardar los catálogos descargados en la base de datos
    suspend fun guardarCatalogosABd(response: CatalogsResponse) {
        //Mapear DTOs a Entities y guardar en la base de datos
        response.ageCatalog?.let { listaDto ->
            val entidades = listaDto.map { it.toEntity() }
            edadesDao.agregaTodos(entidades)

        }

        response.incidencesCatalog?.let { listaDto ->
            val entidades = listaDto.map { it.toEntity() }
            incidentesDao.agregaTodos(entidades)

        }

        response.questionsCatalog?.let { listaDto ->
            val entidades = listaDto.map { it.toEntity() }
            faqDao.agregaTodos(entidades)

        }

        response.stateCatalog?.let { listaDto ->
            val entidades = listaDto.map { it.toEntity() }
            estadosDao.agregaTodos(entidades)

        }
    }
    private fun AgeCatalogDto.toEntity() = CatalogoEdadesEntity(id = id, ageRange = ageRange)
    private fun IncidenceCatalogDto.toEntity() = CatalogoIncidentesEntity(id = id, incidence = incidence)
    private fun QuestionCatalogDto.toEntity() = CatalogoFAQEntity(id = id, question = question, answer = answer)
    private fun StateCatalogDto.toEntity() = CatalogoEstadosEntity(id = id, name = name)

    //Método para obtener los catálogos de Room
    //primero el de Estados
    suspend fun obtenerEstados(): List<CatalogoEstadosEntity> = withContext(Dispatchers.IO) {
        val estados = estadosDao.obtenerTodos()
        Log.d("nomsaxt", "CatalogosRepository | Estados obtenidos del DAO: ${estados.size}")
        estados
        //estadosDao.obtenerTodos()
    }

    //Luego el de edades
    suspend fun obtenerEdades(): List<CatalogoEdadesEntity> = withContext(Dispatchers.IO) {
        val edades = edadesDao.obtenerTodas()
        Log.d("nomasxt", "CatalogosRepository | Edades obtenidas del DAO: ${edades.size}")
        edades
        //edadesDao.obtenerTodas()
    }
}




