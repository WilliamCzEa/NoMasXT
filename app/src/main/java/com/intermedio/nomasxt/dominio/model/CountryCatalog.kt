package com.intermedio.nomasxt.dominio.model

import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.Locale

private val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

// Normalizacion internacional: libphonenumber genera el catalogo real de regiones y prefijos.
val defaultCountries: List<Country> = phoneNumberUtil.supportedRegions
    .map { regionCode ->
        Country(
            name = Locale("", regionCode).displayCountry.ifBlank { regionCode },
            dialCode = "+${phoneNumberUtil.getCountryCodeForRegion(regionCode)}",
            code = regionCode
        )
    }
    .sortedBy { it.name }

