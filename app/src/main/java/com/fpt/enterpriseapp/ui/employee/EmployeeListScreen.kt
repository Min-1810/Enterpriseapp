package com.fpt.enterpriseapp.ui.employee

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fpt.enterpriseapp.data.model.Employee
import com.fpt.enterpriseapp.ui.common.ConfirmDialog
import com.fpt.enterpriseapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeListScreen(
    onAddEdit: (String?) -> Unit = {},
    onOpenAttendance: () -> Unit = {},
    viewModel: EmployeeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var deleteTarget by remember { mutableStateOf<Employee?>(null) }
    var showAttendance by remember { mutableStateOf(false) }

    // STATE CHO DIALOG CẤP TÀI KHOẢN HÀNG LOẠT
    var showBulkCreate by remember { mutableStateOf(false) }
    var bulkInput by remember { mutableStateOf("") }

    val isAdmin = uiState.currentRole == "ADMIN"
    val isManager = uiState.currentRole == "MANAGER"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryGradientBrush)
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Nhân sự", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("${uiState.employees.size} nhân viên", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                }
                // Nút Xuất File CSV
                IconButton(
                    onClick = {
                        viewModel.exportToCsv(context)
                        Toast.makeText(context, "Đang xuất file...", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.Download, null, tint = Color.White)
                }
                Spacer(Modifier.width(10.dp))

                // Nút Chấm công (Ai cũng có thể chấm công)
                IconButton(
                    onClick = { showAttendance = true },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.AccessTime, null, tint = Color.White)
                }
                Spacer(Modifier.width(10.dp))

                // Nút Thêm & Cấp TK (CHỈ ADMIN MỚI THẤY)
                if (isAdmin) {
                    // Nút Cấp tài khoản hàng loạt
                    IconButton(
                        onClick = { showBulkCreate = true },
                        modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Default.GroupAdd, null, tint = Color.White)
                    }
                    Spacer(Modifier.width(10.dp))

                    // Nút Thêm nhân viên
                    IconButton(
                        onClick = { onAddEdit(null) },
                        modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.White)
                    }
                }
            }
        }

        // Search Box
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchChange,
            placeholder = { Text("Tìm theo tên / email / phòng ban") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        // Bộ lọc Role & Department
        if (isAdmin || isManager) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ADMIN", "MANAGER", "EMPLOYEE").forEach { role ->
                    FilterChip(
                        selected = uiState.selectedRole == role,
                        onClick = {
                            val newRole = if (uiState.selectedRole == role) null else role
                            viewModel.onRoleFilterChange(newRole)
                        },
                        label = { Text(role, fontSize = 11.sp) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("IT", "HR", "SALES", "MARKETING").forEach { dept ->
                    FilterChip(
                        selected = uiState.selectedDepartment == dept,
                        onClick = {
                            val newDept = if (uiState.selectedDepartment == dept) null else dept
                            viewModel.onDepartmentFilterChange(newDept)
                        },
                        label = { Text(dept, fontSize = 11.sp) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Danh sách nhân viên
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GradientStart)
            }
        } else if (uiState.filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📭", fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Không tìm thấy nhân viên nào", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.filtered, key = { it.id }) { emp ->
                    EmployeeCard(
                        employee = emp,
                        isAdmin = isAdmin,
                        isManager = isManager,
                        onEdit = { onAddEdit(emp.id) },
                        onDelete = { deleteTarget = emp }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // Dialog xóa nhân viên
    deleteTarget?.let { emp ->
        ConfirmDialog(
            title = "Xóa nhân viên",
            message = "Bạn chắc chắn xóa ${emp.fullName}?",
            onConfirm = {
                viewModel.deleteEmployee(emp.id)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }

    // Attendance dialog full-screen
    if (showAttendance) {
        AttendanceScreen(onClose = { showAttendance = false })
    }

    // DIALOG CẤP TÀI KHOẢN HÀNG LOẠT
    if (showBulkCreate) {
        AlertDialog(
            onDismissRequest = { showBulkCreate = false },
            title = { Text("Cấp tài khoản hàng loạt") },
            text = {
                Column {
                    Text("Mỗi dòng nhập theo định dạng: Email, Tên, Phòng ban", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bulkInput,
                        onValueChange = { bulkInput = it },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        placeholder = { Text("ví dụ:\nnew1@fpt.com, Trần Văn A, IT\nnew2@fpt.com, Lê Thị B, HR") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.bulkCreateAccounts(bulkInput) { success, error ->
                        Toast.makeText(context, "Đã tạo $success tài khoản, lỗi $error", Toast.LENGTH_LONG).show()
                        showBulkCreate = false
                        bulkInput = ""
                    }
                }) { Text("Bắt đầu tạo") }
            },
            dismissButton = {
                TextButton(onClick = { showBulkCreate = false }) { Text("Hủy") }
            }
        )
    }
}

@Composable
private fun EmployeeCard(
    employee: Employee,
    isAdmin: Boolean,
    isManager: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = GradientStart,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        employee.fullName.firstOrNull()?.uppercase() ?: "?",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    employee.fullName.ifBlank { "(Chưa đặt tên)" },
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    employee.email,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    Text(
                        employee.department.ifBlank { "—" },
                        fontSize = 11.sp,
                        color = GradientStart,
                        fontWeight = FontWeight.Medium
                    )
                    Text("  •  ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        employee.role,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // PHÂN QUYỀN HIỂN THỊ NÚT:
            if (isAdmin || isManager) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, null, tint = StatusProgress)
                }
            }
            if (isAdmin) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = StatusCancelled)
                }
            }
        }
    }
}