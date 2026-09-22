package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.data.repository.UserSettings
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.OnboardingPermissionScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.SalimTheme
import kotlinx.coroutines.launch

enum class SalimScreen {
    ONBOARDING,
    LIBRARY,
    PLAYER,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as SalimApplication
        val repository = app.videoRepository
        val settingsRepo = app.settingsRepository
        val playbackManager = app.playbackManager

        setContent {
            val settings by settingsRepo.userSettingsFlow.collectAsState(initial = UserSettings())
            val scope = rememberCoroutineScope()

            var currentScreen by remember { mutableStateOf(SalimScreen.LIBRARY) }

            // Check Permission state
            val hasVideoPermission = remember {
                val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_VIDEO
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
                ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
            }

            var isPermissionGranted by remember { mutableStateOf(hasVideoPermission) }

            LaunchedEffect(isPermissionGranted) {
                if (isPermissionGranted) {
                    currentScreen = SalimScreen.LIBRARY
                    repository.scanLibrary()

                    // Check if launched from Widget to resume video
                    val resumeId = intent?.getLongExtra("RESUME_VIDEO_ID", -1L) ?: -1L
                    if (resumeId > 0L) {
                        val video = repository.getVideoByIdSync(resumeId)
                        if (video != null) {
                            playbackManager.playVideo(video)
                            currentScreen = SalimScreen.PLAYER
                        }
                    }
                } else {
                    currentScreen = SalimScreen.ONBOARDING
                }
            }

            val systemInDark = isSystemInDarkTheme()
            val isDark = when (settings.themeMode) {
                "light" -> false
                "dark" -> true
                else -> systemInDark
            }

            SalimTheme(
                darkTheme = isDark,
                isOledBlack = settings.isTrueBlackOled
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen_transition",
                    modifier = Modifier.fillMaxSize()
                ) { screen ->
                    when (screen) {
                        SalimScreen.ONBOARDING -> {
                            OnboardingPermissionScreen(
                                onPermissionGranted = {
                                    isPermissionGranted = true
                                    scope.launch {
                                        settingsRepo.setOnboardingCompleted(true)
                                    }
                                }
                            )
                        }
                        SalimScreen.LIBRARY -> {
                            LibraryScreen(
                                videoRepository = repository,
                                settingsRepository = settingsRepo,
                                onVideoSelected = { video, playlist ->
                                    playbackManager.playVideo(video, playlist)
                                    currentScreen = SalimScreen.PLAYER
                                },
                                onOpenSettings = {
                                    currentScreen = SalimScreen.SETTINGS
                                }
                            )
                        }
                        SalimScreen.PLAYER -> {
                            BackHandler {
                                currentScreen = SalimScreen.LIBRARY
                            }
                            PlayerScreen(
                                playbackManager = playbackManager,
                                onBack = {
                                    currentScreen = SalimScreen.LIBRARY
                                }
                            )
                        }
                        SalimScreen.SETTINGS -> {
                            BackHandler {
                                currentScreen = SalimScreen.LIBRARY
                            }
                            SettingsScreen(
                                settingsRepository = settingsRepo,
                                videoRepository = repository,
                                onBack = {
                                    currentScreen = SalimScreen.LIBRARY
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
