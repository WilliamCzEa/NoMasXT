package com.intermedio.nomasxt.presentacion.navegacion

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.*
import com.intermedio.nomasxt.presentacion.pantallas.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navItems = listOf(
        BottomNavItem.Inicio,
        //BottomNavItem.Llamadas,
        BottomNavItem.AcercaDe,
        BottomNavItem.Mas
    )
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry.value?.destination
    val currentRoute = currentDestination?.route

    val mostrarBarraInferior = currentRoute in navItems.map { it.route }
    val isSecondaryScreen = !mostrarBarraInferior

    val context = LocalContext.current

    Scaffold(
        topBar = {
            if (isSecondaryScreen) {
                TopAppBar(
                    title = { Text(obtenerTituloDePantalla(currentRoute), fontSize = 20.sp) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                        }
                    }
                )
            } else {
            TopAppBar(
                title = { Text("No Más XT", fontSize = 20.sp) },
            )
            }
        },
        bottomBar = {
            if(mostrarBarraInferior) {
                NavigationBar {
                    val destinoActual = navController.currentBackStackEntryAsState().value?.destination
                    navItems.forEach { item ->
                    NavigationBarItem(
                        selected = destinoActual?.route == item.route,
                        icon = { Icon(
                            painter = painterResource(id = item.iconResId),
                            contentDescription = item.title
                        )},
                        label = { Text(item.title) },
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
            }
        }
    ) { innerPadding -> 
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Inicio.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Inicio.route) { PantallaInicio() }
            //composable(BottomNavItem.Llamadas.route) { PantallaContactos() } //Tenía PantallaLlamadas()
            composable(BottomNavItem.AcercaDe.route) { PantallaAcercaDe() }
            composable(BottomNavItem.Mas.route) { PantallaMenu(navController) } //Aquí debe ir la pantalla de Menú
            //Aquí vienen las siguientes opciones del menú
            composable("mi_perfil") { PantallaPerfil(
                onRegistroExitoso = {
                    Toast.makeText(context, "Registro Exitoso", Toast.LENGTH_LONG).show()
                }
            ) } 
            composable("contactanos") { PantallaContactanos()}
            composable("preguntas_frecuentes") { PantallaFAQ() }
            composable("terminos_y_condiciones") { PantallaTyC() }
            composable("reportar_incidencias") { PantallaQuejas() }
            composable("sugerencias") { PantallaSugerencias() }
        }
    }
}

fun obtenerTituloDePantalla(ruta: String?): String {
    return when (ruta) {
        "mi_perfil" -> "Mi Perfil"
        "contactanos" -> "Contáctanos"
        "preguntas_frecuentes" -> "Preguntas Frecuentes"
        "terminos_y_condiciones" -> "Términos y Condiciones"
        "reportar_incidencias" -> "Reportar Incidencias"
        "sugerencias" -> "Sugerencias"
        else -> ""
    }
}
