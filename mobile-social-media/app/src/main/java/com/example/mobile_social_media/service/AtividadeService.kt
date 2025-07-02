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
import com.example.mobile_social_media.data.repository.AtividadeRepository
import com.example.mobile_social_media.data.viewModels.AtividadeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AtividadeService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    private var passos = 0
    private var inicioContagem: Long = 0
    private var usuarioUid: String = ""

    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "atividade_service_channel"
        const val NOTIFICATION_ID = 1
        const val EXTRA_USER_UID = "extra_user_uid"
        const val BROADCAST_PASSOS = "com.example.mobile_social_media.PASSOS_ATUALIZADOS"
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        usuarioUid = intent?.getStringExtra(EXTRA_USER_UID) ?: ""

        passos = 0
        inicioContagem = System.currentTimeMillis()

        startForeground(NOTIFICATION_ID, createNotification())

        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        } ?: stopSelf()

        return START_STICKY
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        enviarDadosParaFirebase()
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
            .setContentTitle("Monitorando passos")
            .setContentText("Contando seus passos...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
            passos++

            val tempoMinutos = (System.currentTimeMillis() - inicioContagem) / 60000.0
            val ritmo = if (tempoMinutos > 0) passos / tempoMinutos else 0.0

            AtividadeViewModel.instancia?.atualizarPassosERitmo(passos, ritmo)


        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun enviarDadosParaFirebase() {
        val tempoMinutos = (System.currentTimeMillis() - inicioContagem) / 60000.0
        val nivel = calcularNivel(passos, tempoMinutos)

        val atividade = com.example.mobile_social_media.data.model.AtividadeFisica(
            usuarioUid = usuarioUid,
            nivel = nivel,
            passos = passos,
            timestamp = System.currentTimeMillis()
        )

        scope.launch {
            try {
                AtividadeRepository().registrarAtividade(atividade)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun calcularNivel(passos: Int, duracaoMinutos: Double): Int {
        if (duracaoMinutos <= 0) return 0
        val ritmo = passos / duracaoMinutos
        return when {
            ritmo < 20 -> 0
            ritmo < 60 -> 1
            ritmo < 100 -> 2
            else -> 3
        }
    }


}
