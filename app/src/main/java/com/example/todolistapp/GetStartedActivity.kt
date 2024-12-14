package com.example.todolistapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.todolistapp.databinding.ActivityGetStartedBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth

class GetStartedActivity : AppCompatActivity() {
    private var binding:ActivityGetStartedBinding? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetStartedBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding?.root)
        binding?.cvGetStarted?.setOnClickListener {
            startActivity(Intent(this,SignInActivity :: class.java))
            finish()
        }
        val auth= Firebase.auth
        if (auth.currentUser!= null)
        {
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }
}