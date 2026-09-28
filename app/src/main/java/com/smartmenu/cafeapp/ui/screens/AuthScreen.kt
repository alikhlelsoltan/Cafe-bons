package com.smartmenu.cafeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartmenu.cafeapp.ui.theme.*
import com.smartmenu.cafeapp.ui.viewmodel.AuthTab
import com.smartmenu.cafeapp.ui.viewmodel.MainViewModel
import com.smartmenu.cafeapp.ui.viewmodel.Screen

@Composable
fun AuthScreen(viewModel: MainViewModel) {
    val authTab by viewModel.authTab.collectAsState()
    val isPendingView by viewModel.isPendingView.collectAsState()
    val authError by viewModel.authError.collectAsState()

    var loginCafeId by remember { mutableStateOf("bustan-cafe") }
    var loginPassword by remember { mutableStateOf("123") }

    var regName by remember { mutableStateOf("") }
    var regSlug by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .verticalScroll(rememberScrollState())
                .testTag("auth_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo Box
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(DarkCard, RoundedCornerShape(18.dp))
                        .border(2.dp, GoldPrimary, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "☕", fontSize = 34.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "سمارت مينيو - نظام الكافيهات",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "معرف الحزمة: com.smartmenu.cafeapp",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Text(
                    text = "Firebase Project: cafe-bons",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (isPendingView) {
                    // Pending Approval View
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pending_approval_view"),
                        colors = CardDefaults.cardColors(containerColor = Color(0x22F0A500)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "⏳ تم إرسال طلبك بنجاح!",
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "حسابك الآن قيد المراجعة وبانتظار موافقة الأدمن لتفعيل لوحة التحكم والمنيو الخاص بك.",
                                color = TextWhite,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.showPendingView(false) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("back_to_login_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("العودة لتسجيل الدخول", color = TextWhite)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Shortcut to Super Admin for convenience in reviewing/approving
                    TextButton(
                        onClick = { viewModel.navigateTo(Screen.SUPER_ADMIN) },
                        modifier = Modifier.testTag("admin_shortcut_btn")
                    ) {
                        Icon(Icons.Default.SupervisorAccount, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("لوحة موافقة المشرف (Super Admin)", color = GoldPrimary, fontSize = 12.sp)
                    }

                } else {
                    // Auth Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkCard, RoundedCornerShape(10.dp))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (authTab == AuthTab.LOGIN) GoldPrimary else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setAuthTab(AuthTab.LOGIN) }
                                .padding(vertical = 10.dp)
                                .testTag("login_tab"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "تسجيل الدخول",
                                fontWeight = FontWeight.Bold,
                                color = if (authTab == AuthTab.LOGIN) Color.Black else TextGray,
                                fontSize = 14.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (authTab == AuthTab.REGISTER) GoldPrimary else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setAuthTab(AuthTab.REGISTER) }
                                .padding(vertical = 10.dp)
                                .testTag("register_tab"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "إنشاء حساب كافيه",
                                fontWeight = FontWeight.Bold,
                                color = if (authTab == AuthTab.REGISTER) Color.Black else TextGray,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Error Message Banner
                    authError?.let { err ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0x33D9534F)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = err,
                                color = Color(0xFFFF8888),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (authTab == AuthTab.LOGIN) {
                        // Login Form
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "معرف الكافيه (Cafe ID)",
                                color = TextGray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = loginCafeId,
                                onValueChange = { loginCafeId = it },
                                placeholder = { Text("مثال: bustan-cafe", color = TextMuted) },
                                leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = GoldPrimary) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_cafe_id_input"),
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

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "كلمة المرور",
                                color = TextGray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = loginPassword,
                                onValueChange = { loginPassword = it },
                                placeholder = { Text("أدخل كلمة المرور", color = TextMuted) },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GoldPrimary) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_password_input"),
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

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { viewModel.login(loginCafeId, loginPassword) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("login_submit_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "دخول لوحة التحكم",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "حساب تجريبي مسبق معتمد: bustan-cafe / 123",
                                fontSize = 12.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        // Register Form
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "اسم الكافيه",
                                color = TextGray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = regName,
                                onValueChange = { regName = it },
                                placeholder = { Text("مثال: كافيه الروشة", color = TextMuted) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reg_name_input"),
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

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "معرف فريد بالإنجليزية (Cafe ID)",
                                color = TextGray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = regSlug,
                                onValueChange = { regSlug = it },
                                placeholder = { Text("مثال: rawsheh-cafe", color = TextMuted) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reg_slug_input"),
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

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "كلمة المرور",
                                color = TextGray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = { regPassword = it },
                                placeholder = { Text("اختر كلمة مرور قوية", color = TextMuted) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reg_password_input"),
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

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { viewModel.register(regName, regSlug, regPassword) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("register_submit_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "إرسال طلب التسجيل",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Divider(color = DarkBorder)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.navigateTo(Screen.SUPER_ADMIN) },
                            modifier = Modifier.testTag("nav_super_admin_btn")
                        ) {
                            Icon(Icons.Default.SupervisorAccount, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("لوحة إدارة المشرف العام (Approvals)", color = GoldSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
