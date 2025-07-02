package com.example.mobile_social_media.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.mobile_social_media.R
import com.example.mobile_social_media.data.model.AtividadeFisica
import com.example.mobile_social_media.data.repository.AtividadeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AtividadeService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private val atividadeRepository = AtividadeRepository()
    private val scope = CoroutineScope(Dispatchers.IO)

    private var usuarioUid: String = ""

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "atividade_service_channel"
        const val NOTIFICATION_ID = 1
        const val EXTRA_USER_UID = "extra_user_uid"
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        usuarioUid = intent?.getStringExtra(EXTRA_USER_UID) ?: ""

        // ✅ Android 10+ exige startForeground logo no início com tipo definido no Manifest
        startForeground(NOTIFICATION_ID, createNotification())

        // ✅ Registra sensor se disponível
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        } ?: stopSelf()

        return START_STICKY
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Atividade Física Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Monitorando atividade física")
            .setContentText("Serviço em execução")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    // Lógica de envio a cada 5 minutos
    private var lastTimestamp = 0L

    override fun onSensorChanged(event: SensorEvent?) {
        event?.takeIf { it.sensor.type == Sensor.TYPE_ACCELEROMETER }?.let {
            val x = it.values[0]
            val y = it.values[1]
            val z = it.values[2]
            val magnitude = kotlin.math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()

            val nivel = calcularNivelAtividade(magnitude)
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastTimestamp > 300_000) {
                lastTimestamp = currentTime

                val atividade = AtividadeFisica(
                    usuarioUid = usuarioUid,
                    nivel = nivel,
                    timestamp = currentTime
                )

                scope.launch {
                    try {
                        atividadeRepository.registrarAtividade(atividade)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun calcularNivelAtividade(magnitude: Float): Int {
        return when {
            magnitude < 10.5 -> 0 // parado
            magnitude < 12.5 -> 1 // leve
            magnitude < 15.0 -> 2 // moderado
            else -> 3 // intenso
        }
    }
}
