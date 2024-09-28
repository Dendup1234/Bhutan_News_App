package com.example.bhutan_news_app

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import android.text.format.DateUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class BbsAdapter(
    private var videos: List<VideoItem>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<BbsAdapter.VideoViewHolder>() {

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.videoTitle)
        val dateTextView: TextView = itemView.findViewById(R.id.videoDate)
        val thumbnailImageView: ImageView = itemView.findViewById(R.id.videoThumbnail)
        val liveIndicator: TextView = itemView.findViewById(R.id.liveIndicator) // "Live" indicator
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videos[position]
        holder.titleTextView.text = video.snippet.title
        holder.dateTextView.text = getRelativeTime(video.snippet.publishedAt)
        Glide.with(holder.thumbnailImageView.context)
            .load(video.snippet.thumbnails.medium.url)
            .into(holder.thumbnailImageView)

        // Check if the video is live
        if (video.snippet.liveBroadcastContent == "live") {
            holder.liveIndicator.visibility = View.VISIBLE // Show the "Live" indicator
        } else {
            holder.liveIndicator.visibility = View.GONE // Hide the "Live" indicator
        }

        holder.itemView.setOnClickListener {
            onClick(video.id.videoId)
        }
    }

    override fun getItemCount(): Int {
        return videos.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateVideos(newVideos: List<VideoItem>) {
        videos = newVideos
        notifyDataSetChanged()
    }
    fun getRelativeTime(publishedAt: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")

        return try {
            val date = dateFormat.parse(publishedAt) ?: return "Unknown time"
            val now = Date()
            val timeDifferenceMillis = now.time - date.time

            val seconds = TimeUnit.MILLISECONDS.toSeconds(timeDifferenceMillis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifferenceMillis)
            val hours = TimeUnit.MILLISECONDS.toHours(timeDifferenceMillis)
            val days = TimeUnit.MILLISECONDS.toDays(timeDifferenceMillis)

            when {
                seconds < 60 -> "Just now"
                minutes < 60 -> "$minutes minutes ago"
                hours < 24 -> "$hours hours ago"
                days < 7 -> "$days days ago"
                days < 30 -> "${days / 7} weeks ago"
                days < 365 -> "${days / 30} months ago"
                else -> "${days / 365} years ago"
            }

        } catch (e: ParseException) {
            e.printStackTrace()
            "Unknown time"
        }
    }
}
