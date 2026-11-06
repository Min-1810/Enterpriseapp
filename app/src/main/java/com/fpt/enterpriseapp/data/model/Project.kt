package com.fpt.enterpriseapp.data.model

data class Project(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val startDate: Long = System.currentTimeMillis(),
    val deadline: Long = 0L,
    val managerId: String = "",
    val managerName: String = "",
    val progress: Int = 0,
    val status: String = "ACTIVE",
    val contractId: String = "",
    val customerId: String = "",
    val customerName: String = ""
)