package com.intermedio.nomasxt.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intermedio.nomasxt.datos.CargaInicialDeNumeros
import com.intermedio.nomasxt.dominio.casosdeuso.DescargarYGuardarUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val cargaInicialDeNumeros: CargaInicialDeNumeros,
    private val descargarYGuardarUseCase: DescargarYGuardarUseCase
): ViewModel()  {
    
    private val _estanCargadosLosDatos = MutableStateFlow(false)
    val estanCargadosLosDatos: StateFlow<Boolean> get() = _estanCargadosLosDatos

    init {
        viewModelScope.launch {
            ejecutaCargaInicial()
        }
    }

    private suspend fun ejecutaCargaInicial() {
        try {
            //Cargar números desde JSON si la tabla está vacía
            cargaInicialDeNumeros.cargarNumerosDeJson("numeros.json")
            //Descargar y guardar catálogos si es necesario
            val catalogosRsultado = descargarYGuardarUseCase()
            //TODO: manejar el resultado de la descarga
            delay(500)

            _estanCargadosLosDatos.value = true

        } catch (e: Exception) {
            Log.d("NoMasXT", "No se pudo cargar inicialmente, ocurrió el error: ${e.message}")
            _estanCargadosLosDatos.value = true
        }
    }
}
