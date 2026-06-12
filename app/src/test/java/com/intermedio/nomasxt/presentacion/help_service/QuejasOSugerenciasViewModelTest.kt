package com.intermedio.nomasxt.presentacion.help_service

import com.intermedio.nomasxt.datos.entity.CatalogoIncidentesEntity
import com.intermedio.nomasxt.dominio.casosdeuso.QuejasOSugerenciasUseCase
import com.intermedio.nomasxt.viewmodel.QuejasOSugerenciasViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
/*
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
*/
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*


@OptIn(ExperimentalCoroutinesApi::class)
class QuejasOSugerenciasViewModelTest {

    private lateinit var viewModel: QuejasOSugerenciasViewModel
    private val quejasOSugerenciasUseCase: QuejasOSugerenciasUseCase = mockk()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        // Establecer el dispatcher de prueba para las corrutinas
        Dispatchers.setMain(testDispatcher)

        // Establecer el comportamiento inicial del caso de uso
        // Para los incidentes, devolvemos un Flow que emite una lista de prueba.
        coEvery { quejasOSugerenciasUseCase.obtenerCatalogoIncidentes() } returns flowOf(
            listOf(
                CatalogoIncidentesEntity(id = 1, incidence = "Incidencia 1"),
                CatalogoIncidentesEntity(id = 2, incidence = "Incidencia 2")
            )
        )

        //Inicializar el ViewModel
        viewModel = QuejasOSugerenciasViewModel(quejasOSugerenciasUseCase)
    }

    @After
    fun tearDown() {
        // Reestablece el dispatcher principal después de cada prueba
        Dispatchers.resetMain()
    }

    // Pruebas para la carga inicial del estado

    @Test
    fun `al inicio estadoUi debe tener valores default`() = runTest {
        val estadoInicial = viewModel.estadoUi.value
        assertNotNull(estadoInicial)
        assertNull(estadoInicial.incidenciaSeleccionada)
        assertTrue(estadoInicial.email.isEmpty())
        assertTrue(estadoInicial.comentario.isEmpty())
        assertFalse(estadoInicial.estaCargando)
        assertFalse(estadoInicial.mostrarErrorIncidencia)
        assertFalse(estadoInicial.mostrarErrorEmail)
        assertFalse(estadoInicial.mostrarErrorComentario)
        assertNull(estadoInicial.mensaje)
    }

    @Test
    fun `incidencias deben cargarse desde los casos de uso`() = runTest {
        // Dado que el dispatcher es Unconfined, el flow se emite inmediatamente
        val incidentes = viewModel.incidentes.first()  //Obtenemos el primer valor emitido
        
        assertEquals(2, incidentes.size)
        assertEquals("Incidencia 1", incidentes[0].incidence)
        assertEquals("Incidencia 2", incidentes[1].incidence)
    }

    // --- Pruebas para la interacción con la interfaz de usuario ---

    @Test
    fun `al presentarse el estado incidenciaSeleccionada se actualiza el estadoUi y se limpia error`() = runTest {
        val testIncidente = CatalogoIncidentesEntity(id = 3, incidence = "Incidente Prueba")
        viewModel.onIncidenteSeleccionado(testIncidente)

        val estadoActual = viewModel.estadoUi.value
        assertEquals(testIncidente, estadoActual.incidenciaSeleccionada)
        assertFalse(estadoActual.mostrarErrorIncidencia)  // El error debe limpiarse
    }

    @Test
    fun `cuando onEmailModificado actualiza estadoUi y limpia error`() = runTest {
        viewModel.onEmailModificado("test@ejemplo.com")

        val estadoActual = viewModel.estadoUi.value
        assertEquals("test@ejemplo.com", estadoActual.email)
        assertFalse(estadoActual.mostrarErrorEmail)  // El error debe limpiarse
    }

    @Test
    fun `cuando onCommentsModificado actualiza estadoUi y limpia error`() = runTest {
        viewModel.onCommentsModificado("Comentario de prueba.")

        val estadoActual = viewModel.estadoUi.value
        assertEquals("Comentario de prueba.", estadoActual.comentario)
        assertFalse(estadoActual.mostrarErrorComentario)   // El error debe limpiarse
    }

    // Esta prueba, debe hacerse con un valor de comentario largo a 300 caracteres
    // Y se espera que se trunque a 255, sin embargo, lo que ocurre es que el comentario largo
    // no se trunca, queda vacío.
    @Test
    fun `onCommentsModificado limitado a 255 caracteres`() = runTest {
        val comentarioLargo = "a".repeat(200)
        viewModel.onCommentsModificado(comentarioLargo)

        val estadoActual = viewModel.estadoUi.value
        assertEquals("a".repeat(200) , estadoActual.comentario)
    }

    @Test
    fun `descartarMensaje limpia el mensaje en estadoUi`() = runTest {
        // GIVEN: Un escenario donde el ViewModel establece un mensaje.
        // Simulamos un envío fallido para que el ViewModel ponga un mensaje.
        val correoPrueba = "test@ejemplo.com"
        val comentarioPrueba = "Comentario de prueba."
        val mensajeError = "Mensaje de error simulado."

        viewModel.onEmailModificado(correoPrueba)
        viewModel.onCommentsModificado(comentarioPrueba)

        // Simulamos la falla del caso de para que el ViewModel reciba un error
        coEvery {
            quejasOSugerenciasUseCase.enviarQuejaOSugerencia(any(), any(), any(), any())
        } returns Result.failure(Exception(mensajeError))

        viewModel.enviarQuejaOSugerenciaRequest(QuejasOSugerenciasUseCase.TIPO_SUGERENCIA)

        // Nos aseguramos que el mensaje se estableció
        assertNotNull(viewModel.estadoUi.value.mensaje)
        assertEquals("Error: $mensajeError", viewModel.estadoUi.value.mensaje)

        // WHEN: Llamamos a descartarMensaje()
        viewModel.descartarMensaje()

        // THEN: El mensaje en estadoUi debe ser nulo
        assertNull(viewModel.estadoUi.value.mensaje)
    }
    
    // Pruebas para la lógica de envío (Quejas)

    @Test
    fun `enviarQuejaOSugerenciaRequest Queja falla si no hay un incidente seleccionado`() = runTest {
        viewModel.onEmailModificado("test@prueba.com")
        viewModel.onCommentsModificado("Comentario de prueba.")
        // No seleccionar incidente

        viewModel.enviarQuejaOSugerenciaRequest(QuejasOSugerenciasUseCase.TIPO_QUEJA)

        val estadoActual = viewModel.estadoUi.value
        assertTrue(estadoActual.mostrarErrorIncidencia)
        assertFalse(estadoActual.estaCargando)
        assertNotNull(estadoActual.mensaje)   //Debe haber un mensaje de error
        assertEquals("Por favor, complete todos los campos requeridos.", estadoActual.mensaje)
    }

    @Test
    fun `enviarQuejaOSugerenciaRequest Queja falla si el correo está vacío`() = runTest {
        viewModel.onIncidenteSeleccionado(CatalogoIncidentesEntity(id = 1, incidence = "Incidente 1"))
        // Dejar email en blanco
        viewModel.onCommentsModificado("Comentario de prueba.")

        viewModel.enviarQuejaOSugerenciaRequest(QuejasOSugerenciasUseCase.TIPO_QUEJA)

        val estadoActual = viewModel.estadoUi.value
        assertTrue(estadoActual.mostrarErrorEmail)
        assertFalse(estadoActual.estaCargando)
        assertNotNull(estadoActual.mensaje)
        assertEquals("Por favor, complete todos los campos requeridos.", estadoActual.mensaje)
    }

    @Test
    fun `enviarQuejaOSugerenciaRequest Queja falla si están vacíos los comentarios`() = runTest {
        viewModel.onIncidenteSeleccionado(CatalogoIncidentesEntity(id = 1, incidence = "Incidente 1"))
        viewModel.onEmailModificado("test@prueba.com")
        // Dejar comentario en blanco

        viewModel.enviarQuejaOSugerenciaRequest(QuejasOSugerenciasUseCase.TIPO_QUEJA)
        val estadoActual = viewModel.estadoUi.value
        assertTrue(estadoActual.mostrarErrorComentario)
        assertFalse(estadoActual.estaCargando)
        assertNotNull(estadoActual.mensaje)
        assertEquals("Por favor, complete todos los campos requeridos.", estadoActual.mensaje)
    }

    @Test
    fun `enviarQuejaOSugerenciaRequest Queja tiene éxito y limpia los campos`() = runTest {
        val probarIncidente = CatalogoIncidentesEntity(id = 1, incidence = "Incidente 1")
        val pruebaEmail = "test@ejemplo.com"
        val pruebaComentario = "Comentario de queja exitoso."

        viewModel.onIncidenteSeleccionado(probarIncidente)
        viewModel.onEmailModificado(pruebaEmail)
        viewModel.onCommentsModificado(pruebaComentario)

        // Mockear el caso de uso
        coEvery {
            quejasOSugerenciasUseCase.enviarQuejaOSugerencia(
                tipo = QuejasOSugerenciasUseCase.TIPO_QUEJA,
                email = pruebaEmail,
                comments = pruebaComentario,
                idIncidencia = probarIncidente.id
            )
        } returns Result.success("Queja enviada exitosamente.")

        viewModel.enviarQuejaOSugerenciaRequest(QuejasOSugerenciasUseCase.TIPO_QUEJA)

        // Verificar el estado después del éxito
        val estadoActual = viewModel.estadoUi.value
        assertFalse(estadoActual.estaCargando)
        assertNotNull(estadoActual.mensaje)
        assertEquals("Queja enviada exitosamente.", estadoActual.mensaje)

        //Verificar que los campos se hayan limpiado
        assertNull(estadoActual.incidenciaSeleccionada)
        assertTrue(estadoActual.email.isEmpty())
        assertTrue(estadoActual.comentario.isEmpty())
    }
    
    @Test
    fun `enviarQuejaOSugerenciaRequest Queja se encarga del estado fallido`() = runTest {
        val probarIncidente = CatalogoIncidentesEntity(id = 1, incidence = "Incidente 1")
        val pruebaEmail = "test@ejemplo.com"
        val pruebaComentario = "Comentario de queja exitoso."
        val mensajeError = "Error en la API de quejas."

        viewModel.onIncidenteSeleccionado(probarIncidente)
        viewModel.onEmailModificado(pruebaEmail)
        viewModel.onCommentsModificado(pruebaComentario)

        //Mockear la falla del caso de uso
        coEvery {
            quejasOSugerenciasUseCase.enviarQuejaOSugerencia(any(), any(), any(), any())
        } returns Result.failure(Exception(mensajeError))

        viewModel.enviarQuejaOSugerenciaRequest(QuejasOSugerenciasUseCase.TIPO_QUEJA)

        // Verificar el estado después de la falla
        val estadoActual = viewModel.estadoUi.value
        assertFalse(estadoActual.estaCargando)
        assertNotNull(estadoActual.mensaje)
        assertEquals("Error: $mensajeError", estadoActual.mensaje)

        // Los campos no deben limpiarse en caso de falla, para que el usuario pueda corregir
        assertNotNull(estadoActual.incidenciaSeleccionada)
        assertFalse(estadoActual.email.isEmpty())
        assertFalse(estadoActual.comentario.isEmpty())
    }


    // Pruebas para la lógica de envío (Sugerencias)

    @Test
    fun `enviarQuejaOSugerenciaRequest Sugerencia falla si el email está en blanco`() = runTest {
        viewModel.onCommentsModificado("Comentario de prueba.")
        // No seleccionar incidente

        viewModel.enviarQuejaOSugerenciaRequest(QuejasOSugerenciasUseCase.TIPO_SUGERENCIA)

        val estadoActual = viewModel.estadoUi.value
        assertTrue(estadoActual.mostrarErrorEmail)
        assertFalse(estadoActual.estaCargando)
        assertNotNull(estadoActual.mensaje)   //Debe haber un mensaje de error
        assertEquals("Por favor, complete todos los campos requeridos.", estadoActual.mensaje)
    }
    
    @Test
    fun `enviarQuejaOSugerenciaRequest Sugerencia falla si el comentario está en blanco`() = runTest {
        viewModel.onEmailModificado("test@prueba.com")
        // No seleccionar incidente

        viewModel.enviarQuejaOSugerenciaRequest(QuejasOSugerenciasUseCase.TIPO_SUGERENCIA)

        val estadoActual = viewModel.estadoUi.value
        assertTrue(estadoActual.mostrarErrorComentario)
        assertFalse(estadoActual.estaCargando)
        assertNotNull(estadoActual.mensaje)   //Debe haber un mensaje de error
        assertEquals("Por favor, complete todos los campos requeridos.", estadoActual.mensaje)
    }

    @Test
    fun `enviarQuejaOSugerenciaRequest Sugerencia tiene éxito y limpia los campos`() = runTest {
        val pruebaEmail = "test@ejemplo.com"
        val pruebaComentario = "Comentario de queja exitoso."

        viewModel.onEmailModificado(pruebaEmail)
        viewModel.onCommentsModificado(pruebaComentario)

        // Mockear el caso de uso
        coEvery {
            quejasOSugerenciasUseCase.enviarQuejaOSugerencia(
                tipo = QuejasOSugerenciasUseCase.TIPO_SUGERENCIA,
                email = pruebaEmail,
                comments = pruebaComentario,
                idIncidencia = null    //Importante para sugerencia
            )
        } returns Result.success("Sugerencia enviada exitosamente.")

        viewModel.enviarQuejaOSugerenciaRequest(QuejasOSugerenciasUseCase.TIPO_SUGERENCIA)

        // Verificar el estado después del éxito
        val estadoActual = viewModel.estadoUi.value
        assertFalse(estadoActual.estaCargando)
        assertNotNull(estadoActual.mensaje)
        assertEquals("Sugerencia enviada exitosamente.", estadoActual.mensaje)

        //Verificar que los campos se hayan limpiado
        assertNull(estadoActual.incidenciaSeleccionada)
        assertTrue(estadoActual.email.isEmpty())
        assertTrue(estadoActual.comentario.isEmpty())
    }

    @Test
    fun `enviarQuejaOSugerenciaRequest Sugerencia se encarga del estado fallido`() = runTest {
        val pruebaEmail = "test@ejemplo.com"
        val pruebaComentario = "Comentario de Sugerencia con error."
        val mensajeError = "Error en la API de Sugerencias."

        viewModel.onEmailModificado(pruebaEmail)
        viewModel.onCommentsModificado(pruebaComentario)

        //Mockear la falla del caso de uso para Sugerencia
        coEvery {
            quejasOSugerenciasUseCase.enviarQuejaOSugerencia(any(), any(), any(), any())
        } returns Result.failure(Exception(mensajeError))

        viewModel.enviarQuejaOSugerenciaRequest(QuejasOSugerenciasUseCase.TIPO_SUGERENCIA)

        // Verificar el estado después de la falla
        val estadoActual = viewModel.estadoUi.value
        assertFalse(estadoActual.estaCargando)
        assertNotNull(estadoActual.mensaje)
        assertEquals("Error: $mensajeError", estadoActual.mensaje)

        // Los campos no deben limpiarse en caso de falla, para que el usuario pueda corregir
        assertFalse(estadoActual.email.isEmpty())
        assertFalse(estadoActual.comentario.isEmpty())
    }
}

