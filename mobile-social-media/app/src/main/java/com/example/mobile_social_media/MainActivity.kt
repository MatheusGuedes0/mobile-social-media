package com.example.mobile_social_media

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import com.example.mobile_social_media.data.viewModels.AtividadeViewModel
import com.example.mobile_social_media.data.viewModels.MainViewModel
import com.example.mobile_social_media.service.AtividadeService
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private val atividadeViewModel: AtividadeViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var txtPassos: TextView
    private lateinit var txtRitmo: TextView
    private lateinit var btnStartStop: Button
    private lateinit var btnRanking: ImageButton
    private lateinit var btnGrupos: ImageButton

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

        AtividadeViewModel.instancia = atividadeViewModel

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

        lifecycleScope.launchWhenStarted {
            atividadeViewModel.passos.collect { passos ->
                txtPassos.text = "Passos: $passos"
            }
        }

        lifecycleScope.launchWhenStarted {
            atividadeViewModel.ritmo.collect { ritmo ->
                txtRitmo.text = "Ritmo: %.2f passos/min".format(ritmo)
            }
        }

        btnStartStop.setOnClickListener {
            if (isServiceRunning) {
                pararServicoAtividade()
                atividadeViewModel.resetarPassosERitmo()  // Aqui zera os valores
            } else {
                usuarioUid?.let { iniciarServicoAtividade(it) }
            }
            isServiceRunning = !isServiceRunning
            updateStartStopButton()
        }


        updateStartStopButton()
    }

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

    private fun updateStartStopButton() {
        btnStartStop.text = if (isServiceRunning) "Parar Atividade" else "Iniciar Atividade"
    }

    private fun hasActivityRecognitionPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else true
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



}
