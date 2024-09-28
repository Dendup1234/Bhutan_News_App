package com.example.bhutan_news_app

data class YouTubeResponse(
    val items: List<VideoItem>
)

data class VideoItem(
    val id: VideoId,
    val snippet: Snippet
)

data class VideoId(
    val videoId: String
)

data class Snippet(
    val title: String,
    val description: String,
    val publishedAt: String,
    val thumbnails: Thumbnails,
    val liveBroadcastContent: String
)

data class Thumbnails(
    val medium: ThumbnailDetails
)

data class ThumbnailDetails(
    val url: String
)
