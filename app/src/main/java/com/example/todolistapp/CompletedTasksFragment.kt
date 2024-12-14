package com.example.todolistapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolistapp.databinding.FragmentCompletedTasksBinding
import com.google.firebase.database.*

class CompletedTasksFragment : Fragment() {

    private var binding: FragmentCompletedTasksBinding? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var database: DatabaseReference

    private lateinit var userId: String
    private var isCompleted: Boolean = true

    // Reference to the ChildEventListener to manage its lifecycle
    private var childEventListener: ChildEventListener? = null

    companion object {
        private const val ARG_USER_ID = "user_id"
        private const val ARG_IS_COMPLETED = "is_completed"

        // Factory method to create a new instance of this fragment
        fun newInstance(userId: String, isCompleted: Boolean): CompletedTasksFragment {
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
        // Inflate the layout for the fragment using View Binding
        binding = FragmentCompletedTasksBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve the userId and completion status from arguments
        arguments?.let {
            userId = it.getString(ARG_USER_ID) ?: ""
            isCompleted = it.getBoolean(ARG_IS_COMPLETED, true)
        } ?: run {
            // Handle the case where arguments are missing
            Log.e("CompletedTasksFragment", "Arguments are missing")
            // Optionally, redirect or show an error message
            Toast.makeText(requireContext(), "Invalid data", Toast.LENGTH_SHORT).show()
            return
        }

        if (userId.isEmpty()) {
            // Handle the case where userId is empty
            Log.e("CompletedTasksFragment", "User ID is empty")
            // Optionally, redirect or show an error message
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Initialize Firebase reference
        database = FirebaseDatabase.getInstance("https://todolistapp-259e9-default-rtdb.europe-west1.firebasedatabase.app/").reference

        // Set up RecyclerView
        recyclerView = binding?.recyclerViewCompletedTasks ?: return
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize empty view
        emptyView = binding?.tvEmptyCompletedTasks ?: return

        // Initialize the adapter with the necessary lambdas for actions
        taskAdapter = TaskAdapter(
            onCompleteClick = { task ->
                // Typically not needed for completed tasks
                // Optionally, implement if you want to allow unmarking as completed
                // For now, we'll leave it empty or repurpose it
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

        // Observe adapter data changes to toggle empty view
        taskAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                toggleEmptyView()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                toggleEmptyView()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                toggleEmptyView()
            }
        })

        // Fetch and display completed tasks
        fetchCompletedTasks()
    }

    override fun onResume() {
        super.onResume()
        // Clear existing tasks to avoid duplicates
        taskAdapter.clearTasks()
        // Re-fetch tasks from the database
        fetchCompletedTasks()
    }

    override fun onPause() {
        super.onPause()
        // Remove the listener to prevent memory leaks
        removeTasksListener()
    }

    private fun fetchCompletedTasks() {
        // Ensure userId is initialized and not empty
        if (::userId.isInitialized && userId.isNotEmpty()) {
            // Create a query to get tasks for the current user
            val tasksRef = database.child("tasks").child(userId)

            // Filter tasks based on their completion status
            val query: Query = tasksRef.orderByChild("completed").equalTo(isCompleted)

            // Initialize the ChildEventListener
            childEventListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val task = snapshot.getValue(Task::class.java)
                    task?.let {
                        it.id = snapshot.key ?: ""  // Ensure task.id is populated correctly
                        taskAdapter.addTask(it)  // Add the task to the RecyclerView
                        Log.d("Firebase", "Task added: ${it.title}")  // Log the task title
                    } ?: run {
                        Log.d("Firebase", "Task is null")
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val task = snapshot.getValue(Task::class.java)
                    task?.let {
                        it.id = snapshot.key ?: ""  // Ensure task.id is populated correctly
                        taskAdapter.updateTask(it)  // Update the task if it changes
                        Log.d("Firebase", "Task updated: ${it.title}")  // Log the updated task
                    } ?: run {
                        Log.d("Firebase", "Task is null")
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val task = snapshot.getValue(Task::class.java)
                    task?.let {
                        it.id = snapshot.key ?: ""  // Ensure task.id is populated correctly
                        taskAdapter.removeTask(it)  // Remove the task if it is deleted
                        Log.d("Firebase", "Task removed: ${it.title}")  // Log the removed task
                    } ?: run {
                        Log.d("Firebase", "Task is null")
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    // Not used in this context
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error fetching tasks: ${error.message}")
                    Toast.makeText(requireContext(), "Failed to load tasks", Toast.LENGTH_SHORT).show()
                }
            }

            // Add the listener to the query
            query.addChildEventListener(childEventListener!!)
        } else {
            Log.e("CompletedTasksFragment", "User ID is not initialized or empty")
            Toast.makeText(requireContext(), "User ID is invalid", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to mark a task as completed
    private fun markTaskAsCompleted(task: Task) {
        // Since this fragment displays completed tasks, marking them as completed again is redundant
        // You can choose to remove this function or repurpose it as needed
    }

    // Function to edit a task
    private fun editTask(task: Task) {
        // Open an activity to edit the task
        val intent = Intent(requireContext(), EditTaskActivity::class.java)
        intent.putExtra("task", task) // Pass task object to the EditTaskActivity
        startActivity(intent)
    }

    // Function to delete a task
    private fun deleteTask(task: Task) {
        // Ensure task.id is non-null before using it
        val taskId = task.id
        if (taskId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Task ID is invalid", Toast.LENGTH_SHORT).show()
            Log.e("CompletedTasksFragment", "Task ID is null or empty")
            return
        }

        // Delete the task from the Firebase database
        database.child("tasks").child(userId).child(taskId).removeValue()
            .addOnSuccessListener {
                taskAdapter.removeTask(task)  // Remove task from RecyclerView
                Log.d("Firebase", "Task deleted: ${task.title}")
                Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace() // Log failure
                Toast.makeText(requireContext(), "Failed to delete task", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeTasksListener() {
        childEventListener?.let { listener ->
            database.child("tasks").child(userId).removeEventListener(listener)
            childEventListener = null
            Log.d("CompletedTasksFragment", "Removed ChildEventListener")
        }
    }

    private fun toggleEmptyView() {
        if (taskAdapter.itemCount == 0) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove the listener to prevent memory leaks
        removeTasksListener()
        binding = null
    }
}
