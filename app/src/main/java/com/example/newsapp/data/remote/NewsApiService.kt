package com.example.newsapp.data.remote

import com.example.newsapp.data.model.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("v2/top-headlines")
    suspend fun getTopHeadline(
        @Query("category") category: String,
        @Query("apiKey") apiKey: String
    ): NewsResponse

    @GET("v2/everything")
    suspend fun getEverything(
        @Query("q") query: String,
        @Query("apiKey") apiKey: String,
        @Query("page") page: Int
    ): NewsResponse
}


