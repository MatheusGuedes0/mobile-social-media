package com.example.mobile_social_media

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_social_media.adapter.RankingAdapter
import com.example.mobile_social_media.data.viewModels.RankingViewModel

class RankingActivity : AppCompatActivity() {

    private val viewModel: RankingViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RankingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        recyclerView = findViewById(R.id.recyclerRanking)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RankingAdapter()
        recyclerView.adapter = adapter

        lifecycleScope.launchWhenStarted {
            viewModel.ranking.collect { lista ->
                adapter.submitList(lista)
            }
        }
    }
}
