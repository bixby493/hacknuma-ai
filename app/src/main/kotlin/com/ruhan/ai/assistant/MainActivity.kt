package com.ruhan.ai.assistant

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import com.ruhan.ai.assistant.presentation.dashboard.DashboardScreen
import com.ruhan.ai.assistant.presentation.main.RuhanScreen
import com.ruhan.ai.assistant.presentation.main.RuhanViewModel
import com.ruhan.ai.assistant.presentation.settings.SettingsScreen
import com.ruhan.ai.assistant.presentation.settings.SettingsViewModel
import com.ruhan.ai.assistant.presentation.theme.RuhanTheme
import com.ruhan.ai.assistant.presentation.theme.RuhanThemeColors
import com.ruhan.ai.assistant.security.BiometricManager
import com.ruhan.ai.assistant.util.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var biometricManager: BiometricManager

    private var isAuthenticated by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsVm: SettingsViewModel = hiltViewModel()
            val settingsState by settingsVm.uiState.collectAsState()

            RuhanTheme(themeMode = settingsState.theme) {
                val colors = RuhanThemeColors.current
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colors.background
                ) {
                    if (!isAuthenticated) {
                        LockScreen()
                    } else {
                        val navController = rememberNavController()
                        var currentTab by remember { mutableStateOf("main") }

                        Box(modifier = Modifier.fillMaxSize()) {
                            NavHost(
                                navController = navController,
                                startDestination = "main",
                                modifier = Modifier.fillMaxSize()
                            ) {
                                composable("dashboard") {
                                    currentTab = "dashboard"
                                    DashboardScreen(
                                        onNavigateToSettings = {
                                            navController.navigate("settings")
                                        }
                                    )
                                }
                                composable("main") {
                                    currentTab = "main"
                                    val viewModel: RuhanViewModel = hiltViewModel()
                                    RuhanScreen(
                                        viewModel = viewModel,
                                        onNavigateToSettings = {
                                            navController.navigate("settings")
                                        }
                                    )
                                }
                                composable("settings") {
                                    currentTab = "settings"
                                    SettingsScreen(
                                        viewModel = settingsVm,
                                        onNavigateBack = {
                                            navController.popBackStack()
                                        }
                                    )
                                }
                            }

                            if (currentTab != "settings") {
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .background(Color(0xE6050805))
                                        .padding(vertical = 8.dp, horizontal = 32.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BottomNavItem(
                                        icon = Icons.Default.Dashboard,
                                        label = "DASHBOARD",
                                        isSelected = currentTab == "dashboard",
                                        onClick = {
                                            if (currentTab != "dashboard") {
                                                navController.navigate("dashboard") {
                                                    popUpTo("dashboard") { inclusive = true }
                                                }
                                            }
                                        }
                                    )
                                    BottomNavItem(
                                        icon = Icons.AutoMirrored.Filled.Chat,
                                        label = "RUHAN",
                                        isSelected = currentTab == "main",
                                        onClick = {
                                            if (currentTab != "main") {
                                                navController.navigate("main") {
                                                    popUpTo("dashboard")
                                                }
                                            }
                                        }
                                    )
                                    BottomNavItem(
                                        icon = Icons.Default.Settings,
                                        label = "SETTINGS",
                                        isSelected = currentTab == "settings",
                                        onClick = {
                                            navController.navigate("settings") {
                                                popUpTo("dashboard")
                                            }
                                        }
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
    private fun LockScreen() {
        val isSetup = preferencesManager.isLockSetup
        val lockType = preferencesManager.lockType

        val neonGreen = Color(0xFF00FF41)

        if (!isSetup) {
            LockSetupScreen(neonGreen)
        } else if (lockType == "pin") {
            PinEntryScreen(neonGreen)
        } else if (lockType == "face") {
            FaceLockScreen(neonGreen)
        } else {
            LockSetupScreen(neonGreen)
        }
    }

    @Composable
    private fun LockSetupScreen(accent: Color) {
        var showPinSetup by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "R U H A N",
                color = accent,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 8.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "SECURITY SETUP",
                color = accent.copy(alpha = 0.6f),
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(48.dp))

            if (!showPinSetup) {
                Text(
                    "Choose your lock type:",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(32.dp))

                LockOptionButton(
                    icon = Icons.Default.Lock,
                    title = "PIN Lock",
                    subtitle = "4-digit PIN se secure karo",
                    accent = accent,
                    onClick = { showPinSetup = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                LockOptionButton(
                    icon = Icons.Default.Face,
                    title = "Face / Fingerprint",
                    subtitle = "Biometric se unlock karo",
                    accent = accent,
                    onClick = {
                        if (biometricManager.isAvailable()) {
                            preferencesManager.lockType = "face"
                            preferencesManager.isLockSetup = true
                            biometricManager.authenticate(
                                activity = this@MainActivity,
                                onSuccess = { isAuthenticated = true },
                                onFail = { }
                            )
                        } else {
                            showPinSetup = true
                        }
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    "Skip >",
                    color = accent.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.clickable {
                        preferencesManager.isLockSetup = true
                        preferencesManager.lockType = "none"
                        isAuthenticated = true
                    }
                )
            } else {
                PinSetupView(accent)
            }
        }
    }

    @Composable
    private fun PinSetupView(accent: Color) {
        var pin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var isConfirming by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf("") }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (!isConfirming) "Enter new PIN" else "Confirm PIN",
                color = Color.White,
                fontSize = 18.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(24.dp))

            PinDots(
                length = if (!isConfirming) pin.length else confirmPin.length,
                accent = accent
            )

            if (error.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(error, color = Color.Red, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(32.dp))

            PinKeypad(accent = accent) { key ->
                if (key == "DEL") {
                    if (!isConfirming) {
                        pin = pin.dropLast(1)
                    } else {
                        confirmPin = confirmPin.dropLast(1)
                    }
                    error = ""
                } else {
                    if (!isConfirming) {
                        if (pin.length < 4) pin += key
                        if (pin.length == 4) {
                            isConfirming = true
                        }
                    } else {
                        if (confirmPin.length < 4) confirmPin += key
                        if (confirmPin.length == 4) {
                            if (confirmPin == pin) {
                                preferencesManager.lockPin = pin
                                preferencesManager.lockType = "pin"
                                preferencesManager.isLockSetup = true
                                isAuthenticated = true
                            } else {
                                error = "PIN match nahi hua!"
                                confirmPin = ""
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun PinEntryScreen(accent: Color) {
        var pin by remember { mutableStateOf("") }
        var error by remember { mutableStateOf("") }
        var attempts by remember { mutableStateOf(0) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "R U H A N",
                color = accent,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 8.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "LOCKED",
                color = accent.copy(alpha = 0.6f),
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Enter PIN",
                color = Color.White,
                fontSize = 18.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(24.dp))

            PinDots(length = pin.length, accent = accent)

            if (error.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(error, color = Color.Red, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(32.dp))

            PinKeypad(accent = accent) { key ->
                if (key == "DEL") {
                    pin = pin.dropLast(1)
                    error = ""
                } else {
                    if (pin.length < 4) pin += key
                    if (pin.length == 4) {
                        if (pin == preferencesManager.lockPin) {
                            isAuthenticated = true
                        } else {
                            attempts++
                            error = "Wrong PIN! ($attempts)"
                            pin = ""
                            if (attempts >= 3 && preferencesManager.fakeCrashEnabled) {
                                showFakeCrash()
                            }
                            if (attempts >= 2 && preferencesManager.breakInPhotoEnabled) {
                                biometricManager.captureBreakInPhoto(this@MainActivity)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun FaceLockScreen(accent: Color) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "R U H A N",
                color = accent,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 8.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "BIOMETRIC AUTH",
                color = accent.copy(alpha = 0.6f),
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(32.dp))

            Icon(
                Icons.Default.Face,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    biometricManager.authenticate(
                        activity = this@MainActivity,
                        onSuccess = { isAuthenticated = true },
                        onFail = { }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = accent)
            ) {
                Text("Unlock", color = Color.Black, fontFamily = FontFamily.Monospace)
            }
        }

        androidx.compose.runtime.LaunchedEffect(Unit) {
            try {
                biometricManager.authenticate(
                    activity = this@MainActivity,
                    onSuccess = { isAuthenticated = true },
                    onFail = { }
                )
            } catch (_: Exception) {}
        }
    }

    @Composable
    private fun PinDots(length: Int, accent: Color) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(4) { i ->
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (i < length) accent else Color.Transparent)
                        .border(2.dp, accent, CircleShape)
                )
            }
        }
    }

    @Composable
    private fun PinKeypad(accent: Color, onKey: (String) -> Unit) {
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "DEL")
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            keys.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    row.forEach { key ->
                        if (key.isBlank()) {
                            Box(modifier = Modifier.size(64.dp))
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, accent.copy(alpha = 0.3f), CircleShape)
                                    .clickable { onKey(key) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (key == "DEL") "<" else key,
                                    color = accent,
                                    fontSize = if (key == "DEL") 20.sp else 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun LockOptionButton(
        icon: ImageVector,
        title: String,
        subtitle: String,
        accent: Color,
        onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .clickable { onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text(subtitle, color = Color.Gray, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }

    @Composable
    private fun BottomNavItem(
        icon: ImageVector,
        label: String,
        isSelected: Boolean,
        onClick: () -> Unit
    ) {
        val green = Color(0xFF00FF41)
        Column(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (isSelected) green else Color.Gray,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                label,
                color = if (isSelected) green else Color.Gray,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }

    private fun showFakeCrash() {
        try {
            android.app.AlertDialog.Builder(this)
                .setTitle("System UI has stopped")
                .setMessage(
                    "Unfortunately, System UI has stopped working.\n\n" +
                            "Error: 0xDEAD_BEEF\n" +
                            "Process: com.android.systemui\n" +
                            "Signal: 11 (SIGSEGV)\n\n" +
                            "This application will now close."
                )
                .setPositiveButton("OK") { _, _ -> finishAffinity() }
                .setCancelable(false)
                .show()
        } catch (_: Exception) {
            finishAffinity()
        }
    }
}
