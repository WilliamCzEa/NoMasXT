package com.intermedio.nomasxt.presentacion.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.intermedio.nomasxt.R

@Composable
fun PantallaContactanos() {
    val scrollState = rememberScrollState()
    val avisoTexto = stringResource(R.string.link_about_us)
    val avisoURL = stringResource(R.string.url_about_us)
    val context = LocalContext.current

    Column(
    modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = stringResource(id = R.string.text_contact_us_view))
    }
}