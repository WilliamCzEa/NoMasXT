package com.intermedio.nomasxt.dominio.model

/**
 * Modelo de datos para representar un país y su clave telefónica internacional.
 */
data class Country(
    val name: String,
    val dialCode: String, // Ejemplo: "+52"
    val code: String,     // Ejemplo: "MX"
    val flag: String      // Emoji de la bandera
)