package com.smartmenu.cafeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun AuthScreen(viewModel: MainViewModel) {
    val authTab by viewModel.authTab.collectAsState()
    val isPendingView by viewModel.isPendingView.collectAsState()
    val authError by viewModel.authError.collectAsState()

    var loginEmail by remember { mutableStateOf("alikhlel132@gmail.com") }
    var loginPassword by remember { mutableStateOf("123") }

    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
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
                // Cafe Logo
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
                    text = "Firebase Auth & Firestore: cafe-bons",
                    style = MaterialTheme.typography.bodySmall,
                    color = GoldSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
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
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "⏳ تم استلام طلبك بنجاح!",
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary,
                                fontSize = 17.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "حساب الكافيه الآن قيد المراجعة. سيقوم مطور النظام باعتماد حسابك وتفعيله عبر لوحة تحكم المطور السحابية، وستتمكن من الدخول مباشرة فور الموافقة.",
                                color = TextWhite,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 19.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.checkPendingApproval() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("check_approval_status_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("التحقق من اعتماد الحساب 🔄", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.showPendingView(false) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("back_to_login_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333333))
                    ) {
                        Text("العودة لتسجيل الدخول", color = TextWhite, fontWeight = FontWeight.Bold)
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
                                .padding(bottom = 14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0x33D9534F)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = err,
                                color = Color(0xFFFF9999),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (authTab == AuthTab.LOGIN) {
                        // Login Form
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "البريد الإلكتروني",
                                color = TextGray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = loginEmail,
                                onValueChange = { loginEmail = it },
                                placeholder = { Text("مثال: owner@cafe.com", color = TextMuted) },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = GoldPrimary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_email_input"),
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
                                onClick = { viewModel.login(loginEmail, loginPassword) },
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

                            Spacer(modifier = Modifier.height(16.dp))

                            // Or Divider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF2C2C2E))
                                Text(
                                    text = "أو",
                                    color = TextGray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF2C2C2E))
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Google Sign-In Button
                            GoogleSignInButton(
                                text = "الدخول السريع بواسطة Google",
                                onClick = {
                                    viewModel.loginWithGoogle(
                                        email = loginEmail.ifBlank { "alikhlel132@gmail.com" },
                                        displayName = "كافيه البستان"
                                    )
                                },
                                testTag = "google_login_btn"
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
                                leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = GoldPrimary) },
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
                                text = "البريد الإلكتروني",
                                color = TextGray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = { regEmail = it },
                                placeholder = { Text("مثال: owner@gmail.com", color = TextMuted) },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = GoldPrimary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reg_email_input"),
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
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GoldPrimary) },
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
                                onClick = { viewModel.register(regName, regEmail, regPassword) },
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

                            Spacer(modifier = Modifier.height(16.dp))

                            // Or Divider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF2C2C2E))
                                Text(
                                    text = "أو",
                                    color = TextGray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF2C2C2E))
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Google Sign-Up Button
                            GoogleSignInButton(
                                text = "التسجيل السريع بواسطة Google",
                                onClick = {
                                    val email = regEmail.ifBlank { "alikhlel132@gmail.com" }
                                    val name = regName.ifBlank { "كافيه ${email.substringBefore("@")}" }
                                    viewModel.loginWithGoogle(email = email, displayName = name)
                                },
                                testTag = "google_register_btn"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoogleSignInButton(
    text: String,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF242426)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38383A))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Google "G" Badge
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "G",
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF4285F4),
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = text,
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
