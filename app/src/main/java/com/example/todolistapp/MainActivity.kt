package com.example.todolistapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.todolistapp.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = FirebaseAuth.getInstance()

        // Get the user ID from Firebase Authentication
        val userId = auth.currentUser?.uid

        // Check if the user is logged in
        if (userId == null) {
            // If not, redirect to the GetStartedActivity (login or registration screen)
            startActivity(Intent(this, GetStartedActivity::class.java))
            finish()
            return
        }

        // Setting up TabLayout and ViewPager2
        val tabTitles = arrayOf("Ongoing Tasks", "Completed Tasks")
        val viewPagerAdapter = ViewPagerAdapter(this, userId)

        binding?.viewPager?.adapter = viewPagerAdapter
        TabLayoutMediator(binding?.tabLayout!!, binding?.viewPager!!) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        // Button listeners
        binding?.addTaskButton?.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            startActivity(intent)
        }

        binding?.statsButton?.setOnClickListener {
            val intent = Intent(this, StatsActivity::class.java)
            startActivity(intent)
        }

        binding?.btnSignOut?.setOnClickListener {
            if (auth.currentUser != null) {
                auth.signOut()
                startActivity(Intent(this, GetStartedActivity::class.java))
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
