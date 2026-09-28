package com.smartmenu.cafeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartmenu.cafeapp.data.model.OrderStatus
import com.smartmenu.cafeapp.ui.theme.*
import com.smartmenu.cafeapp.ui.viewmodel.MainViewModel
import com.smartmenu.cafeapp.ui.viewmodel.Screen

@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val currentCafe by viewModel.repository.currentCafe.collectAsState()
    val orders by viewModel.repository.orders.collectAsState()
    val products by viewModel.repository.products.collectAsState()
    val tables by viewModel.repository.tables.collectAsState()

    val cafe = currentCafe ?: return

    val activeOrdersCount = orders.count { it.status == OrderStatus.PENDING || it.status == OrderStatus.PREPARING }
    val todayCompletedSales = orders.filter { it.status == OrderStatus.COMPLETED }.sumOf { it.total }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("dashboard_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "☕ ${cafe.name}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0x225CB85C), RoundedCornerShape(4.dp))
                            .border(1.dp, SuccessGreen, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "معتمد", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text = "معرف الكافيه: ${cafe.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("logout_btn")
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("خروج", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Stats Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "مبيعات اليوم",
                value = "${"%.1f".format(todayCompletedSales)} ${cafe.currency}",
                icon = Icons.Default.AttachMoney,
                color = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "طلبات نشطة",
                value = "$activeOrdersCount طلب",
                icon = Icons.Default.NotificationsActive,
                color = WarningOrange,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "إجمالي الطاولات",
                value = "${tables.size} طاولة",
                icon = Icons.Default.TableRestaurant,
                color = InfoBlue,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "الوظائف ولوحة التحكم",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 2-Column Grid of Functional Dash Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardActionCard(
                title = "الأقسام والمنتجات",
                subtitle = "${products.size} صنف متوفر",
                iconText = "📂",
                modifier = Modifier.weight(1f),
                testTag = "dash_categories_products_card",
                onClick = { viewModel.navigateTo(Screen.CATEGORIES_PRODUCTS) }
            )
            DashboardActionCard(
                title = "باركود الطاولات (PDF)",
                subtitle = "توليد وطباعة QR الطاولات",
                iconText = "🪑",
                modifier = Modifier.weight(1f),
                testTag = "dash_tables_qr_card",
                onClick = { viewModel.navigateTo(Screen.TABLES_QR) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardActionCard(
                title = "الطلبات والكاشير",
                subtitle = "بونات الطلب والتحضير",
                iconText = "🔔",
                badgeCount = if (activeOrdersCount > 0) activeOrdersCount else null,
                modifier = Modifier.weight(1f),
                testTag = "dash_orders_card",
                onClick = { viewModel.navigateTo(Screen.ORDERS) }
            )
            DashboardActionCard(
                title = "إعدادات الكافيه",
                subtitle = "الضريبة، الشعار، والواي فاي",
                iconText = "⚙️",
                modifier = Modifier.weight(1f),
                testTag = "dash_settings_card",
                onClick = { viewModel.navigateTo(Screen.SETTINGS) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Full-width Feature Card: Customer Interactive Preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.navigateTo(Screen.CUSTOMER_PREVIEW) }
                .testTag("dash_customer_preview_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF222B22)),
            border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color(0xFF2E3D2E), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📱", fontSize = 26.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "معاينة منيو الزبون المباشر",
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(SuccessGreen, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("تفاعلي", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = "جرب تجربة الزبون عند مسح كود الطاولة والطلب إلى الكاشير فوراً",
                        color = TextGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System Info Footer
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "بيئة النظام والربط السحابي",
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Firebase Firestore: cafe-bons | Package: com.smartmenu.cafeapp",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                TextButton(
                    onClick = { viewModel.navigateTo(Screen.SUPER_ADMIN) },
                    modifier = Modifier.testTag("dash_super_admin_btn")
                ) {
                    Text("المشرف العام", color = GoldPrimary, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 14.sp)
            Text(text = title, color = TextGray, fontSize = 11.sp)
        }
    }
}

@Composable
private fun DashboardActionCard(
    title: String,
    subtitle: String,
    iconText: String,
    modifier: Modifier = Modifier,
    badgeCount: Int? = null,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = iconText, fontSize = 32.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = subtitle,
                    color = TextGray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            if (badgeCount != null && badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(22.dp)
                        .background(WarningOrange, RoundedCornerShape(11.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$badgeCount",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
