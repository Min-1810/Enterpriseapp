package com.fpt.enterpriseapp.data.model

data class Employee(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val department: String = "",
    val role: String = "EMPLOYEE",
    val avatarUrl: String = "",
    val phone: String = "",
    val position: String = "",
    val joinDate: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)