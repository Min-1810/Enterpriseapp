package com.fpt.enterpriseapp.ui.employee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fpt.enterpriseapp.ui.theme.*
import com.fpt.enterpriseapp.utils.DateUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    onClose: () -> Unit = {},
    viewModel: AttendanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            kotlinx.coroutines.delay(1500)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chấm công") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GradientStart,
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
                .padding(16.dp)
        ) {
            // Card hôm nay
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .background(PrimaryGradientBrush)
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        uiState.currentEmployee?.fullName ?: "—",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        DateUtil.formatDate(System.currentTimeMillis()),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TimeBox(
                            label = "Check-in",
                            time = uiState.today?.checkInTime?.takeIf { it > 0 }
                                ?.let { DateUtil.formatTime(it) } ?: "--:--"
                        )
                        TimeBox(
                            label = "Check-out",
                            time = uiState.today?.checkOutTime?.takeIf { it > 0 }
                                ?.let { DateUtil.formatTime(it) } ?: "--:--"
                        )
                        TimeBox(
                            label = "Tổng giờ",
                            time = "%.1f".format(uiState.today?.totalHours ?: 0.0)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Nút check-in/out
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = viewModel::checkIn,
                    enabled = !uiState.isProcessing &&
                            (uiState.today?.checkInTime ?: 0L) <= 0,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDone)
                ) {
                    Text("CHECK-IN", fontWeight = FontWeight.Bold, color = Color.White)
                }
                Button(
                    onClick = viewModel::checkOut,
                    enabled = !uiState.isProcessing &&
                            (uiState.today?.checkInTime ?: 0L) > 0 &&
                            (uiState.today?.checkOutTime ?: 0L) <= 0,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusProgress)
                ) {
                    Text("CHECK-OUT", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            if (uiState.message != null) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = StatusDone.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        uiState.message ?: "",
                        color = StatusDone,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Lịch sử chấm công", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.history) { att ->
                    Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(att.date, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text(
                                    "In: ${att.checkInTime.takeIf { it > 0 }?.let { DateUtil.formatTime(it) } ?: "--"}  •  " +
                                            "Out: ${att.checkOutTime.takeIf { it > 0 }?.let { DateUtil.formatTime(it) } ?: "--"}",
                                    fontSize = 12.sp, color = TextSecondary
                                )
                            }
                            Text(
                                "%.1f h".format(att.totalHours),
                                color = GradientStart,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun TimeBox(label: String, time: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
        Text(time, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}