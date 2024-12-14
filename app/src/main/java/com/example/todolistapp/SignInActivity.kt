package com.example.todolistapp

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.todolistapp.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignInActivity : BaseActivity() {

    private var binding: ActivitySignInBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Register the launcher
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            when (result.resultCode) {
                RESULT_OK -> {
                    // Handle successful result from the intent
                    showToast(this, "Sign-in process started. Please complete the flow.")
                }
                RESULT_CANCELED -> {
                    // User canceled the sign-in process
                    showToast(this, "Sign-in canceled.")
                }
                else -> {
                    // Handle other cases, if any
                    showToast(this, "Sign-in failed. Try again.")
                }
            }
        }

        // Set up listeners
        binding?.btnSignInWithGoogle?.setOnClickListener {
            initiateGoogleSignIn()
        }

        binding?.tvRegister?.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        binding?.tvForgotPassword?.setOnClickListener {
            startActivity(Intent(this, ForgetPasswordActivity::class.java))
        }

        binding?.btnSignIn?.setOnClickListener {
            signInUser()
        }

        // Handle edge-to-edge UI adjustments
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initiateGoogleSignIn() {
        GoogleSignInUtils.doGoogleSignIn(
            context = this,
            scope = CoroutineScope(Dispatchers.Main),
            launcher = googleSignInLauncher
        ) {
            // Handle successful login
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun signInUser() {
        val email = binding?.etSinInEmail?.text.toString()
        val password = binding?.etSinInPassword?.text.toString()
        if (validateForm(email, password)) {
            showProgressBar()
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        showToast(this, "Cannot login currently. Try again later.")
                    }
                    hideProgressBar()
                }
        }
    }

    private fun validateForm(email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding?.tilEmail?.error = "Enter a valid email address"
                false
            }

            TextUtils.isEmpty(password) -> {
                binding?.tilPassword?.error = "Enter password"
                false
            }

            else -> {
                true
            }
        }
    }
}
