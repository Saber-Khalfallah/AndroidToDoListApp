package com.example.todolistapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class TaskAdapter(
    private val onCompleteClick: (Task) -> Unit,
    private val onEditClick: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val tasks = mutableListOf<Task>()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int = tasks.size

    fun addTask(task: Task) {
        tasks.add(task)
        notifyItemInserted(tasks.size - 1)
    }

    fun updateTask(task: Task) {
        val index = tasks.indexOfFirst { it.title == task.title }
        if (index != -1) {
            tasks[index] = task
            notifyItemChanged(index)
        }
    }

    fun removeTask(task: Task) {
        val index = tasks.indexOfFirst { it.title == task.title }
        if (index != -1) {
            tasks.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskTitle: TextView = itemView.findViewById(R.id.taskTitle)
        private val taskDescription: TextView = itemView.findViewById(R.id.taskDescription)
        private val taskPriority: TextView = itemView.findViewById(R.id.taskPriority)
        private val taskDeadline: TextView = itemView.findViewById(R.id.taskDeadline)
        private val btnComplete: Button = itemView.findViewById(R.id.btnComplete)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(task: Task) {
            taskTitle.text = task.title
            taskDescription.text = task.description
            taskPriority.text = "Priority: ${task.priority}"
            taskDeadline.text = "Deadline: ${task.deadline}"

            // Handle button clicks for task completion, edit, and deletion

            btnComplete.setOnClickListener {
                // Mark the task as completed in the UI
                task.completed = true
                onCompleteClick(task)  // Trigger the complete action (e.g., update in Firebase)
                // Show confirmation toast
                Toast.makeText(itemView.context, "Task marked as completed", Toast.LENGTH_SHORT).show()
            }

            btnEdit.setOnClickListener {
                val intent = Intent(it.context, EditTaskActivity::class.java)
                intent.putExtra("taskId", task.id)
                intent.putExtra("taskTitle", task.title)
                intent.putExtra("taskDescription", task.description)
                intent.putExtra("taskPriority", task.priority)
                intent.putExtra("taskDeadline", task.deadline)
                intent.putExtra("taskEmailNotification", task.emailNotification)
                it.context.startActivity(intent)
            }

            btnDelete.setOnClickListener {
                onDeleteClick(task)  // Trigger the delete action (remove from the list)
                removeTask(task)  // Remove from the local list
                deleteTaskFromFirebase(task)  // Also delete from Firebase
                // Show confirmation toast
                Toast.makeText(itemView.context, "Task deleted", Toast.LENGTH_SHORT).show()
            }
        }

        private fun updateTaskStatus(task: Task) {
            val userId = "current_user_id"  // Replace with actual user ID logic
            val taskId = task.id  // Assuming task has an 'id' field for its unique identifier in Firebase

            // Update the task's completion status in Firebase
            database.child("tasks").child(userId).child(taskId).child("isCompleted").setValue(true)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        // Task status updated successfully
                    } else {
                        // Handle error
                    }
                }
        }

        private fun deleteTaskFromFirebase(task: Task) {
            val userId = "current_user_id"  // Replace with actual user ID logic
            val taskId = task.id  // Assuming task has an 'id' field for its unique identifier in Firebase

            // Delete the task from Firebase
            database.child("tasks").child(userId).child(taskId).removeValue()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        // Task deleted successfully
                    } else {
                        // Handle error
                    }
                }
        }
    }
}
