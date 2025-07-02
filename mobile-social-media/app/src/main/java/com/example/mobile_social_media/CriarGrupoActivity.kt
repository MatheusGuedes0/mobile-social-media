package com.example.mobile_social_media

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile_social_media.data.model.Grupo
import com.example.mobile_social_media.data.viewModels.GrupoViewModel
import java.util.UUID

class CriarGrupoActivity : AppCompatActivity() {

    private lateinit var etNomeGrupo: EditText
    private lateinit var btnCriarGrupo: Button
    private lateinit var btnSelecionarUsuarios: Button

    private val grupoViewModel: GrupoViewModel by viewModels()
    private val usuariosSelecionados = mutableListOf<String>() // Lista de UIDs
    private val REQUEST_SELECIONAR_USUARIOS = 1001


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_criar_grupo)

        etNomeGrupo = findViewById(R.id.etNomeGrupo)
        btnCriarGrupo = findViewById(R.id.btnCriarGrupo)
        btnSelecionarUsuarios = findViewById(R.id.btnSelecionarUsuarios)

        btnSelecionarUsuarios.setOnClickListener {
            val intent = Intent(this, SelecionarUsuariosActivity::class.java)
            startActivityForResult(intent, REQUEST_SELECIONAR_USUARIOS)
        }

        btnCriarGrupo.setOnClickListener {
            val nome = etNomeGrupo.text.toString()
            val grupo = Grupo(
                id = UUID.randomUUID().toString(),
                nome = nome,
                membros = usuariosSelecionados,
                pontuacaoTotal = 0
            )
            grupoViewModel.criarGrupo(grupo)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECIONAR_USUARIOS && resultCode == RESULT_OK) {
            val selecionados = data?.getStringArrayListExtra("usuariosSelecionados") ?: return
            usuariosSelecionados.clear()
            usuariosSelecionados.addAll(selecionados)
        }
    }

}
