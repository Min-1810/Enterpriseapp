package com.fpt.enterpriseapp.data.model

data class Attendance(
    val id: String = "",
    val employeeId: String = "",
    val employeeName: String = "",
    val checkInTime: Long = 0L,
    val checkOutTime: Long = 0L,
    val date: String = "",
    val totalHours: Double = 0.0
)