package com.example.bhutan_news_app

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class LikedActivity : AppCompatActivity() {
    private lateinit var dbHelper: UserDatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter
    private val likedNewsList = mutableListOf<NewsItem>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.liked_activity)

        // Get user email from SharedPreferences
        val sharedPref = getSharedPreferences("user_pref", MODE_PRIVATE)
        val userEmail = sharedPref.getString("user_email", "Guest")

        // Initialize the database helper and RecyclerView
        dbHelper = UserDatabaseHelper(this)
        recyclerView = findViewById(R.id.recyclerviewLikedNews)
        recyclerView.layoutManager = LinearLayoutManager(this)
        // Get liked news for this user
        userEmail?.let {
            likedNewsList.addAll(dbHelper.getLikedNews(it))
        }
        // Initialize the adapter with liked news
        newsAdapter = NewsAdapter(likedNewsList, dbHelper,userEmail!!)
        recyclerView.adapter = newsAdapter
        //Setting up the toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        // Enabling the back button action
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Handle the back button click
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
