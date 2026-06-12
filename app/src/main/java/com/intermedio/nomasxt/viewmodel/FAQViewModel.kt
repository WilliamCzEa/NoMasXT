package com.intermedio.nomasxt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
//Importar aquí el DAO y el Entity
import com.intermedio.nomasxt.datos.dao.CatalogoFAQDao
import com.intermedio.nomasxt.datos.entity.CatalogoFAQEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FAQViewModel @Inject constructor(
    private val faqDao: CatalogoFAQDao
): ViewModel() {
    private val _listaFaq = MutableStateFlow<List<CatalogoFAQEntity>>(emptyList())
    val listaFaq: StateFlow<List<CatalogoFAQEntity>> = _listaFaq

    init {
        viewModelScope.launch {
            cargarFaqs()
        }
    }

    private suspend fun cargarFaqs() {
        _listaFaq.value = faqDao.obtenerTodos()
    }
}
