package com.intermedio.nomasxt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intermedio.nomasxt.datos.entity.ReportesEntity
import com.intermedio.nomasxt.datos.repository.ReportesRepository
import com.intermedio.nomasxt.dominio.casosdeuso.ReportarNumeroUseCase
import com.intermedio.nomasxt.dominio.model.Country
import com.intermedio.nomasxt.dominio.model.defaultCountries
import com.intermedio.nomasxt.utilerias.TelefonoNormalizer
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
) : ViewModel() {
    init {
        viewModelScope.launch {
            reportarNumeroUseCase.sincronizarReportesPendientes()
        }
    }

    private val _mostrarDialogoReporte = MutableStateFlow(false)
    val mostrarDialogoReporte: StateFlow<Boolean> = _mostrarDialogoReporte.asStateFlow()

    // Normalizacion internacional: el campo guarda lo que escribe el usuario, sin forzar +52.
    private val _numeroAReportar = MutableStateFlow("")
    val numeroAReportar: StateFlow<String> = _numeroAReportar.asStateFlow()

    val countries: List<Country> = defaultCountries

    // Normalizacion internacional: Mexico queda como pais por defecto aunque el catalogo este ordenado.
    private val _paisSeleccionado = MutableStateFlow(
        defaultCountries.firstOrNull { it.code == "MX" } ?: defaultCountries.first()
    )
    val paisSeleccionado: StateFlow<Country> = _paisSeleccionado.asStateFlow()

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
        _numeroAReportar.value = ""
        _reporteMensajeDeEstado.value = null
        _mostrarDialogoReporte.value = true
    }

    fun onSalirDialogoReporte() {
        _mostrarDialogoReporte.value = false
        _numeroAReportar.value = ""
        _reporteMensajeDeEstado.value = null
    }

    fun onPaisSeleccionado(country: Country) {
        _paisSeleccionado.value = country
        _reporteMensajeDeEstado.value = null
    }

    fun onNumeroIntroducidoCambia(numero: String) {
        // Normalizacion internacional: permitimos digitos y, si pegan un numero completo, un + inicial.
        _numeroAReportar.value = numero.filterIndexed { index, char ->
            char.isDigit() || (index == 0 && char == '+')
        }
        _reporteMensajeDeEstado.value = null
    }

    fun onReportarPresionado() {
        if (_numeroAReportar.value.isBlank()) {
            _reporteMensajeDeEstado.value = "El numero no puede estar vacio."
            return
        }

        val numeroNormalizado = TelefonoNormalizer.normalizar(
            numeroIngresado = _numeroAReportar.value,
            dialCode = _paisSeleccionado.value.dialCode,
            regionCode = _paisSeleccionado.value.code
        )

        if (!TelefonoNormalizer.esValido(numeroNormalizado, _paisSeleccionado.value.code)) {
            _reporteMensajeDeEstado.value = "Numero invalido. Verifique el pais y los digitos."
            return
        }

        _estaCargando.value = true
        _reporteMensajeDeEstado.value = null

        viewModelScope.launch {
            val mensaje = reportarNumeroUseCase.reportar(
                phoneNumber = numeroNormalizado
            )

            _estaCargando.value = false
            _reporteMensajeDeEstado.value = mensaje

            if (mensaje.contains("registrado correctamente") || mensaje.contains("guardado en la tabla 'reportes'")) {
                onSalirDialogoReporte()
            }
        }
    }

    fun onEliminarReportePresionado(numero: String) {
        viewModelScope.launch {
            reportarNumeroUseCase.eliminarNumeroReportado(numero)
        }
    }
}
