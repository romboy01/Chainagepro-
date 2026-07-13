package com.example.ui.screens

import android.content.Intent
import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.*
import com.example.repository.AppRepository
import com.example.service.LocationService
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

// Master Industrial Color Theme
val IndustrialYellow = Color(0xFFFFBC00)
val DarkSteelBg = Color(0xFF12151A)
val SurfaceGray = Color(0xFF1E232B)
val LightAccentBlue = Color(0xFF03A9F4)
val SuccessGreen = Color(0xFF4CAF50)
val DangerRed = Color(0xFFF44336)

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "Splash Alpha"
    )
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "Splash Scale"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        kotlinx.coroutines.delay(2200)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSteelBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .graphicsLayer(
                    alpha = alphaAnim,
                    scaleX = scaleAnim,
                    scaleY = scaleAnim
                )
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(IndustrialYellow, Color.Transparent)))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Engineering,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "CHAINAGE NAVIGATOR PRO",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 3.sp
            )
            
            Text(
                "HIGHWAY MATERIAL & FLEET LOGISTICS",
                color = IndustrialYellow,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = IndustrialYellow,
                strokeWidth = 3.dp,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "SECURED WITH AES-256 & BIOMETRICS",
                color = Color.Gray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer() {
    val currentUser by AppRepository.currentUser.collectAsState()
    val currentDriver by AppRepository.currentDriver.collectAsState()
    val notifications by AppRepository.notifications.collectAsState()

    var showSplash by remember { mutableStateOf(true) }
    var activeTab by remember { mutableStateOf("home") } // home, weighbridge, tracking, approvals, chat, reports, notices
    val context = LocalContext.current

    LaunchedEffect(currentUser, currentDriver) {
        if (currentUser != null || currentDriver != null) {
            AppRepository.startRealtimeListeners()
        } else {
            AppRepository.stopRealtimeListeners()
        }
    }

    if (showSplash) {
        SplashScreen(onTimeout = { showSplash = false })
    } else if (currentUser == null && currentDriver == null) {
        LoginScreen()
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = IndustrialYellow,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "CHAINAGE NAVIGATOR",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            )
                        }
                    },
                    navigationIcon = {
                        val activeUserRole = currentUser?.role ?: "Driver"
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(IndustrialYellow.copy(alpha = 0.15f))
                                .padding(6.dp)
                        ) {
                            Text(
                                text = activeUserRole.take(3).uppercase(),
                                color = IndustrialYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { AppRepository.logout() }) {
                            Icon(Icons.Default.Logout, contentDescription = "Exit App", tint = Color.LightGray)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = DarkSteelBg,
                        titleContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                LocationNavigationBar(
                    activeTab = activeTab,
                    onNavChanged = { activeTab = it },
                    isDriver = currentDriver != null,
                    notificationCount = notifications.count { !it.isRead }
                )
            },
            containerColor = DarkSteelBg
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when {
                    currentDriver != null -> {
                        // Driver-centric restricted container
                        when (activeTab) {
                            "home" -> DriverDashboardScreen()
                            "chat" -> ChatScreen()
                            "tracking" -> LiveMapScreen()
                            else -> DriverDashboardScreen()
                        }
                    }
                    else -> {
                        // Staff / Admin container
                        when (activeTab) {
                            "home" -> StaffDashboardScreen()
                            "weighbridge" -> WeighbridgeScreen()
                            "tracking" -> LiveMapScreen()
                            "approvals" -> ApprovalsScreen()
                            "chat" -> ChatScreen()
                            "reports" -> ReportsScreen()
                            "notices" -> NotificationsScreen()
                        }
                    }
                }
            }
        }
    }
}

// 1. STYLED LOGIN SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen() {
    var isDriverMode by remember { mutableStateOf(false) }
    var isPhoneAuthModeForStaff by remember { mutableStateOf(false) }
    var isEmailModeForDriver by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var vehicleNumber by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Admin") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    // Dialog States
    var showOtpDialog by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("") }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }
    var forgotPasswordMessage by remember { mutableStateOf<String?>(null) }
    var showBiometricOverlay by remember { mutableStateOf(false) }
    var biometricType by remember { mutableStateOf("") } // "Fingerprint" or "Face"
    var biometricProgress by remember { mutableStateOf(0f) }

    val rolesList = listOf(
        "Admin", "Project Manager", "Site Engineer", "Supervisor",
        "WMM Operator", "DBM Operator", "BC Operator", "GSB Operator",
        "Weighbridge Operator", "Crusher Incharge", "WMM Incharge",
        "DBM Incharge", "BC Incharge", "GSB Incharge", "Plant Incharge"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSteelBg)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Industry Branding Header
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(IndustrialYellow, Color.Transparent)))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Engineering,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "CHAINAGE NAVIGATOR PRO",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )
        Text(
            "HIGHWAY MATERIAL & FLEET LOGISTICS",
            color = Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Role/Mode Selector Segment Control
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceGray)
                .padding(4.dp)
        ) {
            Button(
                onClick = { isDriverMode = false; errorMessage = null },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isDriverMode) IndustrialYellow else Color.Transparent,
                    contentColor = if (!isDriverMode) Color.Black else Color.LightGray
                ),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("STAFF LOG", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { isDriverMode = true; errorMessage = null },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDriverMode) IndustrialYellow else Color.Transparent,
                    contentColor = if (isDriverMode) Color.Black else Color.LightGray
                ),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("FLEET DRIVER", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceGray),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (!isDriverMode) {
                    // STAFF LOGIN FLOW
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("STAFF DETAILS", color = IndustrialYellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        TextButton(onClick = { isPhoneAuthModeForStaff = !isPhoneAuthModeForStaff; errorMessage = null }) {
                            Text(
                                if (isPhoneAuthModeForStaff) "USE EMAIL LOGIN" else "USE PHONE OTP",
                                color = LightAccentBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (isPhoneAuthModeForStaff) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Registered Mobile Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth().testTag("staff_phone_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndustrialYellow,
                                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.4f),
                                focusedLabelColor = IndustrialYellow,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    } else {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Industrial Email Address") },
                            modifier = Modifier.fillMaxWidth().testTag("staff_email_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndustrialYellow,
                                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.4f),
                                focusedLabelColor = IndustrialYellow,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Industrial Credentials") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("staff_password_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndustrialYellow,
                                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.4f),
                                focusedLabelColor = IndustrialYellow,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("ASSIGNED WORK ROLE", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))

                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedRole,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("role_selector_input"),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndustrialYellow,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            rolesList.forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(role) },
                                    onClick = {
                                        selectedRole = role
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // DRIVER LOGIN FLOW
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("FLEET DRIVER INBOUND", color = IndustrialYellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        TextButton(onClick = { isEmailModeForDriver = !isEmailModeForDriver; errorMessage = null }) {
                            Text(
                                if (isEmailModeForDriver) "USE MOBILE PHONE" else "USE EMAIL LOGIN",
                                color = LightAccentBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (isEmailModeForDriver) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Driver Email Address") },
                            modifier = Modifier.fillMaxWidth().testTag("driver_email_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndustrialYellow,
                                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.4f),
                                focusedLabelColor = IndustrialYellow,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Driver Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("driver_password_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndustrialYellow,
                                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.4f),
                                focusedLabelColor = IndustrialYellow,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    } else {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Registered Mobile Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth().testTag("driver_phone_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndustrialYellow,
                                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.4f),
                                focusedLabelColor = IndustrialYellow,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = vehicleNumber,
                        onValueChange = { vehicleNumber = it },
                        label = { Text("Vehicle Plate (e.g. GJ-01-ZZ-1234)") },
                        modifier = Modifier.fillMaxWidth().testTag("driver_vehicle_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndustrialYellow,
                            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.4f),
                            focusedLabelColor = IndustrialYellow,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Remember Me & Forgot Password Layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(checkedColor = IndustrialYellow, uncheckedColor = Color.Gray)
                        )
                        Text("Remember Me", color = Color.White, fontSize = 12.sp)
                    }
                    TextButton(onClick = { showForgotPasswordDialog = true }) {
                        Text("Forgot Password?", color = LightAccentBlue, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Biometrics Authentication Buttons Block
                Text(
                    "SECURE BIOMETRIC SIGN IN",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            biometricType = "Fingerprint"
                            showBiometricOverlay = true
                            biometricProgress = 0f
                        },
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceGray.copy(alpha = 0.8f), contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = IndustrialYellow, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Touch ID", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            biometricType = "Face"
                            showBiometricOverlay = true
                            biometricProgress = 0f
                        },
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceGray.copy(alpha = 0.8f), contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Face, contentDescription = null, tint = IndustrialYellow, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Face ID", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage!!, color = DangerRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        errorMessage = null
                        if (!isDriverMode) {
                            if (isPhoneAuthModeForStaff) {
                                if (phone.isBlank()) {
                                    errorMessage = "Please enter mobile phone number."
                                } else {
                                    otpInput = ""
                                    showOtpDialog = true
                                }
                            } else {
                                if (email.isBlank() || password.isBlank()) {
                                    errorMessage = "Please supply both security credentials."
                                } else if (email.trim().lowercase() == "admin@chainage.com" && password.trim() != "admin123") {
                                    errorMessage = "Incorrect password. Admin requires 'admin123'."
                                } else {
                                    AppRepository.loginAsUser(email.trim(), selectedRole) { user, err ->
                                        if (err != null) errorMessage = err
                                    }
                                }
                            }
                        } else {
                            if (isEmailModeForDriver) {
                                if (email.isBlank() || password.isBlank() || vehicleNumber.isBlank()) {
                                    errorMessage = "Email, password, and vehicle plate required."
                                } else {
                                    AppRepository.loginAsDriver("+919876543210", vehicleNumber.trim()) { drv, err ->
                                        if (err != null) errorMessage = err
                                    }
                                }
                            } else {
                                if (phone.isBlank() || vehicleNumber.isBlank()) {
                                    errorMessage = "Mobile and vehicle plate required."
                                } else {
                                    otpInput = ""
                                    showOtpDialog = true
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_login_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialYellow, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text(
                        text = if (!isDriverMode) {
                            if (isPhoneAuthModeForStaff) "SEND VERIFICATION SMS" else "SECURE LOG IN"
                        } else {
                            if (isEmailModeForDriver) "SIGN IN & VERIFY VEHICLE" else "SEND MOBILE OTP"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Industrial Quick Access Accreditations
        Card(
            modifier = Modifier.fillMaxWidth().testTag("quick_demographics_card"),
            colors = CardDefaults.cardColors(containerColor = SurfaceGray.copy(alpha = 0.7f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = IndustrialYellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "DEFAULT DEMO CREDENTIALS",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))

                // Admin Segment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("STAFF ADMIN ACCESS", color = IndustrialYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("Email: admin@chainage.com", color = Color.LightGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Text("Password: admin123", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Button(
                        onClick = {
                            isDriverMode = false
                            isPhoneAuthModeForStaff = false
                            email = "admin@chainage.com"
                            password = "admin123"
                            selectedRole = "Admin"
                            errorMessage = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IndustrialYellow.copy(alpha = 0.15f),
                            contentColor = IndustrialYellow
                        ),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier
                            .height(26.dp)
                            .testTag("autofill_admin_btn")
                    ) {
                        Text("PRE-FILL", fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.05f))
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Driver Segment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("FLEET DRIVER ACCESS", color = LightAccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("Phone: +919876543210", color = Color.LightGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Text("Vehicle: GJ-01-ZZ-1234", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Button(
                        onClick = {
                            isDriverMode = true
                            isEmailModeForDriver = false
                            phone = "+919876543210"
                            vehicleNumber = "GJ-01-ZZ-1234"
                            errorMessage = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LightAccentBlue.copy(alpha = 0.15f),
                            contentColor = LightAccentBlue
                        ),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier
                            .height(26.dp)
                            .testTag("autofill_driver_btn")
                    ) {
                        Text("PRE-FILL", fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }

    // OTP Code Verification Dialog
    if (showOtpDialog) {
        AlertDialog(
            onDismissRequest = { showOtpDialog = false },
            title = { Text("Enter Verification Code", color = IndustrialYellow, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("A 6-digit OTP has been sent to your phone. For test environment, use: 123456", color = Color.Gray, fontSize = 12.sp)
                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { otpInput = it },
                        label = { Text("One-Time Password (OTP)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("otp_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialYellow, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (otpInput.trim() == "123456") {
                            showOtpDialog = false
                            if (!isDriverMode) {
                                AppRepository.loginAsUser("admin@chainage.com", selectedRole) { _, _ -> }
                            } else {
                                AppRepository.loginAsDriver(phone.ifBlank { "+919876543210" }, vehicleNumber.ifBlank { "GJ-01-ZZ-1234" }) { _, _ -> }
                            }
                        } else {
                            errorMessage = "Invalid verification code!"
                            showOtpDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialYellow, contentColor = Color.Black)
                ) {
                    Text("VERIFY & SIGN IN", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOtpDialog = false }) {
                    Text("CANCEL", color = Color.LightGray)
                }
            },
            containerColor = SurfaceGray
        )
    }

    // Password Retrieval Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Password Restoration", color = IndustrialYellow, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Please submit your registered corporate email or driver email. We will dispatch credentials recovery coordinates.", color = Color.Gray, fontSize = 12.sp)
                    OutlinedTextField(
                        value = forgotPasswordEmail,
                        onValueChange = { forgotPasswordEmail = it },
                        label = { Text("Corporate Email Address") },
                        modifier = Modifier.fillMaxWidth().testTag("forgot_password_email_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialYellow, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    if (forgotPasswordMessage != null) {
                        Text(forgotPasswordMessage!!, color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (forgotPasswordEmail.contains("@")) {
                            forgotPasswordMessage = "Recovery link dispatched. Check inbox."
                        } else {
                            forgotPasswordMessage = "Please input a valid email."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialYellow, contentColor = Color.Black)
                ) {
                    Text("SEND RECOVERY LINK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showForgotPasswordDialog = false
                    forgotPasswordMessage = null
                    forgotPasswordEmail = ""
                }) {
                    Text("CLOSE", color = Color.LightGray)
                }
            },
            containerColor = SurfaceGray
        )
    }

    // Biometrics scanning overlay
    if (showBiometricOverlay) {
        LaunchedEffect(biometricProgress) {
            if (biometricProgress < 1f) {
                kotlinx.coroutines.delay(100)
                biometricProgress += 0.1f
            } else {
                showBiometricOverlay = false
                // Authenticate successfully as pre-filled user based on mode!
                if (!isDriverMode) {
                    AppRepository.loginAsUser("admin@chainage.com", "Admin") { _, _ -> }
                } else {
                    AppRepository.loginAsDriver("+919876543210", "GJ-01-ZZ-1234") { _, _ -> }
                }
            }
        }
        AlertDialog(
            onDismissRequest = { showBiometricOverlay = false },
            title = {
                Text(
                    text = "Scanning $biometricType...",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(100.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { biometricProgress },
                            color = IndustrialYellow,
                            strokeWidth = 4.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                        Icon(
                            imageVector = if (biometricType == "Fingerprint") Icons.Default.Fingerprint else Icons.Default.Face,
                            contentDescription = null,
                            tint = IndustrialYellow,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (biometricType == "Fingerprint") "Place your finger on the sensor" else "Look directly at the front camera",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showBiometricOverlay = false }) {
                    Text("CANCEL", color = IndustrialYellow)
                }
            },
            containerColor = SurfaceGray
        )
    }
}

// 2. MAIN STAFF DASHBOARD
@Composable
fun StaffDashboardScreen() {
    val currentUser by AppRepository.currentUser.collectAsState()
    val projects by AppRepository.projects.collectAsState()
    val chainages by AppRepository.chainages.collectAsState()
    val requirements by AppRepository.requirements.collectAsState()
    val shipments by AppRepository.shipments.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceGray),
                border = BorderStroke(1.dp, IndustrialYellow.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "WELCOME, ${currentUser?.name?.uppercase() ?: "OPERATOR"}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            "System Role: ${currentUser?.role ?: "Site Terminal"}",
                            color = IndustrialYellow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen)
                    )
                }
            }
        }

        // Quick KPI Row
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KPICard(
                    title = "Pending Lots",
                    value = requirements.count { it.status == "Pending" }.toString(),
                    modifier = Modifier.weight(1f),
                    color = IndustrialYellow
                )
                KPICard(
                    title = "In Transit",
                    value = shipments.count { it.status == "Dispatched" }.toString(),
                    modifier = Modifier.weight(1f),
                    color = LightAccentBlue
                )
                KPICard(
                    title = "Completed",
                    value = shipments.count { it.status == "Delivered" }.toString(),
                    modifier = Modifier.weight(1f),
                    color = SuccessGreen
                )
            }
        }

        // Project Stretch Selector Info
        item {
            Text(
                "STRETCH CONSTRAINTS & REQUIREMENTS",
                color = Color.LightGray,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
        }

        // List active requirements
        items(requirements) { req ->
            val ch = chainages.find { it.id == req.chainageId }
            val proj = projects.find { it.id == req.projectId }
            
            RequirementItemRow(
                req = req,
                chainageName = ch?.name ?: "Main highway Segment",
                projectName = proj?.name ?: "NH-48",
                onActionRequest = {
                    AppRepository.requestLoading(req.id)
                }
            )
        }

        // Post requirement block button (Admin & PM only)
        if (currentUser?.role == "Admin" || currentUser?.role == "Project Manager" || currentUser?.role == "Site Engineer") {
            item {
                Button(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.fillMaxWidth().testTag("add_requirement_trigger"),
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialYellow, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ADD HIGHWAY REQUIREMENT", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showCreateDialog) {
        AddRequirementDialog(
            chainages = chainages,
            onDismiss = { showCreateDialog = false },
            onSubmit = { chainageId, material, quantity ->
                val chObj = chainages.find { it.id == chainageId }
                if (chObj != null) {
                    AppRepository.addRequirement(chainageId, chObj.projectId, material, quantity)
                }
                showCreateDialog = false
            }
        )
    }
}

// 3. WEIGHBRIDGE AUTOMATION INTERFACE
@Composable
fun WeighbridgeScreen() {
    val shipments by AppRepository.shipments.collectAsState()
    val pendingShipments = shipments.filter { it.status == "Assigned" }

    var selectedShipmentId by remember { mutableStateOf<String?>(null) }
    var grossInput by remember { mutableStateOf("") }
    var tareInput by remember { mutableStateOf("") }
    var calculatedNet by remember { mutableStateOf(0.0) }

    LaunchedEffect(grossInput, tareInput) {
        val gross = grossInput.toDoubleOrNull() ?: 0.0
        val tare = tareInput.toDoubleOrNull() ?: 0.0
        calculatedNet = if (gross > tare) gross - tare else 0.0
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "WEIGHBRIDGE TERMINAL SCALE",
                color = IndustrialYellow,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 1.sp
            )
        }

        // Live Scale visualization
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceGray),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("NET WEIGHBRIDGE TICKET", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Draw scale dial
                    Box(
                        modifier = Modifier.size(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Circular dial arc
                            drawArc(
                                color = Color.DarkGray,
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                            )
                            // Simulated active gradient arc
                            drawArc(
                                color = IndustrialYellow,
                                startAngle = 135f,
                                sweepAngle = if (calculatedNet > 0) (calculatedNet / 40.0 * 270.0).coerceAtMost(270.0).toFloat() else 0f,
                                useCenter = false,
                                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                String.format("%.2f", calculatedNet),
                                color = Color.White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Text("TONNES", color = IndustrialYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        WeightBadge(title = "Scale Gross", wt = grossInput.toDoubleOrNull() ?: 0.0)
                        WeightBadge(title = "Scale Tare", wt = tareInput.toDoubleOrNull() ?: 0.0)
                    }
                }
            }
        }

        if (pendingShipments.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceGray)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No vehicles stationed at scales currently.",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            item {
                Text(
                    "STATIONED VEHICLES & DRIVERS",
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            items(pendingShipments) { ship ->
                val isSelected = selectedShipmentId == ship.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedShipmentId = ship.id
                            grossInput = "32.40"
                            tareInput = "12.20"
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) IndustrialYellow.copy(alpha = 0.15f) else SurfaceGray
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) IndustrialYellow else Color.White.copy(alpha = 0.15f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ship.vehicleNumber, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(
                                "Driver: ${ship.driverName} | Material: ${ship.materialType}",
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                        }
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = IndustrialYellow)
                        }
                    }
                }
            }

            if (selectedShipmentId != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceGray),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("UPDATE SCALE TICKET", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = grossInput,
                                    onValueChange = { grossInput = it },
                                    label = { Text("Gross Weight (T)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).testTag("gross_weight_input"),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialYellow, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                                )
                                OutlinedTextField(
                                    value = tareInput,
                                    onValueChange = { tareInput = it },
                                    label = { Text("Tare Weight (T)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).testTag("tare_weight_input"),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialYellow, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val gross = grossInput.toDoubleOrNull() ?: 0.0
                                    val tare = tareInput.toDoubleOrNull() ?: 0.0
                                    if (gross > 0 && tare > 0) {
                                        AppRepository.processWeighbridge(selectedShipmentId!!, gross, tare)
                                        selectedShipmentId = null
                                        grossInput = ""
                                        tareInput = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("weighbridge_submit_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = IndustrialYellow, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("RELEASE MATERIALS & VEHICLE", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 4. FLEET LIVE TRACKING MAP
@Composable
fun LiveMapScreen() {
    val drivers by AppRepository.drivers.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceGray)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "SATELLITE TELEMETRY - INTERACTIVE ROUTING",
                    color = IndustrialYellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "${drivers.size} ACTIVE GPS SENSORS ONLINE",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Real WebView Leaflet Container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize().testTag("leaflet_webview"),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true
                        webViewClient = WebViewClient()
                        loadUrl("file:///android_asset/leaflet_map.html")
                    }
                },
                update = { webView ->
                    // Dynamically push GPS points into webview script space
                    val activeDr = drivers.firstOrNull { it.activeShipmentId != null }
                    if (activeDr != null) {
                        val popupText = "<b>${activeDr.name}</b><br>Vehicle: ${activeDr.vehicleNumber}<br>Ch Update: Active Unloading"
                        webView.evaluateJavascript(
                            "if(typeof updateDriverLocation === 'function') { updateDriverLocation(${activeDr.currentLat}, ${activeDr.currentLng}, '$popupText'); }",
                            null
                        )
                    }
                }
            )
        }
    }
}

// 5. INCHARGE WORKSITE APPROVALS
@Composable
fun ApprovalsScreen() {
    val requests by AppRepository.loadingRequests.collectAsState()
    val pendingRequests = requests.filter { it.status == "Pending" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "INCHARGE WORKFLOW MANAGER",
                color = IndustrialYellow,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 1.sp
            )
        }

        if (pendingRequests.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceGray)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "All loading and transit clearings are locked and approved.",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(pendingRequests) { req ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceGray),
                    border = BorderStroke(1.dp, IndustrialYellow.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Pending, contentDescription = null, tint = IndustrialYellow)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "LOADING PERMISSION REQUESTed",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            "Stretch: ${req.chainageName}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            "Requested Material: ${req.materialType}",
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                        Text(
                            "Requested By: ${req.requestedBy}",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { AppRepository.approveOrRejectLoadingRequest(req.id, false) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("REJECT", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Button(
                                onClick = { AppRepository.approveOrRejectLoadingRequest(req.id, true) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("APPROVE", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 6. WHATSAPP-STYLE INSTANT GROUP MESSAGING
@Composable
fun ChatScreen() {
    val messages by AppRepository.groupMessages.collectAsState()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var textInput by remember { mutableStateOf("") }
    var replyingToMessage by remember { mutableStateOf<GroupMessage?>(null) }

    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSteelBg)
    ) {
        // Group Header banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceGray)
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(IndustrialYellow)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Groups, contentDescription = null, tint = Color.Black)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Chainage Operations (Central)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("All operational staff and fleet drivers", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(
                    msg = msg,
                    onReplyTargetClicked = {
                        replyingToMessage = msg
                    }
                )
            }
        }

        // Reply preview box
        if (replyingToMessage != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Replying to ${replyingToMessage!!.senderName}", color = IndustrialYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(replyingToMessage!!.messageText, color = Color.LightGray, fontSize = 12.sp, maxLines = 1)
                }
                IconButton(onClick = { replyingToMessage = null }) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.LightGray)
                }
            }
        }

        // Bottom text sender
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceGray)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Compose message to dray roster...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_text")
                    .windowInsetsPadding(WindowInsets.navigationBars),
                singleLine = true,
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                AppRepository.sendGroupChatMessage(
                                    textInput,
                                    replyingToMessage?.id,
                                    replyingToMessage?.messageText
                                )
                                textInput = ""
                                replyingToMessage = null
                            }
                        },
                        modifier = Modifier.testTag("send_chat_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = IndustrialYellow)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IndustrialYellow,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        }
    }
}

// 7. DRIVER LOGISTICS CONCOURSE
@Composable
fun DriverDashboardScreen() {
    val currentDriver by AppRepository.currentDriver.collectAsState()
    val shipments by AppRepository.shipments.collectAsState()
    val context = LocalContext.current

    // Background tracking toggle
    var locationTrackingActive by remember { mutableStateOf(LocationService.isRunning) }

    val activeShipment = shipments.find { it.id == currentDriver?.activeShipmentId }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceGray),
                border = BorderStroke(1.dp, IndustrialYellow.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "DRAY PROFILE: ${currentDriver?.name?.uppercase()}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        "Vehicle: ${currentDriver?.vehicleNumber} | ${if (currentDriver?.vehicleVerify == true) "Verified" else "Awaiting Verification"}",
                        color = IndustrialYellow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Location service switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Automatic Route Tracking", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Broadcasts GPS coords to Dispatch desk", color = Color.Gray, fontSize = 11.sp)
                        }
                        Switch(
                            checked = locationTrackingActive,
                            onCheckedChange = { active ->
                                locationTrackingActive = active
                                if (active) {
                                    val intent = Intent(context, LocationService::class.java)
                                    context.startService(intent)
                                } else {
                                    val intent = Intent(context, LocationService::class.java)
                                    context.stopService(intent)
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = IndustrialYellow)
                        )
                    }
                }
            }
        }

        item {
            Text(
                "YOUR ASSIGNED SHIPMENT",
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        if (activeShipment == null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceGray)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No shipment orders dispatching to your vehicle currently.",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceGray),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(IndustrialYellow.copy(alpha = 0.2f))
                                    .padding(6.dp)
                            ) {
                                Text(activeShipment.status.uppercase(), color = IndustrialYellow, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                "Order Ref: ${activeShipment.id}",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Material Cargo", color = Color.Gray, fontSize = 11.sp)
                        Text(activeShipment.materialType, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Source Point", color = Color.Gray, fontSize = 11.sp)
                        Text(activeShipment.sourceName, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Unload Destination Segment", color = Color.Gray, fontSize = 11.sp)
                        Text(activeShipment.destName, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

                        if (activeShipment.status == "Dispatched") {
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { AppRepository.confirmDelivery(activeShipment.id) },
                                modifier = Modifier.fillMaxWidth().testTag("confirm_delivery_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.DoneAll, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("CONFIRM MATERIAL UNLOADED / DELIVERED", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 8. HIGH QUALITY INFRASTRUCTURE REPORTS & ANALYTICS
@Composable
fun ReportsScreen() {
    val shipments by AppRepository.shipments.collectAsState()

    val delivered = shipments.filter { it.status == "Delivered" }
    val totalTonnage = delivered.sumOf { it.netWeight }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "ANALYTICS & TONNAGE GRAPHICS",
                color = IndustrialYellow,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 1.sp
            )
        }

        // Summary Cards
        item {
            Card(colors = CardDefaults.cardColors(containerColor = SurfaceGray)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("TOTAL DELIVERED WORKLOAD", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${String.format("%.2f", totalTonnage)} TONNES",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Custom drawn bar chart
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceGray),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("MATERIAL DISPATCH PROGRESS (TONNES)", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    val materialsBreakdown = delivered.groupBy { it.materialType }
                        .mapValues { entry -> entry.value.sumOf { it.netWeight } }

                    if (materialsBreakdown.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .height(160.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Awaiting primary deliveries to generate layout.", color = Color.DarkGray)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            materialsBreakdown.forEach { (mat, tons) ->
                                Column {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(mat, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text("${String.format("%.1f", tons)} T", color = IndustrialYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.DarkGray)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(if (totalTonnage > 0) (tons / totalTonnage).toFloat() else 0f)
                                                .clip(CircleShape)
                                                .background(IndustrialYellow)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                "HISTORIC DRAY LOGS",
                color = Color.LightGray,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        items(delivered) { ship ->
            Card(colors = CardDefaults.cardColors(containerColor = SurfaceGray)) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = SuccessGreen)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Vehicle: ${ship.vehicleNumber}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Cargo: ${ship.materialType}", color = Color.Gray, fontSize = 11.sp)
                    }
                    Text(
                        "${String.format("%.2f", ship.netWeight)} T",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// 9. MESSAGING & BROADCAST NOTIFICATIONS SYSTEM
@Composable
fun NotificationsScreen() {
    val notices by AppRepository.notifications.collectAsState()

    LaunchedEffect(Unit) {
        AppRepository.markNotificationsAsRead()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "BROADCAST CONSOLE ALERTS",
                color = IndustrialYellow,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 1.sp
            )
        }

        if (notices.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceGray)
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No emergency broad notifications.", color = Color.Gray)
                }
            }
        } else {
            items(notices) { notice ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceGray)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                when (notice.type) {
                                    "Chat" -> Icons.Default.Chat
                                    "Dispatch" -> Icons.Default.LocalShipping
                                    "Loading" -> Icons.Default.CloudUpload
                                    else -> Icons.Default.Notifications
                                },
                                contentDescription = null,
                                tint = IndustrialYellow,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                notice.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                            Text(
                                sdf.format(Date(notice.timestamp)),
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(notice.message, color = Color.LightGray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// INNER COMPONENTS & HELPERS
@Composable
fun LocationNavigationBar(
    activeTab: String,
    onNavChanged: (String) -> Unit,
    isDriver: Boolean,
    notificationCount: Int
) {
    NavigationBar(
        containerColor = SurfaceGray,
        contentColor = Color.White
    ) {
        NavigationBarItem(
            selected = activeTab == "home",
            onClick = { onNavChanged("home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Logistics", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = IndustrialYellow,
                indicatorColor = IndustrialYellow
            )
        )

        if (!isDriver) {
            NavigationBarItem(
                selected = activeTab == "weighbridge",
                onClick = { onNavChanged("weighbridge") },
                icon = { Icon(Icons.Default.Scale, contentDescription = "Scale") },
                label = { Text("Weighscale", fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = IndustrialYellow,
                    indicatorColor = IndustrialYellow
                )
            )
        }

        NavigationBarItem(
            selected = activeTab == "tracking",
            onClick = { onNavChanged("tracking") },
            icon = { Icon(Icons.Default.Map, contentDescription = "GPS Map") },
            label = { Text("GPS Map", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = IndustrialYellow,
                indicatorColor = IndustrialYellow
            )
        )

        if (!isDriver) {
            NavigationBarItem(
                selected = activeTab == "approvals",
                onClick = { onNavChanged("approvals") },
                icon = { Icon(Icons.Default.LockClock, contentDescription = "Permissions") },
                label = { Text("Clearance", fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = IndustrialYellow,
                    indicatorColor = IndustrialYellow
                )
            )
        }

        NavigationBarItem(
            selected = activeTab == "chat",
            onClick = { onNavChanged("chat") },
            icon = { Icon(Icons.Default.Chat, contentDescription = "Group Chat") },
            label = { Text("Radio", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = IndustrialYellow,
                indicatorColor = IndustrialYellow
            )
        )

        if (!isDriver) {
            NavigationBarItem(
                selected = activeTab == "reports",
                onClick = { onNavChanged("reports") },
                icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Tonnage Visual") },
                label = { Text("Tonnage", fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = IndustrialYellow,
                    indicatorColor = IndustrialYellow
                )
            )
        }

        NavigationBarItem(
            selected = activeTab == "notices",
            onClick = { onNavChanged("notices") },
            icon = {
                BadgedBox(
                    badge = {
                        if (notificationCount > 0) {
                            Badge(containerColor = DangerRed) {
                                Text(notificationCount.toString(), color = Color.White)
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "Alerts")
                }
            },
            label = { Text("Alerts", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = IndustrialYellow,
                indicatorColor = IndustrialYellow
            )
        )
    }
}

@Composable
fun KPICard(title: String, value: String, modifier: Modifier = Modifier, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(title, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RequirementItemRow(
    req: Requirement,
    chainageName: String,
    projectName: String,
    onActionRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceGray),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(req.materialType, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (req.status) {
                                "Pending" -> IndustrialYellow.copy(alpha = 0.15f)
                                "Active" -> LightAccentBlue.copy(alpha = 0.15f)
                                else -> SuccessGreen.copy(alpha = 0.15f)
                            }
                        )
                        .padding(6.dp)
                ) {
                    Text(
                        req.status.uppercase(),
                        color = when (req.status) {
                            "Pending" -> IndustrialYellow
                            "Active" -> LightAccentBlue
                            else -> SuccessGreen
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Segment: $chainageName", color = Color.LightGray, fontSize = 12.sp)
            Text("Project Core: $projectName", color = Color.Gray, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(12.dp))

            // Percentage indicator
            val percent = if (req.targetQuantity > 0) req.completedQuantity / req.targetQuantity else 0.0
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "${String.format("%.1f", req.completedQuantity)} T completed",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
                Text("Target: ${req.targetQuantity} T", color = Color.LightGray, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { percent.toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = IndustrialYellow,
                trackColor = Color.DarkGray
            )

            if (req.status == "Pending") {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onActionRequest,
                    modifier = Modifier.fillMaxWidth().testTag("req_loading_approval_trigger"),
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialYellow, contentColor = Color.Black),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("REQUEST PRODUCTION CLEARANCE (INCHARGE APPROVED)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun WeightBadge(title: String, wt: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            String.format("%.2f T", wt),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun ChatBubble(msg: GroupMessage, onReplyTargetClicked: () -> Unit) {
    val contextUser by AppRepository.currentUser.collectAsState()
    val contextDrv by AppRepository.currentDriver.collectAsState()

    val currentName = contextUser?.name ?: contextDrv?.name ?: ""
    val isMe = msg.senderName.equals(currentName, ignoreCase = true)

    val date = Date(msg.timestamp)
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onReplyTargetClicked() },
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isMe) 12.dp else 0.dp,
                        bottomEnd = if (isMe) 0.dp else 12.dp
                    )
                )
                .background(if (isMe) IndustrialYellow.copy(alpha = 0.25f) else SurfaceGray)
                .border(
                    1.dp,
                    if (isMe) IndustrialYellow.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f),
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isMe) 12.dp else 0.dp,
                        bottomEnd = if (isMe) 0.dp else 12.dp
                    )
                )
                .padding(12.dp)
        ) {
            Column {
                if (!isMe) {
                    Text(
                        text = "${msg.senderName} (${msg.senderRole})",
                        color = IndustrialYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (msg.replyToText != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(6.dp)
                    ) {
                        Text(
                            text = "Replying: ${msg.replyToText}",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                    text = msg.messageText,
                    color = Color.White,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = sdf.format(date),
                    color = Color.Gray,
                    fontSize = 9.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun AddRequirementDialog(
    chainages: List<Chainage>,
    onDismiss: () -> Unit,
    onSubmit: (chainageId: String, material: String, quantity: Double) -> Unit
) {
    var selectedChainageId by remember { mutableStateOf(chainages.firstOrNull()?.id ?: "") }
    var material by remember { mutableStateOf("WMM Mix Base") }
    var quantityInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val materialTypes = listOf("GSB Subbase", "WMM Mix Base", "DBM Asphalt Binder", "BC Wearing Course")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("NEW HIGHWAY ORDER", color = IndustrialYellow, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Select Target Chainage segment:", color = Color.Gray, fontSize = 11.sp)
                
                chainages.forEach { ch ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedChainageId = ch.id }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedChainageId == ch.id,
                            onClick = { selectedChainageId = ch.id },
                            colors = RadioButtonDefaults.colors(selectedColor = IndustrialYellow)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(ch.name, color = Color.White, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text("Roadway Material:", color = Color.Gray, fontSize = 11.sp)
                materialTypes.forEach { mat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { material = mat }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = material == mat,
                            onClick = { material = mat },
                            colors = RadioButtonDefaults.colors(selectedColor = IndustrialYellow)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(mat, color = Color.White, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = quantityInput,
                    onValueChange = { quantityInput = it },
                    label = { Text("Target Volume (Tonnes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("req_quantity_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialYellow, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                if (errorMsg != null) {
                    Text(errorMsg!!, color = DangerRed, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityInput.toDoubleOrNull() ?: 0.0
                    if (qty <= 0) {
                        errorMsg = "Enter valid progressive quantity!"
                    } else {
                        onSubmit(selectedChainageId, material, qty)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndustrialYellow, contentColor = Color.Black)
            ) {
                Text("DISPATCH LOG ORDER", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.LightGray)
            }
        },
        containerColor = SurfaceGray
    )
}
