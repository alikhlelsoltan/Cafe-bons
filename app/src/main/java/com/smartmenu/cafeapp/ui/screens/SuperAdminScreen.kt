package com.smartmenu.cafeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartmenu.cafeapp.data.model.Cafe
import com.smartmenu.cafeapp.ui.theme.*
import com.smartmenu.cafeapp.ui.viewmodel.MainViewModel
import com.smartmenu.cafeapp.ui.viewmodel.Screen

@Composable
fun SuperAdminScreen(viewModel: MainViewModel) {
    val cafes by viewModel.repository.cafes.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val pendingCafes = cafes.filter { it.status == "pending" }
    val approvedCafes = cafes.filter { it.status == "approved" }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("super_admin_back_btn")
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "رجوع", tint = TextWhite)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "لوحة المشرف العام (SaaS Platform Admin)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                        Text(
                            text = "إدارة طلبات التسجيل واعتماد الكافيهات الجديدة",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray
                        )
                    }
                }
            }
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Firebase Status Box
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2630)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, InfoBlue)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudQueue, contentDescription = null, tint = InfoBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("قاعدة بيانات Firebase السحابية", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "المشروع: cafe-bons • الحزمة: com.smartmenu.cafeapp\nنظام متعدد المستأجرين (Multi-tenant): كل كافيه يتم عزله بمعرف فريد (Slug)",
                            color = TextGray,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Pending Approvals Section
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⏳ طلبات الكافيهات بانتظار الموافقة (${pendingCafes.size})",
                        fontWeight = FontWeight.Bold,
                        color = WarningOrange,
                        fontSize = 15.sp
                    )
                }
            }

            if (pendingCafes.isEmpty()) {
                item {
                    Text(
                        text = "لا توجد طلبات معلقة حالياً. جميع الكافيهات معتمدة.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(pendingCafes, key = { it.id }) { cafe ->
                    CafeAdminCard(
                        cafe = cafe,
                        onApprove = { viewModel.approveCafe(cafe.id) },
                        onReject = { viewModel.rejectCafe(cafe.id) },
                        onSwitchTo = {
                            viewModel.repository.setCurrentCafe(cafe)
                            viewModel.navigateTo(Screen.DASHBOARD)
                        }
                    )
                }
            }

            // Approved Cafes Section
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "✓ الكافيهات المعتمدة النشطة (${approvedCafes.size})",
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen,
                    fontSize = 15.sp
                )
            }

            items(approvedCafes, key = { it.id }) { cafe ->
                CafeAdminCard(
                    cafe = cafe,
                    onApprove = null,
                    onReject = { viewModel.rejectCafe(cafe.id) },
                    onSwitchTo = {
                        viewModel.repository.setCurrentCafe(cafe)
                        viewModel.navigateTo(Screen.DASHBOARD)
                    }
                )
            }
        }
    }
}

@Composable
private fun CafeAdminCard(
    cafe: Cafe,
    onApprove: (() -> Unit)?,
    onReject: () -> Unit,
    onSwitchTo: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_cafe_${cafe.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (cafe.status == "pending") WarningOrange else DarkBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = cafe.name,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "المعرف: ${cafe.id} • كلمة المرور: ${cafe.password}",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            if (cafe.status == "approved") Color(0x335CB85C) else Color(0x33FF8800),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (cafe.status == "approved") "معتمد" else "بانتظار الموافقة",
                        color = if (cafe.status == "approved") SuccessGreen else WarningOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (onApprove != null) {
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("approve_cafe_${cafe.id}")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("موافقة وتفعيل", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Button(
                    onClick = onSwitchTo,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).testTag("switch_to_cafe_${cafe.id}")
                ) {
                    Icon(Icons.Default.Login, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("دخول لوحة التحكم", color = TextWhite, fontSize = 12.sp)
                }
            }
        }
    }
}
