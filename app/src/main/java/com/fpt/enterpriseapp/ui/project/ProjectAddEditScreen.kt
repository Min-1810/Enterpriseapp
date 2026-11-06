package com.fpt.enterpriseapp.ui.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.fpt.enterpriseapp.ui.common.GradientButton
import com.fpt.enterpriseapp.ui.common.SuccessDialog
import com.fpt.enterpriseapp.ui.theme.*
import com.fpt.enterpriseapp.utils.DateUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectAddEditScreen(
    projectId: String? = null,
    onBack: () -> Unit = {},
    viewModel: ProjectEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(projectId) { viewModel.load(projectId) }
    LaunchedEffect(uiState.saved) { if (uiState.saved) onBack() }

    var deadlineInput by remember { mutableStateOf("") }
    LaunchedEffect(uiState.project.id) {
        deadlineInput = if (uiState.project.deadline > 0)
            DateUtil.formatDate(uiState.project.deadline) else ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (projectId.isNullOrBlank()) "Tạo dự án" else "Sửa dự án") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GradientOrangeStart,
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
            OutlinedTextField(
                value = uiState.project.name,
                onValueChange = { v -> viewModel.updateField { it.copy(name = v) } },
                label = { Text("Tên dự án *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.project.description,
                onValueChange = { v -> viewModel.updateField { it.copy(description = v) } },
                label = { Text("Mô tả") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedTextField(
                value = deadlineInput,
                onValueChange = { v ->
                    deadlineInput = v
                    // Parse dd/MM/yyyy → millis
                    runCatching {
                        val parts = v.split("/")
                        val cal = java.util.Calendar.getInstance()
                        cal.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
                        viewModel.updateField { it.copy(deadline = cal.timeInMillis) }
                    }
                },
                label = { Text("Deadline (dd/MM/yyyy)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            if (uiState.error != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = StatusCancelled.copy(alpha = 0.12f)
                ) {
                    Text(uiState.error ?: "", color = StatusCancelled, modifier = Modifier.padding(12.dp))
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

    if (uiState.saved) {
        SuccessDialog(
            title = "Đã lưu",
            message = "Thông tin dự án đã cập nhật",
            onDismiss = onBack
        )
    }
}