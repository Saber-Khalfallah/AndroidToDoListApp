package com.example.todolistapp

import java.io.Serializable

data class Task(
    var id: String? = null,
    var title: String = "",
    var description: String = "",
    var priority: Int = 0,  // 1 = Low, 2 = Medium, 3 = High
    var deadline: String = "",
    var emailNotification: Boolean = false,
    var userId: String? = null,
    var completed: Boolean = false,
    var timestamp: Long = 0
) : Serializable
