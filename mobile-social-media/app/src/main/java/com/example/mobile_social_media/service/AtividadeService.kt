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
    private var passos: Int = 0

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "atividade_service_channel"
        const val NOTIFICATION_ID = 1

        const val EXTRA_USER_UID = "extra_user_uid"
        const val EXTRA_PASSOS = "extra_passos"
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        usuarioUid = intent?.getStringExtra(EXTRA_USER_UID) ?: usuarioUid
        passos = intent?.getIntExtra(EXTRA_PASSOS, passos) ?: passos

        startForeground(NOTIFICATION_ID, createNotification())

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

    private var lastTimestamp = 0L

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]

                val magnitude = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                val nivel = calcularNivelAtividade(magnitude)

                val currentTime = System.currentTimeMillis()
                if (currentTime - lastTimestamp > 300000) { // 5 minutos
                    lastTimestamp = currentTime

                    val atividade = AtividadeFisica(
                        usuarioUid = usuarioUid,
                        nivel = nivel,
                        passos = passos,
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
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun calcularNivelAtividade(magnitude: Float): Int {
        return when {
            magnitude < 10.5 -> 0   // parado
            magnitude < 12.5 -> 1   // leve
            magnitude < 15.0 -> 2   // moderado
            else -> 3               // intenso
        }
    }
}
