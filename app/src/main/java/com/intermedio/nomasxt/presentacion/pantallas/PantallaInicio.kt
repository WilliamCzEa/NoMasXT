package com.intermedio.nomasxt.presentacion.pantallas

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.intermedio.nomasxt.viewmodel.MisReportesViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import com.intermedio.nomasxt.datos.entity.ReportesEntity

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

    Scaffold(
        topBar = {
            TopAppBar( title = { Text("Mis Reportes") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::onFabPresionado) {
                Icon(Icons.Filled.Add, "Reportar nuevo número")
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
                if(reportesDeApi.isEmpty()) {
                    Text(
                        text = "Aún no tienes reportes enviados.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 32.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(reportesDeApi) { reporte ->
                            ReporteItem(reporte = reporte)
                        }
                    }
                }
            }
            //Diálogo para reportar número.
            if(mostrarDialogoReporte) {
                AlertDialog(
                    onDismissRequest = viewModel::onSalirDialogoReporte,
                    title = { Text("Reportar Número") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = numeroAReportar,
                                onValueChange = viewModel::onNumeroIntroducidoCambia,
                                label = { Text("Número a reportar (+52XXXXXXXXXX)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = estaCargando
                            )
                            if (reporteMensajeDeEstado != null ) {
                                Log.d("nomasxt", "PantallaInicio | Entramos a que el reporteMensajeDeEstado no es null y el dato es: $reporteMensajeDeEstado")
                                Text(
                                    text = reporteMensajeDeEstado!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            } else {
                                Log.d("nomasxt", "PantallaInicio | reporteMensajeDeEstado es null")

                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = viewModel::onReportarPresionado,
                            enabled = !estaCargando && numeroAReportar.isNotBlank() && numeroAReportar.length >= 10
                        ) {
                            if(estaCargando) {
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
fun ReporteItem(reporte: ReportesEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Número: ${reporte.numeroReportado}", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Folio: ${reporte.folio}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Fecha: ${reporte.fecha}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
