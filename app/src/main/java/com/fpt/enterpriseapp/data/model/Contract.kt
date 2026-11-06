package com.fpt.enterpriseapp.data.model

data class Contract(
    val id: String = "",
    val code: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val value: Double = 0.0,
    val signedDate: Long = System.currentTimeMillis(),
    val endDate: Long = 0L,
    val status: String = "PENDING",
    val note: String = "",
    val assignedEmployeeId: String = "",
    val assignedEmployeeName: String = ""
)