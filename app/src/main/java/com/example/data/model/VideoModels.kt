package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class VideoItemEntity(
    @PrimaryKey val id: Long,
    val uri: String,
    val title: String,
    val displayName: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val dateAdded: Long,
    val resolution: String,
    val width: Int,
    val height: Int,
    val mimeType: String,
    val path: String,
    val folderName: String,
    val lastWatchedPositionMs: Long = 0L,
    val lastWatchedTimestamp: Long = 0L,
    val isCompleted: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val selectedSubtitleTrack: Int = -1,
    val subtitleDelayMs: Long = 0L
) {
    val progressFraction: Float
        get() = if (durationMs > 0) (lastWatchedPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val isResumeAvailable: Boolean
        get() = lastWatchedPositionMs > 3000L && !isCompleted && (durationMs - lastWatchedPositionMs > 5000L)

    fun formattedDuration(): String {
        val totalSeconds = durationMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }

    fun formattedSize(): String {
        val mb = sizeBytes / (1024.0 * 1024.0)
        return if (mb >= 1024.0) {
            String.format("%.1f GB", mb / 1024.0)
        } else {
            String.format("%.1f MB", mb)
        }
    }
}

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val playlistId: Long = 0L,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_items", primaryKeys = ["playlistId", "videoId"])
data class PlaylistItemCrossRef(
    val playlistId: Long,
    val videoId: Long,
    val orderIndex: Int = 0
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val historyId: Long = 0L,
    val videoId: Long,
    val videoTitle: String,
    val watchedAt: Long = System.currentTimeMillis(),
    val positionMs: Long,
    val durationMs: Long
)
