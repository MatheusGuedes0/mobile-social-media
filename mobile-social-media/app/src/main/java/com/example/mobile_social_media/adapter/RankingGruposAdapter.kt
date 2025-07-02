package com.example.mobile_social_media.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_social_media.R
import com.example.mobile_social_media.data.model.Grupo

class RankingGruposAdapter : RecyclerView.Adapter<RankingGruposAdapter.ViewHolder>() {
    private var grupos = listOf<Grupo>()

    fun submitList(lista: List<Grupo>) {
        grupos = lista
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtGrupo: TextView = view.findViewById(R.id.txtGrupo)
        val txtPontuacao: TextView = view.findViewById(R.id.txtPontuacao)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_ranking, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val grupo = grupos[position]
        holder.txtGrupo.text = "Grupo: ${grupo.nome}"
        holder.txtPontuacao.text = "Pontuação: ${grupo.pontuacaoTotal}"
    }

    override fun getItemCount() = grupos.size
}
