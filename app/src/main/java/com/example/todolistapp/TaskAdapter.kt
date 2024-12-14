package com.example.todolistapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private val onCompleteClick: (Task) -> Unit,
    private val onEditClick: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val tasks = mutableListOf<Task>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int = tasks.size

    /**
     * Adds a new task to the adapter.
     */
    fun addTask(task: Task) {
        // Avoid adding duplicate tasks based on unique ID
        if (tasks.none { it.id == task.id }) {
            tasks.add(task)
            notifyItemInserted(tasks.size - 1)
            Log.d("TaskAdapter", "Added task: ${task.title} with ID: ${task.id}")
        }
    }

    /**
     * Updates an existing task in the adapter.
     */
    fun updateTask(task: Task) {
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index != -1) {
            tasks[index] = task
            notifyItemChanged(index)
            Log.d("TaskAdapter", "Updated task: ${task.title} with ID: ${task.id}")
        }
    }

    /**
     * Removes a task from the adapter.
     */
    fun removeTask(task: Task) {
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index != -1) {
            tasks.removeAt(index)
            notifyItemRemoved(index)
            Log.d("TaskAdapter", "Removed task: ${task.title} with ID: ${task.id}")
        }
    }

    /**
     * Clears all tasks from the adapter.
     */
    fun clearTasks() {
        tasks.clear()
        notifyDataSetChanged()
        Log.d("TaskAdapter", "Cleared all tasks")
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

            // Optionally hide the "Complete" button if the task is already completed
            btnComplete.visibility = if (task.completed) View.GONE else View.VISIBLE

            // Handle button clicks for task completion, edit, and deletion

            btnComplete.setOnClickListener {
                onCompleteClick(task)  // Delegate completion action to the fragment
                // Optionally, you can update the UI immediately
                btnComplete.visibility = View.GONE
                Toast.makeText(itemView.context, "Task marked as completed", Toast.LENGTH_SHORT).show()
            }

            btnEdit.setOnClickListener {
                onEditClick(task)  // Delegate edit action to the fragment
            }

            btnDelete.setOnClickListener {
                onDeleteClick(task)  // Delegate delete action to the fragment
                Toast.makeText(itemView.context, "Task deleted", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
