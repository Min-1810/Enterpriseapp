package com.fpt.enterpriseapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.fpt.enterpriseapp.data.repository.AuthRepository
import com.fpt.enterpriseapp.ui.navigation.AppNavGraph
import com.fpt.enterpriseapp.ui.navigation.Screen
import com.fpt.enterpriseapp.ui.theme.FptEnterpriseTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FptEnterpriseTheme {
                val navController = rememberNavController()

                // Biến state theo dõi trạng thái đăng nhập ngay trong Compose
                var isLoggedIn by remember { mutableStateOf(authRepository.isLoggedIn) }

                // Chọn màn hình bắt đầu dựa trên state
                val startRoute = if (isLoggedIn) Screen.Main.route else Screen.Login.route

                AppNavGraph(
                    navController = navController,
                    startDestination = startRoute,
                    onLoginSuccess = {
                        // Khi đăng nhập thành công, cập nhật state
                        isLoggedIn = true
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onLogoutSuccess = {
                        // Khi đăng xuất, cập nhật state
                        isLoggedIn = false
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true } // Xóa sạch backstack
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}