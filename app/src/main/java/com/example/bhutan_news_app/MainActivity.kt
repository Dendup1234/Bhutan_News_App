package com.example.bhutan_news_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var bottomNavigation : BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initializing the variable
        viewPager = findViewById(R.id.fragment_container)
        tabLayout = findViewById(R.id.menu_item)
        bottomNavigation = findViewById(R.id.bottom_navigation)


        // Set up the ViewPager2 and TabLayout for top tabs
        viewPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Home"
                1 -> "News"
                2 -> "Business"
                3 -> "Editorial"
                4 -> "Nation"
                5 -> "Feature"
                6 -> "Life Style"
                7 -> "Opinion"
                8 -> "Sport"
                else -> null
            }
        }.attach()
        // Handle BottomNavigationView item clicks
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    showTopNavigation()  // Show top navigation when Home is selected
                }
                R.id.nav_dashboard -> {
                    hideTopNavigation()  // Hide top navigation for Dashboard
                    loadFragment(DashboardFragment())
                }
                R.id.nav_profile -> {
                    hideTopNavigation()  // Hide top navigation for Profile
                    checkLoginStatusAndNavigate()
                }
            }
            true
        }

        // Load the default tab (Home with top tabs) on startup
        if (savedInstanceState == null) {
            showTopNavigation()
        }

    }

    // Show Top Navigation (TabLayout and ViewPager2)
    private fun showTopNavigation() {
        tabLayout.visibility = View.VISIBLE
        viewPager.visibility = View.VISIBLE
    }

    // Hide Top Navigation (TabLayout and ViewPager2)
    private fun hideTopNavigation() {
        tabLayout.visibility = View.GONE
        viewPager.visibility = View.GONE
    }

    // Function to load a fragment (for Dashboard, Profile, etc.)
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_bottom, fragment)
            .commit()
    }
    // Checking the logging status
    private fun checkLoginStatusAndNavigate() {
        val sharedPref = getSharedPreferences("user_pref", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("is_logged_in", false)
        if (isLoggedIn) {
            // If the user is logged in, go to ProfileFragment
            loadFragment(ProfileFragment())
        } else {
            // If not logged in, redirect to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
