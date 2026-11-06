package com.fpt.enterpriseapp.data.model

data class AppNotification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val targetUserId: String = "",
    val createdDate: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)