package com.fpt.enterpriseapp.ui.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fpt.enterpriseapp.ui.common.GradientButton
import com.fpt.enterpriseapp.ui.common.SuccessDialog
import com.fpt.enterpriseapp.ui.theme.*

@Composable
fun CustomerAddEditScreen(
    customerId: String? = null,
    onClose: () -> Unit = {},
    viewModel: CustomerEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(customerId) { viewModel.load(customerId) }
    LaunchedEffect(uiState.saved) { if (uiState.saved) onClose() }

    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = SurfaceLight) {
            Column {
                Box(Modifier.fillMaxWidth().background(BlueGradientBrush).padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onClose) { Icon(Icons.Default.Close, null, tint = Color.White) }
                        Text(if (customerId.isNullOrBlank()) "Thêm khách hàng" else "Sửa khách hàng",
                            color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.customer.name,
                        onValueChange = { v -> viewModel.updateField { it.copy(name = v) } },
                        label = { Text("Tên khách hàng *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = uiState.customer.company,
                        onValueChange = { v -> viewModel.updateField { it.copy(company = v) } },
                        label = { Text("Công ty") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = uiState.customer.email,
                        onValueChange = { v -> viewModel.updateField { it.copy(email = v) } },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    OutlinedTextField(
                        value = uiState.customer.phone,
                        onValueChange = { v -> viewModel.updateField { it.copy(phone = v) } },
                        label = { Text("Số điện thoại") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    OutlinedTextField(
                        value = uiState.customer.address,
                        onValueChange = { v -> viewModel.updateField { it.copy(address = v) } },
                        label = { Text("Địa chỉ") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    if (uiState.error != null) {
                        Surface(shape = RoundedCornerShape(10.dp),
                            color = StatusCancelled.copy(alpha = 0.12f)) {
                            Text(uiState.error ?: "", color = StatusCancelled,
                                modifier = Modifier.padding(12.dp))
                        }
                    }
                    GradientButton(
                        text = if (uiState.isSaving) "Đang lưu…" else "LƯU",
                        onClick = viewModel::save,
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth(),
                        brush = BlueGradientBrush
                    )
                }
            }
        }
    }

    if (uiState.saved) {
        SuccessDialog(title = "Đã lưu", message = "Khách hàng đã cập nhật", onDismiss = onClose)
    }
}