package com.intermedio.nomasxt.interceptor

import android.Manifest
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.CallLog
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.util.Log
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.intermedio.nomasxt.R
import com.intermedio.nomasxt.dominio.VerificarNumeroUseCase
import com.intermedio.nomasxt.presentacion.alertas.AlertaEnPantallaActivity
import com.intermedio.nomasxt.utilerias.TelefonoNormalizer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private const val TAG = "LlamadaEntranteInterceptor"

@AndroidEntryPoint
class LlamadaEntranteInterceptor : Service() {

    // TelephonyManager permite escuchar cambios en el estado de las llamadas.
    private lateinit var telephonyManager: TelephonyManager

    // Listener usado en versiones anteriores a Android 12.
    private var legacyListener: PhoneStateListener? = null

    // Callback usado en Android 12/API 31 o superior.
    private var modernCallback: TelephonyCallback? = null

    // Caso de uso real: consulta la tabla local de numeros reportados.
    @Inject
    lateinit var verificarNumeroUseCase: VerificarNumeroUseCase

    // Scope inyectado para ejecutar trabajo de base de datos fuera del hilo principal.
    @Inject
    lateinit var serviceScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        Log.d("nomasxt", "LlamadaEntranteInterceptor | Iniciando servicio de llamada entrante")
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        Log.d("nomasxt", "LlamadaEntranteInterceptor | Antes de iniciar el servicio en primer plano")

        // Android exige que este servicio quede en primer plano para poder seguir activo.
        iniciarServicioEnPrimerPlano()

        // Android 12+ usa TelephonyCallback para escuchar el estado de las llamadas.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.d("nomasxt","LlamadaEntranteInterceptor | Entramos a on Create con versión Code S o superior.")
            //return

            modernCallback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                override fun onCallStateChanged(state: Int) {
                    Log.d("nomasxt", "LlamadaEntranteInterceptor | Entramos a onCallStateChanged, state: $state")

                    // CALL_STATE_RINGING significa que hay una llamada entrante sonando.
                    if (state == TelephonyManager.CALL_STATE_RINGING) {
                        val logEntry = "${getTimestamp()} - Llamada entrante (API 31+)\n"
                        Log.d("CallMonitor", logEntry)
                        writeToLogFile(logEntry)
                        Log.d("CallMonitor", "Pasamos writeToLogFile")

                        Handler(Looper.getMainLooper()).postDelayed({
                            Log.d(
                                "CallMonitor",
                                "Entramos al Handler, antes de getLastIncomingNumber"
                            )
                            val recentNumber = getLastIncomingNumber()
                            Log.d("CallMonitor", "Pasamos getLastIncomingNumber")

                            // Flujo real: consulta Room usando el ultimo numero recuperado del historial.
                            if (recentNumber != null) {
                                serviceScope.launch(Dispatchers.IO) {
                                    val numeroABuscar = normalizarNumeroParaBusqueda(recentNumber)
                                    val existeEnBD = verificarNumeroUseCase(numeroABuscar)

                                    if (existeEnBD) {
                                        Handler(Looper.getMainLooper()).post {
                                            mostrarAlerta(recentNumber)
                                        }
                                    }
                                }
                            }
                        }, 3000) //Dar tiempo a que el CallLog se actualice
                    }
                }
            }
            telephonyManager.registerTelephonyCallback(mainExecutor, modernCallback!!)

            Log.d("nomasxt", "LlamadaEntranteInterceptor | Registramos el callback")

        } else {
            // Versiones anteriores a Android 12: incomingNumber llega directamente aqui.
            legacyListener = object : PhoneStateListener() {
                override fun onCallStateChanged(state: Int, incomingNumber: String?) {
                    if (state == TelephonyManager.CALL_STATE_RINGING) {
                        val logEntry = "${getTimestamp()} - Llamada entrante de: $incomingNumber\n"
                        Log.d("CallMonitor", logEntry)
                        writeToLogFile(logEntry)
                        Log.d("CallMonitor", "-----------------")
                        Log.d("CallMonitor", "antes de entrar")
                        Log.d("CallMonitor", "-----------------")
                        Log.d("CallMonitor", "Número entrante: $incomingNumber")

                        if(incomingNumber.isNullOrEmpty()) {
                            Log.d("NoMasXT", "$TAG | Número entrante inválido (nulo o vacío)")
                            return
                        }

                        serviceScope.launch(Dispatchers.IO) {
                            // Flujo actual de la app: asume Mexico y busca +52 + numero nacional.
                            val numeroConPrefijo = normalizarNumeroParaBusqueda(incomingNumber)

                            // Flujo real: consulta la tabla local de numeros reportados.
                            val existeEnBD = verificarNumeroUseCase(numeroConPrefijo)  //incomingNumber)

                            // Si coincide con Room, se muestra la alerta.
                            if(existeEnBD) {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    mostrarAlerta(incomingNumber)


                                }, 800)
                            }
                        }

                       /* if (incomingNumber != null && watchlistNumbers.contains(incomingNumber.toString())) {
                            Handler(Looper.getMainLooper()).postDelayed({
                                val alertIntent = Intent(this@LlamadaEntranteInterceptor,
                                    AlertaEnPantallaActivity::class.java).apply {
                                    putExtra("number", incomingNumber)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                }
                                startActivity(alertIntent)


                            }, 800)
                        }*/
                    }
                }
            }

            // Activa el listener antiguo para escuchar cambios de estado de llamada.
            telephonyManager.listen(legacyListener, PhoneStateListener.LISTEN_CALL_STATE)
        }

    }

    // Guarda logs simples en almacenamiento externo privado de la app para depurar llamadas.
    private fun writeToLogFile(data: String) {
        val dir = File(getExternalFilesDir(null), "logs")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "call_log.txt")
        try {
            FileWriter(file, true).use { it.write(data) }
        } catch (e: IOException) {
            Log.e("CallMonitor", "Error escribiendo el log", e)
        }
    }

    // Devuelve fecha y hora para los mensajes de log.
    private fun getTimestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //val notificacion = crearNotificacion(this)
        //startForeground(1, notificacion)

        Log.d("nomasxt", "LlamadaEntranteInterceptor | Entramos a onStartCommand")
        // Aquí iría tu lógica para interceptar llamadas dependiendo de la versión:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Lógica con TelecomManager para Android 12+
        } else {
            // Lógica con TelephonyManager para Android < 12
        }

        return START_STICKY
    }

    // Este servicio no se conecta con bindService; solo se arranca y queda escuchando.
    override fun onBind(intent: Intent?): IBinder? = null

    // Crea la notificacion permanente que Android requiere para un foreground service.
    private fun iniciarServicioEnPrimerPlano() {
        val canalId = "canal_llamadas"
        val canalNombre = "Monitoreo de llamadas"
        val ID_NOTIFICACION = 1

        Log.d("nomasxt", "LlamadaEntranteInterceptor | Entramos a iniciar servicio en primer plano")

        // Crear canal de notificaciones para Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                canalId,
                canalNombre,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }

        // Notificación
        val notification = NotificationCompat.Builder(this, canalId)
            .setContentTitle("Monitoreo activo")
            .setContentText("Se están supervisando las llamadas entrantes")
            .setSmallIcon(R.drawable.ic_call)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // Elevar a foreground dependiendo de la versión
        /*
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL)
            } else {
                //Versiones anteriores a Android 14
                startForeground(1, notification)
            }
        } else {
            startForeground(1, notification)
        }
        */
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(ID_NOTIFICACION, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL)
        } else {
            startForeground(ID_NOTIFICACION, notification)
        }
    }

    // Funcion actualmente sin uso real; quedo como posible punto para mover logica de monitoreo.
    private fun iniciarMonitoreoLlamadas() {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        // Aquí debes aplicar la lógica de detección dependiendo de la versión de Android
        // Por ejemplo, usando PhoneStateListener para versiones < 12, o CallScreeningService si aplica
        Log.d("nomasxt", "LlamadaEntranteInterceptor | Entramos a iniciar monitoreo de llamadas")
    }

    override fun onDestroy() {
        super.onDestroy()
        // Aquí puedes liberar recursos si es necesario
    }

    // Android 12+ no entrega el numero directamente, por eso se intenta leer el ultimo registro del CallLog.
    private fun getLastIncomingNumber(): String? {
        Log.d("CallMonitor", "Entramos a getLastIncomingNumber, antes de contentResolver")
        //Verificación de que exista el permiso para acceder al registro de llamadas
        if  (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            Log.d("CallMonitor", "Permiso READ_CALL_LOG no concedido")
            return null
        } else {
            Log.d("CallMonitor", "Permiso READ_CALL_LOG concedido")
        }
        val cursor = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE),
            null,//"${CallLog.Calls.TYPE} = ${CallLog.Calls.INCOMING_TYPE}",
            null,
            "${CallLog.Calls.DATE} DESC"
        )
        Log.d("CallMonitor", "Pasamos contentResolver")

        cursor?.use {
            Log.d("CallMonitor", "Entramos al cursor, antes de la comparación.")
            Log.d("CallMonitor", "Cursor tamaño: ${it.count}")
            /*
            if (it.moveToFirst()) {
                Log.d("CallMonitor", "Pasamos la comparación.")
                val number = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                Log.d("CallMonitor", "Pasamos el getString.")
                return number.replace(" ", "").replace("+52","")
            }*/
            // Si no hay registros, no hay numero que comparar.
            if (!it.moveToFirst()) return null

            do {
                val number = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                val type = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.TYPE))
                val fecha = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.DATE))

                Log.d("CallMonitor", "Número: $number, Tipo: $type, Fecha: $fecha")

                // Regresa el registro mas reciente normalizado de forma basica.
                return number.replace(" ", "").replace("-", "")

            } while (it.moveToNext())
        }
        return null
    }

    // Normalizacion internacional: respeta numeros con + y usa Mexico como fallback si Android manda nacional.
    private fun normalizarNumeroParaBusqueda(numero: String): String {
        return TelefonoNormalizer.normalizar(numero)
    }

    // Abre la pantalla roja de alerta cuando el numero existe en la tabla local.
    private fun mostrarAlerta(numero: String) {
        val alertIntent = Intent(this@LlamadaEntranteInterceptor,
            AlertaEnPantallaActivity::class.java).apply {
            putExtra("number", numero)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(alertIntent)
    }
}
