package com.smartmenu.cafeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartmenu.cafeapp.data.model.OrderStatus
import com.smartmenu.cafeapp.data.model.Product
import com.smartmenu.cafeapp.ui.components.TopNavHeader
import com.smartmenu.cafeapp.ui.components.TopNavTab
import com.smartmenu.cafeapp.ui.theme.*
import com.smartmenu.cafeapp.ui.viewmodel.MainViewModel
import com.smartmenu.cafeapp.ui.viewmodel.Screen

@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val currentCafe by viewModel.repository.currentCafe.collectAsState()
    val orders by viewModel.repository.orders.collectAsState()
    val products by viewModel.repository.products.collectAsState()
    val categories by viewModel.repository.categories.collectAsState()
    val selectedTableNumber by viewModel.customerTableNumber.collectAsState()

    val cafe = currentCafe ?: return

    val pendingOrdersCount = orders.count { it.status == OrderStatus.PENDING }
    var selectedCategoryId by remember { mutableStateOf("all") }
    var showTablePickerSheet by remember { mutableStateOf(false) }

    val filteredProducts = remember(selectedCategoryId, products) {
        if (selectedCategoryId == "all") products else products.filter { it.categoryId == selectedCategoryId }
    }

    Scaffold(
        topBar = {
            TopNavHeader(
                currentTab = TopNavTab.MENU,
                pendingOrdersCount = pendingOrdersCount,
                onMenuClick = { /* Already on menu */ },
                onCashierClick = { viewModel.navigateTo(Screen.ORDERS) },
                onSettingsClick = { viewModel.navigateTo(Screen.SETTINGS) }
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Table Selector Pill: [ طاولة #1 (تغيير) ⚑ ▼ ]
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF222224))
                            .border(1.dp, Color(0xFF333333), RoundedCornerShape(20.dp))
                            .clickable { showTablePickerSheet = true }
                            .padding(horizontal = 16.dp, vertical = 7.dp)
                            .testTag("table_selector_pill"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "طاولة #$selectedTableNumber (تغيير)",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = TextGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Promotional Banner Card (عروض وجديد)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("promo_banner_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66F0A500))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF2A2013), Color(0xFF1A1612))
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Tag Icon Box
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF221C14))
                                    .border(1.dp, Color(0x55F0A500), RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Right Text Details
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.End
                            ) {
                                // Badges
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFEF4444), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "🔥 تخفيضات نشطة 3",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(Color(0x33F0A500), RoundedCornerShape(12.dp))
                                            .border(1.dp, Color(0x66F0A500), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "✨ عروض وجديد",
                                            color = GoldPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "اطلع على الإضافات الجديدة والعروض والتخفيضات",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextWhite,
                                    textAlign = TextAlign.End
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "أشهى النكهات المضافة حديثاً وأقوى الخصومات • اضغط للتصفح",
                                    fontSize = 11.sp,
                                    color = TextGray,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }

            // Categories Horizontal Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        val isAllSelected = selectedCategoryId == "all"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (isAllSelected) GoldPrimary else Color(0xFF1E1E1E))
                                .border(1.dp, if (isAllSelected) GoldPrimary else Color(0xFF2C2C2E), RoundedCornerShape(18.dp))
                                .clickable { selectedCategoryId = "all" }
                                .padding(horizontal = 18.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "الكل",
                                color = if (isAllSelected) Color.Black else TextWhite,
                                fontSize = 13.sp,
                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }

                    items(categories) { cat ->
                        val isSelected = selectedCategoryId == cat.id
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (isSelected) GoldPrimary else Color(0xFF1E1E1E))
                                .border(1.dp, if (isSelected) GoldPrimary else Color(0xFF2C2C2E), RoundedCornerShape(18.dp))
                                .clickable { selectedCategoryId = cat.id }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(cat.iconEmoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cat.nameArabic,
                                color = if (isSelected) Color.Black else TextWhite,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Products List (Matching Screenshot 1)
            items(filteredProducts) { product ->
                ProductItemCard(
                    product = product,
                    currency = cafe.currency,
                    onOrderClick = {
                        viewModel.quickOrderProduct(product, selectedTableNumber)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Table Selection Dialog
    if (showTablePickerSheet) {
        AlertDialog(
            onDismissRequest = { showTablePickerSheet = false },
            title = {
                Text(
                    text = "اختر رقم الطاولة للطلب",
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val rows = (1..cafe.totalTables).chunked(4)
                    rows.forEach { rowTables ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowTables.forEach { tableNum ->
                                val isSelected = tableNum == selectedTableNumber
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) GoldPrimary else Color(0xFF252528))
                                        .border(1.dp, if (isSelected) GoldPrimary else Color(0xFF333333), RoundedCornerShape(10.dp))
                                        .clickable {
                                            viewModel.setCustomerTableNumber(tableNum)
                                            showTablePickerSheet = false
                                        }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "#$tableNum",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else TextWhite,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTablePickerSheet = false }) {
                    Text("إغلاق", color = TextGray)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
fun ProductItemCard(
    product: Product,
    currency: String,
    onOrderClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2C2E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Action & Icon Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Square Icon Box
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF242426))
                        .border(1.dp, Color(0xFF2F2F32), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = product.iconEmoji,
                        fontSize = 28.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // + اوردر Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF242426))
                        .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp))
                        .clickable { onOrderClick() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("btn_order_${product.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+ اوردر",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Right Product Details
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = product.nameArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextWhite,
                    textAlign = TextAlign.End
                )

                if (product.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.description,
                        fontSize = 12.sp,
                        color = TextGray,
                        textAlign = TextAlign.End,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val formattedPrice = "%,d".format(product.price.toInt())
                Text(
                    text = "$formattedPrice $currency",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = GoldPrimary,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}
