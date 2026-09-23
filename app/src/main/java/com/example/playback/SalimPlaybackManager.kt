package com.example.playback

import android.content.Context
import android.media.AudioManager
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.example.data.model.BookmarkEntity
import com.example.data.model.VideoItemEntity
import com.example.data.repository.VideoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

data class TrackInfo(
    val index: Int,
    val name: String,
    val language: String?,
    val isSelected: Boolean
)

enum class AspectRatioMode(val label: String) {
    FIT("Fit"),
    FILL("Fill"),
    STRETCH("Stretch"),
    ZOOM_100("100%"),
    ZOOM_200("200%")
}

enum class RepeatMode(val label: String) {
    OFF("Off"),
    ONE("Repeat 1"),
    ALL("Repeat All")
}

data class PlaybackUiState(
    val currentVideo: VideoItemEntity? = null,
    val playlist: List<VideoItemEntity> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val isFastForwardingPreview: Boolean = false,
    val aspectRatioMode: AspectRatioMode = AspectRatioMode.FIT,
    val subtitleTracks: List<TrackInfo> = emptyList(),
    val selectedSubtitleTrackIndex: Int = -1,
    val audioTracks: List<TrackInfo> = emptyList(),
    val selectedAudioTrackIndex: Int = -1,
    val subtitleDelayMs: Long = 0L,
    val isControlsVisible: Boolean = true,
    val sleepTimerSecondsRemaining: Int? = null,
    val zoomScale: Float = 1.0f,
    val panOffsetX: Float = 0f,
    val panOffsetY: Float = 0f,
    val volumeLevel: Float = 1.0f,
    val audioBoostMultiplier: Float = 1.0f, // up to 2.0x (200% volume)
    val brightnessLevel: Float = 0.5f,
    val showVolumeIndicator: Boolean = false,
    val showBrightnessIndicator: Boolean = false,
    val isBuffering: Boolean = false,
    val isLocked: Boolean = false,
    val isAudioOnlyMode: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val isShuffle: Boolean = false,
    val loopPointA: Long? = null,
    val loopPointB: Long? = null,
    val isAbLoopActive: Boolean = false,
    val orientationLock: String = "sensor", // "sensor", "landscape", "portrait"
    val doubleTapSeekSeconds: Int = 20,
    val bookmarks: List<BookmarkEntity> = emptyList()
)

@OptIn(UnstableApi::class)
class SalimPlaybackManager(
    private val context: Context,
    val videoRepository: VideoRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val trackSelector = DefaultTrackSelector(context)
    val player: ExoPlayer = ExoPlayer.Builder(context)
        .setTrackSelector(trackSelector)
        .build()

    private val _uiState = MutableStateFlow(PlaybackUiState())
    val uiState: StateFlow<PlaybackUiState> = _uiState.asStateFlow()

    private var positionTrackerJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var autoHideControlsJob: Job? = null
    private var indicatorHideJob: Job? = null
    private var speedBeforeHold: Float = 1.0f
    private var loudnessEnhancer: LoudnessEnhancer? = null

    init {
        // Initialize current volume
        val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        _uiState.value = _uiState.value.copy(
            volumeLevel = if (maxVol > 0) currentVol.toFloat() / maxVol else 0.5f
        )

        player.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                recreateLoudnessEnhancer(audioSessionId)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
                if (isPlaying) {
                    startPositionTracker()
                    scheduleAutoHideControls()
                } else {
                    stopPositionTracker()
                    saveCurrentProgress()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        _uiState.value = _uiState.value.copy(isBuffering = true)
                    }
                    Player.STATE_READY -> {
                        val duration = player.duration.coerceAtLeast(0L)
                        _uiState.value = _uiState.value.copy(
                            isBuffering = false,
                            durationMs = duration,
                            currentPositionMs = player.currentPosition
                        )
                        updateTracks()
                    }
                    Player.STATE_ENDED -> {
                        _uiState.value = _uiState.value.copy(isBuffering = false, isPlaying = false)
                        markCurrentVideoCompleted()
                        handlePlaybackEnded()
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(isBuffering = false)
                    }
                }
            }

            override fun onTracksChanged(tracks: Tracks) {
                updateTracks()
            }
        })
    }

    fun playVideo(video: VideoItemEntity, playlist: List<VideoItemEntity> = listOf(video)) {
        val index = playlist.indexOfFirst { it.id == video.id }.coerceAtLeast(0)
        _uiState.value = _uiState.value.copy(
            currentVideo = video,
            playlist = playlist,
            currentIndex = index,
            playbackSpeed = video.playbackSpeed,
            zoomScale = 1.0f,
            panOffsetX = 0f,
            panOffsetY = 0f,
            loopPointA = null,
            loopPointB = null,
            isAbLoopActive = false,
            isControlsVisible = true
        )

        val uri = Uri.parse(video.uri)
        val mediaItemBuilder = MediaItem.Builder().setUri(uri)

        // Check for local sidecar subtitles (.srt, .vtt) in the same directory
        val sidecarSubtitles = findSidecarSubtitles(video.path)
        if (sidecarSubtitles.isNotEmpty()) {
            mediaItemBuilder.setSubtitleConfigurations(sidecarSubtitles)
        }

        val mediaItem = mediaItemBuilder.build()
        player.setMediaItem(mediaItem)
        player.setPlaybackParameters(PlaybackParameters(video.playbackSpeed))
        player.prepare()

        // Auto-resume from last watched position
        if (video.isResumeAvailable) {
            player.seekTo(video.lastWatchedPositionMs)
        } else {
            player.seekTo(0)
        }

        player.play()
        scheduleAutoHideControls()
        loadBookmarks(video.id)
    }

    private fun loadBookmarks(videoId: Long) {
        scope.launch {
            videoRepository.getBookmarksForVideo(videoId).collect { marks ->
                _uiState.value = _uiState.value.copy(bookmarks = marks)
            }
        }
    }

    private fun findSidecarSubtitles(videoPath: String): List<MediaItem.SubtitleConfiguration> {
        if (videoPath.isBlank()) return emptyList()
        val list = mutableListOf<MediaItem.SubtitleConfiguration>()
        try {
            val videoFile = File(videoPath)
            val parent = videoFile.parentFile
            if (parent != null && parent.exists() && parent.isDirectory) {
                val baseName = videoFile.nameWithoutExtension
                val subFiles = parent.listFiles { file ->
                    val name = file.name
                    (name.endsWith(".srt", ignoreCase = true) || name.endsWith(".vtt", ignoreCase = true)) &&
                            name.startsWith(baseName, ignoreCase = true)
                }
                subFiles?.forEach { subFile ->
                    val mime = if (subFile.name.endsWith(".vtt", ignoreCase = true)) {
                        MimeTypes.TEXT_VTT
                    } else {
                        MimeTypes.APPLICATION_SUBRIP
                    }
                    val config = MediaItem.SubtitleConfiguration.Builder(Uri.fromFile(subFile))
                        .setMimeType(mime)
                        .setLanguage("en")
                        .setLabel(subFile.name)
                        .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                        .build()
                    list.add(config)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun addExternalSubtitle(uri: Uri, label: String = "External Subtitle") {
        val current = _uiState.value.currentVideo ?: return
        val currentPos = player.currentPosition
        val isPlaying = player.isPlaying

        val mime = if (uri.toString().endsWith(".vtt", ignoreCase = true)) {
            MimeTypes.TEXT_VTT
        } else {
            MimeTypes.APPLICATION_SUBRIP
        }

        val subConfig = MediaItem.SubtitleConfiguration.Builder(uri)
            .setMimeType(mime)
            .setLanguage("und")
            .setLabel(label)
            .setSelectionFlags(C.SELECTION_FLAG_FORCED)
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(current.uri))
            .setSubtitleConfigurations(listOf(subConfig))
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.seekTo(currentPos)
        if (isPlaying) player.play()
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
            showControls()
        } else {
            player.play()
            scheduleAutoHideControls()
        }
    }

    fun setDoubleTapSeekSeconds(seconds: Int) {
        _uiState.value = _uiState.value.copy(doubleTapSeekSeconds = seconds)
    }

    fun seekBy(seconds: Int) {
        val current = player.currentPosition
        val duration = player.duration.coerceAtLeast(0L)
        val target = (current + (seconds * 1000L)).coerceIn(0L, duration)
        player.seekTo(target)
        _uiState.value = _uiState.value.copy(currentPositionMs = target)
        showControls()
        scheduleAutoHideControls()
    }

    fun seekToPosition(positionMs: Long) {
        player.seekTo(positionMs)
        _uiState.value = _uiState.value.copy(currentPositionMs = positionMs)
    }

    private fun handlePlaybackEnded() {
        when (_uiState.value.repeatMode) {
            RepeatMode.ONE -> {
                player.seekTo(0)
                player.play()
            }
            RepeatMode.ALL -> {
                val nextIndex = (_uiState.value.currentIndex + 1) % _uiState.value.playlist.size
                playVideo(_uiState.value.playlist[nextIndex], _uiState.value.playlist)
            }
            RepeatMode.OFF -> {
                playNextIfAvailable()
            }
        }
    }

    fun playNextIfAvailable() {
        val playlist = _uiState.value.playlist
        if (playlist.isEmpty()) return
        val nextIndex = if (_uiState.value.isShuffle) {
            playlist.indices.random()
        } else {
            _uiState.value.currentIndex + 1
        }
        if (nextIndex in playlist.indices) {
            playVideo(playlist[nextIndex], playlist)
        }
    }

    fun playPreviousIfAvailable() {
        val playlist = _uiState.value.playlist
        if (playlist.isEmpty()) return
        val prevIndex = _uiState.value.currentIndex - 1
        if (prevIndex in playlist.indices) {
            playVideo(playlist[prevIndex], playlist)
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        player.setPlaybackParameters(PlaybackParameters(speed))
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
        val current = _uiState.value.currentVideo ?: return
        scope.launch {
            videoRepository.updatePlaybackSpeed(current.id, speed)
        }
    }

    // YouTube 2.0x hold preview
    fun startTemporaryFastForward() {
        if (!_uiState.value.isFastForwardingPreview) {
            speedBeforeHold = _uiState.value.playbackSpeed
            player.setPlaybackParameters(PlaybackParameters(2.0f))
            _uiState.value = _uiState.value.copy(
                isFastForwardingPreview = true,
                playbackSpeed = 2.0f
            )
        }
    }

    fun stopTemporaryFastForward() {
        if (_uiState.value.isFastForwardingPreview) {
            player.setPlaybackParameters(PlaybackParameters(speedBeforeHold))
            _uiState.value = _uiState.value.copy(
                isFastForwardingPreview = false,
                playbackSpeed = speedBeforeHold
            )
        }
    }

    // A-B Loop functionality (VLC powerhouse feature)
    fun setLoopPointA() {
        val currentPos = player.currentPosition
        _uiState.value = _uiState.value.copy(loopPointA = currentPos, isAbLoopActive = false)
    }

    fun setLoopPointB() {
        val currentPos = player.currentPosition
        val pointA = _uiState.value.loopPointA ?: 0L
        if (currentPos > pointA) {
            _uiState.value = _uiState.value.copy(loopPointB = currentPos, isAbLoopActive = true)
        }
    }

    fun clearAbLoop() {
        _uiState.value = _uiState.value.copy(loopPointA = null, loopPointB = null, isAbLoopActive = false)
    }

    // Aspect ratio & Zoom modes
    fun setAspectRatioMode(mode: AspectRatioMode) {
        _uiState.value = _uiState.value.copy(
            aspectRatioMode = mode,
            zoomScale = when (mode) {
                AspectRatioMode.ZOOM_100 -> 1.0f
                AspectRatioMode.ZOOM_200 -> 2.0f
                else -> 1.0f
            }
        )
    }

    fun cycleAspectRatioMode() {
        val next = when (_uiState.value.aspectRatioMode) {
            AspectRatioMode.FIT -> AspectRatioMode.FILL
            AspectRatioMode.FILL -> AspectRatioMode.STRETCH
            AspectRatioMode.STRETCH -> AspectRatioMode.ZOOM_100
            AspectRatioMode.ZOOM_100 -> AspectRatioMode.ZOOM_200
            AspectRatioMode.ZOOM_200 -> AspectRatioMode.FIT
        }
        setAspectRatioMode(next)
    }

    fun setZoomAndPan(scale: Float, offsetX: Float, offsetY: Float) {
        _uiState.value = _uiState.value.copy(
            zoomScale = scale.coerceIn(1.0f, 5.0f),
            panOffsetX = offsetX,
            panOffsetY = offsetY
        )
    }

    fun resetZoomAndPan() {
        _uiState.value = _uiState.value.copy(
            zoomScale = 1.0f,
            panOffsetX = 0f,
            panOffsetY = 0f,
            aspectRatioMode = AspectRatioMode.FIT
        )
    }

    // Audio Boost (Volume up to 200%)
    fun setAudioBoostMultiplier(multiplier: Float) {
        val clamped = multiplier.coerceIn(1.0f, 2.0f)
        _uiState.value = _uiState.value.copy(audioBoostMultiplier = clamped)
        player.volume = clamped
    }

    // Audio-Only mode toggle
    fun toggleAudioOnlyMode() {
        _uiState.value = _uiState.value.copy(isAudioOnlyMode = !_uiState.value.isAudioOnlyMode)
    }

    // Repeat & Shuffle
    fun toggleRepeatMode() {
        val next = when (_uiState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _uiState.value = _uiState.value.copy(repeatMode = next)
    }

    fun toggleShuffle() {
        _uiState.value = _uiState.value.copy(isShuffle = !_uiState.value.isShuffle)
    }

    // Orientation Lock
    fun setOrientationLock(lock: String) {
        _uiState.value = _uiState.value.copy(orientationLock = lock)
    }

    fun toggleOrientationLock() {
        val next = when (_uiState.value.orientationLock) {
            "sensor" -> "landscape"
            "landscape" -> "portrait"
            else -> "sensor"
        }
        setOrientationLock(next)
    }

    // Bookmarks
    fun addBookmarkAtCurrent(note: String = "") {
        val video = _uiState.value.currentVideo ?: return
        val pos = player.currentPosition
        scope.launch {
            videoRepository.addBookmark(video.id, pos, note)
        }
    }

    fun deleteBookmark(bookmarkId: Long) {
        scope.launch {
            videoRepository.deleteBookmark(bookmarkId)
        }
    }

    // Capture Frame
    suspend fun captureCurrentFrame(): String? {
        val video = _uiState.value.currentVideo ?: return null
        val pos = player.currentPosition
        return videoRepository.captureVideoFrame(video, pos)
    }

    fun toggleControls() {
        if (_uiState.value.isControlsVisible) {
            _uiState.value = _uiState.value.copy(isControlsVisible = false)
            autoHideControlsJob?.cancel()
        } else {
            showControls()
        }
    }

    fun showControls() {
        _uiState.value = _uiState.value.copy(isControlsVisible = true)
        scheduleAutoHideControls()
    }

    fun setScreenLock(locked: Boolean) {
        _uiState.value = _uiState.value.copy(isLocked = locked)
    }

    private fun scheduleAutoHideControls() {
        autoHideControlsJob?.cancel()
        if (_uiState.value.isPlaying && !_uiState.value.isLocked) {
            autoHideControlsJob = scope.launch {
                delay(3800)
                _uiState.value = _uiState.value.copy(isControlsVisible = false)
            }
        }
    }

    // Audio Boost DSP (LoudnessEnhancer) & Volume Management
    private fun recreateLoudnessEnhancer(sessionId: Int = player.audioSessionId) {
        try {
            loudnessEnhancer?.release()
            loudnessEnhancer = null
            if (sessionId != C.AUDIO_SESSION_ID_UNSET && sessionId > 0) {
                loudnessEnhancer = LoudnessEnhancer(sessionId)
                applyAudioBoostGain()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun applyAudioBoostGain() {
        try {
            val enhancer = loudnessEnhancer ?: return
            val level = _uiState.value.volumeLevel
            if (level > 1.0f) {
                val boostFraction = (level - 1.0f).coerceIn(0f, 1.0f)
                val targetGainMb = (boostFraction * 2000).toInt() // up to +20dB (2000 mB)
                enhancer.setTargetGain(targetGainMb)
                enhancer.enabled = true
            } else {
                enhancer.setTargetGain(0)
                enhancer.enabled = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Volume Adjustment up to 200% (2.0f) with Smooth Audio Boost
    fun adjustVolume(deltaFraction: Float) {
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentLevel = _uiState.value.volumeLevel
        val newLevel = (currentLevel + deltaFraction).coerceIn(0f, 2.0f)

        if (newLevel <= 1.0f) {
            val newTargetVol = (newLevel * maxVol).toInt()
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newTargetVol, 0)
        } else {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, 0)
        }

        _uiState.value = _uiState.value.copy(
            volumeLevel = newLevel,
            audioBoostMultiplier = if (newLevel > 1.0f) newLevel else 1.0f,
            showVolumeIndicator = true,
            showBrightnessIndicator = false
        )
        applyAudioBoostGain()
        scheduleHideIndicators()
    }

    // Picture-in-Picture mode management: clear overlays immediately
    fun setInPipMode(inPip: Boolean) {
        if (inPip) {
            _uiState.value = _uiState.value.copy(
                isControlsVisible = false,
                showVolumeIndicator = false,
                showBrightnessIndicator = false
            )
            autoHideControlsJob?.cancel()
            indicatorHideJob?.cancel()
        }
    }

    // Brightness Adjustment
    fun adjustBrightness(deltaFraction: Float) {
        val newBrightness = (_uiState.value.brightnessLevel + deltaFraction).coerceIn(0.01f, 1.0f)
        _uiState.value = _uiState.value.copy(
            brightnessLevel = newBrightness,
            showBrightnessIndicator = true,
            showVolumeIndicator = false
        )
        scheduleHideIndicators()
    }

    private fun scheduleHideIndicators() {
        indicatorHideJob?.cancel()
        indicatorHideJob = scope.launch {
            delay(1200)
            _uiState.value = _uiState.value.copy(
                showVolumeIndicator = false,
                showBrightnessIndicator = false
            )
        }
    }

    // Sleep Timer
    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _uiState.value = _uiState.value.copy(sleepTimerSecondsRemaining = null)
            return
        }

        val totalSeconds = minutes * 60
        _uiState.value = _uiState.value.copy(sleepTimerSecondsRemaining = totalSeconds)

        sleepTimerJob = scope.launch {
            var remaining = totalSeconds
            while (remaining > 0 && isActive) {
                delay(1000)
                remaining--
                _uiState.value = _uiState.value.copy(sleepTimerSecondsRemaining = remaining)
            }
            if (isActive) {
                player.pause()
                _uiState.value = _uiState.value.copy(sleepTimerSecondsRemaining = null)
            }
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _uiState.value = _uiState.value.copy(sleepTimerSecondsRemaining = null)
    }

    // Tracks Selection (Subtitles & Audio)
    private fun updateTracks() {
        val currentTracks = player.currentTracks
        val subs = mutableListOf<TrackInfo>()
        val audios = mutableListOf<TrackInfo>()

        for (group in currentTracks.groups) {
            val trackType = group.type
            for (i in 0 until group.length) {
                val format = group.getTrackFormat(i)
                val isSelected = group.isTrackSelected(i)
                val name = format.label ?: format.language ?: "Track ${i + 1}"

                if (trackType == C.TRACK_TYPE_TEXT) {
                    subs.add(
                        TrackInfo(
                            index = subs.size,
                            name = name,
                            language = format.language,
                            isSelected = isSelected
                        )
                    )
                } else if (trackType == C.TRACK_TYPE_AUDIO) {
                    audios.add(
                        TrackInfo(
                            index = audios.size,
                            name = name,
                            language = format.language,
                            isSelected = isSelected
                        )
                    )
                }
            }
        }

        val selectedSub = subs.indexOfFirst { it.isSelected }
        val selectedAudio = audios.indexOfFirst { it.isSelected }

        _uiState.value = _uiState.value.copy(
            subtitleTracks = subs,
            selectedSubtitleTrackIndex = selectedSub,
            audioTracks = audios,
            selectedAudioTrackIndex = selectedAudio
        )
    }

    fun selectSubtitleTrack(trackIndex: Int) {
        val parameters = trackSelector.buildUponParameters()
        if (trackIndex < 0) {
            parameters.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
        } else {
            parameters.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
            val currentTracks = player.currentTracks
            var counter = 0
            for (group in currentTracks.groups) {
                if (group.type == C.TRACK_TYPE_TEXT) {
                    for (i in 0 until group.length) {
                        if (counter == trackIndex) {
                            parameters.setOverrideForType(
                                TrackSelectionOverride(group.mediaTrackGroup, i)
                            )
                        }
                        counter++
                    }
                }
            }
        }
        trackSelector.setParameters(parameters)
        _uiState.value = _uiState.value.copy(selectedSubtitleTrackIndex = trackIndex)
    }

    fun selectAudioTrack(trackIndex: Int) {
        val parameters = trackSelector.buildUponParameters()
        val currentTracks = player.currentTracks
        var counter = 0
        for (group in currentTracks.groups) {
            if (group.type == C.TRACK_TYPE_AUDIO) {
                for (i in 0 until group.length) {
                    if (counter == trackIndex) {
                        parameters.setOverrideForType(
                            TrackSelectionOverride(group.mediaTrackGroup, i)
                        )
                    }
                    counter++
                }
            }
        }
        trackSelector.setParameters(parameters)
        _uiState.value = _uiState.value.copy(selectedAudioTrackIndex = trackIndex)
    }

    fun setSubtitleDelayMs(delayMs: Long) {
        _uiState.value = _uiState.value.copy(subtitleDelayMs = delayMs)
    }

    private fun startPositionTracker() {
        positionTrackerJob?.cancel()
        positionTrackerJob = scope.launch {
            while (isActive) {
                val currentPos = player.currentPosition
                _uiState.value = _uiState.value.copy(
                    currentPositionMs = currentPos,
                    bufferedPositionMs = player.bufferedPosition
                )

                // A-B loop boundary enforcement
                val state = _uiState.value
                if (state.isAbLoopActive && state.loopPointA != null && state.loopPointB != null) {
                    if (currentPos >= state.loopPointB) {
                        player.seekTo(state.loopPointA)
                    }
                }

                // Periodically save watch progress
                saveCurrentProgress()
                delay(1000)
            }
        }
    }

    private fun stopPositionTracker() {
        positionTrackerJob?.cancel()
    }

    private fun saveCurrentProgress() {
        val video = _uiState.value.currentVideo ?: return
        val pos = player.currentPosition
        val duration = player.duration.coerceAtLeast(0L)
        if (duration > 0) {
            val isCompleted = pos >= duration - 3000L
            scope.launch {
                videoRepository.updateWatchProgress(video.id, pos, isCompleted)
            }
        }
    }

    private fun markCurrentVideoCompleted() {
        val video = _uiState.value.currentVideo ?: return
        scope.launch {
            videoRepository.updateWatchProgress(video.id, player.duration, isCompleted = true)
        }
    }

    fun release() {
        stopPositionTracker()
        sleepTimerJob?.cancel()
        autoHideControlsJob?.cancel()
        indicatorHideJob?.cancel()
        saveCurrentProgress()
        try {
            loudnessEnhancer?.release()
            loudnessEnhancer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        player.release()
    }
}
