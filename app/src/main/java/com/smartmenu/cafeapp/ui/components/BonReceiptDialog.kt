package com.smartmenu.cafeapp.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.smartmenu.cafeapp.data.model.Cafe
import com.smartmenu.cafeapp.data.model.Order
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BonReceiptDialog(
    cafe: Cafe,
    order: Order,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar"))
    val formattedDate = dateFormat.format(Date(order.createdAt))

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("bon_receipt_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("bon_close_button")) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Black)
                    }
                    Text(
                        text = "بون الطلب (Receipt)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(36.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Thermal receipt paper styling
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = cafe.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "رقم الطاولة: ${order.tableNumber}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFD97706)
                        )
                        Text(
                            text = "رقم البون: #${order.id.takeLast(6).uppercase()}",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.Gray
                        )
                        Text(
                            text = formattedDate,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Divider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = Color.LightGray,
                            thickness = 1.dp
                        )

                        // Table header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("المادة", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                            Text("العدد", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                            Text("السعر", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                        }

                        Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color.LightGray)

                        order.items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item.nameArabic,
                                    fontSize = 13.sp,
                                    color = Color.Black,
                                    modifier = Modifier.weight(2f)
                                )
                                Text(
                                    text = "× ${item.quantity}",
                                    fontSize = 13.sp,
                                    color = Color.Black,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "${"%.2f".format(item.price * item.quantity)} ${cafe.currency}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                    modifier = Modifier.weight(1.5f),
                                    textAlign = TextAlign.End
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray)

                        // Totals
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("المجموع الفرعي:", fontSize = 13.sp, color = Color.DarkGray)
                            Text("${"%.2f".format(order.subtotal)} ${cafe.currency}", fontSize = 13.sp, color = Color.Black)
                        }
                        if (cafe.vatPercentage > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("ضريبة القيمة المضافة (${cafe.vatPercentage}%):", fontSize = 12.sp, color = Color.DarkGray)
                                Text("${"%.2f".format(order.vat)} ${cafe.currency}", fontSize = 12.sp, color = Color.Black)
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("الإجمالي الكلي:", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                            Text(
                                "${"%.2f".format(order.total)} ${cafe.currency}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color(0xFFD97706)
                            )
                        }

                        if (order.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "ملاحظات الزبون: ${order.notes}",
                                fontSize = 12.sp,
                                color = Color.DarkGray,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // QR on receipt
                        QrCodeView(
                            data = "https://cafe-bons.web.app/?cafe=${cafe.id}&order=${order.id}",
                            modifier = Modifier.size(90.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = cafe.welcomeMessage,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Print and Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            Toast.makeText(context, "تم إرسال البون إلى طابعة الكاشير / البلوتوث بنجاح", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("print_bon_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E))
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, tint = Color(0xFFF0A500))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("طباعة", color = Color.White)
                    }

                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    """
                                    ☕ ${cafe.name} - بون طلب
                                    طاولة رقم: ${order.tableNumber}
                                    رقم البون: #${order.id.takeLast(6).uppercase()}
                                    الإجمالي: ${"%.2f".format(order.total)} ${cafe.currency}
                                    شكراً لزيارتكم!
                                    """.trimIndent()
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "مشاركة بون الطلب"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_bon_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFFD97706))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشاركة", color = Color.Black)
                    }
                }
            }
        }
    }
}
