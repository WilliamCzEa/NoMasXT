package com.intermedio.nomasxt.presentacion.pantallas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.intermedio.nomasxt.R

@Composable
fun PantallaMenu(navController: NavHostController) {
    val opciones = listOf(
        "Mi perfil" to "mi_perfil",
        "Contáctanos" to "contactanos",
        "Preguntas Frecuentes" to "preguntas_frecuentes",
        "Términos y Condiciones" to "terminos_y_condiciones",
        "Reportar Incidencias" to "reportar_incidencias",
        "Sugerencias" to "sugerencias"
    )

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(opciones.count()) { index ->
            val (titulo, ruta) = opciones[index]
            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable { navController.navigate(ruta) }
            ) {
                Text(titulo, modifier = Modifier.padding(16.dp), fontSize = 18.sp)
            }
        }
    }
}
