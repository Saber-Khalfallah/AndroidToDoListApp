package com.example.todolistapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolistapp.databinding.FragmentCompletedTasksBinding
import com.google.firebase.database.*

class CompletedTasksFragment : Fragment() {

    private var binding: FragmentCompletedTasksBinding? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var database: DatabaseReference

    private var userId: String? = null
    private var isCompleted: Boolean = true

    companion object {
        private const val ARG_USER_ID = "user_id"
        private const val ARG_IS_COMPLETED = "is_completed"

        // Factory method to create a new instance of this fragment
        fun newInstance(userId: String?, isCompleted: Boolean): CompletedTasksFragment {
            val fragment = CompletedTasksFragment()
            val args = Bundle()
            args.putString(ARG_USER_ID, userId)
            args.putBoolean(ARG_IS_COMPLETED, isCompleted)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for the fragment
        binding = FragmentCompletedTasksBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the userId and isCompleted status from the fragment arguments
        userId = arguments?.getString(ARG_USER_ID)
        isCompleted = arguments?.getBoolean(ARG_IS_COMPLETED) ?: true

        // Initialize Firebase reference
        database = FirebaseDatabase.getInstance("\"https://todolistapp-259e9-default-rtdb.europe-west1.firebasedatabase.app/").reference

        // Set up RecyclerView
        recyclerView = binding?.recyclerViewCompletedTasks ?: return
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize the adapter with the necessary lambdas for actions
        taskAdapter = TaskAdapter(
            onCompleteClick = { task ->
                // Handle task completion logic here
                markTaskAsCompleted(task)
            },
            onEditClick = { task ->
                // Handle task editing logic here
                editTask(task)
            },
            onDeleteClick = { task ->
                // Handle task deletion logic here
                deleteTask(task)
            }
        )

        recyclerView.adapter = taskAdapter

        // Fetch and display completed tasks
        fetchCompletedTasks()
    }

    private fun fetchCompletedTasks() {
        userId?.let { id ->
            // Create a query to get tasks for the current user
            val tasksRef = database.child("tasks").child(id)

            // Filter tasks based on their completion status (use 'completed' field)
            val query: Query = tasksRef.orderByChild("completed").equalTo(true)

            // Use a ChildEventListener to listen for changes in the database
            query.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val task = snapshot.getValue(Task::class.java)
                    if (task != null) {
                        task.id = snapshot.key ?: ""  // Ensure task.id is populated correctly
                        taskAdapter.addTask(task)  // Add the task to the RecyclerView
                        Log.d("Firebase", "Task added: ${task.title}")  // Log the task title
                    } else {
                        Log.d("Firebase", "Task is null")  // Log if the task is null
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val task = snapshot.getValue(Task::class.java)
                    if (task != null) {
                        task.id = snapshot.key ?: ""  // Ensure task.id is populated correctly
                        taskAdapter.updateTask(task)  // Update the task if it changes
                        Log.d("Firebase", "Task updated: ${task.title}")  // Log the updated task
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val task = snapshot.getValue(Task::class.java)
                    if (task != null) {
                        task.id = snapshot.key ?: ""  // Ensure task.id is populated correctly
                        taskAdapter.removeTask(task)  // Remove the task if it is deleted
                        Log.d("Firebase", "Task removed: ${task.title}")  // Log the removed task
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error fetching tasks: ${error.message}")
                }
            })
        }
    }

    // Function to mark a task as completed
    private fun markTaskAsCompleted(task: Task) {
        // Set task as completed
        task.completed = true
        task.id?.let { taskId ->
            database.child("tasks").child(userId!!).child(taskId).setValue(task)
                .addOnSuccessListener {
                    taskAdapter.updateTask(task)  // Update RecyclerView after marking as completed
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace() // Log failure or show a toast
                }
        }
    }

    // Function to edit a task
    private fun editTask(task: Task) {
        // Implement your task editing logic here
        // You could launch a new activity or fragment to allow the user to edit the task
        val intent = Intent(requireContext(), EditTaskActivity::class.java)
        intent.putExtra("task", task) // Pass task object to the EditTaskActivity
        startActivity(intent)
    }

    // Function to delete a task
    private fun deleteTask(task: Task) {
        task.id?.let { taskId ->
            // Delete the task from the Firebase database
            database.child("tasks").child(userId!!).child(taskId).removeValue()
                .addOnSuccessListener {
                    taskAdapter.removeTask(task)  // Remove task from RecyclerView
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace() // Log failure or show a toast
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
