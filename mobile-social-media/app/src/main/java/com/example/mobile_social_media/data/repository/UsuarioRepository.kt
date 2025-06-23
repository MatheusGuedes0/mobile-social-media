package com.example.mobile_social_media.data.repository

import com.example.mobile_social_media.data.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UsuarioRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usuariosCollection = firestore.collection("usuarios")

    suspend fun salvarUsuario(usuario: Usuario) {
        usuariosCollection.document(usuario.uid).set(usuario).await()
    }

    suspend fun buscarUsuario(uid: String): Usuario? {
        val doc = usuariosCollection.document(uid).get().await()
        return doc.toObject(Usuario::class.java)
    }

    fun getUsuarioAtual(): Usuario? {
        val user = auth.currentUser ?: return null
        return Usuario(
            uid = user.uid,
            nome = user.displayName ?: "",
            email = user.email ?: ""
        )
    }
}
