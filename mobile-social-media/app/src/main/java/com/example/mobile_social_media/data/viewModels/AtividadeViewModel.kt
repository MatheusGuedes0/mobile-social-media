package com.example.mobile_social_media.data.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_social_media.data.model.AtividadeFisica
import com.example.mobile_social_media.data.repository.AtividadeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import kotlin.math.sqrt

class AtividadeViewModel(
    private val atividadeRepository: AtividadeRepository = AtividadeRepository()
) : ViewModel(), SensorEventListener {

    companion object {
        var instancia: AtividadeViewModel? = null
    }

    private val _atividades = MutableStateFlow<List<AtividadeFisica>>(emptyList())
    val atividades: StateFlow<List<AtividadeFisica>> = _atividades

    private val _passos = MutableStateFlow(0)
    val passos: StateFlow<Int> = _passos

    private val _ritmo = MutableStateFlow(0.0)
    val ritmo: StateFlow<Double> = _ritmo

    private var ultimoPasso: Long = 0

    fun registrarAtividade(atividade: AtividadeFisica) {
        viewModelScope.launch {
            atividadeRepository.registrarAtividade(atividade)
        }
    }

    fun carregarAtividades(uid: String) {
        viewModelScope.launch {
            val lista = atividadeRepository.listarAtividadesPorUsuario(uid)
            _atividades.value = lista
        }
    }

    fun resetarPassosERitmo() {
        _passos.value = 0
        _ritmo.value = 0.0
        ultimoPasso = 0L
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        when (event.sensor.type) {
            Sensor.TYPE_STEP_DETECTOR -> contarPasso()
            Sensor.TYPE_ACCELEROMETER -> processarAcelerometro(event.values)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun contarPasso() {
        val now = System.currentTimeMillis()
        _passos.value = _passos.value + 1

        if (ultimoPasso != 0L) {
            val intervalo = now - ultimoPasso
            if (intervalo > 0) {
                val cadencia = 60000.0 / intervalo // passos por minuto
                _ritmo.value = cadencia
            }
        }

        ultimoPasso = now
    }

    private fun processarAcelerometro(values: FloatArray) {
        val (x, y, z) = values
        val magnitude = sqrt(x * x + y * y + z * z)
        val now = System.currentTimeMillis()

        if (magnitude > 12 && now - ultimoPasso > 300) { // threshold e debounce
            contarPasso()
        }
    }

    fun atualizarPassosERitmo(passos: Int, ritmo: Double) {
        _passos.value = passos
        _ritmo.value = ritmo
    }
}
