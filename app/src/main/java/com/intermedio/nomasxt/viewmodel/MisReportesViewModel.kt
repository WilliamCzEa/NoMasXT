package com.intermedio.nomasxt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intermedio.nomasxt.datos.entity.ReportesEntity
import com.intermedio.nomasxt.datos.repository.ReportesRepository
import com.intermedio.nomasxt.dominio.casosdeuso.ReportarNumeroUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MisReportesViewModel @Inject constructor(
    private val reportarNumeroUseCase: ReportarNumeroUseCase,
    private val reportesRepository: ReportesRepository
): ViewModel() {

    private val _mostrarDialogoReporte = MutableStateFlow(false)
    val mostrarDialogoReporte: StateFlow<Boolean> = _mostrarDialogoReporte.asStateFlow()

    private val _numeroAReportar = MutableStateFlow("+52")
    val numeroAReportar: StateFlow<String> = _numeroAReportar.asStateFlow()

    private val _estaCargando = MutableStateFlow(false)
    val estaCargando: StateFlow<Boolean> = _estaCargando.asStateFlow()

    private val _reporteMensajeDeEstado = MutableStateFlow<String?>(null)
    val reporteMensajeDeEstado: StateFlow<String?> = _reporteMensajeDeEstado.asStateFlow()

    val reportesDeApi: StateFlow<List<ReportesEntity>> =
        reportesRepository.obtenerTodoslosReportesLocales()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun onFabPresionado() {
        _numeroAReportar.value = "+52"
        _reporteMensajeDeEstado.value = null
        _mostrarDialogoReporte.value = true
    }

    fun onSalirDialogoReporte() {
        _mostrarDialogoReporte.value = false
        _numeroAReportar.value = "+52"
        _reporteMensajeDeEstado.value = null
    }

    fun onNumeroIntroducidoCambia(numero: String) {
        if(!numero.startsWith("+52")){
            _numeroAReportar.value = "+52" + numero.filter { it.isDigit() }
        } else {
            //Sólo permitir dígitos después del +52
            val parteNumerica = numero.removePrefix("+52").filter { it.isDigit() }
            _numeroAReportar.value = "+52" + parteNumerica
        }
        _reporteMensajeDeEstado.value = null
    }

    fun onReportarPresionado() {
        val numeroSinPrefijo = _numeroAReportar.value.removePrefix("+52")

        if(numeroSinPrefijo.isBlank()) {
            _reporteMensajeDeEstado.value = "El número no puede estar vacío."
            return
        }

        if(numeroSinPrefijo.length < 10) {
            _reporteMensajeDeEstado.value = "Número demasiado corto, debe tener 10 dígitos."
            return
        }

        _estaCargando.value = true
        _reporteMensajeDeEstado.value = null

        viewModelScope.launch {
            val mensaje = reportarNumeroUseCase.reportar(
                phoneNumber = _numeroAReportar.value
            )

            _estaCargando.value = false
            //if(exito) {
                _reporteMensajeDeEstado.value = mensaje //"Número reportado exitosamente"
            if(mensaje.contains("guardado en la tabla 'reportes'")) {
                onSalirDialogoReporte()
            }
            //} else {
            //    _reporteMensajeDeEstado.value = "No se pudo reportar el número. Intente de nuevo."
            //}
        }
    }
}