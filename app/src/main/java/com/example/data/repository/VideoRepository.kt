package com.example.data.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.data.db.BookmarkDao
import com.example.data.db.PlaylistDao
import com.example.data.db.VideoDao
import com.example.data.db.WatchHistoryDao
import com.example.data.mediastore.MediaStoreVideoScanner
import com.example.data.mediastore.VideoDetailedMetadata
import com.example.data.model.BookmarkEntity
import com.example.data.model.PlaylistEntity
import com.example.data.model.PlaylistItemCrossRef
import com.example.data.model.VideoItemEntity
import com.example.data.model.WatchHistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

sealed interface ScanState {
    object Idle : ScanState
    data class Scanning(val processedCount: Int, val totalFound: Int) : ScanState
    data class Completed(val totalFound: Int) : ScanState
}

class VideoRepository(
    private val context: Context,
    private val videoDao: VideoDao,
    private val playlistDao: PlaylistDao,
    private val watchHistoryDao: WatchHistoryDao,
    private val bookmarkDao: BookmarkDao,
    private val scanner: MediaStoreVideoScanner,
    private val settingsRepository: UserSettingsRepository
) {

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    val allVideos: Flow<List<VideoItemEntity>> = videoDao.getAllVideos()
    val continueWatchingVideos: Flow<List<VideoItemEntity>> = videoDao.getContinueWatching()
    val allPlaylists: Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()
    val watchHistory: Flow<List<WatchHistoryEntity>> = watchHistoryDao.getWatchHistory()

    fun getVideoById(id: Long): Flow<VideoItemEntity?> = videoDao.getVideoById(id)

    suspend fun getVideoByIdSync(id: Long): VideoItemEntity? = videoDao.getVideoByIdSync(id)

    fun getVideosForPlaylist(playlistId: Long): Flow<List<VideoItemEntity>> =
        playlistDao.getVideosForPlaylist(playlistId)

    suspend fun scanLibrary() = withContext(Dispatchers.IO) {
        val settings = settingsRepository.userSettingsFlow.first()
        _scanState.value = ScanState.Scanning(0, 0)

        val scannedVideos = scanner.scanDeviceVideos(settings.excludedFolders)
        _scanState.value = ScanState.Scanning(scannedVideos.size, scannedVideos.size)

        // Preserve already saved watch progress
        val existingVideos = videoDao.getAllVideos().first().associateBy { it.id }

        val mergedVideos = scannedVideos.map { scanned ->
            val existing = existingVideos[scanned.id]
            if (existing != null) {
                scanned.copy(
                    lastWatchedPositionMs = existing.lastWatchedPositionMs,
                    lastWatchedTimestamp = existing.lastWatchedTimestamp,
                    isCompleted = existing.isCompleted,
                    playbackSpeed = existing.playbackSpeed,
                    selectedSubtitleTrack = existing.selectedSubtitleTrack,
                    subtitleDelayMs = existing.subtitleDelayMs
                )
            } else {
                scanned
            }
        }

        videoDao.insertOrUpdateVideos(mergedVideos)
        if (mergedVideos.isNotEmpty()) {
            videoDao.deleteOrphanedVideos(mergedVideos.map { it.id })
        }

        _scanState.value = ScanState.Completed(mergedVideos.size)
    }

    suspend fun updateWatchProgress(id: Long, positionMs: Long, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        videoDao.updateWatchProgress(id, positionMs, now, isCompleted)

        val video = videoDao.getVideoByIdSync(id)
        if (video != null && positionMs > 5000L) {
            watchHistoryDao.insertHistory(
                WatchHistoryEntity(
                    videoId = id,
                    videoTitle = video.title,
                    watchedAt = now,
                    positionMs = positionMs,
                    durationMs = video.durationMs
                )
            )
        }
    }

    suspend fun resetWatchProgress(id: Long) = withContext(Dispatchers.IO) {
        videoDao.resetWatchProgress(id)
    }

    suspend fun updatePlaybackSpeed(id: Long, speed: Float) = withContext(Dispatchers.IO) {
        videoDao.updatePlaybackSpeed(id, speed)
    }

    suspend fun updateSubtitleSettings(id: Long, trackIndex: Int, delayMs: Long) = withContext(Dispatchers.IO) {
        videoDao.updateSubtitleSettings(id, trackIndex, delayMs)
    }

    suspend fun renameVideo(id: Long, newTitle: String) = withContext(Dispatchers.IO) {
        videoDao.renameVideo(id, newTitle)
    }

    suspend fun deleteVideo(video: VideoItemEntity): Boolean = withContext(Dispatchers.IO) {
        var deleted = false
        try {
            val uri = Uri.parse(video.uri)
            val rows = context.contentResolver.delete(uri, null, null)
            if (rows > 0) {
                deleted = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Also delete from local db
        videoDao.deleteVideoById(video.id)
        deleted
    }

    // Playlists
    suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        playlistDao.insertPlaylist(PlaylistEntity(name = name.trim()))
    }

    suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylist(playlistId)
    }

    suspend fun renamePlaylist(playlistId: Long, newName: String) = withContext(Dispatchers.IO) {
        playlistDao.renamePlaylist(playlistId, newName.trim())
    }

    suspend fun addVideoToPlaylist(playlistId: Long, videoId: Long) = withContext(Dispatchers.IO) {
        playlistDao.addVideoToPlaylist(PlaylistItemCrossRef(playlistId = playlistId, videoId = videoId))
    }

    suspend fun removeVideoFromPlaylist(playlistId: Long, videoId: Long) = withContext(Dispatchers.IO) {
        playlistDao.removeVideoFromPlaylist(playlistId, videoId)
    }

    suspend fun clearWatchHistory() = withContext(Dispatchers.IO) {
        watchHistoryDao.clearHistory()
    }

    // Bookmarks
    fun getBookmarksForVideo(videoId: Long): Flow<List<BookmarkEntity>> =
        bookmarkDao.getBookmarksForVideo(videoId)

    suspend fun addBookmark(videoId: Long, positionMs: Long, note: String = ""): Long = withContext(Dispatchers.IO) {
        bookmarkDao.insertBookmark(BookmarkEntity(videoId = videoId, positionMs = positionMs, note = note.trim()))
    }

    suspend fun deleteBookmark(bookmarkId: Long) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmark(bookmarkId)
    }

    // High-Res Frame Capture
    suspend fun captureVideoFrame(video: VideoItemEntity, positionMs: Long): String? = withContext(Dispatchers.IO) {
        try {
            val retriever = MediaMetadataRetriever()
            if (video.path.isNotEmpty() && File(video.path).exists()) {
                retriever.setDataSource(video.path)
            } else {
                retriever.setDataSource(context, Uri.parse(video.uri))
            }

            val timeMicros = positionMs * 1000L
            val frameBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                retriever.getScaledFrameAtTime(timeMicros, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, 1920, 1080)
                    ?: retriever.getFrameAtTime(timeMicros, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            } else {
                retriever.getFrameAtTime(timeMicros, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            }
            retriever.release()

            if (frameBitmap != null) {
                val fileName = "Salim_${System.currentTimeMillis()}.jpg"
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Salim")
                    }
                }

                val imageUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    context.contentResolver.openOutputStream(imageUri)?.use { out ->
                        frameBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                    }
                    return@withContext fileName
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }

    suspend fun getDetailedMetadata(video: VideoItemEntity): VideoDetailedMetadata =
        scanner.getDetailedMetadata(video)


    // Local JSON Backup & Restore
    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        val playlists = playlistDao.getAllPlaylists().first()
        val plArray = JSONArray()

        playlists.forEach { pl ->
            val obj = JSONObject()
            obj.put("id", pl.playlistId)
            obj.put("name", pl.name)
            obj.put("createdAt", pl.createdAt)
            plArray.put(obj)
        }
        root.put("playlists", plArray)
        root.put("exportDate", System.currentTimeMillis())
        root.toString(2)
    }

    suspend fun importBackupJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (root.has("playlists")) {
                val array = root.getJSONArray("playlists")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val name = obj.getString("name")
                    playlistDao.insertPlaylist(PlaylistEntity(name = name))
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
