package com.example.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.example.data.model.PlaylistEntity
import com.example.data.model.PlaylistItemCrossRef
import com.example.data.model.VideoItemEntity
import com.example.data.model.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY dateAdded DESC")
    fun getAllVideos(): Flow<List<VideoItemEntity>>

    @Query("SELECT * FROM videos WHERE lastWatchedPositionMs > 3000 AND isCompleted = 0 ORDER BY lastWatchedTimestamp DESC LIMIT 20")
    fun getContinueWatching(): Flow<List<VideoItemEntity>>

    @Query("SELECT * FROM videos WHERE id = :id")
    fun getVideoById(id: Long): Flow<VideoItemEntity?>

    @Query("SELECT * FROM videos WHERE id = :id")
    suspend fun getVideoByIdSync(id: Long): VideoItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateVideos(videos: List<VideoItemEntity>)

    @Query("UPDATE videos SET lastWatchedPositionMs = :positionMs, lastWatchedTimestamp = :timestamp, isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateWatchProgress(id: Long, positionMs: Long, timestamp: Long, isCompleted: Boolean)

    @Query("UPDATE videos SET playbackSpeed = :speed WHERE id = :id")
    suspend fun updatePlaybackSpeed(id: Long, speed: Float)

    @Query("UPDATE videos SET selectedSubtitleTrack = :trackIndex, subtitleDelayMs = :delayMs WHERE id = :id")
    suspend fun updateSubtitleSettings(id: Long, trackIndex: Int, delayMs: Long)

    @Query("UPDATE videos SET lastWatchedPositionMs = 0, isCompleted = 0 WHERE id = :id")
    suspend fun resetWatchProgress(id: Long)

    @Query("UPDATE videos SET title = :newTitle WHERE id = :id")
    suspend fun renameVideo(id: Long, newTitle: String)

    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun deleteVideoById(id: Long)

    @Query("DELETE FROM videos WHERE id NOT IN (:validIds)")
    suspend fun deleteOrphanedVideos(validIds: List<Long>)
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE playlistId = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("UPDATE playlists SET name = :newName WHERE playlistId = :playlistId")
    suspend fun renamePlaylist(playlistId: Long, newName: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addVideoToPlaylist(crossRef: PlaylistItemCrossRef)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId AND videoId = :videoId")
    suspend fun removeVideoFromPlaylist(playlistId: Long, videoId: Long)

    @Query("""
        SELECT v.* FROM videos v
        INNER JOIN playlist_items pi ON v.id = pi.videoId
        WHERE pi.playlistId = :playlistId
        ORDER BY pi.orderIndex ASC
    """)
    fun getVideosForPlaylist(playlistId: Long): Flow<List<VideoItemEntity>>

    @Query("SELECT COUNT(*) FROM playlist_items WHERE playlistId = :playlistId")
    fun getVideoCountForPlaylist(playlistId: Long): Flow<Int>
}

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC LIMIT 100")
    fun getWatchHistory(): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: WatchHistoryEntity)

    @Query("DELETE FROM watch_history")
    suspend fun clearHistory()
}

@Database(
    entities = [
        VideoItemEntity::class,
        PlaylistEntity::class,
        PlaylistItemCrossRef::class,
        WatchHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SalimDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun watchHistoryDao(): WatchHistoryDao
}
