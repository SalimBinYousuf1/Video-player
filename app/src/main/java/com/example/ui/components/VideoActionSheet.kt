package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.mediastore.VideoDetailedMetadata
import com.example.data.model.PlaylistEntity
import com.example.data.model.VideoItemEntity
import com.example.ui.theme.DarkGlassBase
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoActionSheet(
    video: VideoItemEntity,
    playlists: List<PlaylistEntity>,
    onDismiss: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: (Long) -> Unit,
    onCreateAndAddToPlaylist: (String) -> Unit,
    onRename: (String) -> Unit,
    onDeleteFromDevice: () -> Unit,
    onGoToFolder: (String) -> Unit,
    fetchDetails: suspend () -> VideoDetailedMetadata
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var showDetailsDialog by remember { mutableStateOf(false) }
    var detailedMetadata by remember { mutableStateOf<VideoDetailedMetadata?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(video) {
        detailedMetadata = fetchDetails()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF16181F),
        scrimColor = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header: Video Title & Meta
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = video.title,
                        color = TextPrimaryDark,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${video.formattedDuration()} • ${video.formattedSize()} • ${video.resolution}",
                        color = TextSecondaryDark,
                        fontSize = 13.sp
                    )
                }
            }

            HorizontalDivider(color = Color(0x1FFFFFFF), modifier = Modifier.padding(vertical = 8.dp))

            // Action Items
            ActionSheetItem(
                icon = Icons.Default.PlayArrow,
                label = "Play Next",
                onClick = {
                    scope.launch { sheetState.hide() }
                    onPlayNext()
                    onDismiss()
                }
            )

            ActionSheetItem(
                icon = Icons.AutoMirrored.Filled.QueueMusic,
                label = "Add to Queue",
                onClick = {
                    scope.launch { sheetState.hide() }
                    onAddToQueue()
                    onDismiss()
                }
            )

            ActionSheetItem(
                icon = Icons.Default.PlaylistAdd,
                label = "Add to Playlist...",
                onClick = { showPlaylistDialog = true }
            )

            ActionSheetItem(
                icon = Icons.Default.Edit,
                label = "Rename Video",
                onClick = { showRenameDialog = true }
            )

            ActionSheetItem(
                icon = Icons.Default.Share,
                label = "Share File",
                onClick = {
                    shareVideo(context, video)
                    onDismiss()
                }
            )

            ActionSheetItem(
                icon = Icons.Default.Info,
                label = "View Details",
                onClick = { showDetailsDialog = true }
            )

            ActionSheetItem(
                icon = Icons.Default.Folder,
                label = "Go to Folder (${video.folderName})",
                onClick = {
                    onGoToFolder(video.folderName)
                    onDismiss()
                }
            )

            ActionSheetItem(
                icon = Icons.Default.Delete,
                label = "Delete from Device",
                tint = Color(0xFFFF453A),
                onClick = { showDeleteConfirmDialog = true }
            )
        }
    }

    // Rename Dialog
    if (showRenameDialog) {
        var newTitle by remember { mutableStateOf(video.title) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            containerColor = Color(0xFF1E2029),
            title = { Text("Rename Video", color = Color.White, fontWeight = FontWeight.SemiBold) },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0x44FFFFFF)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTitle.isNotBlank()) {
                        onRename(newTitle.trim())
                    }
                    showRenameDialog = false
                    onDismiss()
                }) {
                    Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            }
        )
    }

    // Details Dialog
    if (showDetailsDialog) {
        val meta = detailedMetadata
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },
            containerColor = Color(0xFF1E2029),
            title = { Text("Video Details", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    DetailRow(label = "Title", value = video.title)
                    DetailRow(label = "Duration", value = video.formattedDuration())
                    DetailRow(label = "Resolution", value = meta?.resolution ?: video.resolution)
                    DetailRow(label = "File Size", value = video.formattedSize())
                    DetailRow(label = "Video Codec", value = meta?.videoCodec ?: "Standard Codec")
                    DetailRow(label = "Audio Codec", value = meta?.audioCodec ?: "Standard Audio")
                    DetailRow(label = "Frame Rate", value = meta?.frameRate ?: "30 fps")
                    DetailRow(label = "Folder", value = video.folderName)
                    DetailRow(label = "Storage Path", value = meta?.path ?: video.path)
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetailsDialog = false }) {
                    Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Add to Playlist Dialog
    if (showPlaylistDialog) {
        var newPlaylistName by remember { mutableStateOf("") }
        var isCreatingNew by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showPlaylistDialog = false },
            containerColor = Color(0xFF1E2029),
            title = { Text("Add to Playlist", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    if (isCreatingNew) {
                        OutlinedTextField(
                            value = newPlaylistName,
                            onValueChange = { newPlaylistName = it },
                            placeholder = { Text("Playlist name", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color(0x44FFFFFF)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        if (playlists.isEmpty()) {
                            Text("No playlists yet. Create a new one below.", color = TextSecondaryDark)
                        } else {
                            playlists.forEach { pl ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onAddToPlaylist(pl.playlistId)
                                            showPlaylistDialog = false
                                            onDismiss()
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlaylistAdd,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = pl.name, color = Color.White, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (isCreatingNew) {
                    TextButton(onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            onCreateAndAddToPlaylist(newPlaylistName.trim())
                            showPlaylistDialog = false
                            onDismiss()
                        }
                    }) {
                        Text("Create & Add", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    TextButton(onClick = { isCreatingNew = true }) {
                        Text("New Playlist", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showPlaylistDialog = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = Color(0xFF1E2029),
            title = { Text("Delete Video?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to delete \"${video.title}\" from your device? This cannot be undone.",
                    color = TextSecondaryDark
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmDialog = false
                    onDeleteFromDevice()
                    onDismiss()
                }) {
                    Text("Delete", color = Color(0xFFFF453A), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            }
        )
    }
}

@Composable
private fun ActionSheetItem(
    icon: ImageVector,
    label: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            color = tint,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(text = label, color = TextSecondaryDark, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text(text = value, color = Color.White, fontSize = 14.sp)
    }
}

private fun shareVideo(context: Context, video: VideoItemEntity) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = video.mimeType
            putExtra(Intent.EXTRA_STREAM, Uri.parse(video.uri))
            putExtra(Intent.EXTRA_TITLE, video.title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
