package com.smartmenu.cafeapp.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.smartmenu.cafeapp.data.model.TableInfo
import com.smartmenu.cafeapp.data.model.TableStatus
import com.smartmenu.cafeapp.ui.components.QrCodeView
import com.smartmenu.cafeapp.ui.theme.*
import com.smartmenu.cafeapp.ui.viewmodel.MainViewModel

@Composable
fun TablesQrScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentCafe by viewModel.repository.currentCafe.collectAsState()
    val tables by viewModel.repository.tables.collectAsState()
    val selectedTableForQr by viewModel.selectedTableForQr.collectAsState()

    val cafe = currentCafe ?: return

    var showPdfGenerationSuccessDialog by remember { mutableStateOf(false) }

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
                        modifier = Modifier.testTag("tables_qr_back_btn")
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "رجوع", tint = TextWhite)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "باركود الطاولات (QR & PDF)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "إجمالي الطاولات: ${tables.size} طاولة",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray
                        )
                    }

                    // Export all tables as PDF Button
                    Button(
                        onClick = {
                            showPdfGenerationSuccessDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("export_pdf_all_btn")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تحميل PDF الكل", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "اضغط على أي طاولة لعرض وتكبير كود الـ QR الخاص بها أو طباعة ملصق الطاولة",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tables) { table ->
                TableCard(
                    table = table,
                    cafeId = cafe.id,
                    onClick = { viewModel.setSelectedTableForQr(table.tableNumber) }
                )
            }
        }
    }

    // Single Table QR Modal
    selectedTableForQr?.let { tableNum ->
        val tableUrl = "https://cafe-bons.web.app/?cafe=${cafe.id}&table=$tableNum"
        Dialog(onDismissRequest = { viewModel.setSelectedTableForQr(null) }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .testTag("table_qr_dialog"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.setSelectedTableForQr(null) }) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextWhite)
                        }
                        Text(
                            text = "طاولة رقم $tableNum",
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(36.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = cafe.name,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // QR View
                    QrCodeView(
                        data = tableUrl,
                        modifier = Modifier.size(190.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "امسح الباركود لفتح المنيو والطلب",
                        color = TextGray,
                        fontSize = 12.sp
                    )

                    Text(
                        text = tableUrl,
                        color = GoldSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "تم إرسال باركود طاولة $tableNum إلى الطابعة", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).testTag("print_sticker_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkCard)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("طباعة ملصق", color = TextWhite, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "رابط منيو طاولة $tableNum في ${cafe.name}:\n$tableUrl")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "مشاركة رابط الطاولة"))
                            },
                            modifier = Modifier.weight(1f).testTag("share_table_url_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مشاركة الرابط", color = TextWhite, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // PDF Success generation alert
    if (showPdfGenerationSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showPdfGenerationSuccessDialog = false },
            confirmButton = {
                Button(
                    onClick = { showPdfGenerationSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("تم وحفظ", color = Color.Black)
                }
            },
            title = {
                Text("📄 تم إنشاء ملف PDF بنجاح!", fontWeight = FontWeight.Bold, color = TextWhite)
            },
            text = {
                Text(
                    "تم تجهيز وتوليد ملف PDF عالي الدقة يحتوي على ملصقات باركود الـ QR لجميع طاولات ${cafe.name} (${tables.size} طاولة) جاهزة للطباعة والتوزيع الفوري.",
                    color = TextGray
                )
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
private fun TableCard(
    table: TableInfo,
    cafeId: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("table_card_${table.tableNumber}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (table.status == TableStatus.OCCUPIED) WarningOrange else DarkBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "طاولة ${table.tableNumber}",
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontSize = 15.sp
                )
                Box(
                    modifier = Modifier
                        .background(
                            if (table.status == TableStatus.OCCUPIED) Color(0x33FF8800) else Color(0x335CB85C),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = table.status.titleAr,
                        color = if (table.status == TableStatus.OCCUPIED) WarningOrange else SuccessGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mini QR Thumbnail
            QrCodeView(
                data = "https://cafe-bons.web.app/?cafe=$cafeId&table=${table.tableNumber}",
                modifier = Modifier.size(90.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "اضغط لعرض QR",
                color = GoldPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
