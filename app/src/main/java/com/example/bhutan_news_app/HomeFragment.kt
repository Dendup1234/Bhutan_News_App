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
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {
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
        val view = inflater.inflate(R.layout.home, container, false)
        recyclerView = view.findViewById(R.id.recyclerviewhome)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        dbHelper =  UserDatabaseHelper(requireContext())
        val sharedPref = requireActivity().getSharedPreferences("user_pref", AppCompatActivity.MODE_PRIVATE)
        val userEmail = sharedPref.getString("user_email", "Guest") ?: "Guest"
        newsAdapter = NewsAdapter(newsList,dbHelper,userEmail)
        recyclerView.adapter = newsAdapter
        scrapKuensel()
        return view
    }
    fun convertRelativeDatatoAbsolute(relativeData : String): String{
        val calender =  Calendar.getInstance()
        when{
            relativeData.contains("day") ->{
                val daysAgo = relativeData.split(" ")[0].toInt()
                calender.add(Calendar.DAY_OF_YEAR, -daysAgo) // substracting the day from the current day
            }
            relativeData.contains("week") ->{
                val weeksAgo = relativeData.split(" ")[0].toInt()
                calender.add(Calendar.WEEK_OF_YEAR, -weeksAgo) // substracting the day from the current day
            }

            relativeData.contains("month") ->{
                val monthsAgo = relativeData.split(" ")[0].toInt()
                calender.add(Calendar.MONTH, -monthsAgo) // substracting the day from the current day
            }
            relativeData.contains("year") ->{
                val yearsAgo = relativeData.split(" ")[0].toInt()
                calender.add(Calendar.YEAR, -yearsAgo) // substracting the day from the current day
            }
        }
        // Extract day of the month and append the correct suffix (st, nd, rd, th)
        val day = calender.get(Calendar.DAY_OF_MONTH)
        val dayWithSuffix = getDayWithSuffix(day)

        // Format the month and year
        val monthFormat = SimpleDateFormat("MMMM", Locale.ENGLISH)
        val yearFormat = SimpleDateFormat("yyyy", Locale.ENGLISH)

        val month = monthFormat.format(calender.time)
        val year = yearFormat.format(calender.time)

        // Return the full formatted date with day suffix
        return "$month $dayWithSuffix, $year"
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
    // function for sorting the news basis of their date
    fun sortNewsListByDate(){
        // Define the date format used in the NewsItem (e.g., "September 24, 2024")
        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)

        // Sort the newsList based on the date
        newsList.sortByDescending { newsItem ->
            try {
                // Parse the date in the newsItem into a Date object
                dateFormat.parse(newsItem.date)
            } catch (e: Exception) {
                // If parsing fails, return a default Date (e.g., Jan 1, 1970) to prevent crash
                Date(0)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun scrapKuensel() {
        // Using Kotlin Coroutines to handle the background operation
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Connecting to the Kuensel webpage
                val document = Jsoup.connect("https://kuenselonline.com").get()
                // Connecting to the the bhutanese news
                val document2 =Jsoup.connect("https://thebhutanese.bt/category/headline-stories/").get()
                val document3 = Jsoup.connect("https://thebhutanese.bt/").get()
                // Extracting the Top Stories section from the kuensel news
                val topStoriesSection = document.selectFirst("div.top-stories")
                val topStoriesTitle =  topStoriesSection?.selectFirst("div.title h1")?.text()?:""

                // Extracting the highlighted news from Top Stories
                val topStoriesHighlighted = topStoriesSection?.selectFirst("div.highlight")
                val highlightedTitle = topStoriesHighlighted?.selectFirst("h3.post-title")?.text() ?: "No Title"
                val highlightedDate = topStoriesHighlighted?.selectFirst("p.post-date")?.text() ?: "No date"
                val highlightedDescription = topStoriesHighlighted?.selectFirst("p.post-date + p")?.text() ?: "No Description"
                val highlightedImageUrl = topStoriesHighlighted?.selectFirst("div.featured-img img")?.attr("src") ?: ""
                val highlightedNewsUrl = topStoriesHighlighted?.selectFirst("h3.post-title a")?.attr("href") ?: ""

                // Create a NewsItem for highlighted news
                val highlightedNewsItem = NewsItem(topStoriesTitle,highlightedTitle, highlightedImageUrl, highlightedDate, highlightedDescription,highlightedNewsUrl,"From Kuensel", Date(0))
                newsList.add(highlightedNewsItem)

                // Extracting the bhutanese news
                val headlineArticles = document2.selectFirst("div.post-listing.archive-box")
                val headlineMedia = headlineArticles?.select("article.item-list")?: emptyList()
                for (articles in headlineMedia){
                    val title = articles?.selectFirst("h2.post-box-title")?.text()?:"No title"
                    val date = articles?.selectFirst("span.tie-date")?.text()?: "No Date"
                    val description = articles?.selectFirst("div.entry p")?.text()?: "No Description"
                    val newsUrl = articles?.selectFirst("h2.post-box-title a")?.attr("href")?:"No url"
                    val imageUrl = articles?.selectFirst("div.post-thumbnail img")?.attr("src")?:"No image url"
                    val formatedDate = convertRelativeDatatoAbsolute(date)
                    val newsItem = NewsItem("",title,imageUrl,formatedDate,description,newsUrl,"From The Bhutanese News", Date(0))
                    newsList.add(newsItem)
                }

                // Extracting the other media news under Top Stories
                val mediaArticles = topStoriesSection?.select("div.media-body") ?: emptyList()
                for (article in mediaArticles) {
                    val title = article.selectFirst("h5.post-title a")?.text() ?: "No title"
                    val date = article.selectFirst("p.post-date")?.text() ?: "No Date"
                    val description = article.selectFirst("p.post-date + p")?.text() ?: "No description"
                    val newsUrl = article.selectFirst("h5.post-title a")?.attr("href") ?: ""
                    val newsItem = NewsItem("",title, "", date, description,newsUrl,"From Kuensel", Date(0))
                    newsList.add(newsItem)
                }



                // For the Editorial news
                val editorialSection  = document.selectFirst("div.editorial")
                val editorialTitleText =  editorialSection?.selectFirst("h1")?.text()?:"Editorial"

                // Extracting the editorial articles
                val editorialArticles = editorialSection?.select("div.row")
                if (editorialArticles != null){
                    for(articles in editorialArticles){
                        val articleTitle = articles.selectFirst("h3.post-title")?.text()?:"No title"
                        val articleDate = articles.selectFirst("p.post-date")?.text()?: "No Date"
                        val articleDescription = articles.select("p").eq(2).text() ?: "No description"
                        val articleNewUrl = articles.selectFirst("h3.post-title a")?.attr("href")?: ""
                        newsList.add(NewsItem(editorialTitleText,articleTitle,"",articleDate,articleDescription,articleNewUrl,"From Kuensel", Date(0)))
                    }
                }
                // Extracting the editorial from the bhutanese news
                val bhutaneseEditorialSection = document3.selectFirst("section.cat-box.column2.tie-cat-27")

                // Highlighted news for the bhutanese editorial section
                val bhutaneseHighlighted = bhutaneseEditorialSection?.selectFirst("li.first-news")
                val bhutaneseHighlightedTitle = bhutaneseHighlighted?.selectFirst("h2.post-box-title")?.text()?:"No title"
                val bhutaneseHighlightedDate = bhutaneseHighlighted?.selectFirst("span.tie-date")?.text()?:"No date"
                val formatedDate = convertRelativeDatatoAbsolute(bhutaneseHighlightedDate)
                val bhutaneseHighlightedDescription = bhutaneseHighlighted?.selectFirst("div.entry p")?.text()?:"No Description"
                val bhutaneseHighlightedNewsUrl = bhutaneseHighlighted?.selectFirst("h2.post-box-title")?.text()?:"No news url"
                newsList.add(NewsItem("",bhutaneseHighlightedTitle,"",formatedDate,bhutaneseHighlightedDescription,bhutaneseHighlightedNewsUrl,
                    "From The Bhutanese News", Date(0)))


                // Editorial media for the kuensel news
                val editorialMedia = editorialSection?.select("div.media-body")?: emptyList()
                for (articles in editorialMedia){
                    val title = articles.selectFirst("h5.post-title a")?.text()?:"No title"
                    val date = articles.selectFirst("p.post-date")?.text()?: "No date"
                    val description = articles.selectFirst("p.post-date + p")?.text()?: "No description"
                    val newsUrl = articles.selectFirst("h5.post-title a")?.attr("href")?: ""
                    newsList.add(NewsItem("",title,"",date,description,newsUrl,"From Kuensel", Date(0)))
                }

                // For all the sports sections
                val sportsSections = document.select("div.sports")

                // Iterate through each sports section
                for ((index, sportsSection) in sportsSections.withIndex()) {

                    // You can give each sports section a different title based on the index or other unique attributes
                    val sectionTitle = if (index == 0) "Features" else "Sports"

                    // Extract the section title
                    val featureTitleText = sportsSection.selectFirst("div.title h1")?.text() ?: sectionTitle

                    //For the bhutanses news being fetch or not
                    var bhutaneseFeatureNews= false
                    var bhutaneseSportNews = false

                    // Extracting the highlighted news for the features
                    val featuredHighlighted = sportsSection.selectFirst("div.highlight")
                    val featureHighlightedTitle = featuredHighlighted?.selectFirst("h3.post-title")?.text() ?: "No Title"
                    val featureHighlightedDate = featuredHighlighted?.selectFirst("p.post-date")?.text() ?: "No date"
                    val featureHighlightedImageUrl = featuredHighlighted?.selectFirst("div.featured-img img")?.attr("src") ?: ""
                    val featureHighlightedDescription = featuredHighlighted?.selectFirst("p.post-date + p")?.text() ?: "No description"
                    val featureHighlightedNewsUrl = featuredHighlighted?.selectFirst("h3.post-title a")?.attr("href")?:""

                    // Add the highlighted news to the newsList
                    newsList.add(NewsItem(featureTitleText, featureHighlightedTitle, featureHighlightedImageUrl, featureHighlightedDate, featureHighlightedDescription,featureHighlightedNewsUrl,"From Kuensel", Date(0)))

                    // Including one news from the bhutanese news
                    if(index == 0 && !bhutaneseFeatureNews){
                        val featureSectionBhutaneseNews = document3.selectFirst("section.cat-box.list-box.tie-cat-35")
                        val highlighted = featureSectionBhutaneseNews?.selectFirst("li.first-news")
                        val title = highlighted?.selectFirst("h2.post-box-title")?.text()?:"No title"
                        val image = highlighted?.selectFirst("div.post-thumbnail img")?.attr("src")?: "No image"
                        val date = highlighted?.selectFirst("span.tie-date")?.text()?:"No date"
                        val formatedDate = convertRelativeDatatoAbsolute(date)
                        val description = highlighted?.selectFirst("div.entry p")?.text()?:"No Description"
                        val newsUrl = highlighted?.selectFirst("h2.post-box-title")?.text()?:"No news url"
                        newsList.add(NewsItem("",title,image,formatedDate,description,newsUrl,
                            "From The Bhutanese News", Date(0)))
                        bhutaneseFeatureNews = true
                    }

                    // Extract the media body from the feature news
                    val featureMediaArticles = sportsSection.select("div.media-body") ?: emptyList()
                    for (article in featureMediaArticles) {
                        val title = article.selectFirst("h5.post-title a")?.text() ?: "No title"
                        val date = article.selectFirst("p.post-date")?.text() ?: "No Date"
                        val description = article.selectFirst("p.post-date + p")?.text() ?: "No description"
                        val newsUrl = article.selectFirst("h5.post-title a")?.attr("href")?:""
                        // Add each article from the sports section to the newsList
                        val newsItem = NewsItem("", title, "", date, description,newsUrl,"From Kuensel", Date(0))
                        newsList.add(newsItem)
                    }

                }
                // For the business section
                val businessSection = document.select("div.business")
                // Iterating through the business section
                for ((index, businessArticle) in businessSection.withIndex()) {
                    val sectionTitle = when (index) {
                        0 -> "Business"
                        1 -> "LifeStyle"
                        else -> "ANN"
                    }

                    // Extract the section title
                    val businessTitleText = businessArticle.selectFirst("div.title h1")?.text() ?: sectionTitle

                    // Extracting the highlighted news for the features
                    val businessHighlighted = businessArticle.selectFirst("div.highlight")
                    val businessHighlightedTitle = businessHighlighted?.selectFirst("h3.post-title")?.text() ?: "No Title"
                    val businessHighlightedDate = businessHighlighted?.selectFirst("p.post-date")?.text() ?: "No date"
                    val businessHighlightedImageUrl = businessHighlighted?.selectFirst("div.featured-img img")?.attr("src") ?: ""
                    val businessHighlightedDescription = businessHighlighted?.selectFirst("p.post-date + p")?.text() ?: "No description"
                    val businessHighlightedNewsUrl = businessHighlighted?.selectFirst("h3.post-title a")?.attr("href")?:""
                    // Add the highlighted news to the newsList
                    newsList.add(NewsItem(businessTitleText, businessHighlightedTitle, businessHighlightedImageUrl, businessHighlightedDate, businessHighlightedDescription,businessHighlightedNewsUrl,"From Kuensel", Date(0)))

                    // Extracting the business section news from the bhutanese news
                    var businessSectionNews = false
                    if(index== 0 &&  !businessSectionNews){
                        val businessSection = document3.selectFirst("section.cat-box.list-box.tie-cat-5")
                        val highlighted =  businessSection?.selectFirst("li.first-news")
                        val title = highlighted?.selectFirst("h2.post-box-title")?.text()?:"No title"
                        val date = highlighted?.selectFirst("span.tie-date")?.text()?:"No date"
                        val formatedDate = convertRelativeDatatoAbsolute(date)
                        val description = highlighted?.selectFirst("div.entry p")?.text()?:"No Description"
                        val newsUrl = highlighted?.selectFirst("h2.post-box-title")?.text()?:"No news url"
                        newsList.add(NewsItem("",title,"",formatedDate,description,newsUrl,
                            "From The Bhutanese News", Date(0)))
                        businessSectionNews == true
                    }

                    // Extract the media body from the feature news
                    val businessMediaArticles = businessArticle.select("div.media-body") ?: emptyList()
                    for (article in businessMediaArticles) {
                        val title = article.selectFirst("h5.post-title a")?.text() ?: "No title"
                        val date = article.selectFirst("p.post-date")?.text() ?: "No Date"
                        val description = article.selectFirst("p.post-date + p")?.text() ?: "No description"
                        val newsUrl = article.selectFirst("h5.post-title a")?.attr("href")?:""

                        // Add each article from the business section to the newsList
                        val newsItem = NewsItem("", title, "", date, description,newsUrl,"From Kuensel", Date(0))
                        newsList.add(newsItem)
                    }
                }
                // Extracting the opinion section
                val opinionSection = document.selectFirst("div.opinions")
                val opinionTitle =  opinionSection?.selectFirst("div.title h1")?.text()?:"Opinions"
                // Extracting the other media news under Top Stories
                val opinionMediaArticles = opinionSection?.select("div.media-body") ?: emptyList()

                // For the opinion in the bhutanese news
                val opinionSectionBhutanese = document3.selectFirst("section.cat-box.column2.tie-cat-4.last-column")
                val highlighted =  opinionSectionBhutanese?.selectFirst("li.first-news")
                val titleBhutanese = highlighted?.selectFirst("h2.post-box-title")?.text()?:"No title"
                val dateBhutanese = highlighted?.selectFirst("span.tie-date")?.text()?:"No date"
                val formatedDateBhutanese = convertRelativeDatatoAbsolute(dateBhutanese)
                val descriptionBhutanese = highlighted?.selectFirst("div.entry p")?.text()?:"No Description"
                val newsUrlBhutanese = highlighted?.selectFirst("h2.post-box-title")?.text()?:"No news url"
                newsList.add(NewsItem(opinionTitle,titleBhutanese,"",formatedDateBhutanese,descriptionBhutanese,newsUrlBhutanese,
                    "From The Bhutanese News", Date(0)))

                // Opinion in the kuensel news
                for (article in opinionMediaArticles) {
                    val title = article.selectFirst("h5.post-title a")?.text() ?: "No title"
                    val date = article.selectFirst("p.post-date")?.text() ?: "No Date"
                    val description = article.selectFirst("p.post-date + p")?.text() ?: "No description"
                    val newsUrl = article.selectFirst("h5.post-title a")?.attr("href")?: ""
                    val newsItem = NewsItem("",title, "", date, description,newsUrl,"From Kuensel", Date(0))
                    newsList.add(newsItem)
                }
                // Sorting the news list by their date
                sortNewsListByDate()
                // Update the RecyclerView on the main thread
                withContext(Dispatchers.Main) {
                    newsAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
