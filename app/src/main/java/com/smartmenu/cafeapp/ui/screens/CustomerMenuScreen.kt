package com.smartmenu.cafeapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.smartmenu.cafeapp.data.model.Product
import com.smartmenu.cafeapp.ui.theme.*
import com.smartmenu.cafeapp.ui.viewmodel.MainViewModel
import com.smartmenu.cafeapp.ui.viewmodel.Screen

@Composable
fun CustomerMenuScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentCafe by viewModel.repository.currentCafe.collectAsState()
    val categories by viewModel.repository.categories.collectAsState()
    val products by viewModel.repository.products.collectAsState()
    val tables by viewModel.repository.tables.collectAsState()

    val customerTableNumber by viewModel.customerTableNumber.collectAsState()
    val cart by viewModel.customerCart.collectAsState()
    val notes by viewModel.customerNotes.collectAsState()

    val cafe = currentCafe ?: return

    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var showCartDialog by remember { mutableStateOf(false) }
    var showOrderSuccessDialog by remember { mutableStateOf(false) }
    var lastSubmittedOrderId by remember { mutableStateOf("") }

    val filteredProducts = products.filter { prod ->
        val matchesCategory = selectedCategoryId == null || prod.categoryId == selectedCategoryId
        prod.isAvailable && matchesCategory
    }

    val totalCartItems = cart.values.sum()
    val subtotal = cart.entries.sumOf { it.key.price * it.value }
    val vat = subtotal * (cafe.vatPercentage / 100.0)
    val total = subtotal + vat

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
                        modifier = Modifier.testTag("customer_menu_back_btn")
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "رجوع", tint = TextWhite)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "☕ ${cafe.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF225522), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("منيو الزبائن", color = SuccessGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            text = cafe.welcomeMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray,
                            maxLines = 1
                        )
                    }

                    // Table Selector Badge
                    Box(
                        modifier = Modifier
                            .background(DarkCard, RoundedCornerShape(8.dp))
                            .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "طاولة $customerTableNumber",
                            color = GoldPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Table Selector Row
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("اختر طاولة الطلب: ", color = TextMuted, fontSize = 12.sp)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(tables) { table ->
                            FilterChip(
                                selected = customerTableNumber == table.tableNumber,
                                onClick = { viewModel.setCustomerTableNumber(table.tableNumber) },
                                label = { Text("طاولة ${table.tableNumber}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldPrimary,
                                    selectedLabelColor = Color.Black,
                                    containerColor = DarkCard,
                                    labelColor = TextGray
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Categories
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryId == null,
                            onClick = { selectedCategoryId = null },
                            label = { Text("الكل") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary,
                                selectedLabelColor = Color.Black,
                                containerColor = DarkCard,
                                labelColor = TextGray
                            )
                        )
                    }
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategoryId == cat.id,
                            onClick = { selectedCategoryId = cat.id },
                            label = { Text("${cat.iconEmoji} ${cat.nameArabic}") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary,
                                selectedLabelColor = Color.Black,
                                containerColor = DarkCard,
                                labelColor = TextGray
                            )
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (totalCartItems > 0) {
                Surface(
                    color = DarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "السلة ($totalCartItems مادة) • طاولة $customerTableNumber",
                                color = TextGray,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${"%.2f".format(total)} ${cafe.currency}",
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary,
                                fontSize = 18.sp
                            )
                        }

                        Button(
                            onClick = { showCartDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("open_cart_btn")
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("عرض السلة والطلب", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Wi-Fi Banner for Customers
            if (cafe.wifiSsid.isNotBlank()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2833)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = InfoBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("شبكة الواي فاي للزبائن: ${cafe.wifiSsid}", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("كلمة المرور: ${cafe.wifiPassword}", color = TextGray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            items(filteredProducts, key = { it.id }) { product ->
                val quantityInCart = cart[product] ?: 0
                CustomerProductCard(
                    product = product,
                    currency = cafe.currency,
                    quantityInCart = quantityInCart,
                    onAdd = { viewModel.addToCart(product) },
                    onRemove = { viewModel.removeFromCart(product) }
                )
            }
        }
    }

    // Cart Modal Dialog
    if (showCartDialog) {
        Dialog(onDismissRequest = { showCartDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .testTag("cart_dialog"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سلة طلب طاولة $customerTableNumber",
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            fontSize = 16.sp
                        )
                        IconButton(onClick = { showCartDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextWhite)
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = DarkBorder)

                    // Cart Items
                    cart.forEach { (item, qty) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.nameArabic, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("${"%.2f".format(item.price)} ${cafe.currency}", color = GoldPrimary, fontSize = 12.sp)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.removeFromCart(item) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "إنقاص", tint = DangerRed)
                                }
                                Text(
                                    text = "$qty",
                                    color = TextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                IconButton(
                                    onClick = { viewModel.addToCart(item) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "زيادة", tint = SuccessGreen)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { viewModel.setCustomerNotes(it) },
                        label = { Text("ملاحظات خاصة (سكر، ثلج، تحضير خاص...)") },
                        modifier = Modifier.fillMaxWidth().testTag("customer_notes_input"),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Calculations
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("المجموع الفرعي:", color = TextGray, fontSize = 13.sp)
                        Text("${"%.2f".format(subtotal)} ${cafe.currency}", color = TextWhite, fontSize = 13.sp)
                    }
                    if (cafe.vatPercentage > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الضريبة (${cafe.vatPercentage}%):", color = TextGray, fontSize = 12.sp)
                            Text("${"%.2f".format(vat)} ${cafe.currency}", color = TextWhite, fontSize = 12.sp)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("الإجمالي المستحق:", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 15.sp)
                        Text("${"%.2f".format(total)} ${cafe.currency}", fontWeight = FontWeight.Bold, color = GoldPrimary, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            val placed = viewModel.submitCustomerOrder()
                            if (placed != null) {
                                lastSubmittedOrderId = placed.id
                                showCartDialog = false
                                showOrderSuccessDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_order_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("إرسال الطلب إلى الكاشير والمطبخ", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }

    // Order Success Dialog
    if (showOrderSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showOrderSuccessDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        showOrderSuccessDialog = false
                        viewModel.navigateTo(Screen.ORDERS)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("عرض شاشة الطلبات والكاشير", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOrderSuccessDialog = false }) {
                    Text("البقاء في المنيو", color = TextGray)
                }
            },
            title = {
                Text("🎉 تم استلام طلبك بنجاح!", fontWeight = FontWeight.Bold, color = TextWhite)
            },
            text = {
                Text(
                    "طلبك لطاولة $customerTableNumber برقم (#${lastSubmittedOrderId.takeLast(6).uppercase()}) وصل إلى الكاشير وشاشة التحضير وجاري إعداده بأعلى جودة. نتمنى لك وقتاً ممتعاً!",
                    color = TextGray
                )
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
private fun CustomerProductCard(
    product: Product,
    currency: String,
    quantityInCart: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("customer_prod_${product.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(DarkCard, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = product.iconEmoji, fontSize = 26.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.nameArabic,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontSize = 14.sp
                )
                if (product.description.isNotBlank()) {
                    Text(
                        text = product.description,
                        color = TextGray,
                        fontSize = 11.sp,
                        maxLines = 2
                    )
                }
                Text(
                    text = "${"%.2f".format(product.price)} $currency • تحضير: ${product.prepTimeMinutes} دقيقة",
                    color = GoldPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            if (quantityInCart == 0) {
                Button(
                    onClick = onAdd,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("add_btn_${product.id}")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة", color = TextWhite, fontSize = 12.sp)
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(DarkCard, RoundedCornerShape(8.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Remove, contentDescription = "إنقاص", tint = DangerRed)
                    }
                    Text(
                        text = "$quantityInCart",
                        color = GoldPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    IconButton(onClick = onAdd, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "زيادة", tint = SuccessGreen)
                    }
                }
            }
        }
    }
}
