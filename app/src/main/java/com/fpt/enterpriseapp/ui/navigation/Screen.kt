package com.fpt.enterpriseapp.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Main  : Screen("main")

    // Routes con bên trong Main (dashboard, employee, project, sales, profile)
    // Được xử lý bằng bottom navigation nội bộ, không cần route riêng

    // Route cho màn hình Add/Edit (mở full-screen, không bottom nav)
    object EmployeeAddEdit : Screen("employee_add_edit?employeeId={employeeId}") {
        fun createRoute(employeeId: String? = null) =
            "employee_add_edit?employeeId=${employeeId ?: ""}"
    }
    object Attendance : Screen("attendance")
    object ProjectAddEdit : Screen("project_add_edit?projectId={projectId}") {
        fun createRoute(projectId: String? = null) =
            "project_add_edit?projectId=${projectId ?: ""}"
    }
    object TaskList : Screen("task_list?projectId={projectId}") {
        fun createRoute(projectId: String) = "task_list?projectId=$projectId"
    }
    object TaskAddEdit : Screen("task_add_edit?taskId={taskId}&projectId={projectId}") {
        fun createRoute(taskId: String? = null, projectId: String) =
            "task_add_edit?taskId=${taskId ?: ""}&projectId=$projectId"
    }
    object CustomerAddEdit : Screen("customer_add_edit?customerId={customerId}") {
        fun createRoute(customerId: String? = null) =
            "customer_add_edit?customerId=${customerId ?: ""}"
    }
    object ContractAddEdit : Screen("contract_add_edit?contractId={contractId}") {
        fun createRoute(contractId: String? = null) =
            "contract_add_edit?contractId=${contractId ?: ""}"
    }
    object TransactionAddEdit : Screen("transaction_add_edit?transactionId={transactionId}") {
        fun createRoute(transactionId: String? = null) =
            "transaction_add_edit?transactionId=${transactionId ?: ""}"
    }
}