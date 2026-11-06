package com.fpt.enterpriseapp.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fpt.enterpriseapp.ui.common.GradientCard
import com.fpt.enterpriseapp.ui.common.StatCard
import com.fpt.enterpriseapp.ui.common.StatusChip
import com.fpt.enterpriseapp.ui.employee.AdminAttendanceScreen
import com.fpt.enterpriseapp.ui.employee.AttendanceScreen
import com.fpt.enterpriseapp.ui.theme.*

import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // States cho các Dialog
    var showAttendance by remember { mutableStateOf(false) }
    var showAdminAttendance by remember { mutableStateOf(false) }

    val userRole = uiState.currentEmployee?.role ?: "EMPLOYEE"
    val isManagerOrAdmin = (userRole == "ADMIN" || userRole == "MANAGER")
    val isEmployeeOrManager = (userRole == "EMPLOYEE" || userRole == "MANAGER")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            GradientCard(
                brush = PrimaryGradientBrush,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = Color.White.copy(alpha = 0.25f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👤", fontSize = 28.sp)
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Xin chào,", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Text(
                            uiState.currentEmployee?.fullName ?: "Người dùng",
                            color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Thống kê
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    icon = { Icon(Icons.Default.Folder, null, tint = Color.White) },
                    value = if (uiState.projectCount == 0) "--" else "${uiState.projectCount}",
                    label = "Dự án",
                    brush = GreenGradientBrush,
                    modifier = Modifier.weight(1f)
                )
                if (isManagerOrAdmin) {
                    StatCard(
                        icon = { Icon(Icons.Default.People, null, tint = Color.White) },
                        value = "${uiState.employeeCount}",
                        label = "Nhân sự",
                        brush = BlueGradientBrush,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        icon = { Icon(Icons.Default.AttachMoney, null, tint = Color.White) },
                        value = compactMoney(uiState.revenue),
                        label = "Doanh thu",
                        brush = PinkGradientBrush,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Nút Chấm công / Quản lý chấm công
        if (userRole == "ADMIN") {
            // Nút dành riêng cho Admin: Xem lịch sử chấm công
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().clickable { showAdminAttendance = true }
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GradientBlueStart.copy(alpha = 0.15f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.History, null, tint = GradientBlueStart)
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Quản lý Chấm công", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Xem lịch sử chấm công toàn công ty", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        } else if (isEmployeeOrManager) {
            // Nút dành cho Manager/Employee: Tự chấm công
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().clickable { showAttendance = true }
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GradientOrangeStart.copy(alpha = 0.15f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AccessTime, null, tint = GradientOrangeStart)
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Chấm công hôm nay", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Bấm để Check-in / Check-out", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Biểu đồ doanh thu
        if (isManagerOrAdmin) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("📊 Doanh thu 12 tháng", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(16.dp))
                        VicoRevenueChart(monthlyRevenue = uiState.monthlyRevenue)
                    }
                }
            }
        }

        // Recent tasks
        item {
            Text("🔔 Công việc gần đây", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }

        if (uiState.recentTasks.isEmpty()) {
            item {
                Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(32.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📭", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Chưa có công việc nào", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(uiState.recentTasks) { task ->
                Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(4.dp), modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(12.dp), color = statusColor(task.status).copy(alpha = 0.15f), modifier = Modifier.size(44.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Task, null, tint = statusColor(task.status))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(task.title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text(task.projectName.ifBlank { "—" }, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusChip(text = task.status, color = statusColor(task.status))
                    }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }

    // Hiển thị các Dialog tương ứng
    if (showAttendance) {
        AttendanceScreen(onClose = { showAttendance = false })
    }
    if (showAdminAttendance) {
        AdminAttendanceScreen(onClose = { showAdminAttendance = false })
    }
}

@Composable
fun VicoRevenueChart(monthlyRevenue: List<Float>) {
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(monthlyRevenue) {
        modelProducer.runTransaction {
            val safeData = if (monthlyRevenue.all { it == 0f }) List(12) { 0.1f } else monthlyRevenue
            columnSeries { series(y = safeData) }
        }
    }
    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(),
        ),
        modelProducer = modelProducer,
        modifier = Modifier.fillMaxWidth().height(200.dp)
    )
}

@Composable
private fun statusColor(status: String) = when (status) {
    "PENDING" -> StatusPending
    "IN_PROGRESS" -> StatusProgress
    "DONE" -> StatusDone
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

private fun compactMoney(v: Double): String = when {
    v >= 1_000_000_000 -> "%.1fB".format(v / 1_000_000_000)
    v >= 1_000_000     -> "%.1fM".format(v / 1_000_000)
    v >= 1_000         -> "%.1fK".format(v / 1_000)
    else               -> v.toInt().toString()
}