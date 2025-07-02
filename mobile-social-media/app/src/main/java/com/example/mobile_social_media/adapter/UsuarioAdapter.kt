package com.example.mobile_social_media.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_social_media.R
import com.example.mobile_social_media.data.model.Usuario

class UsuarioAdapter(
    private val usuarios: List<Usuario>,
    private val selecionados: MutableSet<String>? = null,
    private val modoSomenteLeitura: Boolean = false
) : RecyclerView.Adapter<UsuarioAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkbox: CheckBox = view.findViewById(R.id.checkboxUsuario)
        val txtNome: TextView = view.findViewById(R.id.txtNomeUsuario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario_selecao, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val usuario = usuarios[position]
        holder.txtNome.text = usuario.nome

        if (modoSomenteLeitura) {
            holder.checkbox.visibility = View.GONE
        } else {
            holder.checkbox.isChecked = selecionados?.contains(usuario.uid) == true

            holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selecionados?.add(usuario.uid)
                } else {
                    selecionados?.remove(usuario.uid)
                }
            }
        }
    }

    override fun getItemCount(): Int = usuarios.size
}
