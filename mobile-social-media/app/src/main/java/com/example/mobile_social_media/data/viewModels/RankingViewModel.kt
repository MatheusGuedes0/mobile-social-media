package com.example.mobile_social_media.data.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_social_media.data.model.AtividadeFisica
import com.example.mobile_social_media.data.repository.AtividadeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RankingViewModel(
    private val repository: AtividadeRepository = AtividadeRepository()
) : ViewModel() {

    private val _ranking = MutableStateFlow<List<AtividadeFisica>>(emptyList())
    val ranking: StateFlow<List<AtividadeFisica>> = _ranking

    init {
        carregarRanking()
    }

    fun carregarRanking() {
        viewModelScope.launch {
            val lista = repository.listarRanking()
            _ranking.value = lista
        }
    }

}
