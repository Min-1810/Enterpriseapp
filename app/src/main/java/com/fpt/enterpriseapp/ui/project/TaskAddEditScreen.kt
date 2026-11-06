package com.fpt.enterpriseapp.ui.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import com.fpt.enterpriseapp.ui.common.GradientButton
import com.fpt.enterpriseapp.ui.common.SuccessDialog
import com.fpt.enterpriseapp.ui.theme.*
import com.fpt.enterpriseapp.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskAddEditScreen(
    taskId: String? = null,
    projectId: String,
    onClose: () -> Unit = {},
    viewModel: TaskEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(taskId, projectId) { viewModel.load(taskId, projectId) }
    LaunchedEffect(uiState.saved) { if (uiState.saved) onClose() }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(Modifier.fillMaxSize(), color = SurfaceLight) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(OrangeGradientBrush)
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, null, tint = Color.White)
                        }
                        Text(
                            if (taskId.isNullOrBlank()) "Thêm task" else "Sửa task",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.task.title,
                        onValueChange = { v -> viewModel.updateField { it.copy(title = v) } },
                        label = { Text("Tiêu đề *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = uiState.task.description,
                        onValueChange = { v -> viewModel.updateField { it.copy(description = v) } },
                        label = { Text("Mô tả") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = uiState.task.assigneeName,
                        onValueChange = { v -> viewModel.updateField { it.copy(assigneeName = v) } },
                        label = { Text("Người phụ trách") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Text("Trạng thái", fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(Constants.TASK_PENDING, Constants.TASK_IN_PROGRESS, Constants.TASK_DONE).forEach { s ->
                            FilterChip(
                                selected = uiState.task.status == s,
                                onClick = { viewModel.updateField { it.copy(status = s) } },
                                label = { Text(s, fontSize = 11.sp) }
                            )
                        }
                    }

                    Text("Độ ưu tiên", fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("LOW", "MEDIUM", "HIGH").forEach { p ->
                            FilterChip(
                                selected = uiState.task.priority == p,
                                onClick = { viewModel.updateField { it.copy(priority = p) } },
                                label = { Text(p, fontSize = 11.sp) }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = if (uiState.task.estimatedHours > 0) uiState.task.estimatedHours.toString() else "",
                            onValueChange = { v ->
                                viewModel.updateField {
                                    it.copy(estimatedHours = v.toDoubleOrNull() ?: 0.0)
                                }
                            },
                            label = { Text("Giờ dự kiến") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = if (uiState.task.actualHours > 0) uiState.task.actualHours.toString() else "",
                            onValueChange = { v ->
                                viewModel.updateField {
                                    it.copy(actualHours = v.toDoubleOrNull() ?: 0.0)
                                }
                            },
                            label = { Text("Giờ thực tế") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )
                    }

                    if (uiState.error != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = StatusCancelled.copy(alpha = 0.12f)
                        ) {
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
        SuccessDialog(
            title = "Đã lưu",
            message = "Task đã được cập nhật",
            onDismiss = onClose
        )
    }
}