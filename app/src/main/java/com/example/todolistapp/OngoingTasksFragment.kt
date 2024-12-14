// OngoingTasksFragment.kt
package com.example.todolistapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolistapp.databinding.FragmentOngoingTasksBinding
import com.google.firebase.database.*

class OngoingTasksFragment : Fragment() {

    private var binding: FragmentOngoingTasksBinding? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var database: DatabaseReference

    private var userId: String? = null
    private var isCompleted: Boolean = false

    // Reference to the ChildEventListener to manage its lifecycle
    private var childEventListener: ChildEventListener? = null

    companion object {
        private const val ARG_USER_ID = "user_id"
        private const val ARG_COMPLETED = "completed"

        // Factory method to create a new instance of this fragment
        fun newInstance(userId: String?, completed: Boolean): OngoingTasksFragment {
            val fragment = OngoingTasksFragment()
            val args = Bundle()
            args.putString(ARG_USER_ID, userId)
            args.putBoolean(ARG_COMPLETED, completed)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for the fragment
        binding = FragmentOngoingTasksBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve the userId and completion status from arguments
        userId = arguments?.getString(ARG_USER_ID)
        isCompleted = arguments?.getBoolean(ARG_COMPLETED) ?: false

        // Initialize Firebase reference
        database = FirebaseDatabase.getInstance("https://todolistapp-259e9-default-rtdb.europe-west1.firebasedatabase.app/").reference

        // Set up RecyclerView
        recyclerView = binding?.recyclerView ?: return
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize TaskAdapter with callbacks for each button action
        taskAdapter = TaskAdapter(
            onCompleteClick = { task -> completeTask(task) },
            onEditClick = { task -> editTask(task) },
            onDeleteClick = { task -> deleteTask(task) }
        )
        recyclerView.adapter = taskAdapter

        // Fetch and display the tasks
        fetchTasks()
    }

    override fun onResume() {
        super.onResume()
        // Clear existing tasks to avoid duplicates
        taskAdapter.clearTasks()
        // Re-fetch tasks from the database
        fetchTasks()
    }

    override fun onPause() {
        super.onPause()
        // Remove the listener to prevent memory leaks
        removeTasksListener()
    }

    private fun fetchTasks() {
        userId?.let { id ->
            // Create a query to get tasks for the current user
            val tasksRef = database.child("tasks").child(id)

            // Filter tasks based on their completion status
            val query: Query = tasksRef.orderByChild("completed").equalTo(isCompleted)

            // Initialize the ChildEventListener
            childEventListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val task = snapshot.getValue(Task::class.java)
                    task?.let {
                        it.id = snapshot.key ?: ""  // Ensure id is non-null
                        Log.d("Firebase", "Task added: ${it.title} - ${it.id}")  // Log task details
                        taskAdapter.addTask(it)  // Add the task to the RecyclerView
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val task = snapshot.getValue(Task::class.java)
                    task?.let {
                        it.id = snapshot.key ?: ""  // Ensure id is non-null
                        Log.d("Firebase", "Task updated: ${it.title} - ${it.id}")  // Log updated task
                        taskAdapter.updateTask(it)  // Update the task if it changes
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val task = snapshot.getValue(Task::class.java)
                    task?.let {
                        it.id = snapshot.key ?: ""  // Ensure id is non-null
                        Log.d("Firebase", "Task removed: ${it.title} - ${it.id}")  // Log removed task
                        taskAdapter.removeTask(it)  // Remove the task if it is deleted
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
        } ?: run {
            // Handle null userId
            Toast.makeText(requireContext(), "User ID not found", Toast.LENGTH_SHORT).show()
            Log.e("OngoingTasksFragment", "User ID is null")
        }
    }

    private fun removeTasksListener() {
        userId?.let { id ->
            childEventListener?.let { listener ->
                database.child("tasks").child(id).removeEventListener(listener)
                childEventListener = null
            }
        }
    }

    private fun completeTask(task: Task) {
        // Ensure task.id is non-null before using it
        val taskId = task.id ?: run {
            Toast.makeText(requireContext(), "Task ID is null", Toast.LENGTH_SHORT).show()
            return
        }

        task.completed = true
        userId?.let { userId ->
            // Use task.id here, since we've ensured it's non-null
            database.child("tasks").child(userId).child(taskId).setValue(task)
                .addOnSuccessListener {
                    // Notify the adapter and refresh UI
                    taskAdapter.updateTask(task)  // Update RecyclerView after completion
                    Log.d("Firebase", "Task completed: ${task.title}")
                    Toast.makeText(requireContext(), "Task marked as completed", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    // Handle failure (e.g., show a Toast message)
                    Log.e("Firebase", "Failed to complete task: ${task.title}")
                    Toast.makeText(requireContext(), "Failed to mark task as completed", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(requireContext(), "User ID is null", Toast.LENGTH_SHORT).show()
            Log.e("OngoingTasksFragment", "User ID is null")
        }
    }

    private fun editTask(task: Task) {
        // Open an activity to edit the task
        val intent = Intent(requireContext(), EditTaskActivity::class.java)
        intent.putExtra("task", task)
        startActivity(intent)
    }

    private fun deleteTask(task: Task) {
        // Ensure task.id is non-null before using it
        val taskId = task.id ?: run {
            Toast.makeText(requireContext(), "Task ID is null", Toast.LENGTH_SHORT).show()
            return
        }

        userId?.let { userId ->
            // Use task.id here, since we've ensured it's non-null
            database.child("tasks").child(userId).child(taskId).removeValue()
                .addOnSuccessListener {
                    // Notify adapter and refresh UI
                    taskAdapter.removeTask(task)  // Remove task from RecyclerView
                    Log.d("Firebase", "Task deleted: ${task.title}")
                    Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    // Handle failure (e.g., show a Toast message)
                    Log.e("Firebase", "Failed to delete task: ${task.title}")
                    Toast.makeText(requireContext(), "Failed to delete task", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(requireContext(), "User ID is null", Toast.LENGTH_SHORT).show()
            Log.e("OngoingTasksFragment", "User ID is null")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove the listener to prevent memory leaks
        removeTasksListener()
        binding = null
    }
}
