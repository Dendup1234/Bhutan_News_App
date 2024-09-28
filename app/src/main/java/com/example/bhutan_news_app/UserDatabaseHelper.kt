package com.example.bhutan_news_app

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Date

class UserDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "news.db"
        private const val DATABASE_VERSION = 2 // Increment version for schema change
        private const val TABLE_LIKED_NEWS = "liked_news"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_URL = "url"
        private const val COLUMN_USER_EMAIL = "user_email" // New column for user email
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_LIKED_NEWS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT,
                $COLUMN_URL TEXT,
                $COLUMN_USER_EMAIL TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_LIKED_NEWS ADD COLUMN $COLUMN_USER_EMAIL TEXT")
        }
    }

    // Check if the news is liked by a specific user
    fun isNewsLiked(url: String, userEmail: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_LIKED_NEWS,
            null,
            "$COLUMN_URL=? AND $COLUMN_USER_EMAIL=?",
            arrayOf(url, userEmail),
            null,
            null,
            null
        )
        val isLiked = cursor.count > 0
        cursor.close()
        return isLiked
    }

    // Toggle the like status of a news item for a specific user
    fun toggleNewsLike(newsItem: NewsItem, userEmail: String): Boolean {
        val db = writableDatabase
        return if (isNewsLiked(newsItem.newsUrl, userEmail)) {
            db.delete(TABLE_LIKED_NEWS, "$COLUMN_URL=? AND $COLUMN_USER_EMAIL=?", arrayOf(newsItem.newsUrl, userEmail))
            false
        } else {
            val values = ContentValues().apply {
                put(COLUMN_TITLE, newsItem.title)
                put(COLUMN_URL, newsItem.newsUrl)
                put(COLUMN_USER_EMAIL, userEmail) // Store the user’s email
            }
            db.insert(TABLE_LIKED_NEWS, null, values)
            true
        }
    }

    // Get all liked news for a specific user
    @SuppressLint("Range")
    fun getLikedNews(userEmail: String): List<NewsItem> {
        val likedNews = mutableListOf<NewsItem>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_LIKED_NEWS,
            null,
            "$COLUMN_USER_EMAIL=?",
            arrayOf(userEmail),
            null,
            null,
            null
        )
        if (cursor.moveToFirst()) {
            do {
                val title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE))
                val url = cursor.getString(cursor.getColumnIndex(COLUMN_URL))
                likedNews.add(NewsItem("", title, "", "", "", url, "", Date()))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return likedNews
    }
}

