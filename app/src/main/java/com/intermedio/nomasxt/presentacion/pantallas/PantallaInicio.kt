package com.intermedio.nomasxt.presentacion.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.intermedio.nomasxt.datos.entity.ReportesEntity
import com.intermedio.nomasxt.presentacion.componentes.CountryCodeSelector
import com.intermedio.nomasxt.viewmodel.MisReportesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicio(
    viewModel: MisReportesViewModel = hiltViewModel()
) {
    val mostrarDialogoReporte by viewModel.mostrarDialogoReporte.collectAsState()
    val numeroAReportar by viewModel.numeroAReportar.collectAsState()
    val estaCargando by viewModel.estaCargando.collectAsState()
    val reporteMensajeDeEstado by viewModel.reporteMensajeDeEstado.collectAsState()
    val reportesDeApi by viewModel.reportesDeApi.collectAsState()
    val paisSeleccionado by viewModel.paisSeleccionado.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mis Reportes") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::onFabPresionado) {
                Icon(Icons.Filled.Add, "Reportar nuevo numero")
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (reportesDeApi.isEmpty()) {
                    Text(
                        text = "Aun no tienes reportes enviados.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 32.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(reportesDeApi) { reporte ->
                            ReporteItem(
                                reporte = reporte,
                                onEliminar = {
                                    viewModel.onEliminarReportePresionado(reporte.numeroReportado)
                                }
                            )
                        }
                    }
                }
            }

            if (mostrarDialogoReporte) {
                AlertDialog(
                    onDismissRequest = viewModel::onSalirDialogoReporte,
                    title = { Text("Reportar Numero") },
                    text = {
                        Column {
                            // Normalizacion internacional: el prefijo sale del pais seleccionado.
                            CountryCodeSelector(
                                selectedCountry = paisSeleccionado,
                                countries = viewModel.countries,
                                onCountrySelected = viewModel::onPaisSeleccionado,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = numeroAReportar,
                                onValueChange = viewModel::onNumeroIntroducidoCambia,
                                label = { Text("Numero a reportar") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = estaCargando
                            )
                            reporteMensajeDeEstado?.let { mensaje ->
                                Text(
                                    text = mensaje,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = viewModel::onReportarPresionado,
                            enabled = !estaCargando && numeroAReportar.isNotBlank() && numeroAReportar.length >= 8
                        ) {
                            if (estaCargando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Reportar")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = viewModel::onSalirDialogoReporte) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    )
}

@Composable
fun ReporteItem(
    reporte: ReportesEntity,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Numero: ${reporte.numeroReportado}", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Folio: ${reporte.folio}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Fecha: ${reporte.fecha}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Filled.Delete, contentDescription = "Eliminar reporte")
            }
        }
    }
}
