package com.intermedio.nomasxt.presentacion.alertas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.intermedio.nomasxt.ui.theme.NoMasXTTheme

class AlertaEnPantallaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mantiene la alerta en pantalla completa sobre la llamada entrante.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            NoMasXTTheme {
                PantallaCompletaAlertUI()
            }
        }
    }
}

@Preview
@Composable
fun PantallaCompletaAlertUI() {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Red),
        contentAlignment = Alignment.Center
    ) {
        Column(
            // fillMaxWidth centra el contenido en pantallas pequenas y grandes sin depender del modelo.
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Llamada Sospechosa",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 28.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // Pendiente: colgar o reportar llamada desde esta accion.
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("Colgar Llamada", color = Color.Red)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    (context as? ComponentActivity)?.finish()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("Ignorar", color = Color.Black)
            }
        }
    }
}
