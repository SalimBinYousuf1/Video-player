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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.playback.AspectRatioMode
import com.example.playback.SalimPlaybackManager
import com.example.ui.components.GestureLevelIndicator
import com.example.ui.components.PlayerBottomBar
import com.example.ui.components.PlayerCenterControls
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

    var showSubtitlesSheet by remember { mutableStateOf(false) }
    var showSpeedSheet by remember { mutableStateOf(false) }
    var showSleepTimerSheet by remember { mutableStateOf(false) }

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
            // Restore window brightness
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
                        }
                    }
                },
                update = { view ->
                    view.player = playbackManager.player
                    view.resizeMode = when (uiState.aspectRatioMode) {
                        AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        AspectRatioMode.STRETCH -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Gesture Overlay Layer: Tap, Double Tap, Vertical Swipes, Pinch to Zoom
        var touchWidth by remember { mutableFloatStateOf(1000f) }
        var touchHeight by remember { mutableFloatStateOf(2000f) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                // Transform Gestures: Pinch-to-zoom (1x - 4x)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        if (!uiState.isLocked) {
                            val newZoom = (uiState.zoomScale * zoom).coerceIn(1.0f, 4.0f)
                            val newPanX = if (newZoom > 1.0f) uiState.panOffsetX + pan.x else 0f
                            val newPanY = if (newZoom > 1.0f) uiState.panOffsetY + pan.y else 0f
                            playbackManager.setZoomAndPan(newZoom, newPanX, newPanY)
                        }
                    }
                }
                // Tap and Double-tap Gestures
                .pointerInput(uiState.isLocked) {
                    touchWidth = size.width.toFloat()
                    touchHeight = size.height.toFloat()

                    detectTapGestures(
                        onTap = {
                            playbackManager.toggleControls()
                        },
                        onDoubleTap = { offset ->
                            if (!uiState.isLocked) {
                                if (offset.x < touchWidth * 0.4f) {
                                    playbackManager.seekBy(-10)
                                } else if (offset.x > touchWidth * 0.6f) {
                                    playbackManager.seekBy(10)
                                } else {
                                    playbackManager.togglePlayPause()
                                }
                            }
                        }
                    )
                }
                // Vertical Swipe for Brightness (Left) and Volume (Right)
                .pointerInput(uiState.isLocked) {
                    detectTapGestures(onPress = { offset ->
                        val isLeftSide = offset.x < size.width / 2f
                        var previousY = offset.y

                        val isReleased = tryAwaitRelease()
                    })
                }
        )

        // Volume / Brightness Floating HUD Indicators
        AnimatedVisibility(
            visible = uiState.showVolumeIndicator,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp)
        ) {
            GestureLevelIndicator(isVolume = true, levelFraction = uiState.volumeLevel)
        }

        AnimatedVisibility(
            visible = uiState.showBrightnessIndicator,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp)
        ) {
            GestureLevelIndicator(isVolume = false, levelFraction = uiState.brightnessLevel)
        }

        // Apple Liquid Glass Controls Overlay
        AnimatedVisibility(
            visible = uiState.isControlsVisible,
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
                        isSleepTimerActive = uiState.sleepTimerSecondsRemaining != null,
                        onBackClick = onBack,
                        onToggleAspectRatio = { playbackManager.toggleAspectRatioMode() },
                        onSubtitlesClick = { showSubtitlesSheet = true },
                        onSpeedClick = { showSpeedSheet = true },
                        onSleepTimerClick = { showSleepTimerSheet = true },
                        onPipClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activity != null) {
                                val params = PictureInPictureParams.Builder()
                                    .setAspectRatio(Rational(16, 9))
                                    .build()
                                activity.enterPictureInPictureMode(params)
                            }
                        },
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }

                // Center Play/Pause & Skip Controls
                if (!uiState.isLocked) {
                    PlayerCenterControls(
                        isPlaying = uiState.isPlaying,
                        skipSec = 10,
                        onPlayPauseClick = { playbackManager.togglePlayPause() },
                        onSeekBackward = { playbackManager.seekBy(-10) },
                        onSeekForward = { playbackManager.seekBy(10) },
                        onPreviousClick = { playbackManager.playPreviousIfAvailable() },
                        onNextClick = { playbackManager.playNextIfAvailable() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Bottom Scrubber Bar & Lock Toggle
                PlayerBottomBar(
                    currentPositionMs = uiState.currentPositionMs,
                    durationMs = uiState.durationMs,
                    playbackSpeed = uiState.playbackSpeed,
                    isLocked = uiState.isLocked,
                    onSeek = { pos -> playbackManager.seekToPosition(pos) },
                    onSpeedClick = { showSpeedSheet = true },
                    onToggleLock = { playbackManager.setScreenLock(!uiState.isLocked) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }

    // Modal Sheets
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
            onAdjustFontSize = { /* updated in state */ },
            onToggleBackground = { /* updated in state */ },
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
