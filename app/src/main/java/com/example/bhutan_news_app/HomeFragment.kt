package com.example.bhutan_news_app

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class HomeFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter
    private val newsList = mutableListOf<NewsItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment(home.xml)
        val view = inflater.inflate(R.layout.home, container, false)
        recyclerView = view.findViewById(R.id.recyclerviewhome)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        newsAdapter = NewsAdapter(newsList)
        recyclerView.adapter = newsAdapter
        scrapKuensel()
        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun scrapKuensel() {
        // Using Kotlin Coroutines to handle the background operation
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Connecting to the Kuensel webpage
                val document = Jsoup.connect("https://kuenselonline.com").get()


                // Extracting the Top Stories section
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
                val highlightedNewsItem = NewsItem(topStoriesTitle,highlightedTitle, highlightedImageUrl, highlightedDate, highlightedDescription,highlightedNewsUrl)
                newsList.add(highlightedNewsItem)

                // Extracting the other media news under Top Stories
                val mediaArticles = topStoriesSection?.select("div.media-body") ?: emptyList()
                for (article in mediaArticles) {
                    val title = article.selectFirst("h5.post-title a")?.text() ?: "No title"
                    val date = article.selectFirst("p.post-date")?.text() ?: "No Date"
                    val description = article.selectFirst("p.post-date + p")?.text() ?: "No description"
                    val newsUrl = article.selectFirst("h5.post-title a")?.attr("href") ?: ""
                    val newsItem = NewsItem("",title, "", date, description,newsUrl)
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
                        newsList.add(NewsItem(editorialTitleText,articleTitle,"",articleDate,articleDescription,articleNewUrl))
                    }
                }
                val editorialMedia = editorialSection?.select("div.media-body")?: emptyList()
                for (articles in editorialMedia){
                    val title = articles.selectFirst("h5.post-title a")?.text()?:"No title"
                    val date = articles.selectFirst("p.post-date")?.text()?: "No date"
                    val description = articles.selectFirst("p.post-date + p")?.text()?: "No description"
                    val newsUrl = articles.selectFirst("h5.post-title a")?.attr("href")?: ""
                    newsList.add(NewsItem("",title,"",date,description,newsUrl))
                }

                // For all the sports sections
                val sportsSections = document.select("div.sports")

                // Iterate through each sports section
                for ((index, sportsSection) in sportsSections.withIndex()) {

                    // You can give each sports section a different title based on the index or other unique attributes
                    val sectionTitle = if (index == 0) "Features" else "Sports"

                    // Extract the section title
                    val featureTitleText = sportsSection.selectFirst("div.title h1")?.text() ?: sectionTitle

                    // Extracting the highlighted news for the features
                    val featuredHighlighted = sportsSection.selectFirst("div.highlight")
                    val featureHighlightedTitle = featuredHighlighted?.selectFirst("h3.post-title")?.text() ?: "No Title"
                    val featureHighlightedDate = featuredHighlighted?.selectFirst("p.post-date")?.text() ?: "No date"
                    val featureHighlightedImageUrl = featuredHighlighted?.selectFirst("div.featured-img img")?.attr("src") ?: ""
                    val featureHighlightedDescription = featuredHighlighted?.selectFirst("p.post-date + p")?.text() ?: "No description"
                    val featureHighlightedNewsUrl = featuredHighlighted?.selectFirst("h3.post-title a")?.attr("href")?:""

                    // Add the highlighted news to the newsList
                    newsList.add(NewsItem(featureTitleText, featureHighlightedTitle, featureHighlightedImageUrl, featureHighlightedDate, featureHighlightedDescription,featureHighlightedNewsUrl))

                    // Extract the media body from the feature news
                    val featureMediaArticles = sportsSection.select("div.media-body") ?: emptyList()
                    for (article in featureMediaArticles) {
                        val title = article.selectFirst("h5.post-title a")?.text() ?: "No title"
                        val date = article.selectFirst("p.post-date")?.text() ?: "No Date"
                        val description = article.selectFirst("p.post-date + p")?.text() ?: "No description"
                        val newsUrl = article.selectFirst("h5.post-title a")?.attr("href")?:""
                        // Add each article from the sports section to the newsList
                        val newsItem = NewsItem("", title, "", date, description,newsUrl)
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
                    newsList.add(NewsItem(businessTitleText, businessHighlightedTitle, businessHighlightedImageUrl, businessHighlightedDate, businessHighlightedDescription,businessHighlightedNewsUrl))

                    // Extract the media body from the feature news
                    val businessMediaArticles = businessArticle.select("div.media-body") ?: emptyList()
                    for (article in businessMediaArticles) {
                        val title = article.selectFirst("h5.post-title a")?.text() ?: "No title"
                        val date = article.selectFirst("p.post-date")?.text() ?: "No Date"
                        val description = article.selectFirst("p.post-date + p")?.text() ?: "No description"
                        val newsUrl = article.selectFirst("h5.post-title a")?.attr("href")?:""

                        // Add each article from the business section to the newsList
                        val newsItem = NewsItem("", title, "", date, description,newsUrl)
                        newsList.add(newsItem)
                    }
                }
                // Extracting the opinion section
                val opinionSection = document.selectFirst("div.opinions")
                val opinionTitle =  opinionSection?.selectFirst("div.title h1")?.text()?:"Opinions"
                newsList.add(NewsItem(opinionTitle,"","","","",""))
                // Extracting the other media news under Top Stories
                val opinionmediaArticles = opinionSection?.select("div.media-body") ?: emptyList()
                for (article in opinionmediaArticles) {
                    val title = article.selectFirst("h5.post-title a")?.text() ?: "No title"
                    val date = article.selectFirst("p.post-date")?.text() ?: "No Date"
                    val description = article.selectFirst("p.post-date + p")?.text() ?: "No description"
                    val newsUrl = article.selectFirst("h5.post-title a")?.attr("href")?: ""
                    val newsItem = NewsItem("",title, "", date, description,newsUrl)
                    newsList.add(newsItem)
                }
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
