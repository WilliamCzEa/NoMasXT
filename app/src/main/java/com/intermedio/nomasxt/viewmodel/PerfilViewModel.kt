package com.intermedio.nomasxt.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intermedio.nomasxt.datos.entity.PerfilEntity
import com.intermedio.nomasxt.datos.entity.CatalogoEdadesEntity
import com.intermedio.nomasxt.datos.entity.CatalogoEstadosEntity
import com.intermedio.nomasxt.datos.remoto.ApiService
import com.intermedio.nomasxt.datos.remoto.dto.AppInfoDto
import com.intermedio.nomasxt.datos.remoto.dto.PerfilDto
import com.intermedio.nomasxt.datos.remoto.dto.PerfilEditResponse
import com.intermedio.nomasxt.datos.remoto.dto.PerfilResponse
import com.intermedio.nomasxt.datos.repository.PerfilRepositorio // Debes crear este repositorio
import com.intermedio.nomasxt.datos.repository.CatalogosRepository // Debes crear este repositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val catalogosRepositorio: CatalogosRepository,
    private val perfilRepositorio: PerfilRepositorio,
    private val apiService: ApiService
): ViewModel() {

    //Estado para los catálogos
    private val _estados = MutableStateFlow<List<CatalogoEstadosEntity>>(emptyList())
    val estados: StateFlow<List<CatalogoEstadosEntity>> = _estados

    private val _edades = MutableStateFlow<List<CatalogoEdadesEntity>>(emptyList())
    val edades: StateFlow<List<CatalogoEdadesEntity>> = _edades

    //Estado para los campos del formulario
    val estadoSeleccionado = mutableStateOf<CatalogoEstadosEntity?>(null)
    val generoSeleccionado = mutableStateOf<Int?>(null) //1 para Masculino, 2 para femenino
    val edadSeleccionada = mutableStateOf<CatalogoEdadesEntity?>(null)
    val email = mutableStateOf("")

    //Estado para la respuesta del registro
    private val _registroResponse = MutableStateFlow<PerfilResponse?>(null)
    val registroResponse: StateFlow<PerfilResponse?> = _registroResponse

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _registroExitoso = MutableStateFlow(false)
    val registroExitoso:  StateFlow<Boolean> = _registroExitoso

    //Estado para controlar el modo de edición
    private val _estaEditando = MutableStateFlow(false)
    val estaEditando: StateFlow<Boolean> = _estaEditando

    //Estado para el perfil existente (si se carga)
    private val _perfilExistente = MutableStateFlow<PerfilEntity?>(null)
    val perfilExistente: StateFlow<PerfilEntity?> = _perfilExistente

    //Verificamos si es la primera vez que se almacena.
    private val _esPrimerGuardado = MutableStateFlow(false)
    val esPrimerGuardado: StateFlow<Boolean> = _esPrimerGuardado

    init {
        viewModelScope.launch {
            cargarCatalogos()
            cargarPerfilExistente()

        }
    }

    private suspend fun cargarCatalogos() {
        //viewModelScope.launch {
            _estados.value = catalogosRepositorio.obtenerEstados()
            Log.d("nomasxt", "PerfilViewModel | cargarCatalogos, los estados son: ${_estados.value}")
            _edades.value = catalogosRepositorio.obtenerEdades()
            Log.d("nomasxt", "PerfilViewModel | cargarCatalogos, las edades son: ${_edades.value}")
        //}
    }

    private suspend fun cargarPerfilExistente() {
        Log.d("nomasxt", "PerfilViewModel | CargarPerfilExistente, antes de lanzar, el valor de _edades es: ${_edades.value}")
        //viewModelScope.launch {
            _perfilExistente.value = perfilRepositorio.obtenerPerfil()
            _perfilExistente.value?.let { perfil ->
                //_estaEditando.value = true
                Log.d("nomasxt", "PerfilViewModel | Al cargarPerfil Existente, el valor es: $perfil")
                Log.d("nomasxt", "PerfilViewModel | Edades cargadas: ${_edades.value}")
                edadSeleccionada.value = _edades.value.find { it.id == perfil.ageRange }
                Log.d("nomasxt", "PerfilViewModel | Edad seleccionada después: ${edadSeleccionada.value}")
                estadoSeleccionado.value = _estados.value.find { it.id == perfil.state }
                generoSeleccionado.value = perfil.gender
                Log.d("nomasxt", "PerfilViewModel | CargarPerfilExistente, edadSeleccionada.value es: ${edadSeleccionada.value}")

            }
        //}
    }

    fun alOprimirEditar() {
        _estaEditando.value = true
    }


    suspend fun guardarPerfil() {

        _isLoading.value = true
        val perfilExistenteLocal = _perfilExistente.value
        _esPrimerGuardado.value = perfilExistenteLocal == null

        val perfilId = _perfilExistente.value?.id
        Log.d("nomasxt", "PerfilViewModel | valor de perfilId?.toString(): ${perfilId.toString()}")

        val perfilDto = PerfilDto(
            appInfo = AppInfoDto(os = "AND", versionOs = "36", versionApp = "2.6", skuApp = "no+xt_and"),
            userId = perfilId?.toString(),//null, //Se espera que el backend lo genero
            stateId = estadoSeleccionado.value?.id ?: return,
            ageId = edadSeleccionada.value?.id ?: return,
            gender = generoSeleccionado.value ?: return,
            email = "-",
            token = null
        )
        Log.d("nomasxt", "PerfilViewModel | Valor de perfilDto: $perfilDto")

        try {

/*
            val response  = if (perfilExistenteLocal == null) {
                apiService.registraUsuario(perfilDto)
            } else
                apiService.actualizaUsuario(perfilDto)
            }
*/
            if(perfilExistenteLocal == null) {
                val response: Response<PerfilResponse> = apiService.registraUsuario(perfilDto)
                _registroResponse.value = response.body()
                _isLoading.value = false

                if(response.isSuccessful && response.body()?.userId != null) {
                    val userId = response.body()!!.userId
                    val perfilEntity = PerfilEntity(
                        id = userId,
                        ageRange = edadSeleccionada.value!!.id,
                        state = estadoSeleccionado.value!!.id,
                        gender = generoSeleccionado.value!!,
                        eMail = email.value
                    )
                    perfilRepositorio.agregarPerfil(perfilEntity)
                    _registroExitoso.value = true
                    _estaEditando.value = false
                } else {
                    //Manejar error de registro
                    Log.d("nomasxt", "PerfilViewModel | Error en el registro")
                    _registroExitoso.value = false
                }
            } else { //Si es una edición
                val response: Response<PerfilEditResponse> = apiService.actualizaUsuario(perfilDto)
                _isLoading.value = false

                if(response.isSuccessful && (response.body()?.result?.resultCode?.equals("200") == true)) {
                    //La edición fue exitosa
                    val perfilEntity = PerfilEntity(
                        id = perfilExistenteLocal.id,
                        ageRange = edadSeleccionada.value!!.id,
                        state = estadoSeleccionado.value!!.id,
                        gender = generoSeleccionado.value!!,
                        eMail = email.value
                    )
                    perfilRepositorio.agregarPerfil(perfilEntity) // Actualizar el repositorio en room
                    _registroExitoso.value = true
                    _estaEditando.value = false

                } else {
                    //Manejar error de edición
                    Log.d("nomasxt", "PerfilViewModel | Error de edición, Response es: ${response.body()}")
                    _registroExitoso.value = false
                    _estaEditando.value = true
                }
            }
            /*
            Log.d("nomasxt", "PerfilViewModel | valor de response: $response")
            _registroResponse.value = response.body()
            _isLoading.value = false
             */



/*
            if(response.isSuccessful && response.body()?.userId != null) {
                //Guardar en la base de datos local
                val userId = response.body()!!.userId
                val perfilEntity = PerfilEntity(
                    id = userId, //response.body()!!.userId,
                    ageRange = edadSeleccionada.value!!.id,
                    state = estadoSeleccionado.value!!.id,
                    gender = generoSeleccionado.value!!,
                    eMail = email.value
                )
                perfilRepositorio.agregarPerfil(perfilEntity)
                _registroExitoso.value = true
                _estaEditando.value = false
            } else {
                //Manejar error de registro
                Log.d("nomasxt", "PerfilViewModel | Error en el registro")
                _registroExitoso.value = false
            }
*/
        } catch(ex: HttpException) {
            _isLoading.value = false
            Log.d("nomasxt", "PerfilViewModel | Error HTTP al descargar datos ${ex.message}")
            _registroExitoso.value = false
        }
        catch (e: Exception) {
            _isLoading.value = false
            Log.d("nomasxt", "PerfilViewModel | Error al descargar datos ${e.message}")
            _registroExitoso.value = false
        }

    }

}
