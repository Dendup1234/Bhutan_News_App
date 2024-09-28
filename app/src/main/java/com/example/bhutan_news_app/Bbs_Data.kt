package com.example.bhutan_news_app

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface Bbs_Data {
    @GET("search")
    fun getLatestVideos(
        @Query("part") part: String,
        @Query("channelId") channelId: String,
        @Query("order") order: String,
        @Query("maxResults") maxResults: Int,
        @Query("key") apiKey: String
    ): Call<YouTubeResponse>
}