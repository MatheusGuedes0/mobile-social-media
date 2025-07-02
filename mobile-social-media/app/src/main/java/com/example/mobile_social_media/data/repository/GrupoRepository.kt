package com.example.mobile_social_media.data.repository

import com.example.mobile_social_media.data.model.Grupo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class GrupoRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val gruposCollection = firestore.collection("grupos")

    suspend fun criarGrupo(grupo: Grupo) {
        gruposCollection.document(grupo.id).set(grupo).await()
    }

    suspend fun buscarGrupo(id: String): Grupo? {
        val doc = gruposCollection.document(id).get().await()
        return doc.toObject(Grupo::class.java)
    }

    suspend fun listarGrupos(): List<Grupo> {
        val snapshot = gruposCollection.get().await()
        return snapshot.documents.mapNotNull { it.toObject(Grupo::class.java) }
    }

    suspend fun atualizarPontuacao(grupoId: String, novaPontuacao: Int) {
        gruposCollection.document(grupoId)
            .update("pontuacaoTotal", novaPontuacao)
            .await()
    }

    suspend fun obterRankingGrupos(): List<Grupo> {
        return listarGrupos().sortedByDescending { it.pontuacaoTotal }
    }

}