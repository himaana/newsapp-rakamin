package com.example.newsapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.newsapp.BuildConfig
import com.example.newsapp.data.repository.NewsRepository
import com.example.newsapp.databinding.ActivityMainBinding
import com.example.newsapp.ui.adapter.NewsAdapter
import com.example.newsapp.ui.viewmodel.NewsViewModel
import com.example.newsapp.ui.viewmodel.NewsViewModelFactory
import com.example.newsapp.utils.DateUtils

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val repository = NewsRepository()
    private val viewModel: NewsViewModel by viewModels { NewsViewModelFactory(repository) }
    private lateinit var newsAdapter: NewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupRecyclerView()
        loadInitialData()
    }

    private fun setupObservers() {
        viewModel.getTopHeadline("business", BuildConfig.API_KEY).observe(this) { response ->
            response.articles.firstOrNull()?.let { article ->
                binding.topNewsTitle.text = article.title
                binding.topNewsSource.text = article.source.name
                binding.topNewsDate.text = DateUtils.formatDate(article.publishedAt)
                Glide.with(this).load(article.urlToImage).into(binding.topNewsImage)
            }
        }

        viewModel.everythingNews.observe(this) { response ->
            newsAdapter.submitList(response.articles)
        }

        viewModel.newsState.observe(this) { state ->
            when (state) {
                is NewsViewModel.NewsState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is NewsViewModel.NewsState.Success -> {
                    binding.progressBar.visibility = View.GONE
                }

                is NewsViewModel.NewsState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Error: ${state.exception.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        newsAdapter = NewsAdapter()

        binding.newsRecyclerView.apply {
            this.layoutManager = layoutManager
            adapter = newsAdapter
            setHasFixedSize(true) // Optimasi performa
        }

        binding.nestedScrollView.setOnScrollChangeListener { v: NestedScrollView, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY) {
                val lastChild = binding.linearLayout.getChildAt(binding.linearLayout.childCount - 1)
                lastChild?.let {
                    val diff = (it.bottom - (v.height + v.scrollY))

                    if (diff < 100 &&
                        viewModel.newsState.value !is NewsViewModel.NewsState.Loading &&
                        !isLastPage()
                    ) {
                        viewModel.getEverything("indonesia", BuildConfig.API_KEY)
                    }
                }
            }
        }
    }

    private fun isLastPage(): Boolean {
        return (viewModel.everythingNews.value?.articles?.size ?: 0) >=
                (viewModel.everythingNews.value?.totalResults ?: 0)
    }

    private fun loadInitialData() {
        viewModel.getEverything("Bank", BuildConfig.API_KEY)
    }
}