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
private const val PREFIJO = "+52"

@AndroidEntryPoint
class LlamadaEntranteInterceptor : Service() {

    private lateinit var telephonyManager: TelephonyManager
    private var legacyListener: PhoneStateListener? = null
    private var modernCallback: TelephonyCallback? = null
    private val watchlistNumbers = listOf("5538017939", "5554326063", "5585714191")

    @Inject
    lateinit var verificarNumeroUseCase: VerificarNumeroUseCase

    @Inject
    lateinit var serviceScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        Log.d("nomasxt", "LlamadaEntranteInterceptor | Iniciando servicio de llamada entrante")
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        Log.d("nomasxt", "LlamadaEntranteInterceptor | Antes de iniciar el servicio en primer plano")
        iniciarServicioEnPrimerPlano()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.d("nomasxt","LlamadaEntranteInterceptor | Entramos a on Create con versión Code S o superior.")
            //return

            modernCallback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                override fun onCallStateChanged(state: Int) {
                    Log.d("nomasxt", "LlamadaEntranteInterceptor | Entramos a onCallStateChanged, state: $state")
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
                            if (recentNumber != null && watchlistNumbers.contains(recentNumber)) {
                                val alertIntent = Intent(this@LlamadaEntranteInterceptor,
                                    AlertaEnPantallaActivity::class.java).apply {
                                    putExtra("number", recentNumber)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                }
                                startActivity(alertIntent)
                                /*val alertIntent = Intent(
                                    this@IncomingCallInterceptor,
                                    FullscreenAlertActivity::class.java
                                ).apply {
                                    putExtra("number", recentNumber)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                }
                                startActivity(alertIntent)*/
                            }
                        }, 3000) //Dar tiempo a que el CallLog se actualice
                    }
                }
            }
            telephonyManager.registerTelephonyCallback(mainExecutor, modernCallback!!)

            Log.d("nomasxt", "LlamadaEntranteInterceptor | Registramos el callback")

        } else {
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
                            val numeroConPrefijo = "$PREFIJO$incomingNumber"
                            val existeEnBD = verificarNumeroUseCase(numeroConPrefijo)  //incomingNumber)

                            if(existeEnBD) {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    val alertIntent = Intent(this@LlamadaEntranteInterceptor,
                                        AlertaEnPantallaActivity::class.java).apply {
                                        putExtra("number", incomingNumber)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    }
                                    startActivity(alertIntent)


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
            telephonyManager.listen(legacyListener, PhoneStateListener.LISTEN_CALL_STATE)
        }

    }

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

    override fun onBind(intent: Intent?): IBinder? = null

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
            it.moveToFirst()

            while (it.moveToNext()){
                val number = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                val type = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.TYPE))
                val fecha = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.DATE))

                Log.d("CallMonitor", "Número: $number, Tipo: $type, Fecha: $fecha")

            }
        }
        return null
    }
}

