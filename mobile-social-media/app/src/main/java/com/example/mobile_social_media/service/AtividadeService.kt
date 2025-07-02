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
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mobile_social_media.R
import com.example.mobile_social_media.data.repository.AtividadeRepository
import com.example.mobile_social_media.data.viewModels.AtividadeViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AtividadeService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    private var passos = 0
    private var inicioContagem: Long = 0
    private var usuarioUid: String = ""
    private var nomeUsuario: String = ""

    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "atividade_service_channel"
        const val NOTIFICATION_ID = 1
        const val EXTRA_USER_UID = "extra_user_uid"
        const val BROADCAST_PASSOS = "com.example.mobile_social_media.PASSOS_ATUALIZADOS"
        private const val TAG = "AtividadeService"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service criado")
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        if (stepSensor == null) {
            Log.e(TAG, "Sensor de passos não disponível neste dispositivo")
        } else {
            Log.d(TAG, "Sensor de passos detectado com sucesso")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        usuarioUid = intent?.getStringExtra(EXTRA_USER_UID) ?: ""

        //Log.d(TAG, "Serviço iniciado para o usuário: $usuarioUid ($nomeUsuario)")

        passos = 0
        inicioContagem = System.currentTimeMillis()

        startForeground(NOTIFICATION_ID, createNotification())

        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "Listener do sensor registrado")
        } ?: run {
            Log.e(TAG, "Sensor não encontrado, encerrando serviço")
            stopSelf()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "Serviço destruído")
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
            Log.d(TAG, "Passo detectado! Total de passos: $passos")

            val tempoMinutos = (System.currentTimeMillis() - inicioContagem) / 60000.0
            val ritmo = if (tempoMinutos > 0) passos / tempoMinutos else 0.0

            Log.d(TAG, "Ritmo atual: %.2f passos/min".format(ritmo))

            AtividadeViewModel.instancia?.atualizarPassosERitmo(passos, ritmo)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun enviarDadosParaFirebase() {
        val tempoMinutos = (System.currentTimeMillis() - inicioContagem) / 60000.0
        val nivel = calcularNivel(passos, tempoMinutos)

        Log.d(TAG, "Enviando dados para o Firebase: passos=$passos, nivel=$nivel")

        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val usuariosRef = firestore.collection("usuarios")

        scope.launch {
            try {
                // Buscar nome do usuário no Firestore usando o UID
                val doc = usuariosRef.document(usuarioUid).get().await()
                val nome = doc.getString("nome") ?: "Desconhecido"

                val atividade = com.example.mobile_social_media.data.model.AtividadeFisica(
                    usuarioUid = usuarioUid,
                    nomeUsuario = nome,
                    nivel = nivel,
                    passos = passos,
                    timestamp = System.currentTimeMillis()
                )

                AtividadeRepository().registrarAtividade(atividade)
                Log.d(TAG, "Atividade registrada com nome=$nome")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao registrar atividade com nome de usuário", e)
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
