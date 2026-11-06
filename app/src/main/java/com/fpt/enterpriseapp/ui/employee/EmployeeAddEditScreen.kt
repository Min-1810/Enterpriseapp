package com.fpt.enterpriseapp.ui.employee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fpt.enterpriseapp.ui.common.GradientButton
import com.fpt.enterpriseapp.ui.common.SuccessDialog
import com.fpt.enterpriseapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeAddEditScreen(
    employeeId: String? = null,
    onBack: () -> Unit = {},
    viewModel: EmployeeEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(employeeId) { viewModel.load(employeeId) }
    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (employeeId.isNullOrBlank()) "Thêm nhân viên"
                        else "Sửa nhân viên"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GradientStart,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(SurfaceLight)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            FormField(
                label = "Họ tên *",
                value = uiState.employee.fullName,
                onChange = { v -> viewModel.updateField { it.copy(fullName = v) } }
            )
            FormField(
                label = "Email *",
                value = uiState.employee.email,
                onChange = { v -> viewModel.updateField { it.copy(email = v) } },
                keyboardType = KeyboardType.Email
            )
            FormField(
                label = "Số điện thoại",
                value = uiState.employee.phone,
                onChange = { v -> viewModel.updateField { it.copy(phone = v) } },
                keyboardType = KeyboardType.Phone
            )
            FormField(
                label = "Phòng ban",
                value = uiState.employee.department,
                onChange = { v -> viewModel.updateField { it.copy(department = v) } }
            )
            FormField(
                label = "Chức vụ",
                value = uiState.employee.position,
                onChange = { v -> viewModel.updateField { it.copy(position = v) } }
            )

            // Vai trò
            Text("Vai trò", fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("EMPLOYEE", "MANAGER", "ADMIN").forEach { role ->
                    FilterChip(
                        selected = uiState.employee.role == role,
                        onClick = { viewModel.updateField { it.copy(role = role) } },
                        label = { Text(role) }
                    )
                }
            }

            if (uiState.error != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = StatusCancelled.copy(alpha = 0.12f)
                ) {
                    Text(
                        uiState.error ?: "",
                        color = StatusCancelled,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            GradientButton(
                text = if (uiState.isSaving) "Đang lưu…" else "LƯU",
                onClick = viewModel::save,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (uiState.saved) {
        SuccessDialog(
            title = "Đã lưu",
            message = "Thông tin nhân viên đã được cập nhật",
            onDismiss = onBack
        )
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}