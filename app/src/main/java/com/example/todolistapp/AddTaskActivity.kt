package com.example.todolistapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.todolistapp.databinding.ActivityAddTaskBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class AddTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTaskBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private var selectedDeadline: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize View Binding
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://todolistapp-259e9-default-rtdb.europe-west1.firebasedatabase.app/").reference

        // Set up priority spinner
        val priorities = arrayOf("Low", "Medium", "High")
        val priorityValues = arrayOf(1, 2, 3) // Map "Low" to 1, "Medium" to 2, "High" to 3
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPriority.adapter = adapter

        // Set up deadline picker
        binding.btnPickDeadline.setOnClickListener {
            pickDeadline()
        }

        // Save task
        binding.btnSaveTask.setOnClickListener {
            saveTask(priorityValues[binding.spinnerPriority.selectedItemPosition])
        }
        binding.btnCancelTask.setOnClickListener {
            cancelAddTask()
        }
    }

    private fun pickDeadline() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            TimePickerDialog(this, { _, hourOfDay, minute ->
                calendar.set(year, month, dayOfMonth, hourOfDay, minute)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                selectedDeadline = dateFormat.format(calendar.time)
                binding.btnPickDeadline.text = selectedDeadline
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun saveTask(priority: Int) {
        val title = binding.etTaskTitle.text.toString().trim()
        val description = binding.etTaskDescription.text.toString().trim()
        val emailNotification = binding.checkBoxEmailNotification.isChecked

        if (title.isEmpty() || description.isEmpty() || selectedDeadline == null) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Get a unique task ID and create the Task object
            val taskId = database.child("tasks").child(userId).push().key

            if (taskId == null) {
                Toast.makeText(this, "Failed to generate task ID", Toast.LENGTH_SHORT).show()
                return
            }

            // Create the task with additional fields: userId, isCompleted, and timestamp
            val task = Task(
                id = taskId, // Ensure the task has a non-null ID
                title = title,
                description = description,
                priority = priority, // Int value: 1=Low, 2=Medium, 3=High
                deadline = selectedDeadline!!,
                emailNotification = emailNotification,
                userId = userId,         // Link the task to the current user
                completed = false,     // Set isCompleted to false for ongoing tasks
                timestamp = System.currentTimeMillis()  // Add timestamp for sorting
            )

            // Save the task to Firebase Realtime Database
            database.child("tasks").child(userId).child(taskId).setValue(task)
                .addOnCompleteListener { taskResult ->
                    if (taskResult.isSuccessful) {
                        Toast.makeText(this, "Task saved successfully", Toast.LENGTH_SHORT).show()
                        finish() // Close the activity
                    } else {
                        Toast.makeText(this, "Failed to save task", Toast.LENGTH_SHORT).show()
                        Log.e("AddTaskActivity", "Failed to save task", taskResult.exception)
                    }
                }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            Log.e("AddTaskActivity", "User ID is null")
        }
    }
    private fun cancelAddTask() {
        // Optionally, show a confirmation dialog before cancelling
        finish() // Close the activity without saving
    }
}
