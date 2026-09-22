package com.example.data.mediastore

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.data.model.VideoItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class VideoDetailedMetadata(
    val title: String,
    val durationFormatted: String,
    val resolution: String,
    val fileSizeFormatted: String,
    val mimeType: String,
    val videoCodec: String,
    val audioCodec: String,
    val frameRate: String,
    val path: String,
    val dateModified: String
)

class MediaStoreVideoScanner(private val context: Context) {

    suspend fun scanDeviceVideos(excludedFolders: Set<String> = emptySet()): List<VideoItemEntity> = withContext(Dispatchers.IO) {
        val videos = mutableListOf<VideoItemEntity>()

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )

            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val titleCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
                val durCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val dateCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val widthCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
                val heightCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
                val dataCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                val mimeCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
                val bucketCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val displayName = it.getString(nameCol) ?: "Video_$id"
                    val rawTitle = it.getString(titleCol)
                    val title = if (!rawTitle.isNullOrBlank()) rawTitle else displayName
                    val duration = it.getLong(durCol)
                    val size = it.getLong(sizeCol)
                    val dateAdded = it.getLong(dateCol)
                    val width = it.getInt(widthCol)
                    val height = it.getInt(heightCol)
                    val path = it.getString(dataCol) ?: ""
                    val mimeType = it.getString(mimeCol) ?: "video/*"
                    val folderName = it.getString(bucketCol) ?: "Unknown"

                    if (excludedFolders.contains(folderName)) {
                        continue
                    }

                    val resolution = if (width > 0 && height > 0) {
                        "${width}x${height}"
                    } else {
                        "Standard"
                    }

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()

                    videos.add(
                        VideoItemEntity(
                            id = id,
                            uri = contentUri,
                            title = title,
                            displayName = displayName,
                            durationMs = duration,
                            sizeBytes = size,
                            dateAdded = dateAdded,
                            resolution = resolution,
                            width = width,
                            height = height,
                            mimeType = mimeType,
                            path = path,
                            folderName = folderName
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        videos
    }

    suspend fun getDetailedMetadata(video: VideoItemEntity): VideoDetailedMetadata = withContext(Dispatchers.IO) {
        var videoCodec = "H.264 / AVC"
        var audioCodec = "AAC"
        var frameRate = "30 fps"
        var dateModified = "Unknown"

        try {
            val retriever = MediaMetadataRetriever()
            try {
                if (video.path.isNotEmpty() && File(video.path).exists()) {
                    retriever.setDataSource(video.path)
                } else {
                    retriever.setDataSource(context, Uri.parse(video.uri))
                }

                val mime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
                val fr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
                if (fr != null) frameRate = "$fr fps"

                val date = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)
                if (date != null) dateModified = date

                val hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)
                val hasAudio = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)

                if (hasVideo == "yes") {
                    videoCodec = mime ?: "Video Stream"
                }
                if (hasAudio == "yes") {
                    audioCodec = "Integrated Audio"
                }
            } finally {
                retriever.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        VideoDetailedMetadata(
            title = video.title,
            durationFormatted = video.formattedDuration(),
            resolution = video.resolution,
            fileSizeFormatted = video.formattedSize(),
            mimeType = video.mimeType,
            videoCodec = videoCodec,
            audioCodec = audioCodec,
            frameRate = frameRate,
            path = video.path.ifEmpty { video.uri },
            dateModified = dateModified
        )
    }
}
