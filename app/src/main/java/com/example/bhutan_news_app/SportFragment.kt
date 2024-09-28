package com.example.bhutan_news_app

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.util.Date

class SportFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter
    private val newsList = mutableListOf<NewsItem>()
    private lateinit var dbHelper:UserDatabaseHelper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment(home.xml)
        val view = inflater.inflate(R.layout.sport,container,false)
        recyclerView = view.findViewById(R.id.recyclerviewsports)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        dbHelper =  UserDatabaseHelper(requireContext())
        val sharedPref = requireActivity().getSharedPreferences("user_pref", AppCompatActivity.MODE_PRIVATE)
        val userEmail = sharedPref.getString("user_email", "Guest") ?: "Guest"
        newsAdapter = NewsAdapter(newsList,dbHelper,userEmail)
        recyclerView.adapter = newsAdapter
        scrapNewsPage()
        return view
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun scrapNewsPage(){
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val document = Jsoup.connect("https://kuenselonline.com/category/sports/").get()
                // Extracting the Highlighted news
                val highlightedNews = document.selectFirst("div.highlight")
                val highlightedTitle = highlightedNews?.selectFirst("h5.mt-0.post-title")?.text()?: "No Title"
                val highlightedDate = highlightedNews?.selectFirst("p.post-date")?.text()?:"No Date"
                val highlightedDescription = highlightedNews?.selectFirst("p.post-date + p + p")?.text()?: "No Description"
                val highlightedImageUrl = highlightedNews?.selectFirst("div.media-left")?.attr("style")?.let{
                    // Extracting the image url from the inline style
                    it.substringAfter("url(").substringBefore(")")
                }?: ""
                val highlightedNewsUrl = highlightedNews?.selectFirst("h5.mt-0.post-title a")?.attr("href")?:""
                val highlightedNewsItem = NewsItem("Sports",highlightedTitle,highlightedImageUrl,highlightedDate,highlightedDescription,highlightedNewsUrl,"From Kuensel", Date(0))
                newsList.add(highlightedNewsItem)

                // Extracting the Other featured news in the media
                val category = document.select("div.category")
                val mediaNews = category.select("div.col-md-6")
                for (articles in mediaNews){
                    val title = articles?.selectFirst("h5.mt-0.post-title")?.text()?: "No Title"
                    val date  = articles?.selectFirst("p.post-date")?.text()?:"No Date"
                    val description = articles?.selectFirst("p.post-date + p")?.text()?:""
                    val imageUrl = articles.selectFirst("div.media-left")?.attr("style")?.let{
                        // Extracting the image url from the inline style
                        it.substringAfter("url(").substringBefore(")")
                    }?: ""
                    val newsUrl = articles.selectFirst("h5.mt-0.post-title a")?.attr("href")?:""
                    newsList.add(NewsItem("",title,imageUrl,date,description,newsUrl,"From Kuensel", Date(0)))
                }

                // Update the RecyclerView on the main thread
                withContext(Dispatchers.Main) {
                    newsAdapter.notifyDataSetChanged()
                }
            }
            catch(e:Exception){
                e.printStackTrace()
            }

        }
    }

}