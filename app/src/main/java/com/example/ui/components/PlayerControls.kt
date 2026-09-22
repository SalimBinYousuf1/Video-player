package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playback.AspectRatioMode
import com.example.playback.PlaybackUiState

fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}

@Composable
fun PlayerTopBar(
    title: String,
    aspectRatioMode: AspectRatioMode,
    isSleepTimerActive: Boolean,
    onBackClick: () -> Unit,
    onToggleAspectRatio: () -> Unit,
    onSubtitlesClick: () -> Unit,
    onSpeedClick: () -> Unit,
    onSleepTimerClick: () -> Unit,
    onPipClick: () -> Unit,
    transparencyAlpha: Float = 0.65f,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Back Button in Squircle Glass
        GlassSurface(
            shape = RoundedCornerShape(16.dp),
            transparencyAlpha = transparencyAlpha,
            modifier = Modifier.size(44.dp),
            onClick = onBackClick
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Video Title pill
        GlassSurface(
            shape = RoundedCornerShape(16.dp),
            transparencyAlpha = transparencyAlpha,
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .padding(horizontal = 4.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Actions Group
        GlassSurface(
            shape = RoundedCornerShape(18.dp),
            transparencyAlpha = transparencyAlpha,
            modifier = Modifier.height(44.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                IconButton(onClick = onToggleAspectRatio, modifier = Modifier.size(38.dp)) {
                    Icon(
                        imageVector = Icons.Default.AspectRatio,
                        contentDescription = "Aspect Ratio: $aspectRatioMode",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onSubtitlesClick, modifier = Modifier.size(38.dp)) {
                    Icon(
                        imageVector = Icons.Default.Subtitles,
                        contentDescription = "Subtitles",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onSpeedClick, modifier = Modifier.size(38.dp)) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Speed",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onSleepTimerClick, modifier = Modifier.size(38.dp)) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = "Sleep Timer",
                        tint = if (isSleepTimerActive) Color(0xFF64D2FF) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onPipClick, modifier = Modifier.size(38.dp)) {
                    Icon(
                        imageVector = Icons.Default.PictureInPictureAlt,
                        contentDescription = "Picture in Picture",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PlayerCenterControls(
    isPlaying: Boolean,
    skipSec: Int,
    onPlayPauseClick: () -> Unit,
    onSeekBackward: () -> Unit,
    onSeekForward: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    transparencyAlpha: Float = 0.65f,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Previous Video
        GlassSurface(
            shape = CircleShape,
            transparencyAlpha = transparencyAlpha,
            modifier = Modifier.size(48.dp),
            onClick = onPreviousClick
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Previous",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // Backward 10s
        GlassSurface(
            shape = CircleShape,
            transparencyAlpha = transparencyAlpha,
            modifier = Modifier.size(54.dp),
            onClick = onSeekBackward
        ) {
            Icon(
                imageVector = Icons.Default.Replay10,
                contentDescription = "Seek -$skipSec sec",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        // Play / Pause Hero Glass Pill
        GlassSurface(
            shape = CircleShape,
            transparencyAlpha = (transparencyAlpha * 1.25f).coerceAtMost(0.95f),
            modifier = Modifier.size(76.dp),
            onClick = onPlayPauseClick
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(38.dp)
            )
        }

        // Forward 10s
        GlassSurface(
            shape = CircleShape,
            transparencyAlpha = transparencyAlpha,
            modifier = Modifier.size(54.dp),
            onClick = onSeekForward
        ) {
            Icon(
                imageVector = Icons.Default.Forward10,
                contentDescription = "Seek +$skipSec sec",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        // Next Video
        GlassSurface(
            shape = CircleShape,
            transparencyAlpha = transparencyAlpha,
            modifier = Modifier.size(48.dp),
            onClick = onNextClick
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Next",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun PlayerBottomBar(
    currentPositionMs: Long,
    durationMs: Long,
    playbackSpeed: Float,
    isLocked: Boolean,
    onSeek: (Long) -> Unit,
    onSpeedClick: () -> Unit,
    onToggleLock: () -> Unit,
    transparencyAlpha: Float = 0.65f,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragPositionMs by remember { mutableFloatStateOf(0f) }

    val effectivePosition = if (isDragging) dragPositionMs.toLong() else currentPositionMs
    val durationFloat = durationMs.coerceAtLeast(1L).toFloat()

    GlassSurface(
        shape = RoundedCornerShape(26.dp),
        transparencyAlpha = transparencyAlpha,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Live Scrubber Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTime(effectivePosition),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Slider(
                    value = effectivePosition.toFloat().coerceIn(0f, durationFloat),
                    onValueChange = {
                        isDragging = true
                        dragPositionMs = it
                    },
                    onValueChangeFinished = {
                        isDragging = false
                        onSeek(dragPositionMs.toLong())
                    },
                    valueRange = 0f..durationFloat,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )

                Text(
                    text = formatTime(durationMs),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Bottom row info: Speed pill and Screen Lock toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lock Button
                IconButton(onClick = onToggleLock, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = if (isLocked) "Unlock Controls" else "Lock Controls",
                        tint = if (isLocked) Color(0xFFFF9F0A) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Speed Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${playbackSpeed}x",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Vertical continuous gesture indicators for Volume and Brightness
 */
@Composable
fun GestureLevelIndicator(
    isVolume: Boolean,
    levelFraction: Float,
    modifier: Modifier = Modifier
) {
    GlassSurface(
        shape = RoundedCornerShape(20.dp),
        transparencyAlpha = 0.85f,
        modifier = modifier
            .width(64.dp)
            .height(180.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = if (isVolume) Icons.Default.VolumeUp else Icons.Default.BrightnessMedium,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )

            // Vertical gauge bar
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .weight(1f)
                    .padding(vertical = 10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(levelFraction.coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White)
                )
            }

            Text(
                text = "${(levelFraction * 100).toInt()}%",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
