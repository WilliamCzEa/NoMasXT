package com.intermedio.nomasxt.presentacion.componentes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.intermedio.nomasxt.dominio.model.Country

/**
 * Componente para seleccionar la clave del país en el formulario de reporte.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryCodeSelector(
    selectedCountry: Country,
    countries: List<Country>,
    onCountrySelected: (Country) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = "${selectedCountry.flag} ${selectedCountry.dialCode}",
            onValueChange = {},
            readOnly = true,
            label = { Text("Lada") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            countries.forEach { country ->
                DropdownMenuItem(
                    text = {
                        Text(text = "${country.flag} ${country.name} (${country.dialCode})")
                    },
                    onClick = {
                        onCountrySelected(country)
                        expanded = false
                    }
                )
            }
        }
    }
}

val defaultCountries = listOf(
    Country("México", "+52", "MX", "🇲🇽"),
    Country("Colombia", "+57", "CO", "🇨🇴"),
    Country("Argentina", "+54", "AR", "🇦🇷"),
    Country("Chile", "+56", "CL", "🇨🇱"),
    Country("Estados Unidos", "+1", "US", "🇺🇸"),
    Country("España", "+34", "ES", "🇪🇸")
)