package com.example.bhutan_news_app

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DashboardFragment : Fragment() {

    private lateinit var webView: WebView
    private val API_KEY = "AIzaSyDJU7cWYrOHn2kytEtaCvVewmTPxtQ-FHg"  // Replace with your YouTube Data API Key
    private val CHANNEL_ID = "UCzZIEQX2fWAmsvztJjIb7eA"  // Bhutan Broadcasting Service Channel ID
    private lateinit var videoAdapter: BbsAdapter

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // Initialize WebView for playing YouTube videos
        webView = rootView.findViewById(R.id.videoWebView)
        webView.settings.javaScriptEnabled = true

        // Initialize RecyclerView for displaying video list
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.recyclerViewVideos)
        recyclerView.layoutManager = LinearLayoutManager(requireContext()) // Use requireContext() in Fragment

        // Initialize the videoAdapter with an empty list
        videoAdapter = BbsAdapter(listOf()) { videoId ->
            // Play video when clicked
            val videoUrl = "https://www.youtube.com/watch?v=$videoId"
            webView.loadUrl(videoUrl)
        }
        recyclerView.adapter = videoAdapter

        // Fetch YouTube videos after view initialization
        fetchYouTubeVideos()

        return rootView
    }

    private fun fetchYouTubeVideos() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/youtube/v3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(Bbs_Data::class.java)
        val call = apiService.getLatestVideos(
            part = "snippet",
            channelId = CHANNEL_ID,
            order = "date",  // Most recent videos first
            maxResults = 20,  // Get the latest 20 videos
            apiKey = API_KEY
        )

        call.enqueue(object : Callback<YouTubeResponse> {
            override fun onResponse(call: Call<YouTubeResponse>, response: Response<YouTubeResponse>) {
                if (response.isSuccessful) {
                    val videoList = response.body()?.items ?: listOf()
                    videoAdapter.updateVideos(videoList)
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch videos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<YouTubeResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
