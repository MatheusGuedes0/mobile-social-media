package com.example.mobile_social_media.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_social_media.R
import com.example.mobile_social_media.data.model.AtividadeFisica

class RankingAdapter : RecyclerView.Adapter<RankingAdapter.ViewHolder>() {

    private var ranking: List<AtividadeFisica> = emptyList()

    fun submitList(newList: List<AtividadeFisica>) {
        ranking = newList
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtUsuario: TextView = view.findViewById(R.id.txtUsuario)
        val txtPontuacao: TextView = view.findViewById(R.id.txtPontuacao)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ranking, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val atividade = ranking[position]
        holder.txtUsuario.text = "Usuário: ${atividade.nomeUsuario}"
        holder.txtPontuacao.text = "Pontuação: ${atividade.nivel}"
    }

    override fun getItemCount(): Int = ranking.size
}
