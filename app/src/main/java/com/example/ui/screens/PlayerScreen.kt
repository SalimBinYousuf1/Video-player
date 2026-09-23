package com.example.ui.screens

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.os.Build
import android.util.Rational
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.playback.AspectRatioMode
import com.example.playback.SalimPlaybackManager
import com.example.ui.components.BookmarksModalSheet
import com.example.ui.components.GestureLevelIndicator
import com.example.ui.components.GlassSurface
import com.example.ui.components.PlayerBottomBar
import com.example.ui.components.PlayerCenterControls
import com.example.ui.components.PlayerMoreFeaturesSheet
import com.example.ui.components.PlayerStatusBanner
import com.example.ui.components.PlayerTopBar
import com.example.ui.components.SleepTimerModalSheet
import com.example.ui.components.SpeedModalSheet
import com.example.ui.components.SubtitlesModalSheet

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    playbackManager: SalimPlaybackManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by playbackManager.uiState.collectAsState()

    var showMoreFeaturesSheet by remember { mutableStateOf(false) }
    var showBookmarksSheet by remember { mutableStateOf(false) }
    var showSubtitlesSheet by remember { mutableStateOf(false) }
    var showSpeedSheet by remember { mutableStateOf(false) }
    var showSleepTimerSheet by remember { mutableStateOf(false) }

    // Orientation Lock controller
    LaunchedEffect(uiState.orientationLock) {
        activity?.let { act ->
            act.requestedOrientation = when (uiState.orientationLock) {
                "landscape" -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                "portrait" -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    // Synchronize brightness with Activity Window
    LaunchedEffect(uiState.brightnessLevel) {
        activity?.window?.let { window ->
            val layoutParams = window.attributes
            layoutParams.screenBrightness = uiState.brightnessLevel
            window.attributes = layoutParams
        }
    }

    // Keep screen on while playing
    DisposableEffect(Unit) {
        activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.window?.let { window ->
                val layoutParams = window.attributes
                layoutParams.screenBrightness = -1f
                window.attributes = layoutParams
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Video Surface with AspectRatio & Zoom/Pan transform
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = uiState.zoomScale,
                    scaleY = uiState.zoomScale,
                    translationX = uiState.panOffsetX,
                    translationY = uiState.panOffsetY
                )
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = playbackManager.player
                        useController = false
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        resizeMode = when (uiState.aspectRatioMode) {
                            AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                            AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            AspectRatioMode.STRETCH -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                            AspectRatioMode.ZOOM_100 -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                            AspectRatioMode.ZOOM_200 -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        }
                    }
                },
                update = { view ->
                    view.player = playbackManager.player
                    view.resizeMode = when (uiState.aspectRatioMode) {
                        AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        AspectRatioMode.STRETCH -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                        AspectRatioMode.ZOOM_100 -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        AspectRatioMode.ZOOM_200 -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Audio-Only Mode Overlay (Apple Ambient Screen)
        if (uiState.isAudioOnlyMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.94f)),
                contentAlignment = Alignment.Center
            ) {
                GlassSurface(
                    shape = CircleShape,
                    modifier = Modifier.size(110.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = "Audio-Only Mode",
                        tint = Color(0xFF34C759),
                        modifier = Modifier.size(54.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 120.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Audio-Only Background Mode Active",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Display rendering paused to save battery",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Gesture Overlay Layer: Tap, Double Tap, Vertical Drag Swipes, Pinch to Zoom, Long-Press 2x Hold
        var touchWidth by remember { mutableFloatStateOf(1000f) }
        var touchHeight by remember { mutableFloatStateOf(2000f) }

        val context = LocalContext.current
        val activity = context as? android.app.Activity
        val isInPip = activity?.isInPictureInPictureMode == true

        // Vertical Drag Gestures: Left 40% -> Brightness, Right 40% -> Volume
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Transform Gestures: Pinch-to-zoom (1.0x - 5.0x) + Pan
                .pointerInput(uiState.isLocked, isInPip) {
                    if (!uiState.isLocked && !isInPip) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newZoom = (uiState.zoomScale * zoom).coerceIn(1.0f, 5.0f)
                            val newPanX = if (newZoom > 1.0f) uiState.panOffsetX + pan.x else 0f
                            val newPanY = if (newZoom > 1.0f) uiState.panOffsetY + pan.y else 0f
                            playbackManager.setZoomAndPan(newZoom, newPanX, newPanY)
                        }
                    }
                }
                // Tap, Double-tap, and YouTube-style Long Press 2.0x Fast Forward Hold
                .pointerInput(uiState.isLocked, uiState.doubleTapSeekSeconds, isInPip) {
                    touchWidth = size.width.toFloat()
                    touchHeight = size.height.toFloat()

                    if (!isInPip) {
                        detectTapGestures(
                            onTap = {
                                playbackManager.toggleControls()
                            },
                            onDoubleTap = { offset ->
                                if (!uiState.isLocked) {
                                    val seekSeconds = uiState.doubleTapSeekSeconds
                                    if (offset.x < touchWidth * 0.38f) {
                                        playbackManager.seekBy(-seekSeconds)
                                    } else if (offset.x > touchWidth * 0.62f) {
                                        playbackManager.seekBy(seekSeconds)
                                    } else {
                                        playbackManager.togglePlayPause()
                                    }
                                }
                            },
                            onPress = {
                                if (!uiState.isLocked) {
                                    try {
                                        val isLongPress = tryAwaitRelease()
                                        if (uiState.isFastForwardingPreview) {
                                            playbackManager.stopTemporaryFastForward()
                                        }
                                    } finally {
                                        if (uiState.isFastForwardingPreview) {
                                            playbackManager.stopTemporaryFastForward()
                                        }
                                    }
                                }
                            },
                            onLongPress = {
                                if (!uiState.isLocked) {
                                    playbackManager.startTemporaryFastForward()
                                }
                            }
                        )
                    }
                }
                // Vertical Drag Gestures: Left 40% -> Brightness, Right 40% -> Volume (Center 20% deadzone)
                .pointerInput(uiState.isLocked, isInPip) {
                    if (!uiState.isLocked && !isInPip) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val posX = change.position.x
                            val totalW = size.width.toFloat()
                            val deltaFraction = -dragAmount.y / (size.height * 0.60f)

                            if (posX < totalW * 0.40f) {
                                playbackManager.adjustBrightness(deltaFraction)
                            } else if (posX > totalW * 0.60f) {
                                playbackManager.adjustVolume(deltaFraction)
                            }
                        }
                    }
                }
        )

        // Volume / Brightness Floating HUD Indicators (Suppressed in PiP)
        AnimatedVisibility(
            visible = !isInPip && uiState.showVolumeIndicator,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp)
        ) {
            GestureLevelIndicator(isVolume = true, levelFraction = uiState.volumeLevel)
        }

        AnimatedVisibility(
            visible = !isInPip && uiState.showBrightnessIndicator,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp)
        ) {
            GestureLevelIndicator(isVolume = false, levelFraction = uiState.brightnessLevel)
        }

        // Status Banners (Fast Forward / A-B Loop - Suppressed in PiP)
        if (!isInPip) {
            PlayerStatusBanner(
                isFastForwarding = uiState.isFastForwardingPreview,
                isAbLoopActive = uiState.isAbLoopActive,
                loopPointA = uiState.loopPointA,
                loopPointB = uiState.loopPointB,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 76.dp)
            )
        }

        // Apple Liquid Glass Controls Overlay (Suppressed in PiP)
        AnimatedVisibility(
            visible = !isInPip && uiState.isControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                if (!uiState.isLocked) {
                    PlayerTopBar(
                        title = uiState.currentVideo?.title ?: "Video",
                        aspectRatioMode = uiState.aspectRatioMode,
                        onBackClick = onBack,
                        onCycleAspectRatio = { playbackManager.cycleAspectRatioMode() },
                        onPipClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activity != null) {
                                val params = PictureInPictureParams.Builder()
                                    .setAspectRatio(Rational(16, 9))
                                    .build()
                                activity.enterPictureInPictureMode(params)
                            }
                        },
                        onMoreOptionsClick = { showMoreFeaturesSheet = true },
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }

                // Center Play/Pause & Skip Controls
                if (!uiState.isLocked) {
                    PlayerCenterControls(
                        isPlaying = uiState.isPlaying,
                        skipSec = uiState.doubleTapSeekSeconds,
                        onPlayPauseClick = { playbackManager.togglePlayPause() },
                        onSeekBackward = { playbackManager.seekBy(-uiState.doubleTapSeekSeconds) },
                        onSeekForward = { playbackManager.seekBy(uiState.doubleTapSeekSeconds) },
                        onPreviousClick = { playbackManager.playPreviousIfAvailable() },
                        onNextClick = { playbackManager.playNextIfAvailable() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Bottom Scrubber Bar & Quick Action Strip
                PlayerBottomBar(
                    currentPositionMs = uiState.currentPositionMs,
                    durationMs = uiState.durationMs,
                    playbackSpeed = uiState.playbackSpeed,
                    repeatMode = uiState.repeatMode,
                    isLocked = uiState.isLocked,
                    onSeek = { pos -> playbackManager.seekToPosition(pos) },
                    onSpeedClick = { showSpeedSheet = true },
                    onRepeatClick = { playbackManager.toggleRepeatMode() },
                    onBookmarksClick = { showBookmarksSheet = true },
                    onMoreToolsClick = { showMoreFeaturesSheet = true },
                    onToggleLock = { playbackManager.setScreenLock(!uiState.isLocked) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }

    // Modal Sheets
    if (showMoreFeaturesSheet) {
        PlayerMoreFeaturesSheet(
            playbackManager = playbackManager,
            uiState = uiState,
            onOpenSubtitles = { showSubtitlesSheet = true },
            onOpenSpeed = { showSpeedSheet = true },
            onOpenSleepTimer = { showSleepTimerSheet = true },
            onOpenBookmarks = { showBookmarksSheet = true },
            onDismiss = { showMoreFeaturesSheet = false }
        )
    }

    if (showBookmarksSheet) {
        BookmarksModalSheet(
            bookmarks = uiState.bookmarks,
            onSelectBookmark = { bm -> playbackManager.seekToPosition(bm.positionMs) },
            onAddBookmark = { note -> playbackManager.addBookmarkAtCurrent(note) },
            onDeleteBookmark = { id -> playbackManager.deleteBookmark(id) },
            onDismiss = { showBookmarksSheet = false }
        )
    }

    if (showSubtitlesSheet) {
        SubtitlesModalSheet(
            subtitleTracks = uiState.subtitleTracks,
            selectedTrackIndex = uiState.selectedSubtitleTrackIndex,
            subtitleDelayMs = uiState.subtitleDelayMs,
            fontSizeSp = 18,
            isBackgroundEnabled = true,
            onSelectTrack = { index -> playbackManager.selectSubtitleTrack(index) },
            onAddExternalSubtitle = { uri -> playbackManager.addExternalSubtitle(uri) },
            onAdjustDelayMs = { delay -> playbackManager.setSubtitleDelayMs(delay) },
            onAdjustFontSize = { /* settings saved */ },
            onToggleBackground = { /* settings saved */ },
            onDismiss = { showSubtitlesSheet = false }
        )
    }

    if (showSpeedSheet) {
        SpeedModalSheet(
            currentSpeed = uiState.playbackSpeed,
            onSelectSpeed = { speed -> playbackManager.setPlaybackSpeed(speed) },
            onDismiss = { showSpeedSheet = false }
        )
    }

    if (showSleepTimerSheet) {
        SleepTimerModalSheet(
            remainingSeconds = uiState.sleepTimerSecondsRemaining,
            onSetTimerMinutes = { mins -> playbackManager.startSleepTimer(mins) },
            onCancelTimer = { playbackManager.cancelSleepTimer() },
            onDismiss = { showSleepTimerSheet = false }
        )
    }
}
