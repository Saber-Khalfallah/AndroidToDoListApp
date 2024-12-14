package com.example.todolistapp


import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.todolistapp.databinding.ActivityStatisticsBinding
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class StatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatisticsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    // Variables to hold statistics
    private var totalTasks = 0
    private var completedTasks = 0
    private var ongoingTasks = 0
    private var lowPriority = 0
    private var mediumPriority = 0
    private var highPriority = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize View Binding
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            title = "Statistics" // Set the title of the ActionBar
            setDisplayHomeAsUpEnabled(true) // Enable the Up button
            setDisplayShowHomeEnabled(true)
        }
        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://todolistapp-259e9-default-rtdb.europe-west1.firebasedatabase.app/").reference

        // Initialize UI elements (charts and text views)
        setupCharts()

        // Fetch tasks and compute statistics
        fetchTasksAndComputeStatistics()
        // Inside onCreate
        binding.btnBack.setOnClickListener {
            finish() // Closes StatisticsActivity and returns to MainActivity
        }

    }

    private fun setupCharts() {
        // Configure PieChart for Task Completion Status
        binding.pieChartCompletionStatus.apply {
            description.isEnabled = false
            isRotationEnabled = false
            setDrawEntryLabels(false)
            centerText = "Completion Status"
            setCenterTextSize(16f)
        }

        // Configure PieChart for Priority Distribution
        binding.pieChartPriority.apply {
            description.isEnabled = false
            isRotationEnabled = false
            setDrawEntryLabels(false)
            centerText = "Priority Distribution"
            setCenterTextSize(16f)
        }
    }

    private fun fetchTasksAndComputeStatistics() {
        val userId = auth.currentUser?.uid
        if (userId.isNullOrEmpty()) {
            Log.e("StatisticsActivity", "User not authenticated")
            return
        }

        val tasksRef = database.child("tasks").child(userId)
        tasksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Reset statistics
                totalTasks = 0
                completedTasks = 0
                ongoingTasks = 0
                lowPriority = 0
                mediumPriority = 0
                highPriority = 0

                for (taskSnapshot in snapshot.children) {
                    val task = taskSnapshot.getValue(Task::class.java)
                    task?.let {
                        totalTasks++
                        if (it.completed) {
                            completedTasks++
                        } else {
                            ongoingTasks++
                        }

                        when (it.priority) {
                            1 -> lowPriority++
                            2 -> mediumPriority++
                            3 -> highPriority++
                            else -> lowPriority++ // Default to Low if unknown
                        }
                    }
                }

                // Update UI with statistics
                updateUIWithStatistics()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("StatisticsActivity", "Error fetching tasks: ${error.message}")
            }
        })
    }

    private fun updateUIWithStatistics() {
        // Update TextViews
        binding.tvTotalTasks.text = "Total Tasks: $totalTasks"
        binding.tvCompletedTasks.text = "Completed Tasks: $completedTasks"
        binding.tvOngoingTasks.text = "Ongoing Tasks: $ongoingTasks"

        // Update PieChart for Completion Status
        val completionEntries = listOf(
            PieEntry(completedTasks.toFloat(), "Completed"),
            PieEntry(ongoingTasks.toFloat(), "Ongoing")
        )
        val completionDataSet = PieDataSet(completionEntries, "")
        completionDataSet.colors = listOf(
            resources.getColor(R.color.pie_completed),
            resources.getColor(R.color.pie_ongoing)
        )
        val completionData = PieData(completionDataSet)
        completionData.setDrawValues(false)
        binding.pieChartCompletionStatus.data = completionData
        binding.pieChartCompletionStatus.invalidate()

        // Update PieChart for Priority Distribution
        val priorityEntries = listOf(
            PieEntry(lowPriority.toFloat(), "Low"),
            PieEntry(mediumPriority.toFloat(), "Medium"),
            PieEntry(highPriority.toFloat(), "High")
        )
        val priorityDataSet = PieDataSet(priorityEntries, "")
        priorityDataSet.colors = listOf(
            resources.getColor(R.color.pie_low),
            resources.getColor(R.color.pie_medium),
            resources.getColor(R.color.pie_high)
        )
        val priorityData = PieData(priorityDataSet)
        priorityData.setDrawValues(false)
        binding.pieChartPriority.data = priorityData
        binding.pieChartPriority.invalidate()
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // Navigate back to the parent activity
        return true
    }
}
