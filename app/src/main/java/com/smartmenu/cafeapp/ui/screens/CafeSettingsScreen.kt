package com.smartmenu.cafeapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartmenu.cafeapp.ui.theme.*
import com.smartmenu.cafeapp.ui.viewmodel.MainViewModel

@Composable
fun CafeSettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentCafe by viewModel.repository.currentCafe.collectAsState()
    val cafe = currentCafe ?: return

    var cafeName by remember { mutableStateOf(cafe.name) }
    var phone by remember { mutableStateOf(cafe.phone) }
    var currency by remember { mutableStateOf(cafe.currency) }
    var vatPercentageText by remember { mutableStateOf(cafe.vatPercentage.toString()) }
    var totalTablesText by remember { mutableStateOf(cafe.totalTables.toString()) }
    var welcomeMsg by remember { mutableStateOf(cafe.welcomeMessage) }
    var wifiSsid by remember { mutableStateOf(cafe.wifiSsid) }
    var wifiPass by remember { mutableStateOf(cafe.wifiPassword) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier.testTag("settings_back_btn")
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "رجوع", tint = TextWhite)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "إعدادات الكافيه",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "تعديل البيانات الأساسية، الضريبة، والرسائل الترحيبية",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray
                    )
                }
            }
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // General Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "معلومات الكافيه الرئيسية",
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = cafeName,
                        onValueChange = { cafeName = it },
                        label = { Text("اسم الكافيه") },
                        modifier = Modifier.fillMaxWidth().testTag("setting_cafe_name"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف أو خدمة العملاء") },
                        modifier = Modifier.fillMaxWidth().testTag("setting_phone"),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pricing & Tax Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "العملة والضريبة والطاولات",
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = currency,
                            onValueChange = { currency = it },
                            label = { Text("رمز العملة") },
                            modifier = Modifier.weight(1f).testTag("setting_currency"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = vatPercentageText,
                            onValueChange = { vatPercentageText = it },
                            label = { Text("الضريبة (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f).testTag("setting_vat"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = totalTablesText,
                            onValueChange = { totalTablesText = it },
                            label = { Text("عدد الطاولات") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("setting_tables_count"),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Guest Experience & Wi-Fi Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "تجربة الزبون والواي فاي",
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = welcomeMsg,
                        onValueChange = { welcomeMsg = it },
                        label = { Text("الرسالة الترحيبية على منيو الزبون") },
                        modifier = Modifier.fillMaxWidth().testTag("setting_welcome_msg"),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = wifiSsid,
                            onValueChange = { wifiSsid = it },
                            label = { Text("اسم شبكة Wi-Fi") },
                            modifier = Modifier.weight(1f).testTag("setting_wifi_ssid"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = wifiPass,
                            onValueChange = { wifiPass = it },
                            label = { Text("كلمة سر Wi-Fi") },
                            modifier = Modifier.weight(1f).testTag("setting_wifi_pass"),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val vat = vatPercentageText.toDoubleOrNull() ?: 15.0
                    val tables = totalTablesText.toIntOrNull() ?: 12
                    val updated = cafe.copy(
                        name = cafeName,
                        phone = phone,
                        currency = currency,
                        vatPercentage = vat,
                        totalTables = tables,
                        welcomeMessage = welcomeMsg,
                        wifiSsid = wifiSsid,
                        wifiPassword = wifiPass
                    )
                    viewModel.repository.updateCafeSettings(updated)
                    Toast.makeText(context, "تم حفظ الإعدادات بنجاح", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_settings_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text("حفظ التغييرات", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
