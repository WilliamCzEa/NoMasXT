package com.intermedio.nomasxt.presentacion.alertas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.intermedio.nomasxt.ui.theme.NoMasXTTheme

class AlertaEnPantallaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Oculta las barras del sistema para pantalla completa
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            NoMasXTTheme{
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
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚠️ Llamada Sospechosa",
                fontSize = 28.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // Aquí puedes colgar la llamada, reportarla, etc.
                    // Puedes usar un callback, ViewModel o una función directa
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("Colgar Llamada", color = Color.Red)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    // Finaliza la Activity
                    (context as? ComponentActivity)?.finish()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("Ignorar", color = Color.Black)
            }
        }
    }
}

