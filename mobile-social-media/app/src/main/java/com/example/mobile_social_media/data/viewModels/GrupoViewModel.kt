package com.example.mobile_social_media.data.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_social_media.data.model.Grupo
import com.example.mobile_social_media.data.repository.GrupoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class GrupoViewModel(
    private val grupoRepository: GrupoRepository = GrupoRepository()
) : ViewModel() {

    private val _grupos = MutableStateFlow<List<Grupo>>(emptyList())
    val grupos: StateFlow<List<Grupo>> = _grupos

    private val _rankingGrupos = MutableStateFlow<List<Grupo>>(emptyList())
    val rankingGrupos: StateFlow<List<Grupo>> = _rankingGrupos

    fun carregarRankingGrupos() {
        viewModelScope.launch {
            _rankingGrupos.value = grupoRepository.obterRankingGrupos()
        }
    }

    fun criarGrupo(grupo: Grupo) {
        viewModelScope.launch {
            grupoRepository.criarGrupo(grupo)
        }
    }

    fun carregarGrupos() {
        viewModelScope.launch {
            val lista = grupoRepository.listarGrupos()
            _grupos.value = lista
        }
    }

    fun atualizarPontuacao(grupoId: String, novaPontuacao: Int) {
        viewModelScope.launch {
            grupoRepository.atualizarPontuacao(grupoId, novaPontuacao)
        }
    }
}
