package com.example.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MainActivity
import com.example.SalimApplication
import kotlinx.coroutines.flow.firstOrNull

class SalimContinueWatchingWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as? SalimApplication
        val latestVideo = app?.videoRepository?.continueWatchingVideos?.firstOrNull()?.firstOrNull()
            ?: app?.videoRepository?.allVideos?.firstOrNull()?.firstOrNull()

        provideContent {
            GlanceTheme {
                WidgetContent(context = context, videoTitle = latestVideo?.title, videoId = latestVideo?.id)
            }
        }
    }

    @Composable
    private fun WidgetContent(context: Context, videoTitle: String?, videoId: Long?) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (videoId != null) {
                putExtra("RESUME_VIDEO_ID", videoId)
            }
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(androidx.compose.ui.graphics.Color(0xFF0F1015)))
                .cornerRadius(20.dp)
                .padding(14.dp)
                .clickable(actionStartActivity(intent)),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play Icon container
                Box(
                    modifier = GlanceModifier
                        .size(46.dp)
                        .background(ColorProvider(androidx.compose.ui.graphics.Color(0x33FFFFFF)))
                        .cornerRadius(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶",
                        style = TextStyle(
                            color = ColorProvider(androidx.compose.ui.graphics.Color.White),
                            fontSize = 18.sp
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.width(12.dp))

                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "Continue Watching",
                        style = TextStyle(
                            color = ColorProvider(androidx.compose.ui.graphics.Color(0x99FFFFFF)),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(3.dp))
                    Text(
                        text = videoTitle ?: "Salim Video Player",
                        maxLines = 1,
                        style = TextStyle(
                            color = ColorProvider(androidx.compose.ui.graphics.Color.White),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

class SalimWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SalimContinueWatchingWidget()
}
