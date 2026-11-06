package com.fpt.enterpriseapp.ui.sales

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fpt.enterpriseapp.data.model.Transaction
import com.fpt.enterpriseapp.ui.common.ConfirmDialog
import com.fpt.enterpriseapp.ui.common.StatusChip
import com.fpt.enterpriseapp.ui.theme.*
import com.fpt.enterpriseapp.utils.Constants
import com.fpt.enterpriseapp.utils.DateUtil

@Composable
fun TransactionListScreen(
    onClose: () -> Unit = {},
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var deleteTarget by remember { mutableStateOf<Transaction?>(null) }
    var showAdd by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = SurfaceLight) {
            Column {
                Box(Modifier.fillMaxWidth().background(OrangeGradientBrush).padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onClose) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                        Column(Modifier.weight(1f)) {
                            Text("Giao dịch", color = Color.White, fontSize = 18.sp,
                                fontWeight = FontWeight.Bold)
                            Text(
                                "Tổng: ${DateUtil.formatCurrency(uiState.totalRevenue)}",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                        }
                        IconButton(
                            onClick = { showAdd = true },
                            modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) { Icon(Icons.Default.Add, null, tint = Color.White) }
                    }
                }

                if (uiState.transactions.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💰", fontSize = 48.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("Chưa có giao dịch nào", color = TextSecondary)
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.transactions, key = { it.id }) { tx ->
                            Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = RoundedCornerShape(12.dp),
                                        color = StatusDone.copy(alpha = 0.15f),
                                        modifier = Modifier.size(44.dp)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("💵", fontSize = 22.sp)
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            DateUtil.formatCurrency(tx.amount),
                                            fontWeight = FontWeight.Bold,
                                            color = StatusDone
                                        )
                                        Text(
                                            "${tx.customerName} • ${tx.contractCode}",
                                            fontSize = 12.sp, color = TextSecondary
                                        )
                                        Text(
                                            DateUtil.formatDate(tx.paymentDate),
                                            fontSize = 11.sp, color = TextSecondary
                                        )
                                    }
                                    StatusChip(tx.status, statusColor(tx.status))
                                    IconButton(onClick = { deleteTarget = tx }) {
                                        Icon(Icons.Default.Delete, null, tint = StatusCancelled)
                                    }
                                }
                            }
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }

    deleteTarget?.let { tx ->
        ConfirmDialog(title = "Xóa giao dịch",
            message = "Xóa giao dịch ${DateUtil.formatCurrency(tx.amount)}?",
            onConfirm = { viewModel.delete(tx.id); deleteTarget = null },
            onDismiss = { deleteTarget = null })
    }

    if (showAdd) {
        TransactionAddEditScreen(onClose = { showAdd = false })
    }
}

private fun statusColor(s: String) = when (s) {
    Constants.TX_PENDING -> StatusPending
    Constants.TX_SUCCESS -> StatusDone
    Constants.TX_FAILED -> StatusCancelled
    else -> TextSecondary
}