package com.fpt.enterpriseapp.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fpt.enterpriseapp.ui.common.ConfirmDialog
import com.fpt.enterpriseapp.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            Modifier
                .fillMaxWidth()
                .background(PrimaryGradientBrush)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.25f),
                    modifier = Modifier.size(90.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            uiState.employee?.fullName?.firstOrNull()?.uppercase() ?: "?",
                            color = Color.White,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    uiState.employee?.fullName ?: "Người dùng",
                    color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold
                )
                Text(
                    uiState.employee?.email ?: "—",
                    color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.25f)) {
                        Text(
                            uiState.employee?.role ?: "EMPLOYEE",
                            color = Color.White, fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.25f)) {
                        Text(
                            uiState.employee?.department?.ifBlank { "—" } ?: "—",
                            color = Color.White, fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Stats
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📅", fontSize = 28.sp)
                    Text(
                        "${uiState.totalCheckIns}",
                        fontSize = 22.sp, fontWeight = FontWeight.Bold, color = GradientStart
                    )
                    Text("Lần chấm công", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⏱️", fontSize = 28.sp)
                    Text(
                        "%.1f".format(uiState.totalHours),
                        fontSize = 22.sp, fontWeight = FontWeight.Bold, color = GradientOrangeStart
                    )
                    Text("Giờ làm việc", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Menu items
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProfileMenuItem(icon = "⚙️", title = "Cài đặt", onClick = {})
            ProfileMenuItem(icon = "🔔", title = "Thông báo", onClick = {})
            ProfileMenuItem(icon = "❓", title = "Trợ giúp", onClick = {})

            Spacer(Modifier.height(8.dp))

            // Nút Đăng xuất
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLogoutConfirm = true },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🚪", fontSize = 22.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Đăng xuất",
                        color = StatusCancelled,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.Logout, null, tint = StatusCancelled)
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            "FPT Enterprise Suite v1.0",
            fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))
    }

    // Hộp thoại xác nhận đăng xuất
    if (showLogoutConfirm) {
        ConfirmDialog(
            title = "Đăng xuất",
            message = "Bạn chắc chắn muốn đăng xuất?",
            onConfirm = {
                // 1. Xóa phiên đăng nhập Firebase
                FirebaseAuth.getInstance().signOut()
                showLogoutConfirm = false
                // 2. Chuyển hướng về màn Login
                onLogout()
            },
            onDismiss = { showLogoutConfirm = false }
        )
    }
}

// Hàm bổ sung để vẽ các nút menu
@Composable
private fun ProfileMenuItem(icon: String, title: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 22.sp)
            Spacer(Modifier.width(12.dp))
            Text(
                title,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

}