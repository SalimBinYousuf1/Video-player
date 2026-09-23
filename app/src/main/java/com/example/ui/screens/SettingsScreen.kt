package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.UserSettings
import com.example.data.repository.UserSettingsRepository
import com.example.data.repository.VideoRepository
import com.example.ui.components.GlassSurface
import com.example.ui.components.LiquidSmokeBackground
import com.example.ui.theme.DarkGlassBase
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TextSecondaryLight
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    settingsRepository: UserSettingsRepository,
    videoRepository: VideoRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by settingsRepository.userSettingsFlow.collectAsState(initial = UserSettings())

    val systemInDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isLightMode = when (settings.themeMode) {
        "light" -> true
        "dark" -> false
        else -> !systemInDark
    }
    val primaryTextColor = if (isLightMode) TextPrimaryLight else TextPrimaryDark
    val secondaryTextColor = if (isLightMode) TextSecondaryLight else TextSecondaryDark
    val dividerColor = if (isLightMode) Color(0x1A000000) else Color(0x1AFFFFFF)

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showImportJsonDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }

    LiquidSmokeBackground(
        modifier = modifier.fillMaxSize(),
        isLightMode = isLightMode,
        accentTheme = settings.smokeGradientTheme,
        alphaMultiplier = settings.smokeIntensity,
        speedMultiplier = settings.smokeSpeed
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassSurface(
                    shape = RoundedCornerShape(16.dp),
                    isLightMode = isLightMode,
                    transparencyAlpha = settings.liquidGlassAlpha,
                    modifier = Modifier.size(44.dp),
                    onClick = onBack
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = primaryTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Settings",
                    color = primaryTextColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Theme & Appearance
            SettingsSectionHeader(title = "THEME & LIGHT MODE", color = secondaryTextColor)
            GlassSurface(
                shape = RoundedCornerShape(22.dp),
                isLightMode = isLightMode,
                transparencyAlpha = settings.liquidGlassAlpha,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Light / Dark Theme selector
                    Text(text = "App Theme Mode", color = primaryTextColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("light", "Light Mode", Icons.Default.WbSunny),
                            Triple("dark", "Dark OLED", Icons.Default.Brightness4),
                            Triple("system", "System", Icons.Default.Contrast)
                        ).forEach { (modeKey, modeTitle, icon) ->
                            val isSelected = settings.themeMode == modeKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) {
                                            if (isLightMode) Color(0xFF007AFF) else Color.White
                                        } else {
                                            if (isLightMode) Color(0x14000000) else Color(0x22FFFFFF)
                                        }
                                    )
                                    .clickable { scope.launch { settingsRepository.setThemeMode(modeKey) } }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) {
                                            if (isLightMode) Color.White else Color.Black
                                        } else primaryTextColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = modeTitle,
                                        color = if (isSelected) {
                                            if (isLightMode) Color.White else Color.Black
                                        } else primaryTextColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = dividerColor, modifier = Modifier.padding(vertical = 14.dp))

                    // Smoke Gradient Accent Theme
                    Text(text = "Smoke Fog Gradient Accent", color = primaryTextColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            Pair("blue_green_orange", "Blue, Green & Orange (Apple White / Fog)"),
                            Pair("google", "Google Spectrum (Blue, Red, Yellow, Green)"),
                            Pair("sunset", "Apple Sunset (Warm Orange, Pink & Purple)"),
                            Pair("aurora", "Aurora Borealis (Emerald, Teal & Cyan)")
                        ).forEach { (themeKey, label) ->
                            val isSelected = settings.smokeGradientTheme == themeKey
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) {
                                            if (isLightMode) Color(0x14007AFF) else Color(0x33FFFFFF)
                                        } else Color.Transparent
                                    )
                                    .clickable { scope.launch { settingsRepository.setSmokeGradientTheme(themeKey) } }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color(0xFF007AFF) else primaryTextColor,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF007AFF))
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = dividerColor, modifier = Modifier.padding(vertical = 14.dp))

                    // Smoke Intensity Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Smoke Fog Opacity", color = primaryTextColor, fontSize = 14.sp)
                        Text(text = "${(settings.smokeIntensity * 100).toInt()}%", color = Color(0xFF007AFF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = settings.smokeIntensity,
                        onValueChange = { scope.launch { settingsRepository.setSmokeIntensity(it) } },
                        valueRange = 0.2f..0.85f,
                        colors = SliderDefaults.colors(
                            thumbColor = if (isLightMode) Color(0xFF007AFF) else Color.White,
                            activeTrackColor = if (isLightMode) Color(0xFF007AFF) else Color.White,
                            inactiveTrackColor = if (isLightMode) Color(0x22000000) else Color(0x33FFFFFF)
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Smoke Motion Speed
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Gentle Drift Motion Speed", color = primaryTextColor, fontSize = 14.sp)
                        Text(text = "${String.format("%.1f", settings.smokeSpeed)}x", color = Color(0xFF007AFF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = settings.smokeSpeed,
                        onValueChange = { scope.launch { settingsRepository.setSmokeSpeed(it) } },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = if (isLightMode) Color(0xFF007AFF) else Color.White,
                            activeTrackColor = if (isLightMode) Color(0xFF007AFF) else Color.White,
                            inactiveTrackColor = if (isLightMode) Color(0x22000000) else Color(0x33FFFFFF)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Playback & Video Engine Features
            SettingsSectionHeader(title = "ADVANCED VIDEO CONTROLS", color = secondaryTextColor)
            GlassSurface(
                shape = RoundedCornerShape(22.dp),
                isLightMode = isLightMode,
                transparencyAlpha = settings.liquidGlassAlpha,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Double-Tap Skip Duration
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.FastForward, contentDescription = null, tint = primaryTextColor, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = "Double-Tap Seek", color = primaryTextColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            }
                            Text(text = "${settings.defaultSkipDurationSec}s", color = Color(0xFF007AFF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(5, 10, 15, 20, 30).forEach { sec ->
                                val isSelected = settings.defaultSkipDurationSec == sec
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) Color(0xFF007AFF) else if (isLightMode) Color(0x14000000) else Color(0x22FFFFFF)
                                        )
                                        .clickable { scope.launch { settingsRepository.setDefaultSkipDuration(sec) } }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${sec}s",
                                        color = if (isSelected) Color.White else primaryTextColor,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = dividerColor, modifier = Modifier.padding(vertical = 12.dp))

                    // Audio Boost Toggle
                    SettingsToggleRow(
                        icon = Icons.Default.VolumeUp,
                        title = "Audio Boost (up to 200%)",
                        subtitle = "Allows boosting media volume past system threshold for quiet videos",
                        checked = settings.audioBoostEnabled,
                        primaryColor = primaryTextColor,
                        secondaryColor = secondaryTextColor,
                        onCheckedChange = { scope.launch { settingsRepository.setAudioBoostEnabled(it) } }
                    )

                    HorizontalDivider(color = dividerColor, modifier = Modifier.padding(vertical = 12.dp))

                    // Long-Press 2.0x Fast Forward
                    SettingsToggleRow(
                        icon = Icons.Default.Speed,
                        title = "Long-Press Fast Forward (2.0x)",
                        subtitle = "Press and hold screen during video to temporarily play at 2.0x",
                        checked = settings.longPressFastForward,
                        primaryColor = primaryTextColor,
                        secondaryColor = secondaryTextColor,
                        onCheckedChange = { scope.launch { settingsRepository.setLongPressFastForward(it) } }
                    )

                    HorizontalDivider(color = dividerColor, modifier = Modifier.padding(vertical = 12.dp))

                    // Screen Orientation Lock
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.ScreenRotation, contentDescription = null, tint = primaryTextColor, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = "Orientation Lock", color = primaryTextColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            }
                            Text(
                                text = when (settings.screenOrientationLock) {
                                    "landscape" -> "Landscape"
                                    "portrait" -> "Portrait"
                                    else -> "Auto"
                                },
                                color = Color(0xFF007AFF),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Pair("sensor", "Auto"),
                                Pair("landscape", "Landscape"),
                                Pair("portrait", "Portrait")
                            ).forEach { (key, name) ->
                                val isSelected = settings.screenOrientationLock == key
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) Color(0xFF007AFF) else if (isLightMode) Color(0x14000000) else Color(0x22FFFFFF)
                                        )
                                        .clickable { scope.launch { settingsRepository.setScreenOrientationLock(key) } }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = name,
                                        color = if (isSelected) Color.White else primaryTextColor,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Library & Data
            SettingsSectionHeader(title = "LIBRARY & DATA", color = secondaryTextColor)
            GlassSurface(
                shape = RoundedCornerShape(22.dp),
                isLightMode = isLightMode,
                transparencyAlpha = settings.liquidGlassAlpha,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsActionRow(
                        icon = Icons.Default.Refresh,
                        title = "Rescan Media Library",
                        subtitle = "Scans internal storage and SD card for new video files",
                        primaryColor = primaryTextColor,
                        secondaryColor = secondaryTextColor,
                        onClick = {
                            scope.launch {
                                videoRepository.scanLibrary()
                                Toast.makeText(context, "Library scan completed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    HorizontalDivider(color = dividerColor, modifier = Modifier.padding(vertical = 12.dp))

                    SettingsActionRow(
                        icon = Icons.Default.Download,
                        title = "Export Playlists & Bookmarks",
                        subtitle = "Copies backup JSON to clipboard",
                        primaryColor = primaryTextColor,
                        secondaryColor = secondaryTextColor,
                        onClick = {
                            scope.launch {
                                val json = videoRepository.exportBackupJson()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Salim Backup", json)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Backup JSON copied to clipboard!", Toast.LENGTH_LONG).show()
                            }
                        }
                    )

                    HorizontalDivider(color = dividerColor, modifier = Modifier.padding(vertical = 12.dp))

                    SettingsActionRow(
                        icon = Icons.Default.Delete,
                        title = "Clear Watch History",
                        subtitle = "Resets resume timestamps across all media",
                        tint = Color(0xFFFF453A),
                        primaryColor = Color(0xFFFF453A),
                        secondaryColor = secondaryTextColor,
                        onClick = { showClearHistoryDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. About
            SettingsSectionHeader(title = "ABOUT", color = secondaryTextColor)
            GlassSurface(
                shape = RoundedCornerShape(22.dp),
                isLightMode = isLightMode,
                transparencyAlpha = settings.liquidGlassAlpha,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Salim Video Player",
                        color = primaryTextColor,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Version 2.0.0 • Apple Design Standard",
                        color = secondaryTextColor,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Engineered with modern Apple HIG principles. Features gentle flowing chromatic smoke fog, frosted liquid glass materials, curved squircle buttons, hardware video decoding, A-B loop, 1.0x-5.0x zoom, frame capture, audio boost, and zero telemetry.",
                        color = secondaryTextColor,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            containerColor = if (isLightMode) Color(0xFFF7F8FA) else Color(0xFF1E2029),
            title = { Text("Clear Watch History?", color = primaryTextColor, fontWeight = FontWeight.Bold) },
            text = { Text("This will remove all progress timestamps from your videos.", color = secondaryTextColor) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        videoRepository.clearWatchHistory()
                        Toast.makeText(context, "Watch history cleared", Toast.LENGTH_SHORT).show()
                    }
                    showClearHistoryDialog = false
                }) {
                    Text("Clear", color = Color(0xFFFF453A), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel", color = secondaryTextColor)
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String, color: Color) {
    Text(
        text = title,
        color = color,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(text = title, color = primaryColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, color = secondaryColor, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF34C759)
            )
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    primaryColor: Color,
    secondaryColor: Color,
    tint: Color = primaryColor,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = tint, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, color = secondaryColor, fontSize = 12.sp)
        }
    }
}
