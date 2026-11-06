package com.fpt.enterpriseapp.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.fpt.enterpriseapp.data.model.Employee
import com.fpt.enterpriseapp.ui.dashboard.DashboardScreen
import com.fpt.enterpriseapp.ui.employee.EmployeeAddEditScreen
import com.fpt.enterpriseapp.ui.employee.EmployeeListScreen
import com.fpt.enterpriseapp.ui.project.ProjectAddEditScreen
import com.fpt.enterpriseapp.ui.project.ProjectListScreen
import com.fpt.enterpriseapp.ui.sales.SalesHomeScreen
import com.fpt.enterpriseapp.ui.profile.ProfileScreen
import com.fpt.enterpriseapp.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

enum class BottomTab(val label: String, val icon: ImageVector) {
    DASHBOARD("Tổng quan", Icons.Default.Dashboard),
    EMPLOYEE("Nhân sự", Icons.Default.People),
    PROJECT("Dự án", Icons.Default.Folder),
    SALES("Kinh doanh", Icons.Default.ShoppingCart),
    PROFILE("Cá nhân", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onLogout: () -> Unit) {
    var selectedTab by remember { mutableStateOf(BottomTab.DASHBOARD) }
    var showEmployeeEdit by remember { mutableStateOf(false) }
    var employeeEditId by remember { mutableStateOf<String?>(null) }
    var showProjectEdit by remember { mutableStateOf(false) }
    var projectEditId by remember { mutableStateOf<String?>(null) }

    // STATE LƯU ROLE CỦA NGƯỜI DÙNG
    var userRole by remember { mutableStateOf("EMPLOYEE") }
    var isLoadingRole by remember { mutableStateOf(true) }

    // LẤY ROLE TỪ FIREBASE KHI MỞ MÀN HÌNH
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            try {
                val doc = FirebaseFirestore.getInstance().collection("employees").document(uid).get().await()
                val emp = doc.toObject(Employee::class.java)
                userRole = emp?.role ?: "EMPLOYEE"
            } catch (e: Exception) {
                userRole = "EMPLOYEE"
            }
        }
        isLoadingRole = false
    }

    val isManagerOrAdmin = (userRole == "ADMIN" || userRole == "MANAGER")

    if (isLoadingRole) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                // 1. Tab Tổng quan (Ai cũng thấy)
                NavigationBarItem(
                    selected = selectedTab == BottomTab.DASHBOARD,
                    onClick = { selectedTab = BottomTab.DASHBOARD },
                    icon = { Icon(BottomTab.DASHBOARD.icon, contentDescription = BottomTab.DASHBOARD.label) },
                    label = { Text(BottomTab.DASHBOARD.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = GradientStart,
                        indicatorColor = GradientStart,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                // 2. Tab Nhân sự (Chỉ Admin/Manager mới thấy)
                if (isManagerOrAdmin) {
                    NavigationBarItem(
                        selected = selectedTab == BottomTab.EMPLOYEE,
                        onClick = { selectedTab = BottomTab.EMPLOYEE },
                        icon = { Icon(BottomTab.EMPLOYEE.icon, contentDescription = BottomTab.EMPLOYEE.label) },
                        label = { Text(BottomTab.EMPLOYEE.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = GradientStart,
                            indicatorColor = GradientStart,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                // 3. Tab Dự án (Ai cũng thấy)
                NavigationBarItem(
                    selected = selectedTab == BottomTab.PROJECT,
                    onClick = { selectedTab = BottomTab.PROJECT },
                    icon = { Icon(BottomTab.PROJECT.icon, contentDescription = BottomTab.PROJECT.label) },
                    label = { Text(BottomTab.PROJECT.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = GradientStart,
                        indicatorColor = GradientStart,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                // 4. Tab Kinh doanh (Chỉ Admin/Manager mới thấy)
                if (isManagerOrAdmin) {
                    NavigationBarItem(
                        selected = selectedTab == BottomTab.SALES,
                        onClick = { selectedTab = BottomTab.SALES },
                        icon = { Icon(BottomTab.SALES.icon, contentDescription = BottomTab.SALES.label) },
                        label = { Text(BottomTab.SALES.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = GradientStart,
                            indicatorColor = GradientStart,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                // 5. Tab Cá nhân (Ai cũng thấy)
                NavigationBarItem(
                    selected = selectedTab == BottomTab.PROFILE,
                    onClick = { selectedTab = BottomTab.PROFILE },
                    icon = { Icon(BottomTab.PROFILE.icon, contentDescription = BottomTab.PROFILE.label) },
                    label = { Text(BottomTab.PROFILE.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = GradientStart,
                        indicatorColor = GradientStart,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            when (selectedTab) {
                BottomTab.DASHBOARD -> DashboardScreen()
                BottomTab.EMPLOYEE  -> EmployeeListScreen(
                    onAddEdit = { id ->
                        employeeEditId = id
                        showEmployeeEdit = true
                    }
                )
                BottomTab.PROJECT   -> ProjectListScreen(
                    onAddEdit = { id ->
                        projectEditId = id
                        showProjectEdit = true
                    }
                )
                BottomTab.SALES     -> SalesHomeScreen()
                BottomTab.PROFILE   -> ProfileScreen(onLogout = onLogout)
            }
        }
    }

    if (showEmployeeEdit) {
        EmployeeAddEditScreen(
            employeeId = employeeEditId,
            onBack = {
                showEmployeeEdit = false
                employeeEditId = null
            }
        )
    }
    if (showProjectEdit) {
        ProjectAddEditScreen(
            projectId = projectEditId,
            onBack = {
                showProjectEdit = false
                projectEditId = null
            }
        )
    }
}