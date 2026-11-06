package com.fpt.enterpriseapp.data.model

data class Customer(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val company: String = "",
    val address: String = "",
    val assignedEmployeeId: String = "",
    val assignedEmployeeName: String = "",
    val createdDate: Long = System.currentTimeMillis()
)