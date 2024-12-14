package com.example.todolistapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class EditTaskActivity : AppCompatActivity() {

    private lateinit var etTaskTitle: EditText
    private lateinit var etTaskDescription: EditText
    private lateinit var spinnerPriority: Spinner
    private lateinit var btnPickDeadline: Button
    private lateinit var checkBoxEmailNotification: CheckBox
    private lateinit var btnSaveTask: Button

    private var selectedDeadline: String? = null
    private lateinit var selectedPriority: String
    private lateinit var taskId: String

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

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

        // Retrieve the task data from the Intent
        taskId = intent.getStringExtra("taskId") ?: ""
        val taskTitle = intent.getStringExtra("taskTitle") ?: ""
        val taskDescription = intent.getStringExtra("taskDescription") ?: ""
        val taskPriority = intent.getStringExtra("taskPriority") ?: ""
        val taskDeadline = intent.getStringExtra("taskDeadline") ?: ""
        val taskEmailNotification = intent.getBooleanExtra("taskEmailNotification", false)

        // Populate the fields with the existing data
        etTaskTitle.setText(taskTitle)  // Set task title
        etTaskDescription.setText(taskDescription)  // Set task description

        // Set up the Spinner with the correct priority
        val priorities = arrayOf("Low", "Medium", "High")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPriority.adapter = spinnerAdapter

        // Set the correct priority selection
        val priorityIndex = priorities.indexOf(taskPriority)
        if (priorityIndex != -1) {
            spinnerPriority.setSelection(priorityIndex)
            selectedPriority = taskPriority // Set the initial priority
        } else {
            selectedPriority = "Low" // Default if not found
        }

        // Set the deadline button text
        btnPickDeadline.text = taskDeadline
        checkBoxEmailNotification.isChecked = taskEmailNotification

        // Handle item selection on the spinner (priority)
        spinnerPriority.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Update the selected priority when a new item is selected
                selectedPriority = parentView?.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Optionally handle this case, though we should always have a selection
            }
        }

        // Set up the date-time picker for deadline selection
        btnPickDeadline.setOnClickListener {
            // Open DatePicker Dialog first to choose the date
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    // Set the calendar to the selected date
                    calendar.set(year, month, dayOfMonth)

                    // Now open the TimePicker Dialog after date is selected
                    val timePickerDialog = TimePickerDialog(
                        this,
                        { _, hourOfDay, minute ->
                            // Set the selected time
                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            calendar.set(Calendar.MINUTE, minute)

                            // Format the selected date and time
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            selectedDeadline = dateFormat.format(calendar.time)

                            // Update the button text to show the selected date and time
                            btnPickDeadline.text = selectedDeadline
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    )
                    timePickerDialog.show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // Set up the save task button
        btnSaveTask.setOnClickListener {
            saveTask()
        }
    }

    private fun saveTask() {
        val title = etTaskTitle.text.toString()
        val description = etTaskDescription.text.toString()
        val priority = selectedPriority
        val emailNotification = checkBoxEmailNotification.isChecked
        val deadline = selectedDeadline ?: btnPickDeadline.text.toString()

        if (title.isEmpty() || description.isEmpty() || deadline.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Create the updated task object
        val updatedTask = Task(
            id = taskId,
            title = title,
            description = description,
            priority = priority,
            deadline = deadline,
            emailNotification = emailNotification,
            completed = false,
            timestamp = System.currentTimeMillis()
        )

        val userId = auth.currentUser?.uid ?: return

        // Update the task in Firebase
        database.child("tasks")
            .child(userId)
            .child(taskId)
            .setValue(updatedTask)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show()

                    // Refresh UI by setting the updated task's title and other details
                    etTaskTitle.setText(title)
                    etTaskDescription.setText(description)
                    spinnerPriority.setSelection(getPriorityIndex(priority))
                    btnPickDeadline.text = deadline
                    checkBoxEmailNotification.isChecked = emailNotification

                    finish() // Close the activity
                } else {
                    Toast.makeText(this, "Failed to update task", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Helper function to get priority index
    private fun getPriorityIndex(priority: String): Int {
        return when (priority) {
            "Medium" -> 1
            "High" -> 2
            else -> 0  // Default to Low
        }
    }
}
