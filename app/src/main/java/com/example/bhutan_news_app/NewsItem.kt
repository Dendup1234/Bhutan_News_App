package com.example.bhutan_news_app

import java.util.Date


data class NewsItem(
    val section: String,
    val title: String,
    val imageUrl: String,
    val date:String,
    val descriptions: String,
    val newsUrl: String,
    val newsSource: String,
    val sortDate: Date,
)
