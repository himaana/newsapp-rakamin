package com.example.newsapp.data.repository

import com.example.newsapp.data.model.NewsResponse
import com.example.newsapp.data.remote.NewsApiConfig

class NewsRepository {
    private val apiService = NewsApiConfig.apiNewsService

    suspend fun getTopHeadline(category: String, apiKey: String): NewsResponse {
        return apiService.getTopHeadline(category, apiKey)
    }

    suspend fun getEverything(query: String, apiKey: String, page: Int): NewsResponse {
        return apiService.getEverything(query, apiKey, page)
    }
}


