package com.smartmenu.cafeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.smartmenu.cafeapp.data.model.Category
import com.smartmenu.cafeapp.data.model.Product
import com.smartmenu.cafeapp.ui.theme.*
import com.smartmenu.cafeapp.ui.viewmodel.MainViewModel

@Composable
fun CategoriesProductsScreen(viewModel: MainViewModel) {
    val currentCafe by viewModel.repository.currentCafe.collectAsState()
    val categories by viewModel.repository.categories.collectAsState()
    val products by viewModel.repository.products.collectAsState()

    val cafe = currentCafe ?: return

    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredProducts = products.filter { prod ->
        val matchesCategory = selectedCategoryId == null || prod.categoryId == selectedCategoryId
        val matchesSearch = searchQuery.isBlank() ||
                prod.nameArabic.contains(searchQuery, ignoreCase = true) ||
                prod.nameEnglish.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
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
                        modifier = Modifier.testTag("cat_prod_back_btn")
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "رجوع", tint = TextWhite)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "الأقسام والمنتجات",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "${categories.size} أقسام • ${products.size} صنف",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray
                        )
                    }

                    Button(
                        onClick = { showAddProductDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_product_open_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة مادة", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("بحث في المنتجات والمشروبات...", color = TextMuted, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("product_search_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkInput,
                        unfocusedContainerColor = DarkInput,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Categories Filter Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryId == null,
                            onClick = { selectedCategoryId = null },
                            label = { Text("الكل (${products.size})") },
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
                    item {
                        IconButton(
                            onClick = { showAddCategoryDialog = true },
                            modifier = Modifier
                                .size(32.dp)
                                .background(DarkCard, RoundedCornerShape(8.dp))
                                .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp))
                                .testTag("add_category_open_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "إضافة قسم", tint = GoldPrimary, modifier = Modifier.size(18.dp))
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
            if (filteredProducts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔍", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("لا توجد منتجات مطابقة", color = TextGray, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductItemRow(
                        product = product,
                        currency = cafe.currency,
                        onToggleAvailability = { viewModel.repository.toggleProductAvailability(product.id) },
                        onDelete = { viewModel.repository.deleteProduct(product.id) }
                    )
                }
            }
        }
    }

    // Add Category Dialog
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onAdd = { nameAr, nameEn, emoji ->
                viewModel.repository.addCategory(nameAr, nameEn, emoji)
                showAddCategoryDialog = false
            }
        )
    }

    // Add Product Dialog
    if (showAddProductDialog) {
        AddProductDialog(
            categories = categories,
            currency = cafe.currency,
            onDismiss = { showAddProductDialog = false },
            onAdd = { catId, nameAr, nameEn, desc, price, emoji, prep ->
                viewModel.repository.addProduct(catId, nameAr, nameEn, desc, price, emoji, prep)
                showAddProductDialog = false
            }
        )
    }
}

@Composable
private fun ProductItemRow(
    product: Product,
    currency: String,
    onToggleAvailability: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_item_${product.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (product.isAvailable) DarkBorder else DangerRed)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(DarkCard, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = product.iconEmoji, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.nameArabic,
                        fontWeight = FontWeight.Bold,
                        color = if (product.isAvailable) TextWhite else TextMuted,
                        fontSize = 14.sp
                    )
                    if (!product.isAvailable) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(غير متوفر)",
                            color = DangerRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = product.nameEnglish,
                    color = TextGray,
                    fontSize = 11.sp
                )
                if (product.description.isNotBlank()) {
                    Text(
                        text = product.description,
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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

            // Availability Switch
            Switch(
                checked = product.isAvailable,
                onCheckedChange = { onToggleAvailability() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = GoldPrimary,
                    checkedTrackColor = Color(0xFF664400),
                    uncheckedThumbColor = TextGray,
                    uncheckedTrackColor = DarkCard
                ),
                modifier = Modifier.testTag("switch_available_${product.id}")
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_prod_${product.id}")
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = DangerRed)
            }
        }
    }
}

@Composable
private fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onAdd: (nameAr: String, nameEn: String, emoji: String) -> Unit
) {
    var nameAr by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("☕") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_category_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "إضافة قسم جديد",
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = nameAr,
                    onValueChange = { nameAr = it },
                    label = { Text("اسم القسم بالعربية") },
                    modifier = Modifier.fillMaxWidth().testTag("cat_name_ar_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text("اسم القسم بالإنجليزية (اختياري)") },
                    modifier = Modifier.fillMaxWidth().testTag("cat_name_en_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = emoji,
                    onValueChange = { emoji = it },
                    label = { Text("رمز تعبيري (Emoji)") },
                    modifier = Modifier.fillMaxWidth().testTag("cat_emoji_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء", color = TextGray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (nameAr.isNotBlank()) {
                                onAdd(nameAr, nameEn, emoji)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        modifier = Modifier.testTag("save_category_btn")
                    ) {
                        Text("حفظ القسم", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddProductDialog(
    categories: List<Category>,
    currency: String,
    onDismiss: () -> Unit,
    onAdd: (catId: String, nameAr: String, nameEn: String, desc: String, price: Double, emoji: String, prep: Int) -> Unit
) {
    var selectedCatId by remember { mutableStateOf(categories.firstOrNull()?.id ?: "") }
    var nameAr by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("☕") }
    var prepMinutesText by remember { mutableStateOf("5") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_product_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "إضافة مادة / منتج جديد",
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Category selector
                Text("اختر القسم:", color = TextGray, fontSize = 12.sp)
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCatId == cat.id,
                            onClick = { selectedCatId = cat.id },
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

                OutlinedTextField(
                    value = nameAr,
                    onValueChange = { nameAr = it },
                    label = { Text("اسم المنتج بالعربية") },
                    modifier = Modifier.fillMaxWidth().testTag("prod_name_ar_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("السعر ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("prod_price_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = emoji,
                        onValueChange = { emoji = it },
                        label = { Text("أيقونة") },
                        modifier = Modifier.weight(0.7f).testTag("prod_emoji_input"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("الوصف والمكونات") },
                    modifier = Modifier.fillMaxWidth().testTag("prod_desc_input"),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء", color = TextGray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val price = priceText.toDoubleOrNull() ?: 0.0
                            val prep = prepMinutesText.toIntOrNull() ?: 5
                            if (nameAr.isNotBlank() && price > 0 && selectedCatId.isNotBlank()) {
                                onAdd(selectedCatId, nameAr, nameEn, desc, price, emoji, prep)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        modifier = Modifier.testTag("save_product_btn")
                    ) {
                        Text("حفظ المادة", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
