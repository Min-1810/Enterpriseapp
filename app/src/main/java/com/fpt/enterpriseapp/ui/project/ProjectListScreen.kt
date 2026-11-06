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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fpt.enterpriseapp.data.model.Project
import com.fpt.enterpriseapp.ui.common.ConfirmDialog
import com.fpt.enterpriseapp.ui.theme.*
import com.fpt.enterpriseapp.utils.DateUtil

@Composable
fun ProjectListScreen(
    onAddEdit: (String?) -> Unit = {},
    onOpenTasks: (String) -> Unit = {},
    viewModel: ProjectViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var deleteTarget by remember { mutableStateOf<Project?>(null) }
    var openTaskProject by remember { mutableStateOf<Project?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(OrangeGradientBrush)
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Dự án", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${uiState.projects.size} dự án",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                }
                IconButton(
                    onClick = { onAddEdit(null) },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White)
                }
            }
        }

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchChange,
            placeholder = { Text("Tìm dự án…") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GradientOrangeStart)
            }
        } else if (uiState.filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📁", fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Chưa có dự án nào", color = TextSecondary)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.filtered, key = { it.id }) { project ->
                    ProjectCard(
                        project = project,
                        onClick = { openTaskProject = project },
                        onEdit = { onAddEdit(project.id) },
                        onDelete = { deleteTarget = project }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    deleteTarget?.let { p ->
        ConfirmDialog(
            title = "Xóa dự án",
            message = "Bạn chắc chắn xóa ${p.name}? Tất cả task bên trong vẫn còn.",
            onConfirm = { viewModel.delete(p.id); deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }

    openTaskProject?.let { p ->
        TaskListScreen(
            projectId = p.id,
            projectName = p.name,
            onClose = { openTaskProject = null }
        )
    }
}

@Composable
private fun ProjectCard(
    project: Project,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GradientOrangeStart.copy(alpha = 0.15f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Folder, null, tint = GradientOrangeStart)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(project.name, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text(
                        "Deadline: ${if (project.deadline > 0) DateUtil.formatDate(project.deadline) else "—"}",
                        fontSize = 12.sp, color = TextSecondary
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, null, tint = StatusProgress)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = StatusCancelled)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { project.progress / 100f },
                    modifier = Modifier.weight(1f).height(8.dp),
                    color = GradientOrangeStart,
                    trackColor = DividerColor
                )
                Spacer(Modifier.width(8.dp))
                Text("${project.progress}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GradientOrangeStart)
            }
        }
    }
}