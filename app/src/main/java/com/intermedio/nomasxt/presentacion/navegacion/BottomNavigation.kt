package com.intermedio.nomasxt.presentacion.navegacion

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.res.painterResource
import com.intermedio.nomasxt.R

//import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    @DrawableRes val iconResId: Int,
    val title: String
) {
    object Inicio : BottomNavItem("inicio", R.drawable.ic_my_reports, "Mis Reportes")
    //object Llamadas : BottomNavItem("llamadas", R.drawable.ic_home_black_24dp, "Contactos")
    object AcercaDe : BottomNavItem("acercaDe", R.drawable.ic_about, "Acerca De")
    object Mas : BottomNavItem("mas", R.drawable.ic_more_horiz, "Más")
}
