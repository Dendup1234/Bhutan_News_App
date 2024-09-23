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

class SportFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter
    private val newsList = mutableListOf<NewsItem>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment(home.xml)
        val view = inflater.inflate(R.layout.sport,container,false)
        recyclerView = view.findViewById(R.id.recyclerviewsports)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        newsAdapter = NewsAdapter(newsList)
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
                val highlightedNewsItem = NewsItem("Sports",highlightedTitle,highlightedImageUrl,highlightedDate,highlightedDescription,highlightedNewsUrl)
                newsList.add(highlightedNewsItem)

                // Extracting the Other featured news in the media
                val mediaNews = document.select("div.col-md-6")
                for (articles in mediaNews){
                    val title = articles?.selectFirst("h5.mt-0.post-title")?.text()?: "No Title"
                    val date  = articles?.selectFirst("p.post-date")?.text()?:"No Date"
                    val description = articles?.selectFirst("p.post-date + p")?.text()?:""
                    val imageUrl = articles.selectFirst("div.media-left")?.attr("style")?.let{
                        // Extracting the image url from the inline style
                        it.substringAfter("url(").substringBefore(")")
                    }?: ""
                    val newsUrl = articles.selectFirst("h5.mt-0.post-title a")?.attr("href")?:""
                    newsList.add(NewsItem("",title,imageUrl,date,description,newsUrl))
                }

                // For the related news
                val relatedNews = document.selectFirst("div.related-wrapper")

                // Extracting the editorial news section
                val editorialSection = relatedNews?.selectFirst("div.editorial")
                val editorialTitle = editorialSection?.selectFirst("h1")?.text()?: ""
                val editorialArticle = editorialSection?.select("div.media-body")?: emptyList()
                newsList.add(NewsItem(editorialTitle,"","","","",""))
                for( articles in editorialArticle){
                    val title = articles?.selectFirst("h5.post-title")?.text()?:""
                    val date = articles?.selectFirst("p.post-date")?.text()?:""
                    val description = articles?.selectFirst("p.post-date +p")?.text()?:""
                    val newsUrl = articles?.selectFirst("h5.post-title a")?.attr("href")?:""
                    newsList.add(NewsItem("",title,"",date,description,newsUrl))
                }
                // Extracting the opinions news section
                val opinionSection = relatedNews?.selectFirst("div.opinions")
                val opinionTitle = opinionSection?.selectFirst("h1")?.text()?: ""
                val opinionArticle = opinionSection?.select("div.media-body")?: emptyList()
                newsList.add(NewsItem(opinionTitle,"","","","",""))
                for( articles in opinionArticle){
                    val title = articles?.selectFirst("h5.post-title")?.text()?:""
                    val date = articles?.selectFirst("p.post-date")?.text()?:""
                    val description = articles?.selectFirst("p.post-date +p")?.text()?:""
                    val newsUrl = articles?.selectFirst("h5.post-title a")?.attr("href")?:""
                    newsList.add(NewsItem("",title,"",date,description,newsUrl))
                }

                // Extracting the sports news section
                val sportsSection = relatedNews?.selectFirst("div.sports")
                val sportsTitle = sportsSection?.selectFirst("h1")?.text()?: ""
                val sportsArticle = sportsSection?.select("div.media-body")?: emptyList()
                newsList.add(NewsItem(sportsTitle,"","","","",""))
                for( articles in sportsArticle){
                    val title = articles?.selectFirst("h5.post-title")?.text()?:""
                    val date = articles?.selectFirst("p.post-date")?.text()?:""
                    val description = articles?.selectFirst("p.post-date +p")?.text()?:""
                    val newsUrl = articles?.selectFirst("h5.post-title a")?.attr("href")?:""
                    newsList.add(NewsItem("",title,"",date,description,newsUrl))
                }

                // Extracting the business news section
                val businessSection = relatedNews?.selectFirst("div.business")
                val businessTitle = businessSection?.selectFirst("h1")?.text()?: ""
                val businessArticle = businessSection?.select("div.media-body")?: emptyList()
                newsList.add(NewsItem(businessTitle,"","","","",""))
                for( articles in businessArticle){
                    val title = articles?.selectFirst("h5.post-title")?.text()?:""
                    val date = articles?.selectFirst("p.post-date")?.text()?:""
                    val description = articles?.selectFirst("p.post-date +p")?.text()?:""
                    val newsUrl = articles?.selectFirst("h5.post-title a")?.attr("href")?:""
                    newsList.add(NewsItem("",title,"",date,description,newsUrl))
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