package com.example.mobile_social_media.data.model

data class Grupo(
    val id: String = "",
    val nome: String = "",
    val membros: List<String> = listOf(),
    val pontuacaoTotal: Int = 0
)