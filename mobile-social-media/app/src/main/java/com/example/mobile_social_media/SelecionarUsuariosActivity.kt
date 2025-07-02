package com.example.mobile_social_media

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_social_media.adapter.UsuarioSelecaoAdapter
import com.example.mobile_social_media.data.model.Usuario
import com.example.mobile_social_media.data.repository.UsuarioRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SelecionarUsuariosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val usuarioRepository = UsuarioRepository()
    private val usuariosSelecionados = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selecionar_usuarios)

        recyclerView = findViewById(R.id.recyclerUsuarios)
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("usuarios").get().await()
            val usuarios = snapshot.documents.mapNotNull { it.toObject(Usuario::class.java) }

            val adapter = UsuarioSelecaoAdapter(usuarios, usuariosSelecionados)
            recyclerView.adapter = adapter
        }
    }

    override fun onBackPressed() {
        val resultIntent = Intent().apply {
            putStringArrayListExtra("usuariosSelecionados", ArrayList(usuariosSelecionados))
        }
        setResult(RESULT_OK, resultIntent)
        super.onBackPressed()
    }
}
