package com.example.bhutan_news_app

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class NewsAdapter(
    private val newsList: List<NewsItem>,
    private val dbHelper: UserDatabaseHelper,
    private val userEmail: String  // User email passed to the adapter
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sectionTextView: TextView = itemView.findViewById(R.id.sectionTitle)
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val sourceTextView: TextView = itemView.findViewById(R.id.sourceTextView)
        val likeButton: ImageButton = itemView.findViewById(R.id.likeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.news_item, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val newsItem = newsList[position]
        holder.titleTextView.text = newsItem.title
        holder.dateTextView.text = newsItem.date
        val description = if (newsItem.descriptions.length > 100) {
            "${newsItem.descriptions.substring(0, 100)}..."
        } else {
            newsItem.descriptions
        }
        holder.descriptionTextView.text = description
        holder.sectionTextView.text = newsItem.section
        holder.sourceTextView.text = newsItem.newsSource

        if (newsItem.imageUrl.isNotEmpty()) {
            holder.imageView.visibility = View.VISIBLE
            Picasso.get().load(newsItem.imageUrl)
                .placeholder(R.drawable.bhutan_news_logo)
                .error(R.drawable.error)
                .into(holder.imageView)
        } else {
            holder.imageView.visibility = View.GONE
        }

        holder.titleTextView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra("url", newsItem.newsUrl)
            context.startActivity(intent)
        }
        // Check if the user is logged in
        val sharedPref = holder.itemView.context.getSharedPreferences("user_pref", AppCompatActivity.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("is_logged_in", false)

        // Check if the news is liked by the user
        if (isLoggedIn) {
            val isLiked = dbHelper.isNewsLiked(newsItem.newsUrl, userEmail)
            holder.likeButton.setImageResource(if (isLiked) R.drawable.ic_liked else R.drawable.ic_like)
        } else {
            holder.likeButton.setImageResource(R.drawable.ic_like)
        }

        // Handle Like button click
        holder.likeButton.setOnClickListener {
            if (!isLoggedIn) {
                // If not logged in, redirect to LoginActivity
                val context = holder.itemView.context
                val intent = Intent(context, LoginActivity::class.java)
                context.startActivity(intent)
            } else {
                // If logged in, toggle the like status
                val isNowLiked = dbHelper.toggleNewsLike(newsItem, userEmail)
                holder.likeButton.setImageResource(if (isNowLiked) R.drawable.ic_liked else R.drawable.ic_like)
            }
        }
    }

    override fun getItemCount(): Int = newsList.size
}
