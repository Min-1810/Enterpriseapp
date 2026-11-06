package com.fpt.enterpriseapp.data.model

data class Task(
    val id: String = "",
    val projectId: String = "",
    val projectName: String = "",
    val title: String = "",
    val description: String = "",
    val assigneeId: String = "",
    val assigneeName: String = "",
    val status: String = "PENDING",
    val priority: String = "MEDIUM",
    val estimatedHours: Double = 0.0,
    val actualHours: Double = 0.0,
    val createdDate: Long = System.currentTimeMillis(),
    val deadline: Long = 0L
)