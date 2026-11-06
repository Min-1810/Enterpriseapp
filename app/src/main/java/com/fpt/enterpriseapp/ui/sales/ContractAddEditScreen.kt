package com.fpt.enterpriseapp.ui.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.fpt.enterpriseapp.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractAddEditScreen(
    contractId: String? = null,
    onClose: () -> Unit = {},
    viewModel: ContractEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(contractId) { viewModel.load(contractId) }
    LaunchedEffect(uiState.saved) { if (uiState.saved) onClose() }

    var customerMenuExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = SurfaceLight) {
            Column {
                Box(Modifier.fillMaxWidth().background(GreenGradientBrush).padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onClose) { Icon(Icons.Default.Close, null, tint = Color.White) }
                        Text(if (contractId.isNullOrBlank()) "Tạo hợp đồng" else "Sửa hợp đồng",
                            color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.contract.code,
                        onValueChange = { v -> viewModel.updateField { it.copy(code = v) } },
                        label = { Text("Mã hợp đồng") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    // Chọn khách hàng
                    Box {
                        OutlinedTextField(
                            value = uiState.contract.customerName.ifBlank { "— Chọn khách hàng —" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Khách hàng *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            trailingIcon = {
                                IconButton(onClick = { customerMenuExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = customerMenuExpanded,
                            onDismissRequest = { customerMenuExpanded = false }
                        ) {
                            uiState.customers.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.name) },
                                    onClick = {
                                        viewModel.updateField {
                                            it.copy(customerId = c.id, customerName = c.name)
                                        }
                                        customerMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = if (uiState.contract.value > 0) uiState.contract.value.toString() else "",
                        onValueChange = { v ->
                            viewModel.updateField { it.copy(value = v.toDoubleOrNull() ?: 0.0) }
                        },
                        label = { Text("Giá trị (VNĐ) *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    Text("Trạng thái", fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            Constants.CONTRACT_PENDING,
                            Constants.CONTRACT_ACTIVE,
                            Constants.CONTRACT_COMPLETED
                        ).forEach { s ->
                            FilterChip(
                                selected = uiState.contract.status == s,
                                onClick = { viewModel.updateField { it.copy(status = s) } },
                                label = { Text(s, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = uiState.contract.note,
                        onValueChange = { v -> viewModel.updateField { it.copy(note = v) } },
                        label = { Text("Ghi chú") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
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
                        brush = GreenGradientBrush
                    )
                }
            }
        }
    }

    if (uiState.saved) {
        SuccessDialog(title = "Đã lưu", message = "Hợp đồng đã cập nhật", onDismiss = onClose)
    }
}