package com.intermedio.nomasxt.presentacion.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {
    private val _datosEjemplo = MutableStateFlow("Hola desde view model")
    val datosEjemplo: StateFlow<String> get() = _datosEjemplo
}
