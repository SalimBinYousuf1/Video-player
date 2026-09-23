package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playback.AspectRatioMode
import com.example.playback.RepeatMode

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

/**
 * Apple Minimalist Top Bar:
 * Back button | Title pill | Quick tools pill (Aspect/Zoom, PiP, More Options)
 * Completely eliminates button overcrowding and overlapping.
 */
@Composable
fun PlayerTopBar(
    title: String,
    aspectRatioMode: AspectRatioMode,
    onBackClick: () -> Unit,
    onCycleAspectRatio: () -> Unit,
    onPipClick: () -> Unit,
    onMoreOptionsClick: () -> Unit,
    transparencyAlpha: Float = 0.70f,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        GlassSurface(
            shape = RoundedCornerShape(14.dp),
            transparencyAlpha = transparencyAlpha,
            modifier = Modifier.size(36.dp),
            onClick = onBackClick
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Video Title pill with truncation
        GlassSurface(
            shape = RoundedCornerShape(14.dp),
            transparencyAlpha = transparencyAlpha,
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .padding(horizontal = 2.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Compact Apple Action Pod
        GlassSurface(
            shape = RoundedCornerShape(14.dp),
            transparencyAlpha = transparencyAlpha,
            modifier = Modifier.height(36.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                // Aspect Ratio / Zoom indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable(onClick = onCycleAspectRatio)
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = aspectRatioMode.label,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(2.dp))

                // PiP
                IconButton(onClick = onPipClick, modifier = Modifier.size(30.dp)) {
                    Icon(
                        imageVector = Icons.Default.PictureInPictureAlt,
                        contentDescription = "Picture in Picture",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // More Options (Opens comprehensive bottom sheet)
                IconButton(onClick = onMoreOptionsClick, modifier = Modifier.size(30.dp)) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Features",
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

/**
 * Status Banners floating below top bar (e.g. YouTube 2.0x Hold Preview or A-B Loop)
 */
@Composable
fun PlayerStatusBanner(
    isFastForwarding: Boolean,
    isAbLoopActive: Boolean,
    loopPointA: Long?,
    loopPointB: Long?,
    modifier: Modifier = Modifier
) {
    if (isFastForwarding) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFFF9500).copy(alpha = 0.90f))
                .padding(horizontal = 14.dp, vertical = 5.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.FastForward, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "2.0X SPEED PREVIEW", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    } else if (isAbLoopActive && loopPointA != null && loopPointB != null) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF007AFF).copy(alpha = 0.88f))
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text(
                text = "⟳ A-B Loop: ${formatTime(loopPointA)} - ${formatTime(loopPointB)}",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Center Playback Controls:
 * Previous | Seek Backward (-20s) | Play/Pause | Seek Forward (+20s) | Next
 * Sized with a sleek 25-30% scale reduction for comfortable ergonomics.
 */
@Composable
fun PlayerCenterControls(
    isPlaying: Boolean,
    skipSec: Int,
    onPlayPauseClick: () -> Unit,
    onSeekBackward: () -> Unit,
    onSeekForward: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    transparencyAlpha: Float = 0.70f,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Previous Video
        GlassSurface(
            shape = CircleShape,
            transparencyAlpha = transparencyAlpha,
            modifier = Modifier.size(36.dp),
            onClick = onPreviousClick
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Previous",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        // Backward Seek (e.g. -20s)
        GlassSurface(
            shape = CircleShape,
            transparencyAlpha = transparencyAlpha,
            modifier = Modifier.size(40.dp),
            onClick = onSeekBackward
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Replay10,
                    contentDescription = "Seek -$skipSec sec",
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )
                Text(
                    text = "${skipSec}s",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Play / Pause Hero Glass Button
        GlassSurface(
            shape = CircleShape,
            transparencyAlpha = (transparencyAlpha * 1.3f).coerceAtMost(0.96f),
            modifier = Modifier.size(56.dp),
            onClick = onPlayPauseClick
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        // Forward Seek (e.g. +20s)
        GlassSurface(
            shape = CircleShape,
            transparencyAlpha = transparencyAlpha,
            modifier = Modifier.size(40.dp),
            onClick = onSeekForward
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Forward10,
                    contentDescription = "Seek +$skipSec sec",
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )
                Text(
                    text = "${skipSec}s",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Next Video
        GlassSurface(
            shape = CircleShape,
            transparencyAlpha = transparencyAlpha,
            modifier = Modifier.size(36.dp),
            onClick = onNextClick
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Next",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Bottom Scrubber Bar & Quick Action Strip
 */
@Composable
fun PlayerBottomBar(
    currentPositionMs: Long,
    durationMs: Long,
    playbackSpeed: Float,
    repeatMode: RepeatMode,
    isLocked: Boolean,
    onSeek: (Long) -> Unit,
    onSpeedClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onBookmarksClick: () -> Unit,
    onMoreToolsClick: () -> Unit,
    onToggleLock: () -> Unit,
    transparencyAlpha: Float = 0.70f,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragPositionMs by remember { mutableFloatStateOf(0f) }

    val effectivePosition = if (isDragging) dragPositionMs.toLong() else currentPositionMs
    val durationFloat = durationMs.coerceAtLeast(1L).toFloat()

    GlassSurface(
        shape = RoundedCornerShape(22.dp),
        transparencyAlpha = transparencyAlpha,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            // Scrubber Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTime(effectivePosition),
                    color = Color.White,
                    fontSize = 11.sp,
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
                        .padding(horizontal = 6.dp)
                )

                Text(
                    text = formatTime(durationMs),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Quick Tool Buttons Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lock Button
                IconButton(onClick = onToggleLock, modifier = Modifier.size(30.dp)) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = if (isLocked) "Unlock Controls" else "Lock Controls",
                        tint = if (isLocked) Color(0xFFFF9F0A) else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Speed Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable(onClick = onSpeedClick)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${playbackSpeed}x",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Repeat Mode Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (repeatMode != RepeatMode.OFF) Color(0xFF007AFF).copy(alpha = 0.35f) else Color.White.copy(alpha = 0.15f))
                            .clickable(onClick = onRepeatClick)
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                            contentDescription = "Repeat",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Bookmarks Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable(onClick = onBookmarksClick)
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Bookmarks",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // More Tools Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable(onClick = onMoreToolsClick)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Tools", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Vertical continuous gesture indicators for Volume and Brightness
 * Features dynamic Audio Boost support (up to 200% / +20dB)
 */
@Composable
fun GestureLevelIndicator(
    isVolume: Boolean,
    levelFraction: Float,
    modifier: Modifier = Modifier
) {
    val isBoost = isVolume && levelFraction > 1.0f
    val displayPercent = (levelFraction * 100).toInt()
    val boostGainDb = if (isBoost) String.format("+%.1fdB", (levelFraction - 1.0f) * 20f) else ""

    GlassSurface(
        shape = RoundedCornerShape(18.dp),
        transparencyAlpha = 0.88f,
        modifier = modifier
            .width(52.dp)
            .height(152.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = if (isVolume) Icons.Default.VolumeUp else Icons.Default.BrightnessMedium,
                contentDescription = null,
                tint = if (isBoost) Color(0xFFFF9F0A) else Color.White,
                modifier = Modifier.size(20.dp)
            )

            // Vertical gauge bar
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .weight(1f)
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Base volume/brightness track (0 - 100%)
                val baseFraction = levelFraction.coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(baseFraction)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isBoost) Color(0xFFFF9F0A) else Color.White)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$displayPercent%",
                    color = if (isBoost) Color(0xFFFF9F0A) else Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                if (isBoost) {
                    Text(
                        text = "BOOST",
                        color = Color(0xFFFF9F0A),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
