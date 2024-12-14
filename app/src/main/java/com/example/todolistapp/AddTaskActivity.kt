package com.example.todolistapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class AddTaskActivity : AppCompatActivity() {

    private lateinit var etTaskTitle: EditText
    private lateinit var etTaskDescription: EditText
    private lateinit var spinnerPriority: Spinner
    private lateinit var btnPickDeadline: Button
    private lateinit var checkBoxEmailNotification: CheckBox
    private lateinit var btnSaveTask: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private var selectedDeadline: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://todolistapp-259e9-default-rtdb.europe-west1.firebasedatabase.app/").reference

        // Initialize views
        etTaskTitle = findViewById(R.id.etTaskTitle)
        etTaskDescription = findViewById(R.id.etTaskDescription)
        spinnerPriority = findViewById(R.id.spinnerPriority)
        btnPickDeadline = findViewById(R.id.btnPickDeadline)
        checkBoxEmailNotification = findViewById(R.id.checkBoxEmailNotification)
        btnSaveTask = findViewById(R.id.btnSaveTask)

        // Set up priority spinner
        val priorities = arrayOf("Low", "Medium", "High")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        spinnerPriority.adapter = adapter

        // Set up deadline picker
        btnPickDeadline.setOnClickListener {
            pickDeadline()
        }

        // Save task
        btnSaveTask.setOnClickListener {
            saveTask()
        }
    }

    private fun pickDeadline() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            TimePickerDialog(this, { _, hourOfDay, minute ->
                calendar.set(year, month, dayOfMonth, hourOfDay, minute)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                selectedDeadline = dateFormat.format(calendar.time)
                btnPickDeadline.text = selectedDeadline
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun saveTask() {
        val title = etTaskTitle.text.toString()
        val description = etTaskDescription.text.toString()
        val priority = spinnerPriority.selectedItem.toString()
        val emailNotification = checkBoxEmailNotification.isChecked

        if (title.isEmpty() || description.isEmpty() || selectedDeadline == null) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Get a unique task ID and create the Task object
            val taskId = database.child("tasks").child(userId).push().key

            // Create the task with additional fields: userId, isCompleted, and timestamp
            val task = Task(
                id = taskId ?: "",  // Ensure the task has a non-null ID
                title = title,
                description = description,
                priority = priority,
                deadline = selectedDeadline!!,
                emailNotification = emailNotification,
                userId = userId,         // Link the task to the current user
                completed = false,     // Set isCompleted to false for ongoing tasks
                timestamp = System.currentTimeMillis()  // Add timestamp for sorting
            )

            // Save the task to Firebase Realtime Database
            if (taskId != null) {
                database.child("tasks").child(userId).child(taskId).setValue(task)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this, "Task saved successfully", Toast.LENGTH_SHORT).show()
                            finish() // Close the activity
                        } else {
                            Toast.makeText(this, "Failed to save task", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }
}
