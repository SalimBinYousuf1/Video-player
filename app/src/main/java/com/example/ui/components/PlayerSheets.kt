package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookmarkEntity
import com.example.playback.AspectRatioMode
import com.example.playback.PlaybackUiState
import com.example.playback.RepeatMode
import com.example.playback.SalimPlaybackManager
import com.example.playback.TrackInfo
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerMoreFeaturesSheet(
    playbackManager: SalimPlaybackManager,
    uiState: PlaybackUiState,
    onOpenSubtitles: () -> Unit,
    onOpenSpeed: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF16181F),
        scrimColor = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Player Controls & Tools",
                color = TextPrimaryDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            HorizontalDivider(color = Color(0x1FFFFFFF))
            Spacer(modifier = Modifier.height(14.dp))

            // 1. Zoom & Aspect Ratio Modes
            SectionTitle("ASPECT RATIO & ZOOM")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AspectRatioMode.values().forEach { mode ->
                    val isSelected = uiState.aspectRatioMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color.White else Color(0x22FFFFFF))
                            .clickable { playbackManager.setAspectRatioMode(mode) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.label,
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Double Tap Seek Duration Options
            SectionTitle("DOUBLE-TAP SEEK (${uiState.doubleTapSeekSeconds}s)")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(5, 10, 15, 20, 30, 60).forEach { sec ->
                    val isSelected = uiState.doubleTapSeekSeconds == sec
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF34C759) else Color(0x22FFFFFF))
                            .clickable { playbackManager.setDoubleTapSeekSeconds(sec) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${sec}s",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. A-B Loop Playback
            SectionTitle("A-B SEAMLESS LOOP")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = { playbackManager.setLoopPointA() },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (uiState.loopPointA != null) Color(0xFF64D2FF).copy(alpha = 0.3f) else Color(0x22FFFFFF),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (uiState.loopPointA != null) "A: ${formatTime(uiState.loopPointA)}" else "Set Point A", fontSize = 12.sp)
                }

                FilledTonalButton(
                    onClick = { playbackManager.setLoopPointB() },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (uiState.loopPointB != null) Color(0xFF34C759).copy(alpha = 0.3f) else Color(0x22FFFFFF),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (uiState.loopPointB != null) "B: ${formatTime(uiState.loopPointB)}" else "Set Point B", fontSize = 12.sp)
                }

                if (uiState.isAbLoopActive) {
                    IconButton(
                        onClick = { playbackManager.clearAbLoop() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF453A).copy(alpha = 0.3f))
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Loop", tint = Color(0xFFFF453A), modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Audio Boost (Up to 200%)
            SectionTitle("AUDIO BOOST (${(uiState.audioBoostMultiplier * 100).toInt()}%)")
            Slider(
                value = uiState.audioBoostMultiplier,
                onValueChange = { playbackManager.setAudioBoostMultiplier(it) },
                valueRange = 1.0f..2.0f,
                steps = 9,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color(0xFFFF9500),
                    inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Actions Grid (Capture Frame, Bookmarks, Audio-Only, Subtitles, Speed, Sleep Timer, Orientation)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FeaturePillButton(
                        icon = Icons.Default.CameraAlt,
                        label = "Capture Frame",
                        onClick = {
                            scope.launch {
                                val fileName = playbackManager.captureCurrentFrame()
                                if (fileName != null) {
                                    Toast.makeText(context, "Frame saved to Pictures/Salim", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Could not capture frame", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    FeaturePillButton(
                        icon = Icons.Default.Bookmark,
                        label = "Bookmarks (${uiState.bookmarks.size})",
                        onClick = {
                            onDismiss()
                            onOpenBookmarks()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FeaturePillButton(
                        icon = Icons.Default.Subtitles,
                        label = "Subtitles",
                        onClick = {
                            onDismiss()
                            onOpenSubtitles()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    FeaturePillButton(
                        icon = Icons.Default.Speed,
                        label = "Speed (${uiState.playbackSpeed}x)",
                        onClick = {
                            onDismiss()
                            onOpenSpeed()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FeaturePillButton(
                        icon = Icons.Default.Headphones,
                        label = if (uiState.isAudioOnlyMode) "Audio-Only On" else "Audio-Only Off",
                        tint = if (uiState.isAudioOnlyMode) Color(0xFF34C759) else Color.White,
                        onClick = { playbackManager.toggleAudioOnlyMode() },
                        modifier = Modifier.weight(1f)
                    )

                    FeaturePillButton(
                        icon = Icons.Default.ScreenRotation,
                        label = "Lock: ${uiState.orientationLock.capitalize()}",
                        onClick = { playbackManager.toggleOrientationLock() },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FeaturePillButton(
                        icon = when (uiState.repeatMode) {
                            RepeatMode.ONE -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        },
                        label = uiState.repeatMode.label,
                        tint = if (uiState.repeatMode != RepeatMode.OFF) Color(0xFF64D2FF) else Color.White,
                        onClick = { playbackManager.toggleRepeatMode() },
                        modifier = Modifier.weight(1f)
                    )

                    FeaturePillButton(
                        icon = Icons.Default.Shuffle,
                        label = if (uiState.isShuffle) "Shuffle On" else "Shuffle Off",
                        tint = if (uiState.isShuffle) Color(0xFF34C759) else Color.White,
                        onClick = { playbackManager.toggleShuffle() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = TextSecondaryDark,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun FeaturePillButton(
    icon: ImageVector,
    label: String,
    tint: Color = Color.White,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceElevated)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, color = tint, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksModalSheet(
    bookmarks: List<BookmarkEntity>,
    onSelectBookmark: (BookmarkEntity) -> Unit,
    onAddBookmark: (String) -> Unit,
    onDeleteBookmark: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAddDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF16181F),
        scrimColor = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bookmarks & Timestamps",
                    color = TextPrimaryDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = "Add Bookmark", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            HorizontalDivider(color = Color(0x1FFFFFFF))
            Spacer(modifier = Modifier.height(8.dp))

            if (bookmarks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No bookmarks yet.\nTap the + button to bookmark the current timestamp.",
                        color = TextSecondaryDark,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(bookmarks, key = { it.bookmarkId }) { bm ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(DarkSurfaceElevated)
                                .clickable {
                                    onSelectBookmark(bm)
                                    onDismiss()
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF64D2FF).copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = bm.formattedPosition(),
                                        color = Color(0xFF64D2FF),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (bm.note.isNotBlank()) bm.note else "Timestamp Bookmark",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    maxLines = 1
                                )
                            }

                            IconButton(
                                onClick = { onDeleteBookmark(bm.bookmarkId) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF453A), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var noteText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = Color(0xFF1E2029),
            title = { Text("Bookmark Timestamp", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text("Add an optional note (e.g. Favorite scene)", color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0x33FFFFFF)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onAddBookmark(noteText)
                    showAddDialog = false
                }) {
                    Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitlesModalSheet(
    subtitleTracks: List<TrackInfo>,
    selectedTrackIndex: Int,
    subtitleDelayMs: Long,
    fontSizeSp: Int,
    isBackgroundEnabled: Boolean,
    onSelectTrack: (Int) -> Unit,
    onAddExternalSubtitle: (Uri) -> Unit,
    onAdjustDelayMs: (Long) -> Unit,
    onAdjustFontSize: (Int) -> Unit,
    onToggleBackground: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onAddExternalSubtitle(uri)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF16181F),
        scrimColor = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Subtitles & Captions",
                color = TextPrimaryDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            HorizontalDivider(color = Color(0x1FFFFFFF))

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "TRACK SELECTION",
                color = TextSecondaryDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Off Track
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelectTrack(-1) }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Off",
                    color = if (selectedTrackIndex == -1) Color(0xFF64D2FF) else Color.White,
                    fontSize = 16.sp,
                    fontWeight = if (selectedTrackIndex == -1) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
                if (selectedTrackIndex == -1) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF64D2FF))
                }
            }

            // Available subtitle tracks
            subtitleTracks.forEach { track ->
                val isSelected = track.index == selectedTrackIndex
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectTrack(track.index) }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = track.name,
                        color = if (isSelected) Color(0xFF64D2FF) else Color.White,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF64D2FF))
                    }
                }
            }

            // Pick External Subtitle File button
            Spacer(modifier = Modifier.height(8.dp))
            FilledTonalButton(
                onClick = {
                    filePickerLauncher.launch(arrayOf("*/*"))
                },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = DarkSurfaceElevated,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add External Subtitle (.srt / .vtt)")
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color(0x1FFFFFFF))
            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle Delay Offset
            Text(
                text = "SUBTITLE SYNC DELAY",
                color = TextSecondaryDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { onAdjustDelayMs(subtitleDelayMs - 100) }) {
                    Text("-0.1s", color = Color.White)
                }
                Text(
                    text = "${String.format("%.1f", subtitleDelayMs / 1000f)}s",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                TextButton(onClick = { onAdjustDelayMs(subtitleDelayMs + 100) }) {
                    Text("+0.1s", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle Font Size
            Text(
                text = "TEXT SIZE (${fontSizeSp}sp)",
                color = TextSecondaryDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Slider(
                value = fontSizeSp.toFloat(),
                onValueChange = { onAdjustFontSize(it.toInt()) },
                valueRange = 12f..32f,
                steps = 10,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                )
            )

            // Subtitle Background Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Dark Text Background",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Switch(
                    checked = isBackgroundEnabled,
                    onCheckedChange = onToggleBackground,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF34C759)
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedModalSheet(
    currentSpeed: Float,
    onSelectSpeed: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val speeds = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 2.5f, 3.0f)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF16181F),
        scrimColor = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = "Playback Speed",
                color = TextPrimaryDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            HorizontalDivider(color = Color(0x1FFFFFFF))
            Spacer(modifier = Modifier.height(12.dp))

            speeds.forEach { speed ->
                val isSelected = speed == currentSpeed
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onSelectSpeed(speed)
                            onDismiss()
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (speed == 1.0f) "Normal (1.0x)" else "${speed}x",
                        color = if (isSelected) Color(0xFF64D2FF) else Color.White,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF64D2FF))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerModalSheet(
    remainingSeconds: Int?,
    onSetTimerMinutes: (Int) -> Unit,
    onCancelTimer: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val durations = listOf(15, 30, 45, 60, 90)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF16181F),
        scrimColor = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = "Sleep Timer",
                color = TextPrimaryDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            if (remainingSeconds != null) {
                val mins = remainingSeconds / 60
                val secs = remainingSeconds % 60
                Text(
                    text = "Playback will pause in ${String.format("%02d:%02d", mins, secs)}",
                    color = Color(0xFF64D2FF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            HorizontalDivider(color = Color(0x1FFFFFFF))
            Spacer(modifier = Modifier.height(12.dp))

            durations.forEach { minutes ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onSetTimerMinutes(minutes)
                            onDismiss()
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$minutes minutes",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (remainingSeconds != null) {
                Spacer(modifier = Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = {
                        onCancelTimer()
                        onDismiss()
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFFFF453A).copy(alpha = 0.2f),
                        contentColor = Color(0xFFFF453A)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Turn Off Timer", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
