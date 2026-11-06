package com.fpt.enterpriseapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.fpt.enterpriseapp.ui.auth.LoginScreen
import com.fpt.enterpriseapp.ui.main.MainScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route,
    onLoginSuccess: () -> Unit = {},  // Bổ sung tham số này
    onLogoutSuccess: () -> Unit = {}  // Đã có sẵn
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    // Gọi callback báo về MainActivity để cập nhật state
                    onLoginSuccess()
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                onLogout = {
                    // Gọi callback báo về MainActivity để xử lý đăng xuất
                    onLogoutSuccess()
                }
            )
        }
    }
}