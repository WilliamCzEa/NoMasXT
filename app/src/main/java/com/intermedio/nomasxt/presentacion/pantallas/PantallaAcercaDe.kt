package com.intermedio.nomasxt.presentacion.pantallas

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.intermedio.nomasxt.R

@Composable
fun PantallaAcercaDe() {
    val scrollState = rememberScrollState()
    val avisoTexto = stringResource(R.string.link_about_us)
    val avisoURL = stringResource(R.string.url_about_us)
    val context = LocalContext.current

    val textoHipervinculo = AnnotatedString.Builder().apply {
        append(avisoTexto)
        addStringAnnotation(
            tag = "URL",
            annotation = avisoURL,
            start = 0,
            end = avisoTexto.length
        )
        addStyle(
            style = SpanStyle(
                color = Color.Blue,
                textDecoration = TextDecoration.Underline
            ),
            start = 0,
            end = avisoTexto.length
        )
    }.toAnnotatedString()
    /*Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Pantalla de Acerca De", style = MaterialTheme.typography.headlineSmall)
    }*/
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = stringResource(id = R.string.text_about_us))
        ClickableText(
                text = textoHipervinculo,
                onClick = { offset ->
                    textoHipervinculo.getStringAnnotations("URL", offset, offset)
                        .firstOrNull()?.let { annotation ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                            context.startActivity(intent)
                        }
                }
            )


        Text(text = stringResource(id = R.string.text_contact_us))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp))
        {
            Image(
                painter = painterResource(id = R.drawable.imagen_disi),
                contentDescription = "DISI",
                modifier = Modifier.width(150.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.imagen_nomasxt),
                contentDescription = "No Mas XT",
                modifier = Modifier.width(150.dp)
            )
        }
    }
}
