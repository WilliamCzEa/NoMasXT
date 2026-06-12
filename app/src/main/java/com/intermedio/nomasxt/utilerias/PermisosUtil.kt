package com.intermedio.nomasxt.utilerias

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner

object PermisosUtil {
    
    fun obtenerPermisosRequeridos(): List<String> {
        val permisos = mutableListOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_CONTACTS
        )

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            permisos.add(Manifest.permission.ANSWER_PHONE_CALLS)
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permisos.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        return permisos
    }

    fun registrarSolicitadorPermisos(
        owner: ComponentActivity,
        onPermisosOtorgados: () -> Unit,
        onPermisosDenegados: () -> Unit
    ): ActivityResultLauncher<Array<String>> {
        return (owner as? androidx.activity.ComponentActivity)?.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permisos ->
            val otorgados = permisos[Manifest.permission.READ_PHONE_STATE] == true &&
                            permisos[Manifest.permission.READ_CALL_LOG] == true &&
                            permisos[Manifest.permission.CALL_PHONE] == true &&
                            permisos[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true && // Revisar si el permiso de lectura de contactos también aplica para ciertas versiones en adelante
                            (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || permisos[Manifest.permission.ANSWER_PHONE_CALLS] == true ) &&
                            (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || permisos[Manifest.permission.POST_NOTIFICATIONS] == true)
            if (otorgados) {
                onPermisosOtorgados()
            } else {
                onPermisosDenegados()
            }

        } ?: throw IllegalStateException("Debe llamarse desde una activity con ciclo de vida")
    }

}
