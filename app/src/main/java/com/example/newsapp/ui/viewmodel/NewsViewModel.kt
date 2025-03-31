package com.example.newsapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.newsapp.data.model.NewsResponse
import com.example.newsapp.data.repository.NewsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NewsViewModel(private val repository: NewsRepository) : ViewModel() {
    sealed class NewsState {
        object Loading : NewsState()
        data class Success(val data: NewsResponse) : NewsState()
        data class Error(val exception: Throwable) : NewsState()
    }
    private val _everythingNews = MutableLiveData<NewsResponse>()
    val everythingNews: LiveData<NewsResponse> get() = _everythingNews

    private val _newsState = MutableLiveData<NewsState>()
    val newsState: LiveData<NewsState> get() = _newsState

    private var currentPage = 1
    private var currentQuery = ""

    fun getEverything(query: String, apiKey: String) {
        if (_newsState.value is NewsState.Loading) return

        if (query != currentQuery) {
            currentPage = 1
            currentQuery = query
            _everythingNews.value = NewsResponse(
                status = "",
                totalResults = 0,
                articles = emptyList()
            )
        }

        _newsState.value = NewsState.Loading
        viewModelScope.launch {
            try {
                val response = repository.getEverything(query, apiKey, currentPage)
                if (response.articles.isNotEmpty()) {
                    val currentArticles = _everythingNews.value?.articles ?: emptyList()
                    val newArticles = if (currentPage == 1) {
                        response.articles
                    } else {
                        currentArticles + response.articles
                    }

                    _everythingNews.value = response.copy(
                        articles = newArticles,
                        totalResults = response.totalResults
                    )
                    currentPage++
                    _newsState.value = NewsState.Success(response)
                } else {
                    _newsState.value = NewsState.Success(response)
                }
            } catch (e: Exception) {
                _newsState.value = NewsState.Error(e)
            }
        }
    }

    fun getTopHeadline(category: String, apiKey: String) = liveData(Dispatchers.IO) {
        try {
            emit(repository.getTopHeadline(category, apiKey))
        } catch (e: Exception) {
            throw e
        }
    }

    fun clearData() {
        currentPage = 1
        _everythingNews.value = NewsResponse(
            status = "",
            totalResults = 0,
            articles = emptyList()
        )
    }
}

class NewsViewModelFactory(private val repository: NewsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}



