package com.fpt.enterpriseapp.ui.project

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.fpt.enterpriseapp.data.model.Task
import com.fpt.enterpriseapp.ui.theme.*
import com.fpt.enterpriseapp.utils.Constants
import com.fpt.enterpriseapp.utils.DateUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    projectId: String,
    projectName: String,
    onClose: () -> Unit = {},
    viewModel: TaskViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var showAddTask by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(projectId) { viewModel.observeProject(projectId) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = SurfaceLight
        ) {
            Column {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryGradientBrush)
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, null, tint = Color.White)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                projectName,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${uiState.tasks.size} task",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                        IconButton(
                            onClick = { showAddTask = true },
                            modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color.White)
                        }
                    }
                }

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White
                ) {
                    val tabs = listOf("PENDING", "IN_PROGRESS", "DONE")
                    tabs.forEachIndexed { i, label ->
                        Tab(
                            selected = selectedTab == i,
                            onClick = { selectedTab = i },
                            text = {
                                Text(
                                    "${label} (${uiState.byStatus[label]?.size ?: 0})",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == i) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                // Task list
                val currentStatus = when (selectedTab) {
                    0 -> Constants.TASK_PENDING
                    1 -> Constants.TASK_IN_PROGRESS
                    else -> Constants.TASK_DONE
                }
                val tasks = uiState.byStatus[currentStatus] ?: emptyList()

                if (tasks.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📝", fontSize = 48.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("Chưa có task nào ở cột này", color = TextSecondary)
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(tasks, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                onEdit = { editingTask = task },
                                onStatusChange = {
                                    val next = when (task.status) {
                                        Constants.TASK_PENDING -> Constants.TASK_IN_PROGRESS
                                        Constants.TASK_IN_PROGRESS -> Constants.TASK_DONE
                                        else -> Constants.TASK_PENDING
                                    }
                                    viewModel.updateStatus(task.id, next, projectId)
                                },
                                onDelete = { viewModel.delete(task.id, projectId) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddTask) {
        TaskAddEditScreen(
            taskId = null,
            projectId = projectId,
            onClose = { showAddTask = false }
        )
    }

    editingTask?.let { task ->
        TaskAddEditScreen(
            taskId = task.id,
            projectId = projectId,
            onClose = { editingTask = null }
        )
    }
}

@Composable
private fun TaskCard(
    task: Task,
    onEdit: () -> Unit,
    onStatusChange: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (task.status) {
        Constants.TASK_PENDING -> StatusPending
        Constants.TASK_IN_PROGRESS -> StatusProgress
        else -> StatusDone
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(statusColor, CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    task.title,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, null, tint = StatusProgress)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, null, tint = StatusCancelled)
                }
            }
            if (task.description.isNotBlank()) {
                Text(
                    task.description,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = onStatusChange,
                    label = { Text("Chuyển trạng thái", fontSize = 11.sp) }
                )
                if (task.deadline > 0) {
                    AssistChip(
                        onClick = {},
                        label = { Text("⏰ ${DateUtil.formatDate(task.deadline)}", fontSize = 11.sp) }
                    )
                }
            }
        }
    }
}