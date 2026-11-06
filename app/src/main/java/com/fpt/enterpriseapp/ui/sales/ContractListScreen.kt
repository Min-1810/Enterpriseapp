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
import com.fpt.enterpriseapp.data.model.Contract
import com.fpt.enterpriseapp.ui.common.ConfirmDialog
import com.fpt.enterpriseapp.ui.common.StatusChip
import com.fpt.enterpriseapp.ui.theme.*
import com.fpt.enterpriseapp.utils.Constants
import com.fpt.enterpriseapp.utils.DateUtil

@Composable
fun ContractListScreen(
    onClose: () -> Unit = {},
    viewModel: ContractViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var deleteTarget by remember { mutableStateOf<Contract?>(null) }
    var editing by remember { mutableStateOf<Contract?>(null) }
    var showAdd by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = SurfaceLight) {
            Column {
                Box(Modifier.fillMaxWidth().background(GreenGradientBrush).padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onClose) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                        Text("Hợp đồng", color = Color.White, fontSize = 18.sp,
                            fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = { showAdd = true },
                            modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) { Icon(Icons.Default.Add, null, tint = Color.White) }
                    }
                }

                if (uiState.contracts.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📄", fontSize = 48.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("Chưa có hợp đồng nào", color = TextSecondary)
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.contracts, key = { it.id }) { c ->
                            Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(c.code, fontWeight = FontWeight.Bold, color = TextPrimary,
                                            modifier = Modifier.weight(1f))
                                        StatusChip(c.status, statusColor(c.status))
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(c.customerName, fontSize = 13.sp, color = TextSecondary)
                                    Spacer(Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            DateUtil.formatCurrency(c.value),
                                            fontWeight = FontWeight.Bold,
                                            color = GradientGreenStart,
                                            fontSize = 16.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(onClick = { editing = c }) {
                                            Icon(Icons.Default.Edit, null, tint = StatusProgress)
                                        }
                                        IconButton(onClick = { deleteTarget = c }) {
                                            Icon(Icons.Default.Delete, null, tint = StatusCancelled)
                                        }
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

    deleteTarget?.let { c ->
        ConfirmDialog(title = "Xóa hợp đồng", message = "Xóa ${c.code}?",
            onConfirm = { viewModel.delete(c.id); deleteTarget = null },
            onDismiss = { deleteTarget = null })
    }

    if (showAdd || editing != null) {
        ContractAddEditScreen(contractId = editing?.id, onClose = { showAdd = false; editing = null })
    }
}

private fun statusColor(s: String) = when (s) {
    Constants.CONTRACT_PENDING -> StatusPending
    Constants.CONTRACT_ACTIVE -> StatusProgress
    Constants.CONTRACT_COMPLETED -> StatusDone
    else -> TextSecondary
}