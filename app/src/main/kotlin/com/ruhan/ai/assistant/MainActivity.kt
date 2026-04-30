package com.ruhan.ai.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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

        if (preferencesManager.biometricLockEnabled) {
            authenticateUser()
        } else {
            isAuthenticated = true
        }

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
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "R U H A N",
                                color = Color(0xFF00E5FF),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 6.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Locked",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { authenticateUser() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                            ) {
                                Text("Unlock", color = Color.Black)
                            }
                        }
                    } else {
                        val navController = rememberNavController()
                        NavHost(
                            navController = navController,
                            startDestination = "main"
                        ) {
                            composable("main") {
                                val viewModel: RuhanViewModel = hiltViewModel()
                                RuhanScreen(
                                    viewModel = viewModel,
                                    onNavigateToSettings = {
                                        navController.navigate("settings")
                                    }
                                )
                            }
                            composable("settings") {
                                SettingsScreen(
                                    viewModel = settingsVm,
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun authenticateUser() {
        biometricManager.authenticate(
            activity = this,
            onSuccess = { isAuthenticated = true },
            onFail = { }
        )
    }
}
