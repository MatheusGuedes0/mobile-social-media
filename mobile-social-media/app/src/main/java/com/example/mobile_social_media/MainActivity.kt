package com.example.mobile_social_media

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.mobile_social_media.data.model.AtividadeFisica
import com.example.mobile_social_media.data.viewModels.AtividadeViewModel
import com.example.mobile_social_media.data.viewModels.MainViewModel
import com.example.mobile_social_media.service.AtividadeService
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), SensorEventListener {

    private val atividadeViewModel: AtividadeViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    private lateinit var txtPassos: TextView
    private lateinit var txtRitmo: TextView
    private lateinit var btnStartStop: Button
    private lateinit var btnRanking: ImageButton
    private lateinit var btnGrupos: ImageButton

    private var passos = 0
    private var inicioContagem: Long = 0
    private var isServiceRunning = false
    private var usuarioUid: String? = null

    private val REQUEST_ACTIVITY_RECOGNITION = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtPassos = findViewById(R.id.txtPassos)
        txtRitmo = findViewById(R.id.txtRitmo)
        btnStartStop = findViewById(R.id.btnStartStop)
        btnRanking = findViewById(R.id.btnRanking)
        btnGrupos = findViewById(R.id.btnGrupos)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        mainViewModel.carregarUsuarioAtual()

        lifecycleScope.launch {
            mainViewModel.usuario.collect { usuario ->
                if (usuario != null) {
                    usuarioUid = usuario.uid
                    if (!hasActivityRecognitionPermission()) {
                        requestActivityRecognitionPermission()
                    }
                }
            }
        }

        btnStartStop.setOnClickListener {
            if (isServiceRunning) {
                // Parar atividade
                pararServicoAtividade()
                sensorManager.unregisterListener(this)
                enviarDadosParaFirebase()
            } else {
                // Iniciar atividade: resetar contadores
                passos = 0
                inicioContagem = System.currentTimeMillis()
                txtPassos.text = "Passos: 0"
                txtRitmo.text = "Ritmo: 0.00 passos/min"

                usuarioUid?.let { iniciarServicoAtividade(it) }
                stepSensor?.let {
                    sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
                }
            }

            isServiceRunning = !isServiceRunning
            updateStartStopButton()
        }

        /*btnRanking.setOnClickListener {
            startActivity(Intent(this, RankingActivity::class.java))
        }

        btnGrupos.setOnClickListener {
            startActivity(Intent(this, GruposActivity::class.java))
        }*/

        updateStartStopButton()
    }

    override fun onResume() {
        super.onResume()
        if (isServiceRunning) {
            stepSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
            if (inicioContagem == 0L) {
                inicioContagem = System.currentTimeMillis()
            }

            passos++

            val tempoMinutos = (System.currentTimeMillis() - inicioContagem) / 60000.0
            val ritmo = if (tempoMinutos > 0) passos / tempoMinutos else 0.0

            txtPassos.text = "Passos: $passos"
            txtRitmo.text = "Ritmo: %.2f passos/min".format(ritmo)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun iniciarServicoAtividade(uid: String) {
        val intent = Intent(this, AtividadeService::class.java).apply {
            putExtra(AtividadeService.EXTRA_USER_UID, uid)
        }
        ContextCompat.startForegroundService(this, intent)
    }

    private fun pararServicoAtividade() {
        val intent = Intent(this, AtividadeService::class.java)
        stopService(intent)
    }

    private fun enviarDadosParaFirebase() {
        val tempoMinutos = (System.currentTimeMillis() - inicioContagem) / 60000.0

        // Calcula o nível com base na quantidade de passos e tempo
        val nivel = calcularNivel(passos, tempoMinutos)

        val atividade = AtividadeFisica(
            usuarioUid = usuarioUid ?: "",
            nivel = nivel,
            passos = passos,
            timestamp = System.currentTimeMillis()
        )

        lifecycleScope.launch {
            try {
                atividadeViewModel.registrarAtividade(atividade)
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Erro ao salvar dados", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun calcularNivel(passos: Int, duracaoMinutos: Double): Int {
        if (duracaoMinutos <= 0) return 0
        val ritmo = passos / duracaoMinutos
        return when {
            ritmo < 20 -> 0  // parado
            ritmo < 60 -> 1  // leve
            ritmo < 100 -> 2 // moderado
            else -> 3        // intenso
        }
    }

    private fun updateStartStopButton() {
        btnStartStop.text = if (isServiceRunning) "Parar Atividade" else "Iniciar Atividade"
    }

    private fun hasActivityRecognitionPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                REQUEST_ACTIVITY_RECOGNITION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_ACTIVITY_RECOGNITION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                usuarioUid?.let { iniciarServicoAtividade(it) }
            } else {
                Toast.makeText(
                    this,
                    "Permissão negada. Não será possível monitorar a atividade.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
