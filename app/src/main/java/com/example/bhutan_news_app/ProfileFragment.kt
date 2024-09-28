package com.example.bhutan_news_app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Get user details from SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("user_pref", AppCompatActivity.MODE_PRIVATE)
        val userName = sharedPref.getString("user_name", "Guest")
        val userEmail = sharedPref.getString("user_email", "Not Available")
        val profileImageUrl = sharedPref.getString("profile_image_url", "")


        // Update the UI with the user’s name and email
        val userNameTextView = view.findViewById<TextView>(R.id.userName)
        val userEmailTextView = view.findViewById<TextView>(R.id.userEmail)
        val profileImageView = view.findViewById<ImageView>(R.id.profileImage)
        val likeNewsTextView = view.findViewById<TextView>(R.id.likePage)
        userNameTextView.text = userName
        userEmailTextView.text = userEmail

        // Load the profile image dynamically (if a valid URL is provided)
        if (profileImageUrl!!.isNotEmpty()) {
            Picasso.get()
                .load(profileImageUrl)
                .placeholder(R.drawable.ic_profile) // Default placeholder image
                .error(R.drawable.ic_profile)       // Error image if loading fails
                .into(profileImageView)
        } else {
            // If no image URL is provided, you can set a default image
            profileImageView.setImageResource(R.drawable.ic_profile)
        }
        // Log Out logic
        val logOutTextView = view.findViewById<TextView>(R.id.logOutTextView)
        logOutTextView.setOnClickListener {
            logOut()
        }
        //Liked page logic
        likeNewsTextView.setOnClickListener{
            val intent = Intent(requireContext(),LikedActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun logOut() {
        // Clear SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("user_pref", AppCompatActivity.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.clear()
        editor.apply()
        // Navigate back to LoginActivity
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish() // Close the current activity
    }
}
