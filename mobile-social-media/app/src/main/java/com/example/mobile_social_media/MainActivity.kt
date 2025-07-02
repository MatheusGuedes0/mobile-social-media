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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
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

    private var passos = 0
    private var inicioContagem: Long = 0

    private val REQUEST_ACTIVITY_RECOGNITION = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtPassos = findViewById(R.id.txtPassos)
        txtRitmo = findViewById(R.id.txtRitmo)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        mainViewModel.carregarUsuarioAtual()

        lifecycleScope.launch {
            mainViewModel.usuario.collect { usuario ->
                if (usuario != null && hasActivityRecognitionPermission()) {
                    iniciarServicoAtividade(usuario.uid)
                } else if (usuario != null) {
                    requestActivityRecognitionPermission()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
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

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Não utilizado
    }

    private fun iniciarServicoAtividade(uid: String) {
        val intent = Intent(this, AtividadeService::class.java).apply {
            putExtra(AtividadeService.EXTRA_USER_UID, uid)
        }
        ContextCompat.startForegroundService(this, intent)
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
                mainViewModel.usuario.value?.uid?.let {
                    iniciarServicoAtividade(it)
                }
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
