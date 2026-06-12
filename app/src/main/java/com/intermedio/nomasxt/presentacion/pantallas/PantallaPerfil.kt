package com.intermedio.nomasxt.presentacion.pantallas

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intermedio.nomasxt.datos.entity.CatalogoEdadesEntity
import com.intermedio.nomasxt.datos.entity.CatalogoEstadosEntity
import com.intermedio.nomasxt.viewmodel.PerfilViewModel
import kotlinx.coroutines.launch
import kotlin.math.exp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(
    perfilViewModel: PerfilViewModel = hiltViewModel(),
    onRegistroExitoso: () -> Unit  //Callback para navegar o mostrar mensaje
) {

    val estados by perfilViewModel.estados.collectAsState()
    var expandedEstado by remember { mutableStateOf(false) }

    val rangoEdad by perfilViewModel.edades.collectAsState()
    var expandedEdad by remember { mutableStateOf(false) }


    val generos = listOf(1 to "Masculino", 2 to "Femenino")
    var expandedGenero by remember { mutableStateOf(false) }
    
    val isLoading by perfilViewModel.isLoading.collectAsState()
    val registroExitoso by perfilViewModel.registroExitoso.collectAsState()

    val estaEditando by perfilViewModel.estaEditando.collectAsState()
    val perfilExistente by perfilViewModel.perfilExistente.collectAsState()
    val esPrimerGuardado by perfilViewModel.esPrimerGuardado.collectAsState()

    LaunchedEffect(registroExitoso) {
        Log.d("nomasxt", "PantallaPerfil | el valor de registroExitoso es: $registroExitoso y el de esPrimerGuardado es: $esPrimerGuardado ")
        if(registroExitoso && esPrimerGuardado) {
            onRegistroExitoso()
        }
    }

    Log.d("nomasxt", "PantallaPerfil | el valor de estaEditando es: $estaEditando y el de perfilExistente es: $perfilExistente")
    Log.d("nomasxt", "PantallaPerfil | el valor de perfilViewModel.ageRange es : ${perfilViewModel.edadSeleccionada?.value?.ageRange}")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text (if (perfilExistente != null && !estaEditando) "Perfil" else "Registro de perfil") },
                actions = {
                    IconButton(
                        onClick = perfilViewModel::alOprimirEditar,
                        enabled = perfilExistente != null && !estaEditando
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar Perfil")
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),//16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Registro de Perfil", style = MaterialTheme.typography.headlineSmall)

                ExposedDropdownMenuBox(
                    expanded = expandedEstado,
                    onExpandedChange = { expandedEstado = !expandedEstado }
                ) {
                    TextField(
                        modifier = Modifier.menuAnchor(),
                        //value = selectedEstado,
                        value = perfilViewModel.estadoSeleccionado.value?.name?: "Seleccionar Estado",
                        onValueChange = { },
                        readOnly = true,
                        label = {Text( "Estado") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEstado) },
                        enabled = estaEditando || perfilExistente == null
                    )
                    ExposedDropdownMenu(
                        expanded = expandedEstado,
                        onDismissRequest = { expandedEstado = false }
                    ) {
                        estados.forEach { estado ->
                            DropdownMenuItem(
                                text = { Text(estado.name) },
                                onClick = {
                                    perfilViewModel.estadoSeleccionado.value = estado
                                    expandedEstado = false
                                    Log.d("nomasxt", "pantallaperfil | El item seleccionado es: $estado")
                                },
                                enabled = estaEditando || perfilExistente == null,
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )

                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedEdad,
                    onExpandedChange = { expandedEdad = !expandedEdad }
                ){
                    TextField(
                        modifier = Modifier.menuAnchor(),
                        value = perfilViewModel.edadSeleccionada.value?.ageRange?: "Seleccionar rango de edad",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Rango de Edad") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEdad ) },
                        enabled = estaEditando || perfilExistente == null
                    )
                    ExposedDropdownMenu(
                        expanded = expandedEdad,
                        onDismissRequest = { expandedEdad = false }
                    ) {
                        rangoEdad.forEach { edad ->
                            DropdownMenuItem(
                                text = { Text(edad.ageRange) },
                                onClick = {
                                    perfilViewModel.edadSeleccionada.value = edad
                                    expandedEdad = false
                                    Log.d("nomasxt", "pantallaperfil | La edad seleccionada es: $edad")
                                },
                                enabled = estaEditando || perfilExistente == null,
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedGenero,
                    onExpandedChange = { expandedGenero = !expandedGenero }
                ) {
                    TextField(
                        modifier = Modifier.menuAnchor(),
                        value = generos.find { it.first == perfilViewModel.generoSeleccionado.value }?.second ?: "Seleccionar Género",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Género") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGenero) },
                        enabled = estaEditando || perfilExistente == null
                    )
                    ExposedDropdownMenu(
                        expanded = expandedGenero,
                        onDismissRequest = { expandedGenero = false }
                    ) {
                        generos.forEach { (id, nombre) ->
                            DropdownMenuItem(
                                text = { Text(nombre) },
                                onClick = {
                                    perfilViewModel.generoSeleccionado.value = id
                                    expandedGenero = false
                                    Log.d("nomasxt", "pantallaperfil | El género seleccionado es: $nombre, y su id es: $id")
                                },
                                enabled = estaEditando || perfilExistente == null,
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        perfilViewModel.viewModelScope.launch {
                            perfilViewModel.guardarPerfil()
                        }
                    },
                    enabled = !isLoading && (estaEditando || perfilExistente == null),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if(isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Guardar")
                    }
                }
            }

        }

    )
}
