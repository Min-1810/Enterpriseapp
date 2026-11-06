package com.fpt.enterpriseapp.ui.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.fpt.enterpriseapp.ui.theme.*
import com.fpt.enterpriseapp.utils.DateUtil

@Composable
fun SalesHomeScreen() {
    var selectedSection by remember { mutableStateOf<SalesSection?>(null) }

    if (selectedSection == null) {
        SalesHomeContent(onSelect = { selectedSection = it })
    } else {
        when (selectedSection) {
            SalesSection.CUSTOMER -> CustomerListScreen(onClose = { selectedSection = null })
            SalesSection.CONTRACT -> ContractListScreen(onClose = { selectedSection = null })
            SalesSection.TRANSACTION -> TransactionListScreen(onClose = { selectedSection = null })
            else -> {}
        }
    }
}

enum class SalesSection(val title: String) {
    CUSTOMER("Khách hàng"),
    CONTRACT("Hợp đồng"),
    TRANSACTION("Giao dịch")
}

@Composable
private fun SalesHomeContent(onSelect: (SalesSection) -> Unit) {
    val txVm: TransactionViewModel = hiltViewModel()
    val txState by txVm.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PinkGradientBrush)
                .padding(20.dp)
        ) {
            Column {
                Text("Kinh doanh", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Doanh thu: ${DateUtil.formatCurrency(txState.totalRevenue)}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SalesCard(
                icon = "👥",
                title = "Khách hàng",
                subtitle = "Quản lý danh sách khách hàng",
                brush = BlueGradientBrush,
                onClick = { onSelect(SalesSection.CUSTOMER) }
            )
            SalesCard(
                icon = "📄",
                title = "Hợp đồng",
                subtitle = "Quản lý hợp đồng ký kết",
                brush = GreenGradientBrush,
                onClick = { onSelect(SalesSection.CONTRACT) }
            )
            SalesCard(
                icon = "💰",
                title = "Giao dịch",
                subtitle = "Ghi nhận thanh toán, doanh thu",
                brush = OrangeGradientBrush,
                onClick = { onSelect(SalesSection.TRANSACTION) }
            )
        }

        // Top 5 giao dịch gần đây
        if (txState.transactions.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
                "Giao dịch gần đây",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
            txState.transactions.take(3).forEach { tx ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("💵", fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(tx.customerName.ifBlank { "—" },
                                fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text(tx.contractCode, fontSize = 12.sp, color = TextSecondary)
                        }
                        Text(
                            DateUtil.formatCurrency(tx.amount),
                            color = StatusDone,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SalesCard(
    icon: String,
    title: String,
    subtitle: String,
    brush: androidx.compose.ui.graphics.Brush,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .background(brush)
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.25f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(icon, fontSize = 28.sp)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.White)
        }
    }
}