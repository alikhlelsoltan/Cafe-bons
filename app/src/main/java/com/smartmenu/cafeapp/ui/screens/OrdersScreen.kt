package com.smartmenu.cafeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartmenu.cafeapp.data.model.Order
import com.smartmenu.cafeapp.data.model.OrderStatus
import com.smartmenu.cafeapp.ui.components.BonReceiptDialog
import com.smartmenu.cafeapp.ui.components.TopNavHeader
import com.smartmenu.cafeapp.ui.components.TopNavTab
import com.smartmenu.cafeapp.ui.theme.*
import com.smartmenu.cafeapp.ui.viewmodel.MainViewModel
import com.smartmenu.cafeapp.ui.viewmodel.Screen

enum class CashierLifecycleTab(val title: String) {
    PENDING("بانتظار الموافقة"),
    PREPARING("مرحلة التحضير"),
    READY("مرحلة التسليم"),
    BILLING("الفواتير والتسديد")
}

@Composable
fun OrdersScreen(viewModel: MainViewModel) {
    val currentCafe by viewModel.repository.currentCafe.collectAsState()
    val orders by viewModel.repository.orders.collectAsState()
    val selectedOrderForBon by viewModel.selectedOrderForBon.collectAsState()

    val cafe = currentCafe ?: return

    var activeTab by remember { mutableStateOf(CashierLifecycleTab.PENDING) }

    val pendingCount = orders.count { it.status == OrderStatus.PENDING }
    val preparingCount = orders.count { it.status == OrderStatus.PREPARING }
    val readyCount = orders.count { it.status == OrderStatus.READY }

    val displayOrders = remember(activeTab, orders) {
        when (activeTab) {
            CashierLifecycleTab.PENDING -> orders.filter { it.status == OrderStatus.PENDING }
            CashierLifecycleTab.PREPARING -> orders.filter { it.status == OrderStatus.PREPARING }
            CashierLifecycleTab.READY -> orders.filter { it.status == OrderStatus.READY }
            CashierLifecycleTab.BILLING -> orders.filter { it.status == OrderStatus.COMPLETED || it.status == OrderStatus.CANCELLED }
        }
    }

    Scaffold(
        topBar = {
            TopNavHeader(
                currentTab = TopNavTab.CASHIER,
                pendingOrdersCount = pendingCount,
                onMenuClick = { viewModel.navigateTo(Screen.DASHBOARD) },
                onCashierClick = { /* Already on cashier */ },
                onSettingsClick = { viewModel.navigateTo(Screen.SETTINGS) }
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Title and Subtitle (Matching Screenshot 2)
            Text(
                text = "إدارة الطلبات والكاشير",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "دورة حياة الطلب: الموافقة ➔ التحضير ➔ التسليم ➔ الفواتير والتسديد",
                fontSize = 12.sp,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Order Lifecycle Tabs (Horizontal Underline Tabs)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(CashierLifecycleTab.values()) { tab ->
                    val isSelected = activeTab == tab
                    val count = when (tab) {
                        CashierLifecycleTab.PENDING -> pendingCount
                        CashierLifecycleTab.PREPARING -> preparingCount
                        CashierLifecycleTab.READY -> readyCount
                        CashierLifecycleTab.BILLING -> 0
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { activeTab = tab }
                            .padding(bottom = 6.dp)
                            .testTag("tab_${tab.name.lowercase()}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = tab.title,
                                color = if (isSelected) GoldPrimary else TextGray,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )

                            if (count > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(if (isSelected) GoldPrimary else Color(0xFFEF4444), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = count.toString(),
                                        color = if (isSelected) Color.Black else Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Underline indicator
                        Box(
                            modifier = Modifier
                                .height(3.dp)
                                .width(if (isSelected) 80.dp else 0.dp)
                                .background(if (isSelected) GoldPrimary else Color.Transparent, RoundedCornerShape(2.dp))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Orders List
            if (displayOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✨", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لا توجد طلبات في قسم ${activeTab.title} حالياً",
                            color = TextGray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(displayOrders) { order ->
                        CashierOrderCard(
                            order = order,
                            currency = cafe.currency,
                            onApprove = {
                                viewModel.updateOrderStatus(order.id, OrderStatus.PREPARING)
                            },
                            onReject = {
                                viewModel.updateOrderStatus(order.id, OrderStatus.CANCELLED)
                            },
                            onReady = {
                                viewModel.updateOrderStatus(order.id, OrderStatus.READY)
                            },
                            onDeliverAndPrint = {
                                viewModel.updateOrderStatus(order.id, OrderStatus.COMPLETED)
                                viewModel.setSelectedOrderForBon(order)
                            },
                            onPrintOnly = {
                                viewModel.setSelectedOrderForBon(order)
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    // Receipt Print Dialog
    selectedOrderForBon?.let { order ->
        BonReceiptDialog(
            order = order,
            cafe = cafe,
            onDismiss = { viewModel.setSelectedOrderForBon(null) }
        )
    }
}

@Composable
fun CashierOrderCard(
    order: Order,
    currency: String,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onReady: () -> Unit,
    onDeliverAndPrint: () -> Unit,
    onPrintOnly: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cashier_order_${order.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2C2E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Price on Left, Table and Customer Name on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Price
                val formattedPrice = "%,d".format(order.total.toInt())
                Text(
                    text = "$formattedPrice $currency",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = GoldPrimary
                )

                // Right Table tag & Customer name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Customer Name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = TextWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = order.customerName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    // Table Pill Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldPrimary)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "طاولة #${order.tableNumber}",
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Order Items (e.g. 1x قهوة تركي بالهيل)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.End
            ) {
                order.items.forEach { item ->
                    Text(
                        text = "${item.quantity}x ${item.nameArabic}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite
                    )
                }

                if (order.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ملاحظة: ${order.notes}",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Action Buttons depending on status
            when (order.status) {
                OrderStatus.PENDING -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // موافقة وبدء التحضير
                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(44.dp)
                                .testTag("btn_approve_order_${order.id}")
                        ) {
                            Text(
                                text = "موافقة وبدء التحضير ✓",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // رفض الطلب
                        OutlinedButton(
                            onClick = onReject,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(0.9f)
                                .height(44.dp)
                                .testTag("btn_reject_order_${order.id}")
                        ) {
                            Text(
                                text = "رفض الطلب",
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                OrderStatus.PREPARING -> {
                    Button(
                        onClick = onReady,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_ready_order_${order.id}")
                    ) {
                        Text(
                            text = "اكتمال التحضير ➔ جاهز للتسليم 🚀",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                OrderStatus.READY -> {
                    Button(
                        onClick = onDeliverAndPrint,
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_deliver_order_${order.id}")
                    ) {
                        Text(
                            text = "تم التسليم وطباعة الفاتورة 🧾",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                OrderStatus.COMPLETED, OrderStatus.CANCELLED -> {
                    OutlinedButton(
                        onClick = onPrintOnly,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Text(
                            text = "عرض وطباعة البون 🧾",
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
