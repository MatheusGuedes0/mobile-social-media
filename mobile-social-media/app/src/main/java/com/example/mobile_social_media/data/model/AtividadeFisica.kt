package com.example.mobile_social_media.data.model

data class AtividadeFisica(
    val usuarioUid: String = "",
    val nomeUsuario: String = "",
    val nivel: Int = 0,
    val passos: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)