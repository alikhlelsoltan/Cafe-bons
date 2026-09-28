package com.smartmenu.cafeapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartmenu.cafeapp.data.model.OrderStatus
import com.smartmenu.cafeapp.ui.components.TopNavHeader
import com.smartmenu.cafeapp.ui.components.TopNavTab
import com.smartmenu.cafeapp.ui.theme.*
import com.smartmenu.cafeapp.ui.viewmodel.MainViewModel
import com.smartmenu.cafeapp.ui.viewmodel.Screen

@Composable
fun CafeSettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentCafe by viewModel.repository.currentCafe.collectAsState()
    val orders by viewModel.repository.orders.collectAsState()
    val cafe = currentCafe ?: return

    val pendingCount = orders.count { it.status == OrderStatus.PENDING }

    var showEditInfoModal by remember { mutableStateOf(false) }
    var showOffersModal by remember { mutableStateOf(false) }
    var showLanguageModal by remember { mutableStateOf(false) }
    var showThemeModal by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopNavHeader(
                currentTab = TopNavTab.SETTINGS,
                pendingOrdersCount = pendingCount,
                onMenuClick = { viewModel.navigateTo(Screen.DASHBOARD) },
                onCashierClick = { viewModel.navigateTo(Screen.ORDERS) },
                onSettingsClick = { /* Already on settings */ }
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Title and Subtitle (Matching Screenshot 3)
            Text(
                text = "الضبط والإدارة",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "تخصيص وإعداد كامل لنظام الكافيه الإلكتروني",
                fontSize = 12.sp,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Settings Navigation Cards
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: المينيو والأقسام
                SettingsNavRowCard(
                    title = "المينيو والأقسام",
                    subtitle = "إضافة وتعديل الأقسام والمنتجات والأسعار والتوافر",
                    icon = Icons.Default.RestaurantMenu,
                    iconBgColor = Color(0xFFD97706),
                    testTag = "settings_menu_categories_btn",
                    onClick = { viewModel.navigateTo(Screen.CATEGORIES_PRODUCTS) }
                )

                // Card 2: العروض والتخفيضات
                SettingsNavRowCard(
                    title = "العروض والتخفيضات",
                    subtitle = "تخفيض المواد، تخفيض الأقسام، وإدارة الخصومات النشطة",
                    icon = Icons.Default.LocalOffer,
                    iconBgColor = Color(0xFFDC2626),
                    testTag = "settings_offers_btn",
                    onClick = { showOffersModal = true }
                )

                // Card 3: إدارة الطاولات والباركود
                SettingsNavRowCard(
                    title = "إدارة الطاولات والباركود",
                    subtitle = "إدارة أرقام الطاولات وتوليد وحفظ أكواد الـ QR",
                    icon = Icons.Default.QrCode2,
                    iconBgColor = Color(0xFF0284C7),
                    testTag = "settings_tables_qr_btn",
                    onClick = { viewModel.navigateTo(Screen.TABLES_QR) }
                )

                // Card 4: بيانات الكافيه
                SettingsNavRowCard(
                    title = "بيانات الكافيه",
                    subtitle = "اسم الكافيه، الشعار، والرسالة الترحيبية للزبائن",
                    icon = Icons.Default.Storefront,
                    iconBgColor = Color(0xFF8B5CF6),
                    testTag = "settings_cafe_info_btn",
                    onClick = { showEditInfoModal = true }
                )

                // Card 5: الثيمات ومظهر المينيو
                SettingsNavRowCard(
                    title = "الثيمات ومظهر المينيو",
                    subtitle = "تخصيص ألوان الواجهة والمظهر العام للتطبيق",
                    icon = Icons.Default.Palette,
                    iconBgColor = Color(0xFF10B981),
                    testTag = "settings_themes_btn",
                    onClick = { showThemeModal = true }
                )

                // Card 6: اللغة
                SettingsNavRowCard(
                    title = "اللغة",
                    subtitle = "التبديل بين العربية والإنجليزية والكردية",
                    icon = Icons.Default.Language,
                    iconBgColor = Color(0xFF3B82F6),
                    testTag = "settings_language_btn",
                    onClick = { showLanguageModal = true }
                )

                // Card 7: تسجيل الخروج (Red border style)
                SettingsNavRowCard(
                    title = "تسجيل الخروج",
                    subtitle = "تسجيل الخروج من حساب الإدارة والكاشير",
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    iconBgColor = Color(0xFFB91C1C),
                    isLogout = true,
                    testTag = "settings_logout_btn",
                    onClick = { viewModel.logout() }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Modal 1: Edit Cafe Info
    if (showEditInfoModal) {
        var cafeName by remember { mutableStateOf(cafe.name) }
        var phone by remember { mutableStateOf(cafe.phone) }
        var welcomeMsg by remember { mutableStateOf(cafe.welcomeMessage) }
        var wifiSsid by remember { mutableStateOf(cafe.wifiSsid) }
        var wifiPass by remember { mutableStateOf(cafe.wifiPassword) }

        AlertDialog(
            onDismissRequest = { showEditInfoModal = false },
            title = {
                Text(
                    text = "بيانات الكافيه",
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = cafeName,
                        onValueChange = { cafeName = it },
                        label = { Text("اسم الكافيه") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف للتواصل") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = welcomeMsg,
                        onValueChange = { welcomeMsg = it },
                        label = { Text("رسالة الترحيب للزبائن") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = wifiSsid,
                        onValueChange = { wifiSsid = it },
                        label = { Text("اسم شبكة الواي فاي (Wi-Fi)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = wifiPass,
                        onValueChange = { wifiPass = it },
                        label = { Text("كلمة مرور الواي فاي") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = cafe.copy(
                            name = cafeName,
                            phone = phone,
                            welcomeMessage = welcomeMsg,
                            wifiSsid = wifiSsid,
                            wifiPassword = wifiPass
                        )
                        viewModel.repository.updateCafeSettings(updated)
                        Toast.makeText(context, "تم حفظ بيانات الكافيه بنجاح!", Toast.LENGTH_SHORT).show()
                        showEditInfoModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("حفظ التغييرات", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditInfoModal = false }) {
                    Text("إلغاء", color = TextGray)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Modal 2: Active Offers
    if (showOffersModal) {
        AlertDialog(
            onDismissRequest = { showOffersModal = false },
            title = {
                Text("🔥 العروض والتخفيضات النشطة", color = GoldPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. خصم 20% على جميع المشروبات الساخنة عند طلب الشيشة", color = TextWhite, fontSize = 13.sp)
                    Text("2. عرض كومبو: أركيلة تفاحتين + شاي بالاستكانة بسعر 7,500 د.ع", color = TextWhite, fontSize = 13.sp)
                    Text("3. تخفيض 15% على قسم الحلويات والمخبوزات طيلة اليوم", color = TextWhite, fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showOffersModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("إغلاق", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Modal 3: Themes
    if (showThemeModal) {
        AlertDialog(
            onDismissRequest = { showThemeModal = false },
            title = { Text("🎨 ثيم ومظهر المينيو", color = GoldPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("✓ الثيم الذهبي الملكي الداكن (الافتراضي والمعتمد)", color = GoldPrimary, fontWeight = FontWeight.Bold)
                    Text("• المظهر الليلي الكربوني (Carbon Midnight)", color = TextGray)
                    Text("• المظهر الزمردي الكلاسيكي (Emerald Luxe)", color = TextGray)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showThemeModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("تم", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Modal 4: Language
    if (showLanguageModal) {
        AlertDialog(
            onDismissRequest = { showLanguageModal = false },
            title = { Text("🌐 لغة النظام والمينيو", color = GoldPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("✓ العربية (العراق) - النشطة حالياً", color = GoldPrimary, fontWeight = FontWeight.Bold)
                    Text("• English (الإنكليزية)", color = TextWhite)
                    Text("• کوردی (الكردية)", color = TextWhite)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showLanguageModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("تأكيد", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun SettingsNavRowCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    isLogout: Boolean = false,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLogout) Color(0xFF1E1414) else Color(0xFF1C1C1E)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isLogout) Color(0x66EF4444) else Color(0xFF2C2C2E)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Chevron Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = if (isLogout) Color(0xFFEF4444) else TextMuted,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Middle Text Details
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isLogout) Color(0xFFEF4444) else TextWhite,
                    textAlign = TextAlign.End
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextGray,
                    textAlign = TextAlign.End,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Right Colored Square Icon Box
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
