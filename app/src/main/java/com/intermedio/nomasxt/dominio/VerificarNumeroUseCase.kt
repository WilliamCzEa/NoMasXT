package com.intermedio.nomasxt.dominio

import com.intermedio.nomasxt.datos.repository.NumeroRepository
import javax.inject.Inject

class VerificarNumeroUseCase @Inject constructor(
    private val numeroRepository: NumeroRepository
) {
    suspend operator fun invoke(numero: String): Boolean {
        return numeroRepository.verificarNumeroEnBD(numero)
    }
}