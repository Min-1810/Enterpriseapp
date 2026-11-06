package com.fpt.enterpriseapp.utils

object RoleManager {
    fun canManageEmployees(role: String) = role == Constants.ROLE_ADMIN
    fun canManageProjects(role: String) =
        role == Constants.ROLE_ADMIN || role == Constants.ROLE_MANAGER
    fun canViewRevenue(role: String) =
        role == Constants.ROLE_ADMIN || role == Constants.ROLE_MANAGER
    fun canManageSales(role: String) =
        role == Constants.ROLE_ADMIN || role == Constants.ROLE_MANAGER
}