package com.intermedio.nomasxt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intermedio.nomasxt.datos.entity.CatalogoIncidentesEntity
import com.intermedio.nomasxt.dominio.casosdeuso.QuejasOSugerenciasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuejasOSugerenciasUiState(
    val incidenciaSeleccionada: CatalogoIncidentesEntity? = null,
    val email: String = "",
    val comentario: String = "",
    val estaCargando: Boolean = false,
    val mostrarErrorIncidencia: Boolean = false,
    val mostrarErrorEmail: Boolean = false,
    val mostrarErrorComentario: Boolean = false,
    val mensaje: String? = null    // Mensaje para el Toast o Snack Bar
)

@HiltViewModel
class QuejasOSugerenciasViewModel @Inject constructor(
    private val quejasOSugerenciasUseCase: QuejasOSugerenciasUseCase
): ViewModel() {

    //Estado observable para la UI
    private val _estadoUi = MutableStateFlow(QuejasOSugerenciasUiState())
    val estadoUi: StateFlow<QuejasOSugerenciasUiState> = _estadoUi.asStateFlow()

    //Catálogo de incidencias (sólo necesario para quejas)
    val incidentes: StateFlow<List<CatalogoIncidentesEntity>> =
        quejasOSugerenciasUseCase.obtenerCatalogoIncidentes()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    //Métodos de la UI para actualizar el estado
    fun onIncidenteSeleccionado(incidente: CatalogoIncidentesEntity) {
        _estadoUi.update { estadoActual ->
            estadoActual.copy(
                incidenciaSeleccionada = incidente,
                mostrarErrorIncidencia = false
            )
        }
    }

    fun onEmailModificado(nuevoEmail: String) {
        _estadoUi.update { estadoActual ->
            estadoActual.copy(
                email = nuevoEmail,
                mostrarErrorEmail = false
            )
        }
    }

    fun onCommentsModificado(nuevoComentario: String) {
        if(nuevoComentario.length <= 255){
            _estadoUi.update { estadoActual ->
                estadoActual.copy(
                    comentario = nuevoComentario,
                    mostrarErrorComentario = false
                )
            }
        }
    }

    fun descartarMensaje() {
        _estadoUi.update { it.copy(mensaje = null) }
    }

    /*
    * Envía la solicitud de ayuda (queja o sugerencia)
    * */
    fun enviarQuejaOSugerenciaRequest(tipoServicio: Int) {
        // Resetear mensajes de error antes de validar
        _estadoUi.update { estadoActual ->
            estadoActual.copy(
                mostrarErrorEmail = false,
                mostrarErrorComentario = false,
                mostrarErrorIncidencia = false,
                mensaje = null
            )
        }

        val estadoActual = _estadoUi.value
        var esValido = true

        // Validaciones comunes
        if(estadoActual.email.isBlank()) {
            _estadoUi.update { it.copy(mostrarErrorEmail = true) }
            esValido = false
        }
        if(estadoActual.comentario.isBlank() || estadoActual.comentario.length > 255) {
            _estadoUi.update { it.copy(mostrarErrorComentario = true) }
            esValido = false
        }

        // Validación específica para queja
        if(tipoServicio == QuejasOSugerenciasUseCase.TIPO_QUEJA && estadoActual.incidenciaSeleccionada == null) {
            _estadoUi.update { it.copy(mostrarErrorIncidencia = true) }
            esValido = false
        }

        if(!esValido){
            _estadoUi.update { it.copy(mensaje = "Por favor, complete todos los campos requeridos.") }
            return
        }

        // Si es válido, proceder con el envío
        _estadoUi.update { it.copy(estaCargando = true) }
        viewModelScope.launch {
            val resultado = quejasOSugerenciasUseCase.enviarQuejaOSugerencia(
                tipo = tipoServicio,
                email = estadoActual.email,
                comments = estadoActual.comentario,
                idIncidencia = estadoActual.incidenciaSeleccionada?.id //Será nulo si es sugerencia, o el Id si es queja
            )

            resultado.onSuccess { mensajeExito ->
                _estadoUi.update {
                    it.copy(
                        mensaje = mensajeExito,
                        estaCargando = false,
                        // Limpiar campos después de éxito
                        incidenciaSeleccionada = null,
                        email = "",
                        comentario = ""
                    )
                }
            }.onFailure { excepcion ->
                _estadoUi.update {
                    it.copy(
                        mensaje = "Error: ${excepcion.message ?: "Ocurrió un error inesperado."}",
                        estaCargando = false
                    )
                }
            }
        }
    }
}
