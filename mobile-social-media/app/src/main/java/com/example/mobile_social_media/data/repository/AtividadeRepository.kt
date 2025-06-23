package com.example.mobile_social_media.data.repository

import com.example.mobile_social_media.data.model.AtividadeFisica
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AtividadeRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val atividadesCollection = firestore.collection("atividades")

    suspend fun registrarAtividade(atividade: AtividadeFisica) {
        atividadesCollection.add(atividade).await()
    }

    suspend fun listarAtividadesPorUsuario(uid: String): List<AtividadeFisica> {
        val snapshot = atividadesCollection
            .whereEqualTo("usuarioUid", uid)
            .get()
            .await()

        return snapshot.documents.mapNotNull { it.toObject(AtividadeFisica::class.java) }
    }
}