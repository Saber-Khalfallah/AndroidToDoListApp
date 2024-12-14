package com.example.todolistapp
import java.io.Serializable

data class Task(
    var id: String = "", // Task ID
    var title: String = "",
    var description: String = "",
    var priority: String = "",
    var deadline: String = "",
    var emailNotification: Boolean = false,
    var completed: Boolean = false,  // Make sure it's 'completed' (not 'isCompleted')
    var timestamp: Long = 0L,
    var userId: String = ""
) : Serializable