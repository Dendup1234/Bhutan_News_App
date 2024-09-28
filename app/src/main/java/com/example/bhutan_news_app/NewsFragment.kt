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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class NewsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter
    private val newsList = mutableListOf<NewsItem>()
    private lateinit var dbHelper:UserDatabaseHelper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment(news.xml)
        val view = inflater.inflate(R.layout.news,container,false)
        recyclerView = view.findViewById(R.id.recyclerviewnews)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        dbHelper =  UserDatabaseHelper(requireContext())
        val sharedPref = requireActivity().getSharedPreferences("user_pref", AppCompatActivity.MODE_PRIVATE)
        val userEmail = sharedPref.getString("user_email", "Guest") ?: "Guest"
        newsAdapter = NewsAdapter(newsList,dbHelper,userEmail)
        recyclerView.adapter = newsAdapter
        scrapNewsPage()
        return view
    }
    fun convertKuenselDate(absoluteDate: String): Date? {
        // Remove the day suffix (st, nd, rd, th) before parsing
        val cleanDate = absoluteDate.replace(Regex("(\\d+)(st|nd|rd|th)"), "$1")

        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
        return try {
            dateFormat.parse(cleanDate)
        } catch (e: Exception) {
            null
        }
    }

    fun convertBhutaneseDate(relativeData: String): Date? {
        val calendar = Calendar.getInstance()

        return when {
            // Handle relative date formats
            relativeData.contains("day") -> {
                val daysAgo = relativeData.split(" ")[0].toInt()
                calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
                calendar.time
            }
            relativeData.contains("week") -> {
                val weeksAgo = relativeData.split(" ")[0].toInt()
                calendar.add(Calendar.WEEK_OF_YEAR, -weeksAgo)
                calendar.time
            }
            relativeData.contains("month") -> {
                val monthsAgo = relativeData.split(" ")[0].toInt()
                calendar.add(Calendar.MONTH, -monthsAgo)
                calendar.time
            }
            relativeData.contains("year") -> {
                val yearsAgo = relativeData.split(" ")[0].toInt()
                calendar.add(Calendar.YEAR, -yearsAgo)
                calendar.time
            }
            // Handle absolute date format (MM/dd/yyyy)
            relativeData.matches(Regex("\\d{2}/\\d{2}/\\d{4}")) -> {
                val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH)
                try {
                    dateFormat.parse(relativeData)
                } catch (e: Exception) {
                    null
                }
            }
            else -> null
        }
    }
    fun convertRelativeDataToAbsolute(relativeData: String): String {
        val calendar = Calendar.getInstance()

        // Check if the format is MM/dd/yyyy
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH)
        return if (relativeData.matches(Regex("\\d{1,2}/\\d{1,2}/\\d{4}"))) {
            // Return the date as it is if it matches the MM/dd/yyyy pattern
            relativeData
        } else {
            when {
                relativeData.contains("day") -> {
                    val daysAgo = relativeData.split(" ")[0].toInt()
                    calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
                }
                relativeData.contains("week") -> {
                    val weeksAgo = relativeData.split(" ")[0].toInt()
                    calendar.add(Calendar.WEEK_OF_YEAR, -weeksAgo)
                }
                relativeData.contains("month") -> {
                    val monthsAgo = relativeData.split(" ")[0].toInt()
                    calendar.add(Calendar.MONTH, -monthsAgo)
                }
                relativeData.contains("year") -> {
                    val yearsAgo = relativeData.split(" ")[0].toInt()
                    calendar.add(Calendar.YEAR, -yearsAgo)
                }
            }

            // Extract day of the month and append the correct suffix (st, nd, rd, th)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val dayWithSuffix = getDayWithSuffix(day)

            // Format the month and year
            val monthFormat = SimpleDateFormat("MMMM", Locale.ENGLISH)
            val yearFormat = SimpleDateFormat("yyyy", Locale.ENGLISH)

            val month = monthFormat.format(calendar.time)
            val year = yearFormat.format(calendar.time)

            // Return the full formatted date with day suffix
            "$month $dayWithSuffix, $year"
        }
    }
    // Function to get the correct suffix for the day of the month
    fun getDayWithSuffix(day: Int): String {
        return when {
            day in 11..13 -> "${day}th"  // Special cases for 11th, 12th, 13th
            day % 10 == 1 -> "${day}st"  // For days ending in 1 (except 11)
            day % 10 == 2 -> "${day}nd"  // For days ending in 2 (except 12)
            day % 10 == 3 -> "${day}rd"  // For days ending in 3 (except 13)
            else -> "${day}th"           // For all other cases
        }
    }
    fun sortNewsListByDate() {
        newsList.sortWith(compareByDescending<NewsItem> { it.sortDate ?: Date(0) })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun scrapNewsPage(){
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val document = Jsoup.connect("https://kuenselonline.com/category/news/").get()
                val document2 = Jsoup.connect("https://thebhutanese.bt/category/news/").get()
                val document3 = Jsoup.connect("https://thebhutanese.bt/category/gewogs-and-dzongkhags/").get()

                // Extracting the Highlighted news
                val highlightedNews = document.selectFirst("div.highlight")
                val highlightedTitle = highlightedNews?.selectFirst("h5.mt-0.post-title")?.text()?: "No Title"
                val highlightedDate = highlightedNews?.selectFirst("p.post-date")?.text()?:"No Date"
                val highlightedFormatedDate = convertKuenselDate(highlightedDate)?: Date(0)
                val highlightedDescription = highlightedNews?.selectFirst("p.post-date + p + p")?.text()?: "No Description"
                val highlightedImageUrl = highlightedNews?.selectFirst("div.media-left")?.attr("style")?.let{
                    // Extracting the image url from the inline style
                    it.substringAfter("url(").substringBefore(")")
                }?: ""
                val highlightedNewsUrl = highlightedNews?.selectFirst("h5.mt-0.post-title a")?.attr("href")?:""
                val highlightedNewsItem = NewsItem("",highlightedTitle,highlightedImageUrl,highlightedDate,highlightedDescription,highlightedNewsUrl,"From Kuensel",highlightedFormatedDate)
                newsList.add(highlightedNewsItem)

                // Extracting the Other featured news in the media
                val category = document.select("div.category")
                val mediaNews = category.select("div.col-md-6")
                for (articles in mediaNews){
                    val title = articles?.selectFirst("h5.mt-0.post-title")?.text()?: "No Title"
                    val date  = articles?.selectFirst("p.post-date")?.text()?:"No Date"
                    val formatedDate = convertKuenselDate(date)?:Date(0)
                    val description = articles?.selectFirst("p.post-date + p")?.text()?:""
                    val imageUrl = articles.selectFirst("div.media-left")?.attr("style")?.let{
                        // Extracting the image url from the inline style
                        it.substringAfter("url(").substringBefore(")")
                    }?: ""
                    val newsUrl = articles.selectFirst("h5.mt-0.post-title a")?.attr("href")?:""
                    newsList.add(NewsItem("",title,imageUrl,date,description,newsUrl,"From Kuensel",formatedDate))
                }
                // Extracting the News from the bhutanese news
                val bhutaneseNewsSection = document2.selectFirst("div.post-listing.archive-box")
                val bhutaneseNews = bhutaneseNewsSection?.select("article.item-list")
                if (bhutaneseNews != null) {
                    for (article in bhutaneseNews){
                        val title = article?.selectFirst("h2.post-box-title")?.text()?:""
                        val date = article?.selectFirst("span.tie-date")?.text()?:""
                        val relativeDate = convertRelativeDataToAbsolute(date)
                        val formatedDate =  convertBhutaneseDate(date)?:Date(0)
                        val description = article?.selectFirst("div.entry p")?.text()?:""
                        val imageUrl = article?.selectFirst("div.post-thumbnail img")?.attr("src")?:""
                        val newsUrl = article?.selectFirst("h2.post-box-title a")?.attr("href")?:""
                        newsList.add(NewsItem("",title,imageUrl,relativeDate,description,newsUrl,"From Bhutanese News",formatedDate))
                    }
                }
                //Extracting the local news from the bhutanese news
                val bhutaneseLocaleNewsSection = document3.selectFirst("div.post-listing.archive-box")
                val bhutaneseLocaleNews = bhutaneseLocaleNewsSection?.select("article.item-list")
                if (bhutaneseLocaleNews != null) {
                    for (article in bhutaneseLocaleNews){
                        val title = article?.selectFirst("h2.post-box-title")?.text()?:""
                        val date = article?.selectFirst("span.tie-date")?.text()?:""
                        val relativeDate = convertRelativeDataToAbsolute(date)
                        val formatedDate =  convertBhutaneseDate(date)?:Date(0)
                        val description = article?.selectFirst("div.entry p")?.text()?:""
                        val imageUrl = article?.selectFirst("div.post-thumbnail img")?.attr("src")?:""
                        val newsUrl = article?.selectFirst("h2.post-box-title a")?.attr("href")?:""
                        newsList.add(NewsItem("",title,imageUrl,relativeDate,description,newsUrl,"From Bhutanese News",formatedDate))
                    }
                }
                sortNewsListByDate()
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