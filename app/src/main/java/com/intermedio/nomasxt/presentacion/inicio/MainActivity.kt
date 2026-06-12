package com.intermedio.nomasxt.presentacion.inicio

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.intermedio.nomasxt.interceptor.LlamadaEntranteInterceptor

import com.intermedio.nomasxt.ui.theme.NoMasXTTheme
import com.intermedio.nomasxt.presentacion.navegacion.AppNavigation
import com.intermedio.nomasxt.utilerias.PermisosUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var permisoLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Registrar el launcher al iniciar la actividad
        permisoLauncher = PermisosUtil.registrarSolicitadorPermisos(
            owner = this,
            onPermisosOtorgados = {
                Log.d("nomasxt", "MainActivity || Todos los permisos otorgados")
                mostrarPermisosYEstado(this)
            },
            onPermisosDenegados = {
                Log.d("nomasxt", "MainActivity || Permisos denegados")
            }
        )

        val permisos = PermisosUtil.obtenerPermisosRequeridos().toTypedArray()
        permisoLauncher.launch(permisos)

        Log.d("nomasxt", "MainActivity | Iniciando servicio de llamada entrante")
        val intent = Intent(this, LlamadaEntranteInterceptor::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("nomasxt", "MainActivity | Entramos a versión O")
            startForegroundService(intent)
        } else {
            Log.d("nomasxt", "MainActivity | Entramos a versión menor a O")
            startService(intent)
        }

        setContent {
            NoMasXTTheme {
                AppNavigation()
            }
        }
    }
}

//Esta función es temporal, para verificar los permisos que tiene el teléfono para la aplicación.
private fun mostrarPermisosYEstado(context: Context) {
    try {
        val informacionDelPaquete = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )

        val permisosDeclarados = informacionDelPaquete.requestedPermissions
        if(permisosDeclarados != null) {
            for (permiso in permisosDeclarados) {
                val estado = ContextCompat.checkSelfPermission(context, permiso)
                Log.d("nomasxt", "MainActivity | Permiso: $permiso -> Estado crudo: $estado")
            }
        } else {
            Log.d("nomasxt", "MainActivity | No hay permisos declarados en el Manifest")
        }

    } catch(e: Exception) {
        Log.d("nomasxt", "MainActivity | Ocurrió un error al mostrar los permisos: ${e.message}")

    }
}
