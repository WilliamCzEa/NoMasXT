package com.intermedio.nomasxt.presentacion.pantallas

import android.R
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.intermedio.nomasxt.viewmodel.QuejasOSugerenciasViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.intermedio.nomasxt.dominio.casosdeuso.QuejasOSugerenciasUseCase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaQuejas(
    viewModel: QuejasOSugerenciasViewModel = hiltViewModel()
) {
    val contexto = LocalContext.current
    val estadoUi by viewModel.estadoUi.collectAsState()
    val incidentes by viewModel.incidentes.collectAsState()
    val focusManager = LocalFocusManager.current
    val estadoScroll = rememberScrollState()

    // Observar mensajes para mostrar Toasts
    LaunchedEffect(estadoUi.mensaje) {
        estadoUi.mensaje?.let {
            Toast.makeText(contexto, it, Toast.LENGTH_LONG).show()
            viewModel.descartarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Enviar queja") })
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                            Log.d("nomasxt", "PantallaQuejas | On Tap")
                        })
                    }
                    .verticalScroll(estadoScroll)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Campo de Incidencias (Radio Buttons)
                Text(
                    text = "Elija la categoría adecuada*",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Start)
                )
                if(incidentes.isEmpty()){
                    CircularProgressIndicator()
                    Text("Cargando incidentes...", style = MaterialTheme.typography.bodySmall)
                } else {
                    Column(Modifier.selectableGroup()){
                        incidentes.forEach { incidente ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .selectable(
                                        selected = (incidente == estadoUi.incidenciaSeleccionada),
                                        onClick = { viewModel.onIncidenteSeleccionado(incidente) },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (incidente == estadoUi.incidenciaSeleccionada),
                                    onClick = null
                                )
                                Text(
                                    text = incidente.incidence,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }
                }
                if(estadoUi.mostrarErrorIncidencia) {
                    Text("Debe seleccionar una incidencia", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                // Correo electrónico
                OutlinedTextField(
                    value = estadoUi.email,
                    onValueChange = viewModel::onEmailModificado,
                    label = { Text("Correo electrónico") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = estadoUi.mostrarErrorEmail,
                    modifier = Modifier.fillMaxWidth()
                )
                if (estadoUi.mostrarErrorEmail) {
                    Text("El correo electrónico es requerido", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                // Comentarios
                OutlinedTextField(
                    value = estadoUi.comentario,
                    onValueChange = viewModel::onCommentsModificado,
                    label = { Text("Comentarios*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp, max = 200.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    isError = estadoUi.mostrarErrorComentario,
                    maxLines = 5
                )
                if(estadoUi.mostrarErrorComentario) {
                    Text("Los comentarios son requeridos (máx. 255 caracteres)", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    text = "${estadoUi.comentario.length}/255",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.End)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botón de enviar queja
                Button(
                    onClick = { viewModel.enviarQuejaOSugerenciaRequest(QuejasOSugerenciasUseCase.TIPO_QUEJA) }, // Envía el tipo queja
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !estadoUi.estaCargando
                ) {
                    if(estadoUi.estaCargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Enviar queja")
                    }
                }
            }
        }
    )
}