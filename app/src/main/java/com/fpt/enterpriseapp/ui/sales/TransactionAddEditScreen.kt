package com.fpt.enterpriseapp.ui.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

@Composable
fun TransactionAddEditScreen(
    onClose: () -> Unit = {},
    viewModel: TransactionEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.saved) { if (uiState.saved) onClose() }
    var contractMenuExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = SurfaceLight) {
            Column {
                Box(Modifier.fillMaxWidth().background(OrangeGradientBrush).padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onClose) { Icon(Icons.Default.Close, null, tint = Color.White) }
                        Text("Ghi nhận giao dịch", color = Color.White, fontSize = 18.sp,
                            fontWeight = FontWeight.Bold)
                    }
                }
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Chọn hợp đồng
                    Box {
                        OutlinedTextField(
                            value = uiState.transaction.contractCode.ifBlank { "— Chọn hợp đồng —" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Hợp đồng *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            trailingIcon = {
                                IconButton(onClick = { contractMenuExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = contractMenuExpanded,
                            onDismissRequest = { contractMenuExpanded = false }
                        ) {
                            uiState.contracts.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text("${c.code} — ${c.customerName}") },
                                    onClick = {
                                        viewModel.updateField {
                                            it.copy(contractId = c.id, contractCode = c.code,
                                                customerName = c.customerName)
                                        }
                                        contractMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = if (uiState.transaction.amount > 0)
                            uiState.transaction.amount.toString() else "",
                        onValueChange = { v ->
                            viewModel.updateField { it.copy(amount = v.toDoubleOrNull() ?: 0.0) }
                        },
                        label = { Text("Số tiền (VNĐ) *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    Text("Trạng thái", fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            Constants.TX_PENDING,
                            Constants.TX_SUCCESS,
                            Constants.TX_FAILED
                        ).forEach { s ->
                            FilterChip(
                                selected = uiState.transaction.status == s,
                                onClick = { viewModel.updateField { it.copy(status = s) } },
                                label = { Text(s, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = uiState.transaction.note,
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
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (uiState.saved) {
        SuccessDialog(title = "Đã lưu", message = "Giao dịch đã được ghi nhận", onDismiss = onClose)
    }
}