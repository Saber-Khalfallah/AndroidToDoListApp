package com.example.todolistapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.todolistapp.databinding.ActivityEditTaskBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class EditTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditTaskBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private var selectedDeadline: String? = null
    private var selectedPriority: Int = 1  // Default to Low
    private lateinit var task: Task

    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize View Binding
        binding = ActivityEditTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://todolistapp-259e9-default-rtdb.europe-west1.firebasedatabase.app/").reference

        // Retrieve the Task object from the Intent
        task = intent.getSerializableExtra("task") as? Task ?: run {
            Toast.makeText(this, "Failed to load task data", Toast.LENGTH_SHORT).show()
            Log.e("EditTaskActivity", "Task data is missing in Intent")
            finish()
            return
        }

        // Populate the fields with the existing task data
        populateFields()

        // Set up priority spinner
        setupPrioritySpinner()

        // Set up deadline picker
        binding.btnPickDeadline.setOnClickListener {
            pickDeadline()
        }

        // Set up save task button
        binding.btnSaveTask.setOnClickListener {
            saveTask()
        }
        binding.btnCancelTask.setOnClickListener {
            cancelEdit()
        }
    }

    private fun populateFields() {
        binding.etTaskTitle.setText(task.title)
        binding.etTaskDescription.setText(task.description)
        binding.checkBoxEmailNotification.isChecked = task.emailNotification
        binding.btnPickDeadline.text = task.deadline
        selectedDeadline = task.deadline

        // Initialize calendar with existing deadline
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = dateFormat.parse(task.deadline)
            date?.let {
                calendar.time = it
            }
        } catch (e: Exception) {
            Log.e("EditTaskActivity", "Error parsing deadline", e)
        }
    }

    private fun setupPrioritySpinner() {
        val priorities = arrayOf("Low", "Medium", "High")
        val priorityValues = arrayOf(1, 2, 3) // 1 = Low, 2 = Medium, 3 = High
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPriority.adapter = adapter

        // Set the spinner selection based on the task's priority
        val priorityIndex = priorityValues.indexOf(task.priority)
        if (priorityIndex != -1) {
            binding.spinnerPriority.setSelection(priorityIndex)
            selectedPriority = task.priority
        } else {
            binding.spinnerPriority.setSelection(0) // Default to Low
            selectedPriority = 1
        }

        // Handle item selection on the spinner (priority)
        binding.spinnerPriority.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedPriority = priorityValues[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Default to Low if nothing is selected
                selectedPriority = 1
            }
        }
    }

    private fun pickDeadline() {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                // Set the calendar to the selected date
                calendar.set(year, month, dayOfMonth)
                // Now open the TimePicker Dialog after date is selected
                TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        // Set the selected time
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        // Format the selected date and time
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        selectedDeadline = dateFormat.format(calendar.time)
                        // Update the button text to show the selected date and time
                        binding.btnPickDeadline.text = selectedDeadline
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveTask() {
        val title = binding.etTaskTitle.text.toString().trim()
        val description = binding.etTaskDescription.text.toString().trim()
        val emailNotification = binding.checkBoxEmailNotification.isChecked
        val deadline = selectedDeadline

        if (title.isEmpty() || description.isEmpty() || deadline.isNullOrEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid
        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            Log.e("EditTaskActivity", "User ID is null or empty")
            return
        }

        // Create the updated task object
        val updatedTask = Task(
            id = task.id,
            title = title,
            description = description,
            priority = selectedPriority,
            deadline = deadline,
            emailNotification = emailNotification,
            userId = userId,
            completed = task.completed, // Preserve the completed status
            timestamp = task.timestamp // Preserve the original timestamp
        )

        // Update the task in Firebase
        database.child("tasks")
            .child(userId)
            .child(task.id!!)
            .setValue(updatedTask)
            .addOnCompleteListener { taskResult ->
                if (taskResult.isSuccessful) {
                    Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show()
                    finish() // Close the activity
                } else {
                    Toast.makeText(this, "Failed to update task", Toast.LENGTH_SHORT).show()
                    Log.e("EditTaskActivity", "Failed to update task", taskResult.exception)
                }
            }
    }
    private fun cancelEdit() {
        finish() // Close the activity without saving changes
    }
}
