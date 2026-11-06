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
import com.fpt.enterpriseapp.data.model.Customer
import com.fpt.enterpriseapp.ui.common.ConfirmDialog
import com.fpt.enterpriseapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    onClose: () -> Unit = {},
    viewModel: CustomerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var deleteTarget by remember { mutableStateOf<Customer?>(null) }
    var editing by remember { mutableStateOf<Customer?>(null) }
    var showAdd by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(Modifier.fillMaxSize(), color = SurfaceLight) {
            Column {
                Box(
                    Modifier.fillMaxWidth()
                        .background(BlueGradientBrush)
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onClose) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                        Text("Khách hàng", color = Color.White, fontSize = 18.sp,
                            fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = { showAdd = true },
                            modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) { Icon(Icons.Default.Add, null, tint = Color.White) }
                    }
                }

                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchChange,
                    placeholder = { Text("Tìm khách hàng…") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                if (uiState.filtered.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("👥", fontSize = 48.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("Chưa có khách hàng", color = TextSecondary)
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.filtered, key = { it.id }) { c ->
                            Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = GradientBlueStart,
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                c.name.firstOrNull()?.uppercase() ?: "?",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(c.name, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                        if (c.company.isNotBlank())
                                            Text(c.company, fontSize = 12.sp, color = TextSecondary)
                                        Text(c.email.ifBlank { c.phone }, fontSize = 11.sp, color = TextSecondary)
                                    }
                                    IconButton(onClick = { editing = c }) {
                                        Icon(Icons.Default.Edit, null, tint = StatusProgress)
                                    }
                                    IconButton(onClick = { deleteTarget = c }) {
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

    deleteTarget?.let { c ->
        ConfirmDialog(
            title = "Xóa khách hàng",
            message = "Xóa ${c.name}?",
            onConfirm = { viewModel.delete(c.id); deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }

    if (showAdd || editing != null) {
        CustomerAddEditScreen(
            customerId = editing?.id,
            onClose = { showAdd = false; editing = null }
        )
    }
}