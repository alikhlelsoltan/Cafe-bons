package com.smartmenu.cafeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.smartmenu.cafeapp.data.model.Order
import com.smartmenu.cafeapp.data.model.OrderStatus
import com.smartmenu.cafeapp.ui.components.BonReceiptDialog
import com.smartmenu.cafeapp.ui.theme.*
import com.smartmenu.cafeapp.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrdersScreen(viewModel: MainViewModel) {
    val currentCafe by viewModel.repository.currentCafe.collectAsState()
    val orders by viewModel.repository.orders.collectAsState()
    val selectedOrderForBon by viewModel.selectedOrderForBon.collectAsState()

    val cafe = currentCafe ?: return

    var selectedStatusFilter by remember { mutableStateOf<OrderStatus?>(null) }

    val filteredOrders = orders.filter { order ->
        selectedStatusFilter == null || order.status == selectedStatusFilter
    }

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
                        modifier = Modifier.testTag("orders_back_btn")
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "رجوع", tint = TextWhite)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "الطلبات والتحضير والكاشير",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "إجمالي الطلبات: ${orders.size} • وارد مباشر من كود الطاولة",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Status Filter Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        FilterChip(
                            selected = selectedStatusFilter == null,
                            onClick = { selectedStatusFilter = null },
                            label = { Text("الكل (${orders.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary,
                                selectedLabelColor = Color.Black,
                                containerColor = DarkCard,
                                labelColor = TextGray
                            )
                        )
                    }
                    items(OrderStatus.values()) { status ->
                        val count = orders.count { it.status == status }
                        FilterChip(
                            selected = selectedStatusFilter == status,
                            onClick = { selectedStatusFilter = status },
                            label = { Text("${status.titleAr} ($count)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(status.badgeColorHex),
                                selectedLabelColor = Color.Black,
                                containerColor = DarkCard,
                                labelColor = TextGray
                            )
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filteredOrders.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔔", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("لا توجد طلبات في هذا القسم حالياً", color = TextGray, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                items(filteredOrders, key = { it.id }) { order ->
                    OrderCard(
                        order = order,
                        currency = cafe.currency,
                        onUpdateStatus = { newStatus -> viewModel.repository.updateOrderStatus(order.id, newStatus) },
                        onPrintBon = { viewModel.setSelectedOrderForBon(order) }
                    )
                }
            }
        }
    }

    // Printable Bon Dialog
    selectedOrderForBon?.let { order ->
        BonReceiptDialog(
            cafe = cafe,
            order = order,
            onDismiss = { viewModel.setSelectedOrderForBon(null) }
        )
    }
}

@Composable
private fun OrderCard(
    order: Order,
    currency: String,
    onUpdateStatus: (OrderStatus) -> Unit,
    onPrintBon: () -> Unit
) {
    val timeFormat = SimpleDateFormat("hh:mm a", Locale("ar"))
    val formattedTime = timeFormat.format(Date(order.createdAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_card_${order.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Table # and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(GoldPrimary, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "طاولة ${order.tableNumber}",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "#${order.id.takeLast(6).uppercase()}",
                        color = TextGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formattedTime,
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(order.status.badgeColorHex), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = order.status.titleAr,
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = DarkBorder)

            // Items List
            order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.quantity}× ${item.nameArabic}",
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${"%.2f".format(item.price * item.quantity)} $currency",
                        color = GoldPrimary,
                        fontSize = 13.sp
                    )
                }
                if (item.notes.isNotBlank()) {
                    Text(
                        text = "• ${item.notes}",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
                    )
                }
            }

            if (order.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ملاحظات الزبون: ${order.notes}",
                    color = WarningOrange,
                    fontSize = 12.sp
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = DarkBorder)

            // Total Price Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الإجمالي مع الضريبة:",
                    color = TextGray,
                    fontSize = 13.sp
                )
                Text(
                    text = "${"%.2f".format(order.total)} $currency",
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Print Bon Button
                OutlinedButton(
                    onClick = onPrintBon,
                    modifier = Modifier.testTag("print_bon_order_${order.id}"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("بون الطلب", color = TextWhite, fontSize = 12.sp)
                }

                // Workflow status transitions
                when (order.status) {
                    OrderStatus.PENDING -> {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.PREPARING) },
                            modifier = Modifier.weight(1f).testTag("start_prep_${order.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = InfoBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("بدء التحضير", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.PREPARING -> {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.READY) },
                            modifier = Modifier.weight(1f).testTag("mark_ready_${order.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("جاهز للتقديم", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.READY -> {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.COMPLETED) },
                            modifier = Modifier.weight(1f).testTag("complete_order_${order.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("إتمام الحساب", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.COMPLETED -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .background(DarkCard, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("تم الحساب والإنهاء ✓", color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.CANCELLED -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .background(DarkCard, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("ملغي", color = DangerRed, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
