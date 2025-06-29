package com.example.mobile_social_media.data.viewModels

import androidx.lifecycle.ViewModel
import com.example.mobile_social_media.data.model.AtividadeFisica
import com.example.mobile_social_media.data.repository.AtividadeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AtividadeViewModel(
    private val atividadeRepository: AtividadeRepository = AtividadeRepository()
) : ViewModel() {

    private val _atividades = MutableStateFlow<List<AtividadeFisica>>(emptyList())
    val atividades: StateFlow<List<AtividadeFisica>> = _atividades

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
}