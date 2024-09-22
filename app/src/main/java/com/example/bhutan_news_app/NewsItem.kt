package com.example.bhutan_news_app


data class NewsItem(
    val section: String,
    val title: String,
    val imageUrl: String,
    val date:String,
    val descriptions: String,
    val newsUrl: String
)
