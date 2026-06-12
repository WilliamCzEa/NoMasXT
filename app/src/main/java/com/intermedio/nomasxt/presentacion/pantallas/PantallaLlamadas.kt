package com.intermedio.nomasxt.presentacion.pantallas

import android.Manifest
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

data class Contacto(val nombre: String, val telefono: String)

@Composable
fun PantallaLlamadas() {
    val context = LocalContext.current
    val contactos = remember { mutableStateListOf<Contacto>() }

    LaunchedEffect(key1 = true) {
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            cargarContactos(context, contactos)
        } else {
            Log.d("nomasxt", "PantallaLlamadas | No hay permiso para leer contactos")
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (contactos.isEmpty()) {
            Text("Cargando contactos...", style = MaterialTheme.typography.headlineSmall)
        } else {
            LazyColumn {
                items(contactos.size) { index ->
                    val contacto = contactos[index]
                    Text(
                        "${contacto.nombre} - ${contacto.telefono}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }

    /*Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Pantalla de llamdas", style = MaterialTheme.typography.headlineSmall)
    }*/
}

private fun cargarContactos(context: android.content.Context, contactos: MutableList<Contacto>) {
    contactos.clear()
    val cursor = context.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER),
        null,
        null,
        null
    )
    cursor?.use {
        while(it.moveToNext()) {
            val nombre = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val telefono = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
            contactos.add(Contacto(nombre, telefono))
        }
    }
}


