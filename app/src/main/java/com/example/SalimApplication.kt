package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.db.SalimDatabase
import com.example.data.mediastore.MediaStoreVideoScanner
import com.example.data.repository.UserSettingsRepository
import com.example.data.repository.VideoRepository
import com.example.playback.SalimPlaybackManager

class SalimApplication : Application() {

    lateinit var database: SalimDatabase
        private set

    lateinit var settingsRepository: UserSettingsRepository
        private set

    lateinit var mediaScanner: MediaStoreVideoScanner
        private set

    lateinit var videoRepository: VideoRepository
        private set

    lateinit var playbackManager: SalimPlaybackManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = Room.databaseBuilder(
            applicationContext,
            SalimDatabase::class.java,
            "salim_video_player.db"
        ).fallbackToDestructiveMigration().build()

        settingsRepository = UserSettingsRepository(this)
        mediaScanner = MediaStoreVideoScanner(this)

        videoRepository = VideoRepository(
            context = this,
            videoDao = database.videoDao(),
            playlistDao = database.playlistDao(),
            watchHistoryDao = database.watchHistoryDao(),
            bookmarkDao = database.bookmarkDao(),
            scanner = mediaScanner,
            settingsRepository = settingsRepository
        )

        playbackManager = SalimPlaybackManager(
            context = this,
            videoRepository = videoRepository
        )
    }

    override fun onTerminate() {
        super.onTerminate()
        playbackManager.release()
    }

    companion object {
        lateinit var instance: SalimApplication
            private set
    }
}
