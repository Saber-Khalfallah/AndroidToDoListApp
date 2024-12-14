package com.example.todolistapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.todolistapp.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    // Make binding non-nullable after initialization
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String  // Non-nullable userId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create Notification Channel
        NotificationUtils.createNotificationChannel(this)
        // Initialize binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Get the user ID from Firebase Authentication
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // If not, redirect to the GetStartedActivity (login or registration screen)
            startActivity(Intent(this, GetStartedActivity::class.java))
            finish()
            return
        }
        userId = currentUser.uid  // Safe to assign since currentUser is not null

        // Setting up TabLayout and ViewPager2
        val tabTitles = arrayOf("Ongoing Tasks", "Completed Tasks")
        val viewPagerAdapter = ViewPagerAdapter(this, userId)

        binding.viewPager.adapter = viewPagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        // Button listeners
        binding.addTaskButton.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            startActivity(intent)
        }

        binding.btnShowStatistics.setOnClickListener {
            val intent = Intent(this, StatisticsActivity::class.java)
            startActivity(intent)
        }

        binding.btnSignOut.setOnClickListener {
            if (auth.currentUser != null) {
                auth.signOut()
                startActivity(Intent(this, GetStartedActivity::class.java))
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // No need to set binding to null since it's non-nullable
    }
}
