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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Phone
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
    val isVerifyingEmail by viewModel.isVerifyingEmail.collectAsState()
    val generatedVerificationCode by viewModel.generatedVerificationCode.collectAsState()
    val lastAttemptEmail by viewModel.lastAttemptEmail.collectAsState()
    val authError by viewModel.authError.collectAsState()

    var loginEmail by remember { mutableStateOf("alikhlel132@gmail.com") }
    var loginPassword by remember { mutableStateOf("1234567") }

    // Register Form Fields as requested
    var regName by remember { mutableStateOf("") }
    var regWhatsapp by remember { mutableStateOf("+964 ") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }

    var inputOtpCode by remember { mutableStateOf("") }

    // Synchronize OTP when generated
    LaunchedEffect(generatedVerificationCode) {
        if (generatedVerificationCode.isNotBlank()) {
            inputOtpCode = generatedVerificationCode
        }
    }

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
                    text = "سحابة Firebase: cafe-bons",
                    style = MaterialTheme.typography.bodySmall,
                    color = GoldSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

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

                if (isVerifyingEmail) {
                    // STEP 2: Email Verification Screen
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_verification_view"),
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
                            Icon(
                                imageVector = Icons.Default.MarkEmailRead,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(46.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "التحقق من البريد الإلكتروني",
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "أدخل رمز التأكيد لتثبيت ملكية البريد:\n$lastAttemptEmail",
                                color = TextWhite,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            // Verification OTP code display box
                            Box(
                                modifier = Modifier
                                    .background(DarkCard, RoundedCornerShape(10.dp))
                                    .border(1.dp, GoldPrimary, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 24.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = generatedVerificationCode.ifBlank { "748921" },
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 4.sp,
                                    color = GoldPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = inputOtpCode,
                                onValueChange = { inputOtpCode = it },
                                placeholder = { Text("أدخل رمز التحقق (OTP)", color = TextMuted) },
                                leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GoldPrimary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("otp_input"),
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

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { viewModel.confirmEmail(inputOtpCode) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("confirm_email_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "تأكيد البريد الإلكتروني والمتابعة ✓",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            TextButton(
                                onClick = { viewModel.skipEmailVerificationToPending() },
                                modifier = Modifier.testTag("skip_to_pending_btn")
                            ) {
                                Text(
                                    text = "الانتقال مباشرة إلى مراجعة الطلب ➡️",
                                    color = TextGray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                } else if (isPendingView) {
                    // STEP 3: Pending Review Message (As Requested by User)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pending_approval_view"),
                        colors = CardDefaults.cardColors(containerColor = Color(0x22F0A500)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "⏳",
                                fontSize = 42.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "تتم مراجعة طلبك\nسيتم تفعيل حسابك باقرب وقت",
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "تم تسجيل طلب كافيهك بالبريد:\n$lastAttemptEmail\n\nيقوم مطور النظام حالياً بمراجعة طلبك وتفعيل الاشتراك من لوحة تحكم المطور السحابية. بعد الموافقة، يمكنك الدخول مباشرة من شاشة تسجيل الدخول.",
                                color = TextWhite,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 19.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Button 1: Go to Login (User's preferred flow)
                    Button(
                        onClick = { viewModel.switchToLoginWithEmail(lastAttemptEmail) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("go_to_login_screen_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "الانتقال إلى تسجيل الدخول 🔑",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Button 2: Quick Check Approval Status
                    Button(
                        onClick = { viewModel.checkPendingApproval() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("check_approval_status_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333333))
                    ) {
                        Text(
                            text = "فحص وتحديث حالة التفعيل 🔄",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
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
                                placeholder = { Text("مثال: alikhlel132@gmail.com أو اسم الكافيه", color = TextMuted) },
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
                                text = "كلمة السر",
                                color = TextGray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = loginPassword,
                                onValueChange = { loginPassword = it },
                                placeholder = { Text("أدخل كلمة السر", color = TextMuted) },
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
                                    text = "تسجيل الدخول",
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
                        // Register Form (Matching User's Specified Flow Exactly)
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // 1. Cafe Name
                            Text(
                                text = "اسم الكافية",
                                color = TextGray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = regName,
                                onValueChange = { regName = it },
                                placeholder = { Text("مثال: كافيه البستان", color = TextMuted) },
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

                            // 2. WhatsApp Number
                            Text(
                                text = "رقم الواتس اب",
                                color = TextGray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = regWhatsapp,
                                onValueChange = { regWhatsapp = it },
                                placeholder = { Text("مثال: +964 770 123 4567", color = TextMuted) },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = GoldPrimary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reg_whatsapp_input"),
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

                            // 3. Email
                            Text(
                                text = "البريد الالكتروني",
                                color = TextGray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = { regEmail = it },
                                placeholder = { Text("مثال: cafe@gmail.com", color = TextMuted) },
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

                            // 4. Password
                            Text(
                                text = "كلمة السر",
                                color = TextGray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = { regPassword = it },
                                placeholder = { Text("أدخل كلمة السر (6 خانات فأكثر)", color = TextMuted) },
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

                            Spacer(modifier = Modifier.height(14.dp))

                            // 5. Confirm Password
                            Text(
                                text = "اعادة كتابة كلمة السر",
                                color = TextGray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = regConfirmPassword,
                                onValueChange = { regConfirmPassword = it },
                                placeholder = { Text("أعد كتابة كلمة السر للتأكيد", color = TextMuted) },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GoldPrimary) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reg_confirm_password_input"),
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

                            Spacer(modifier = Modifier.height(22.dp))

                            // Button: "التالي" (Next)
                            Button(
                                onClick = {
                                    viewModel.register(
                                        name = regName,
                                        whatsappPhone = regWhatsapp,
                                        email = regEmail,
                                        pass = regPassword,
                                        confirmPass = regConfirmPassword
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("register_submit_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "التالي ➡️",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
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
