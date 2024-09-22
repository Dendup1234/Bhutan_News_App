package com.example.bhutan_news_app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private  lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Intializing the viewPager and Adapter
        viewPager = findViewById(R.id.fragment_container)
        tabLayout = findViewById(R.id.menu_item)

        // Set up the adapter with ViewPager2
        viewPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter

        // Linking the Tablayout with ViewPager2 using the TabLayoutMediator
        TabLayoutMediator(tabLayout,viewPager){ tab, position ->
            tab.text  = when(position){
                0 -> "Home"
                1 -> "News"
                2 -> "Business"
                3 -> "Editorial"
                4 -> "Election"
                5 -> "Feature"
                6 -> "Life Style"
                7 -> "Opinion"
                8 -> "Sport"
                else -> null
            }

        }.attach()

    }
}