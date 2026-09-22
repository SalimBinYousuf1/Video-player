package com.example.ui.components

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.VideoItemEntity
import com.example.ui.theme.DarkGlassBase
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VideoGridItem(
    video: VideoItemEntity,
    onClick: () -> Unit,
    onLongPressThreshold: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    val scaleAnim = remember { Animatable(1f) }
    val holdProgress = remember { Animatable(0f) }
    var holdJob by remember { mutableStateOf<Job?>(null) }

    val videoUri = remember(video.id) {
        ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, video.id)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .scale(scaleAnim.value)
            .pointerInput(video.id) {
                detectTapGestures(
                    onPress = {
                        // Instant contact feedback with spring compression
                        val pressScope = this
                        scope.launch {
                            scaleAnim.animateTo(0.96f, spring(stiffness = Spring.StiffnessMediumLow))
                        }
                        holdJob?.cancel()
                        holdJob = scope.launch {
                            // Deliberate 2-second hold progression
                            holdProgress.snapTo(0f)
                            holdProgress.animateTo(1f, tween(durationMillis = 2000, easing = LinearEasing))
                            // Trigger threshold haptic tick
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            onLongPressThreshold()
                            scaleAnim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy))
                            holdProgress.snapTo(0f)
                        }

                        val released = pressScope.tryAwaitRelease()
                        holdJob?.cancel()
                        scope.launch {
                            scaleAnim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy))
                            holdProgress.snapTo(0f)
                        }
                        if (released && holdProgress.value < 0.95f) {
                            onClick()
                        }
                    }
                )
            }
    ) {
        // Thumbnail Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
                .clip(RoundedCornerShape(18.dp))
                .background(DarkSurfaceElevated)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(videoUri)
                    .crossfade(true)
                    .build(),
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Vignette gradient for legibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f)
                            )
                        )
                    )
            )

            // Duration Pill Badge (Liquid Glass)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.70f))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = video.formattedDuration(),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Watch progress bar if partially watched
            if (video.isResumeAvailable) {
                LinearProgressIndicator(
                    progress = { video.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.25f),
                    strokeCap = StrokeCap.Round
                )
            }

            // Hold indicator overlay during 2-second hold
            if (holdProgress.value > 0.05f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { holdProgress.value },
                        modifier = Modifier.size(42.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Video title
        Text(
            text = video.title,
            color = TextPrimaryDark,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Subtext: resolution and size
        Text(
            text = "${video.resolution} • ${video.formattedSize()}",
            color = TextSecondaryDark,
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}

@Composable
fun VideoListItem(
    video: VideoItemEntity,
    onClick: () -> Unit,
    onLongPressThreshold: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    val scaleAnim = remember { Animatable(1f) }
    val holdProgress = remember { Animatable(0f) }
    var holdJob by remember { mutableStateOf<Job?>(null) }

    val videoUri = remember(video.id) {
        ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, video.id)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scaleAnim.value)
            .pointerInput(video.id) {
                detectTapGestures(
                    onPress = {
                        val pressScope = this
                        scope.launch {
                            scaleAnim.animateTo(0.97f, spring(stiffness = Spring.StiffnessMediumLow))
                        }
                        holdJob?.cancel()
                        holdJob = scope.launch {
                            holdProgress.snapTo(0f)
                            holdProgress.animateTo(1f, tween(durationMillis = 2000, easing = LinearEasing))
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            onLongPressThreshold()
                            scaleAnim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy))
                            holdProgress.snapTo(0f)
                        }

                        val released = pressScope.tryAwaitRelease()
                        holdJob?.cancel()
                        scope.launch {
                            scaleAnim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy))
                            holdProgress.snapTo(0f)
                        }
                        if (released && holdProgress.value < 0.95f) {
                            onClick()
                        }
                    }
                )
            }
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail Box
        Box(
            modifier = Modifier
                .width(108.dp)
                .height(68.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurfaceElevated)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(videoUri)
                    .crossfade(true)
                    .build(),
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Duration Pill Badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.70f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = video.formattedDuration(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (video.isResumeAvailable) {
                LinearProgressIndicator(
                    progress = { video.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .align(Alignment.BottomCenter),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.25f)
                )
            }

            if (holdProgress.value > 0.05f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { holdProgress.value },
                        modifier = Modifier.size(32.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Info Column
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = video.title,
                color = TextPrimaryDark,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "${video.folderName} • ${video.formattedSize()} • ${video.resolution}",
                color = TextSecondaryDark,
                fontSize = 12.sp,
                maxLines = 1
            )
            if (video.isResumeAvailable) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Resume from ${video.formattedDuration()}",
                    color = Color(0xFF64D2FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
