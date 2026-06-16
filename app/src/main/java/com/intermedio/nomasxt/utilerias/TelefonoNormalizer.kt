package com.intermedio.nomasxt.utilerias

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil

const val DEFAULT_DIAL_CODE = "+52"
const val DEFAULT_REGION_CODE = "MX"

object TelefonoNormalizer {
    private const val MIN_DIGITS = 8
    private const val MAX_DIGITS_E164 = 15
    private const val UNKNOWN_REGION = "ZZ"
    private val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

    // Normalizacion internacional: devuelve formato E.164, por ejemplo +525510522522.
    fun normalizar(
        numeroIngresado: String,
        dialCode: String = DEFAULT_DIAL_CODE,
        regionCode: String = DEFAULT_REGION_CODE
    ): String {
        val limpio = limpiarEntrada(numeroIngresado)

        return try {
            val regionParaParseo = if (limpio.startsWith("+") || limpio.startsWith("00")) {
                UNKNOWN_REGION
            } else {
                regionCode
            }
            val numeroParseado = phoneNumberUtil.parse(limpio, regionParaParseo)

            phoneNumberUtil.format(numeroParseado, PhoneNumberUtil.PhoneNumberFormat.E164)
        } catch (e: NumberParseException) {
            // Normalizacion internacional: fallback para no bloquear entradas que la libreria no parsea.
            normalizarBasico(limpio, dialCode)
        }
    }

    fun esValido(
        numeroNormalizado: String,
        regionCode: String? = null
    ): Boolean {
        return try {
            val numeroParseado = phoneNumberUtil.parse(numeroNormalizado, UNKNOWN_REGION)
            val validoGlobal = phoneNumberUtil.isValidNumber(numeroParseado)
            val validoParaRegion = regionCode?.let {
                phoneNumberUtil.isValidNumberForRegion(numeroParseado, it)
            } ?: true

            validoGlobal && validoParaRegion
        } catch (e: NumberParseException) {
            esValidoBasico(numeroNormalizado)
        }
    }

    private fun limpiarEntrada(numero: String): String {
        return numero
            .trim()
            .replace(" ", "")
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")
    }

    private fun normalizarBasico(numeroLimpio: String, dialCode: String): String {
        if (numeroLimpio.startsWith("+")) {
            return "+" + numeroLimpio.drop(1).filter { it.isDigit() }
        }

        if (numeroLimpio.startsWith("00")) {
            return "+" + numeroLimpio.drop(2).filter { it.isDigit() }
        }

        val soloDigitos = numeroLimpio.filter { it.isDigit() }
        val codigoPais = dialCode.filter { it.isDigit() }

        return if (soloDigitos.startsWith(codigoPais)) {
            "+$soloDigitos"
        } else {
            "+$codigoPais$soloDigitos"
        }
    }

    private fun esValidoBasico(numeroNormalizado: String): Boolean {
        val digitos = numeroNormalizado.drop(1)
        return numeroNormalizado.startsWith("+") &&
            digitos.all { it.isDigit() } &&
            digitos.length in MIN_DIGITS..MAX_DIGITS_E164
    }
}
