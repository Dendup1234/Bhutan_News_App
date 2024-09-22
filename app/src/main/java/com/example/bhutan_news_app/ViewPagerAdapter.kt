package com.example.bhutan_news_app

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity
) {
    override fun getItemCount(): Int {
        //  Returning the total number of tabs
        return 9
    }

    override fun createFragment(position: Int): Fragment {
        // Return the tab based on it's position
        return  when (position){
            0 -> HomeFragment()
            1 -> NewsFragment()
            2 -> BusinessFragment()
            3 -> EditorialFragment()
            4 -> ElectionFragment()
            5 -> FeatureFragment()
            6 -> Life_Style_Fragment()
            7 -> OpinionFragment()
            8 -> SportFragment()
            else -> HomeFragment()
        }
    }

}