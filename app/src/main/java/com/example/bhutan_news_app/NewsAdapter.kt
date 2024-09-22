package com.example.bhutan_news_app

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class NewsAdapter(private val newsList: List<NewsItem>): RecyclerView.Adapter<NewsAdapter.NewsViewHolder> (){
    inner class NewsViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        val sectionTextView: TextView = itemView.findViewById(R.id.sectionTitle)
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsAdapter.NewsViewHolder {
        val view  = LayoutInflater.from(parent.context).inflate(R.layout.news_item,parent,false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsAdapter.NewsViewHolder, position: Int) {
        val newsItem = newsList[position]
        holder.titleTextView.text = newsItem.title
        holder.dateTextView.text = newsItem.date
        holder.descriptionTextView.text = newsItem.descriptions
        holder.sectionTextView.text = newsItem.section
        // Check if the image URL is empty or null, and handle it
        // Handle the image loading with Picasso
        if (newsItem.imageUrl.isNotEmpty()) {
            // Reset the visibility in case the view was recycled and previously hidden
            holder.imageView.visibility = View.VISIBLE

            // Use Picasso to load the image
            Picasso.get()
                .load(newsItem.imageUrl)
                .placeholder(R.drawable.placeholder)  // Set a placeholder image while loading
                .error(R.drawable.error)         // Set an error image if loading fails
                .into(holder.imageView)
        } else {
            // If imageUrl is empty, show a placeholder or hide the ImageView
            holder.imageView.visibility = View.GONE // Use placeholder image
        }
        // Handling the clicking of the newsTitle action
        holder.titleTextView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra("url", newsItem.newsUrl)  // Pass the URL to WebViewActivity
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = newsList.size
}