# NoMasXT - Cuaderno del proyecto

Este README sirve como cuaderno de estudio para entender la app NoMasXT en Kotlin, Android y la arquitectura real del proyecto.


## Objetivo del proyecto

NoMasXT es una aplicacion Android escrita en Kotlin. Su proposito principal es ayudar a reportar, guardar y consultar numeros relacionados con extorsion o llamadas no deseadas.

La app trabaja con:

- Una base de datos local en el telefono.
- Una API remota para enviar y consultar informacion.
- Pantallas construidas con Jetpack Compose.
- Logica organizada por capas.
- Un servicio que puede interactuar con llamadas entrantes.

## Tecnologias principales

- Kotlin: lenguaje principal del proyecto.
- Android: plataforma donde corre la app.
- Jetpack Compose: framework para construir interfaces de usuario.
- ViewModel: administra estado y logica de pantalla.
- Room: base de datos local.
- Retrofit: cliente HTTP para comunicarse con el servidor.
- Moshi: conversion entre JSON y objetos Kotlin.
- Hilt/Dagger: inyeccion de dependencias.
- Coroutines: ejecucion asincrona.
- Flow/StateFlow: flujos reactivos de datos.

## Estructura general

```text
app/
  src/main/
    AndroidManifest.xml
    java/com/intermedio/nomasxt/
      presentacion/
      viewmodel/
      dominio/
      datos/
      utilerias/
      interceptor/
      ui/theme/
```

## Capas del proyecto

### presentacion/

Contiene lo que ve el usuario:

- Activities.
- Pantallas de Jetpack Compose.
- Navegacion.
- Componentes visuales.

Ejemplos:

- `MainActivity.kt`
- `SplashActivity.kt`
- `AppNavigation.kt`
- `PantallaPerfil.kt`
- `PantallaQuejas.kt`

### viewmodel/

Contiene clases que conectan la pantalla con la logica de la app.

Un ViewModel:

- Guarda el estado de pantalla.
- Recibe eventos del usuario.
- Llama casos de uso o repositorios.
- Evita poner logica pesada directamente en la UI.

Ejemplo:

- `MisReportesViewModel.kt`

### dominio/

Contiene reglas de negocio y casos de uso.

Un caso de uso representa una accion importante de la app, por ejemplo:

- Reportar un numero.
- Descargar y guardar datos.
- Enviar una queja o sugerencia.

Ejemplo:

- `ReportarNumeroUseCase.kt`

### datos/

Contiene acceso a datos locales y remotos.

Aqui viven:

- Repositories.
- DAOs.
- Entities.
- DTOs.
- Modulos de Hilt.
- Base de datos Room.
- API Retrofit.

Ejemplos:

- `ReportesRepository.kt`
- `ApiService.kt`
- `BaseDeDatos.kt`
- `ReportesDao.kt`
- `ReportesEntity.kt`
- `RetrofitModule.kt`

### utilerias/

Contiene funciones auxiliares compartidas.

Ejemplos:

- `TelefonoNormalizer.kt`
- `PermisosUtil.kt`

### interceptor/

Contiene logica relacionada con llamadas entrantes.

Ejemplo:

- `LlamadaEntranteInterceptor.kt`

## Flujo general de una accion

Cuando el usuario reporta un numero, el flujo general es:

```text
Pantalla
  -> ViewModel
    -> UseCase
      -> Repository
        -> Room / API
```

Explicado:

1. La pantalla recibe la accion del usuario.
2. El ViewModel valida datos y administra el estado.
3. El caso de uso arma la accion de negocio.
4. El repositorio decide si guarda localmente, llama a la API o ambas.
5. Room guarda datos en el telefono y Retrofit manda datos al servidor.

## Archivos clave

### `AndroidManifest.xml`

Declara informacion esencial de la app:

- Permisos.
- Activities.
- Servicios.
- Clase principal de aplicacion.
- Configuracion general.

### `NoMasXTApp.kt`

Clase principal de la aplicacion.

```kotlin
@HiltAndroidApp
class NoMasXTApp: Application()
```

`@HiltAndroidApp` activa Hilt en toda la app.

### `MainActivity.kt`

Activity principal de la app.

Responsabilidades principales:

- Solicitar permisos.
- Iniciar el servicio de llamadas entrantes.
- Cargar la interfaz con `setContent`.
- Mostrar `AppNavigation`.

### `AppNavigation.kt`

Define la navegacion entre pantallas usando Jetpack Compose Navigation.

Aqui se decide:

- Cual es la pantalla inicial.
- Que pantallas aparecen en la barra inferior.
- Que titulo tiene cada pantalla.
- Como regresar con la flecha superior.

### `RetrofitModule.kt`

Crea objetos compartidos para red:

- `Moshi`
- `Retrofit`
- `ApiService`

Hilt usa este modulo para inyectar dependencias donde se necesiten.

### `ApiService.kt`

Define los endpoints de la API.

Ejemplo:

```kotlin
@POST("/services/1.0/extortion/ReporterNumber")
suspend fun reportarNumero(@Body body: ReportesDto): Response<ReportesResponseDto>
```

Esta funcion manda un reporte al servidor.

### `BaseDeDatos.kt`

Define la base de datos local usando Room.

Aqui se registran las tablas y los DAOs.

### `ReportesRepository.kt`

Maneja la logica de reportes.

Su flujo principal es offline-first:

1. Guarda el numero localmente.
2. Intenta enviarlo al servidor.
3. Si el servidor falla, deja el reporte como pendiente.
4. Luego puede sincronizarlo.

## Conceptos Kotlin que iremos aprendiendo

### `val`

Variable de solo lectura. Una vez asignada, no se cambia.

```kotlin
val numero = "5551234567"
```

### `var`

Variable modificable.

```kotlin
var contador = 0
contador = 1
```

### `fun`

Declara una funcion.

```kotlin
fun saludar(): String {
    return "Hola"
}
```

### `class`

Declara una clase.

```kotlin
class ReportesRepository
```

### `data class`

Clase pensada para guardar datos.

```kotlin
data class ReportesEntity(
    val id: String,
    val fecha: String
)
```

### `suspend`

Indica que una funcion puede ejecutarse de forma asincrona usando coroutines.

Se usa mucho para:

- Consultas a base de datos.
- Llamadas a API.
- Trabajo pesado que no debe congelar la pantalla.

### `Flow`

Representa un flujo de datos que puede emitir cambios con el tiempo.

En este proyecto se usa para observar datos de Room y actualizar pantallas.

## Glosario

- Activity: pantalla o entrada principal del mundo Android tradicional.
- Composable: funcion que dibuja UI con Jetpack Compose.
- ViewModel: clase que guarda estado y logica de pantalla.
- Repository: clase que centraliza acceso a datos.
- UseCase: clase que representa una accion de negocio.
- DAO: interfaz que define consultas a la base local.
- Entity: clase que representa una tabla de Room.
- DTO: clase usada para enviar o recibir datos de la API.
- API: servidor externo con endpoints.
- Endpoint: ruta especifica del servidor.
- Coroutine: forma de ejecutar tareas asincronas en Kotlin.
- Hilt: herramienta para crear e inyectar objetos automaticamente.

## Clases del curso

### Clase 1 - Mapa general del proyecto

Temas:

- Estructura del proyecto.
- Capas principales.
- Flujo general de una accion.
- Archivos base: Manifest, Gradle, App, MainActivity.

Notas personales:

```text
Aqui puedo escribir lo que entendi de la clase 1.
```

### Clase 2 - Kotlin basico desde el proyecto

Temas:

- `val` y `var`.
- Tipos de datos.
- Funciones.
- Clases.
- Constructores.
- Null safety.
- `data class`.

Archivos usados:

- `app/src/main/java/com/intermedio/nomasxt/datos/di/RetrofitModule.kt`
- `app/src/main/java/com/intermedio/nomasxt/datos/repository/ReportesRepository.kt`

#### 1. `package`

Ejemplo:

```kotlin
package com.intermedio.nomasxt.datos.di
```

El `package` indica donde vive el archivo dentro del proyecto.

En este caso:

```text
com.intermedio.nomasxt.datos.di
```

significa:

- `com.intermedio.nomasxt`: paquete base de la app.
- `datos`: capa de datos.
- `di`: dependency injection, es decir, inyeccion de dependencias.

#### 2. `import`

Ejemplo:

```kotlin
import retrofit2.Retrofit
```

Un `import` permite usar una clase que esta en otro paquete.

Sin ese import, Kotlin no sabria a que `Retrofit` nos referimos.

Otro ejemplo:

```kotlin
import com.intermedio.nomasxt.datos.remoto.ApiService
```

Aqui estamos usando una clase propia del proyecto.

#### 3. Anotaciones

Ejemplo:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule
```

Las anotaciones empiezan con `@`.

Sirven para darle informacion extra al compilador, a Android o a una libreria.

En este proyecto se usan mucho con:

- Hilt.
- Room.
- Retrofit.
- Compose.

Ejemplos:

- `@Module`: esta clase es un modulo de Hilt.
- `@Provides`: esta funcion sabe crear una dependencia.
- `@Singleton`: se crea una sola instancia compartida.
- `@Inject`: Hilt puede construir esta clase.

#### 4. `object`

Ejemplo:

```kotlin
object RetrofitModule
```

`object` crea una unica instancia de algo.

Es decir, no necesitas hacer:

```kotlin
RetrofitModule()
```

Kotlin ya crea un unico `RetrofitModule` por ti.

En este proyecto se usa para el modulo de Retrofit porque solo necesitamos una fabrica global de dependencias.

#### 5. `class`

Ejemplo:

```kotlin
class ReportesRepository @Inject constructor(
    private val apiService: ApiService,
    private val numeroDao: NumerosDao,
    private val reportesDao: ReportesDao
)
```

`class` declara una clase.

Una clase es un molde para crear objetos.

`ReportesRepository` es la clase encargada de coordinar datos de reportes:

- Guarda en Room.
- Llama al API.
- Elimina reportes.
- Sincroniza reportes pendientes.

#### 6. Constructor primario

En Kotlin, el constructor puede escribirse junto al nombre de la clase:

```kotlin
class ReportesRepository @Inject constructor(...)
```

Los parametros del constructor son:

```kotlin
private val apiService: ApiService,
private val numeroDao: NumerosDao,
private val reportesDao: ReportesDao
```

Esto significa:

- `apiService` sirve para llamar a la API.
- `numeroDao` sirve para consultar o modificar la tabla de numeros.
- `reportesDao` sirve para consultar o modificar la tabla de reportes.

`private val` significa:

- `private`: solo se puede usar dentro de esta clase.
- `val`: no se puede reasignar despues.
- `apiService`: nombre de la propiedad.
- `ApiService`: tipo de dato.

#### 7. Tipos de dato

Ejemplo:

```kotlin
fun provideMoshi(): Moshi
```

Esta funcion devuelve un objeto de tipo `Moshi`.

La forma general es:

```kotlin
fun nombreFuncion(): TipoQueDevuelve
```

Otro ejemplo:

```kotlin
suspend fun reportarNumero(reporteDto: ReportesDto): String
```

Significa:

- La funcion se llama `reportarNumero`.
- Recibe un parametro llamado `reporteDto`.
- Ese parametro es de tipo `ReportesDto`.
- Devuelve un `String`.
- Es `suspend`, entonces puede ejecutarse con coroutines.

#### 8. `val`

Ejemplo:

```kotlin
val numeroReportado = reporteDto.reportedMsisdn.orEmpty()
```

`val` declara una variable de solo lectura.

Aqui se toma el numero reportado desde `reporteDto`.

`.orEmpty()` convierte un valor nulo en texto vacio.

Si `reportedMsisdn` es `null`, entonces `numeroReportado` queda como:

```kotlin
""
```

#### 9. Null safety

Kotlin intenta evitar errores por valores nulos.

Ejemplo:

```kotlin
reporteDto.label ?: "Reporte local"
```

El operador `?:` se llama Elvis operator.

Significa:

```text
usa reporteDto.label si no es null;
si es null, usa "Reporte local"
```

Otro ejemplo:

```kotlin
respuesta.body()?.result?.resultCode
```

El operador `?.` significa:

```text
si lo de la izquierda no es null, continua;
si es null, devuelve null sin romper la app
```

#### 10. `suspend`

Ejemplo:

```kotlin
suspend fun reportarNumero(reporteDto: ReportesDto): String
```

`suspend` indica que la funcion puede pausar su ejecucion sin bloquear el hilo principal.

Se usa para:

- Llamadas a servidor.
- Consultas a base de datos.
- Operaciones que tardan.

Esto es muy importante en Android porque la pantalla no debe congelarse.

#### 11. `withContext(Dispatchers.IO)`

Ejemplo:

```kotlin
return withContext(Dispatchers.IO) {
    ...
}
```

`Dispatchers.IO` es un contexto pensado para tareas de entrada/salida:

- Base de datos.
- Archivos.
- Red.

La idea es:

```text
haz este trabajo pesado fuera del hilo principal de la pantalla
```

#### 12. `try/catch`

Ejemplo:

```kotlin
try {
    guardarReporteLocalPendiente(reporteDto)
} catch (e: Exception) {
    return@withContext "No se pudo registrar el numero reportado localmente, intente mas tarde."
}
```

`try/catch` sirve para manejar errores.

Si ocurre un error dentro del `try`, el programa no se rompe de golpe; entra al `catch`.

`e: Exception` representa el error que ocurrio.

#### 13. `if/else`

Ejemplo:

```kotlin
if (enviado) {
    "$mensajeNumeroGuardado\nReporte enviado al servidor correctamente."
} else {
    "$mensajeNumeroGuardado\nServidor no disponible. El reporte queda pendiente por enviar."
}
```

En Kotlin, `if` puede devolver un valor.

Aqui la funcion devuelve un mensaje diferente dependiendo de si el reporte se pudo enviar al servidor.

#### 14. String templates

Ejemplo:

```kotlin
"ReportesRepository | Numero $numeroReportado guardado localmente"
```

`$numeroReportado` inserta el valor de la variable dentro del texto.

Otro ejemplo:

```kotlin
"${e.message}"
```

Cuando la expresion es mas compleja, se usan llaves.

#### 15. Funciones privadas

Ejemplo:

```kotlin
private suspend fun guardarReporteLocalPendiente(reporteDto: ReportesDto)
```

`private` significa que esta funcion solo puede usarse dentro de `ReportesRepository`.

Esto ayuda a proteger la logica interna de la clase.

Desde fuera, otras clases llaman principalmente a funciones publicas como:

```kotlin
reportarNumero(...)
eliminarNumeroReportado(...)
obtenerTodoslosReportesLocales()
```

#### 16. `companion object`

Ejemplo:

```kotlin
private companion object {
    const val STATUS_PENDIENTE = "PENDIENTE"
    const val FOLIO_LOCAL = "LOCAL"
}
```

Un `companion object` guarda valores o funciones asociadas a la clase, no a un objeto individual.

Aqui se usa para constantes.

`const val` significa que el valor es constante desde compilacion.

#### 17. Crear objetos

Ejemplo:

```kotlin
val reporteLocal = ReportesEntity(
    id = numeroReportado,
    fecha = fechaActual(),
    folio = FOLIO_LOCAL,
    etiqueta = reporteDto.label ?: "Reporte local",
    numeroReportado = numeroReportado,
    status = STATUS_PENDIENTE
)
```

Aqui se crea un objeto `ReportesEntity`.

Cada linea asigna un valor a una propiedad.

Este objeto despues se guarda en Room:

```kotlin
reportesDao.agregarReporte(reporteLocal)
```

#### 18. Lectura resumida de `reportarNumero`

Esta funcion:

```kotlin
suspend fun reportarNumero(reporteDto: ReportesDto): String
```

hace esto:

1. Cambia a `Dispatchers.IO`.
2. Obtiene el numero reportado.
3. Intenta guardar el reporte localmente.
4. Si falla el guardado local, devuelve mensaje de error.
5. Si guarda bien, intenta enviar al servidor.
6. Si el servidor responde bien, devuelve mensaje de exito.
7. Si el servidor falla, deja el reporte pendiente y devuelve un mensaje informativo.

#### 19. Lectura guiada de `reportarNumero`

Codigo base:

```kotlin
suspend fun reportarNumero(reporteDto: ReportesDto): String {
    return withContext(Dispatchers.IO) {
        val numeroReportado = reporteDto.reportedMsisdn.orEmpty()
        val mensajeNumeroGuardado = "Numero registrado correctamente en su lista de bloqueo."

        try {
            guardarReporteLocalPendiente(reporteDto)
            Log.d("nomasxt", "ReportesRepository | Numero $numeroReportado guardado localmente")
        } catch (e: Exception) {
            Log.d("nomasxt", "ReportesRepository | Error guardando reporte local $numeroReportado: ${e.message}")
            return@withContext "No se pudo registrar el numero reportado localmente, intente mas tarde."
        }

        val enviado = enviarReporteAlServidor(reporteDto)

        if (enviado) {
            "$mensajeNumeroGuardado\nReporte enviado al servidor correctamente."
        } else {
            "$mensajeNumeroGuardado\nServidor no disponible. El reporte queda pendiente por enviar."
        }
    }
}
```

Esta funcion es una de las mas importantes para entender el proyecto.

##### Linea 1

```kotlin
suspend fun reportarNumero(reporteDto: ReportesDto): String
```

Significa:

- `suspend`: esta funcion puede hacer trabajo asincrono.
- `fun`: se esta declarando una funcion.
- `reportarNumero`: nombre de la funcion.
- `reporteDto`: parametro que recibe.
- `ReportesDto`: tipo del parametro.
- `String`: tipo de dato que devuelve.

En palabras simples:

```text
Esta funcion recibe los datos de un reporte y devuelve un mensaje de resultado.
```

##### Linea 2

```kotlin
return withContext(Dispatchers.IO) {
```

Significa:

```text
Todo lo que esta dentro de este bloque se ejecutara en un hilo pensado para operaciones pesadas.
```

Se usa `Dispatchers.IO` porque la funcion:

- Guarda en base de datos.
- Consulta DAOs.
- Llama al servidor.

Eso no debe hacerse en el hilo principal de la UI.

##### Linea 3

```kotlin
val numeroReportado = reporteDto.reportedMsisdn.orEmpty()
```

Aqui se obtiene el numero reportado desde el DTO.

`reportedMsisdn` puede venir con valor o puede venir como `null`.

Por eso se usa:

```kotlin
.orEmpty()
```

Si trae numero:

```text
"5512345678"
```

se conserva ese valor.

Si viene `null`, se convierte a:

```text
""
```

##### Linea 4

```kotlin
val mensajeNumeroGuardado = "Numero registrado correctamente en su lista de bloqueo."
```

Aqui se crea un mensaje base.

Este mensaje se usara despues tanto si el servidor responde bien como si falla, porque primero se intenta guardar el numero localmente.

##### Bloque `try`

```kotlin
try {
    guardarReporteLocalPendiente(reporteDto)
    Log.d("nomasxt", "ReportesRepository | Numero $numeroReportado guardado localmente")
}
```

Aqui la app intenta guardar el reporte en la base local.

La funcion llamada es:

```kotlin
guardarReporteLocalPendiente(reporteDto)
```

Esa funcion hace dos cosas:

1. Guarda el numero en la tabla de numeros.
2. Guarda un reporte local con estado pendiente.

Luego:

```kotlin
Log.d(...)
```

escribe un mensaje en Logcat para depuracion.

##### Bloque `catch`

```kotlin
catch (e: Exception) {
    Log.d("nomasxt", "ReportesRepository | Error guardando reporte local $numeroReportado: ${e.message}")
    return@withContext "No se pudo registrar el numero reportado localmente, intente mas tarde."
}
```

Si ocurre un error al guardar localmente, se entra aqui.

`e` representa el error.

`e.message` contiene el mensaje tecnico del error.

Esta linea:

```kotlin
return@withContext "No se pudo registrar el numero reportado localmente, intente mas tarde."
```

devuelve un mensaje desde dentro del bloque `withContext`.

Se escribe `return@withContext` porque estamos dentro de una lambda:

```kotlin
withContext(...) { ... }
```

En palabras simples:

```text
Si no se pudo guardar localmente, ya no intentes enviar al servidor.
```

Esto tiene sentido porque el flujo offline-first depende de que primero exista copia local.

##### Envio al servidor

```kotlin
val enviado = enviarReporteAlServidor(reporteDto)
```

Aqui se intenta enviar el mismo reporte al servidor.

La variable `enviado` sera un `Boolean`.

Puede valer:

```kotlin
true
```

si el servidor acepto el reporte.

O:

```kotlin
false
```

si hubo error de red, error HTTP o respuesta invalida.

##### Decision final

```kotlin
if (enviado) {
    "$mensajeNumeroGuardado\nReporte enviado al servidor correctamente."
} else {
    "$mensajeNumeroGuardado\nServidor no disponible. El reporte queda pendiente por enviar."
}
```

Si `enviado` es verdadero, devuelve:

```text
Numero registrado correctamente en su lista de bloqueo.
Reporte enviado al servidor correctamente.
```

Si `enviado` es falso, devuelve:

```text
Numero registrado correctamente en su lista de bloqueo.
Servidor no disponible. El reporte queda pendiente por enviar.
```

La parte importante es que en ambos casos el usuario queda protegido localmente, porque el numero ya se guardo en el telefono.

##### Idea central

Esta funcion no depende completamente del servidor.

Primero protege al usuario localmente y despues intenta sincronizar con la API.

Eso es lo que llamamos:

```text
offline-first
```

Es decir:

```text
La app intenta seguir funcionando aunque el servidor o internet fallen.
```

#### 20. Lectura guiada de `guardarReporteLocalPendiente`

Codigo base:

```kotlin
private suspend fun guardarReporteLocalPendiente(reporteDto: ReportesDto) {
    val numeroReportado = reporteDto.reportedMsisdn.orEmpty()
    numeroDao.insertaNumero(NumerosEntity(numero = numeroReportado))

    val reporteLocal = ReportesEntity(
        id = numeroReportado,
        fecha = fechaActual(),
        folio = FOLIO_LOCAL,
        etiqueta = reporteDto.label ?: "Reporte local",
        numeroReportado = numeroReportado,
        status = STATUS_PENDIENTE
    )
    reportesDao.agregarReporte(reporteLocal)
}
```

##### Firma de la funcion

```kotlin
private suspend fun guardarReporteLocalPendiente(reporteDto: ReportesDto)
```

Significa:

- `private`: solo se puede usar dentro de `ReportesRepository`.
- `suspend`: puede hacer trabajo asincrono.
- `fun`: es una funcion.
- `guardarReporteLocalPendiente`: nombre de la funcion.
- `reporteDto: ReportesDto`: recibe datos del reporte.

No tiene `: String` ni `: Boolean` al final porque no devuelve un valor importante.

Cuando una funcion no devuelve algo explicito, Kotlin usa el tipo:

```kotlin
Unit
```

`Unit` es parecido a decir "no devuelve nada util".

##### Obtener numero

```kotlin
val numeroReportado = reporteDto.reportedMsisdn.orEmpty()
```

Obtiene el numero del DTO.

Si viene `null`, lo convierte en texto vacio.

##### Guardar numero en tabla de numeros

```kotlin
numeroDao.insertaNumero(NumerosEntity(numero = numeroReportado))
```

Aqui pasan varias cosas en una sola linea:

1. Se crea un objeto `NumerosEntity`.
2. Se le asigna el numero reportado.
3. Se manda ese objeto al DAO.
4. El DAO lo guarda en Room.

Separado se veria asi:

```kotlin
val numeroEntity = NumerosEntity(numero = numeroReportado)
numeroDao.insertaNumero(numeroEntity)
```

##### Crear reporte local

```kotlin
val reporteLocal = ReportesEntity(
    id = numeroReportado,
    fecha = fechaActual(),
    folio = FOLIO_LOCAL,
    etiqueta = reporteDto.label ?: "Reporte local",
    numeroReportado = numeroReportado,
    status = STATUS_PENDIENTE
)
```

Aqui se crea el objeto que representa un registro en la tabla `reportes`.

Campo por campo:

- `id`: usa el numero reportado como identificador local.
- `fecha`: usa la fecha actual.
- `folio`: usa `"LOCAL"` porque todavia no hay folio real del servidor.
- `etiqueta`: usa la etiqueta del DTO o `"Reporte local"` si no hay etiqueta.
- `numeroReportado`: guarda el numero.
- `status`: usa `"PENDIENTE"`.

##### Guardar reporte en Room

```kotlin
reportesDao.agregarReporte(reporteLocal)
```

Aqui se guarda el reporte local en la base de datos.

El DAO es quien contiene la operacion real de insert.

##### Idea central

Esta funcion crea una version local del reporte antes de depender de internet.

Por eso el usuario puede ver su reporte aunque el servidor falle.

Notas personales:

```text

```

### Clase 3 - Jetpack Compose

Temas pendientes:

- `@Composable`.
- `Scaffold`.
- `Text`, `Button`, `Column`, `Row`, `Box`.
- Estado en pantalla.
- Navegacion.

Notas personales:

```text

```

### Clase 4 - ViewModel y estado

Temas pendientes:

- `ViewModel`.
- `StateFlow`.
- `MutableStateFlow`.
- `viewModelScope`.
- Eventos de usuario.

Notas personales:

```text

```

### Clase 5 - Room

Temas pendientes:

- `@Entity`.
- `@Dao`.
- `@Query`.
- `@Insert`.
- `RoomDatabase`.
- Consultas locales.

Notas personales:

```text

```

### Clase 6 - Retrofit y API

Temas pendientes:

- `@GET`.
- `@POST`.
- `@Body`.
- `Response`.
- DTOs.
- Manejo de errores.

Notas personales:

```text

```

### Clase 7 - Hilt e inyeccion de dependencias

Temas pendientes:

- `@Inject`.
- `@Module`.
- `@Provides`.
- `@Singleton`.
- `@HiltViewModel`.
- `@AndroidEntryPoint`.

Notas personales:

```text

```

### Clase 8 - Flujo completo: reportar numero

Temas pendientes:

- Desde la pantalla hasta el servidor.
- Guardado local.
- Sincronizacion pendiente.
- Mensajes al usuario.

Notas personales:

```text

```

## Preguntas pendientes

Usa esta seccion para anotar dudas.

```text
1. 
2. 
3. 
```

## Cambios o mejoras que detectemos

Usa esta seccion para anotar posibles mejoras tecnicas.

```text
1. 
2. 
3. 
```
