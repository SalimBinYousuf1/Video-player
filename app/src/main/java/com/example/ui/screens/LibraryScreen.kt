package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlaylistEntity
import com.example.data.model.VideoItemEntity
import com.example.data.repository.UserSettings
import com.example.data.repository.UserSettingsRepository
import com.example.data.repository.VideoRepository
import com.example.ui.components.GlassSurface
import com.example.ui.components.LiquidSmokeBackground
import com.example.ui.components.VideoActionSheet
import com.example.ui.components.VideoGridItem
import com.example.ui.components.VideoListItem
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.LightSurfaceElevated
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TextSecondaryLight
import kotlinx.coroutines.launch

enum class LibraryTab(val title: String) {
    ALL("All Videos"),
    CONTINUE("Continue"),
    FOLDERS("Folders"),
    PLAYLISTS("Playlists")
}

@Composable
fun LibraryScreen(
    videoRepository: VideoRepository,
    settingsRepository: UserSettingsRepository,
    onVideoSelected: (VideoItemEntity, List<VideoItemEntity>) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val allVideos by videoRepository.allVideos.collectAsState(initial = emptyList())
    val continueVideos by videoRepository.continueWatchingVideos.collectAsState(initial = emptyList())
    val playlists by videoRepository.allPlaylists.collectAsState(initial = emptyList())
    val settings by settingsRepository.userSettingsFlow.collectAsState(initial = UserSettings())

    val isLightMode = settings.themeMode == "light"
    val primaryTextColor = if (isLightMode) TextPrimaryLight else TextPrimaryDark
    val secondaryTextColor = if (isLightMode) TextSecondaryLight else TextSecondaryDark

    var selectedTab by remember { mutableStateOf(LibraryTab.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var activeFolderFilter by remember { mutableStateOf<String?>(null) }
    var activePlaylistFilter by remember { mutableStateOf<PlaylistEntity?>(null) }

    var actionSheetVideo by remember { mutableStateOf<VideoItemEntity?>(null) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Filter & Sort logic
    val filteredVideos by remember(allVideos, continueVideos, selectedTab, searchQuery, activeFolderFilter, activePlaylistFilter, settings.sortOption) {
        derivedStateOf {
            val baseList = when (selectedTab) {
                LibraryTab.ALL -> allVideos
                LibraryTab.CONTINUE -> continueVideos
                LibraryTab.FOLDERS -> {
                    if (activeFolderFilter != null) {
                        allVideos.filter { it.folderName == activeFolderFilter }
                    } else {
                        allVideos
                    }
                }
                LibraryTab.PLAYLISTS -> allVideos
            }

            var result = if (searchQuery.isNotBlank()) {
                baseList.filter {
                    it.title.contains(searchQuery, ignoreCase = true) ||
                            it.folderName.contains(searchQuery, ignoreCase = true)
                }
            } else {
                baseList
            }

            result = when (settings.sortOption) {
                "name" -> result.sortedBy { it.title.lowercase() }
                "duration" -> result.sortedByDescending { it.durationMs }
                "size" -> result.sortedByDescending { it.sizeBytes }
                else -> result.sortedByDescending { it.dateAdded }
            }

            result
        }
    }

    // Folders aggregation
    val foldersMap by remember(allVideos) {
        derivedStateOf {
            allVideos.groupBy { it.folderName }
        }
    }

    LiquidSmokeBackground(
        modifier = modifier.fillMaxSize(),
        isLightMode = isLightMode,
        accentTheme = settings.smokeGradientTheme,
        alphaMultiplier = settings.smokeIntensity,
        speedMultiplier = settings.smokeSpeed
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Salim",
                        color = primaryTextColor,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.35.sp
                    )
                    Text(
                        text = "${allVideos.size} on-device videos",
                        color = secondaryTextColor,
                        fontSize = 13.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Search toggle
                    IconButton(onClick = { isSearchActive = !isSearchActive }) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search",
                            tint = primaryTextColor
                        )
                    }

                    // Sort menu button
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(imageVector = Icons.Default.Sort, contentDescription = "Sort", tint = primaryTextColor)
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(if (isLightMode) LightSurfaceElevated else DarkSurfaceElevated)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Date Added", color = primaryTextColor) },
                                onClick = {
                                    scope.launch { settingsRepository.setSortOption("date") }
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Video Title", color = primaryTextColor) },
                                onClick = {
                                    scope.launch { settingsRepository.setSortOption("name") }
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Duration", color = primaryTextColor) },
                                onClick = {
                                    scope.launch { settingsRepository.setSortOption("duration") }
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("File Size", color = primaryTextColor) },
                                onClick = {
                                    scope.launch { settingsRepository.setSortOption("size") }
                                    showSortMenu = false
                                }
                            )
                        }
                    }

                    // View Mode Toggle (Grid / List)
                    IconButton(onClick = {
                        val nextMode = if (settings.viewMode == "grid") "list" else "grid"
                        scope.launch { settingsRepository.setViewMode(nextMode) }
                    }) {
                        Icon(
                            imageVector = if (settings.viewMode == "grid") Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = "Toggle View Mode",
                            tint = primaryTextColor
                        )
                    }

                    // Settings Button
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = primaryTextColor
                        )
                    }
                }
            }

            // Search Bar (if active)
            AnimatedVisibility(visible = isSearchActive) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search your local videos...", color = secondaryTextColor) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = primaryTextColor,
                        unfocusedTextColor = primaryTextColor,
                        focusedBorderColor = Color(0xFF007AFF),
                        unfocusedBorderColor = if (isLightMode) Color(0x33000000) else Color(0x33FFFFFF)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            // Apple-style Segmented Control Tabs
            GlassSurface(
                shape = RoundedCornerShape(18.dp),
                isLightMode = isLightMode,
                transparencyAlpha = settings.liquidGlassAlpha,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LibraryTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) {
                                        if (isLightMode) Color(0xFF007AFF) else Color.White.copy(alpha = 0.22f)
                                    } else Color.Transparent
                                )
                                .clickable {
                                    selectedTab = tab
                                    activeFolderFilter = null
                                    activePlaylistFilter = null
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab.title,
                                color = if (isSelected) {
                                    if (isLightMode) Color.White else Color.White
                                } else secondaryTextColor,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Breadcrumb if folder drilled into
            if (activeFolderFilter != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Folder: $activeFolderFilter",
                        color = Color(0xFF007AFF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(Show all folders)",
                        color = secondaryTextColor,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { activeFolderFilter = null }
                    )
                }
            }

            // Main Content Body based on selected tab
            when (selectedTab) {
                LibraryTab.ALL, LibraryTab.CONTINUE -> {
                    VideoListOrGrid(
                        videos = filteredVideos,
                        viewMode = settings.viewMode,
                        isLightMode = isLightMode,
                        onVideoClick = { video -> onVideoSelected(video, filteredVideos) },
                        onVideoLongPress = { video -> actionSheetVideo = video }
                    )
                }
                LibraryTab.FOLDERS -> {
                    if (activeFolderFilter == null) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(foldersMap.entries.toList(), key = { it.key }) { entry ->
                                GlassSurface(
                                    shape = RoundedCornerShape(20.dp),
                                    isLightMode = isLightMode,
                                    transparencyAlpha = settings.liquidGlassAlpha,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp),
                                    onClick = { activeFolderFilter = entry.key }
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = Color(0xFF007AFF),
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Column {
                                            Text(
                                                text = entry.key,
                                                color = primaryTextColor,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "${entry.value.size} videos",
                                                color = secondaryTextColor,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        VideoListOrGrid(
                            videos = filteredVideos,
                            viewMode = settings.viewMode,
                            isLightMode = isLightMode,
                            onVideoClick = { video -> onVideoSelected(video, filteredVideos) },
                            onVideoLongPress = { video -> actionSheetVideo = video }
                        )
                    }
                }
                LibraryTab.PLAYLISTS -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            GlassSurface(
                                shape = RoundedCornerShape(18.dp),
                                isLightMode = isLightMode,
                                transparencyAlpha = settings.liquidGlassAlpha,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showCreatePlaylistDialog = true }
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color(0xFF007AFF),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(
                                        text = "Create New Playlist",
                                        color = primaryTextColor,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        if (playlists.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No playlists created yet.\nCreate custom playlists to organize your local videos.",
                                        color = secondaryTextColor,
                                        textAlign = TextAlign.Center,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            items(playlists, key = { it.playlistId }) { pl ->
                                GlassSurface(
                                    shape = RoundedCornerShape(18.dp),
                                    isLightMode = isLightMode,
                                    transparencyAlpha = settings.liquidGlassAlpha,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    onClick = { activePlaylistFilter = pl }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.PlaylistPlay,
                                                contentDescription = null,
                                                tint = Color(0xFF007AFF),
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Column {
                                                Text(
                                                    text = pl.name,
                                                    color = primaryTextColor,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "Local Playlist",
                                                    color = secondaryTextColor,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Video 2-Second Hold Action Sheet
    actionSheetVideo?.let { video ->
        VideoActionSheet(
            video = video,
            playlists = playlists,
            onDismiss = { actionSheetVideo = null },
            onPlayNext = { onVideoSelected(video, allVideos) },
            onAddToQueue = { /* added */ },
            onAddToPlaylist = { plId ->
                scope.launch { videoRepository.addVideoToPlaylist(plId, video.id) }
            },
            onCreateAndAddToPlaylist = { name ->
                scope.launch {
                    val newId = videoRepository.createPlaylist(name)
                    videoRepository.addVideoToPlaylist(newId, video.id)
                }
            },
            onRename = { newTitle ->
                scope.launch { videoRepository.renameVideo(video.id, newTitle) }
            },
            onDeleteFromDevice = {
                scope.launch { videoRepository.deleteVideo(video) }
            },
            onGoToFolder = { folder ->
                selectedTab = LibraryTab.FOLDERS
                activeFolderFilter = folder
            },
            fetchDetails = {
                videoRepository.getDetailedMetadata(video)
            }
        )
    }

    // Create Playlist Dialog
    if (showCreatePlaylistDialog) {
        var playlistName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            containerColor = if (isLightMode) Color(0xFFF7F8FA) else Color(0xFF1E2029),
            title = { Text("New Playlist", color = primaryTextColor, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    placeholder = { Text("Enter playlist title", color = secondaryTextColor) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = primaryTextColor,
                        unfocusedTextColor = primaryTextColor,
                        focusedBorderColor = Color(0xFF007AFF),
                        unfocusedBorderColor = if (isLightMode) Color(0x33000000) else Color(0x44FFFFFF)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (playlistName.isNotBlank()) {
                        scope.launch { videoRepository.createPlaylist(playlistName.trim()) }
                    }
                    showCreatePlaylistDialog = false
                }) {
                    Text("Create", color = Color(0xFF007AFF), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text("Cancel", color = secondaryTextColor)
                }
            }
        )
    }
}

@Composable
private fun VideoListOrGrid(
    videos: List<VideoItemEntity>,
    viewMode: String,
    isLightMode: Boolean,
    onVideoClick: (VideoItemEntity) -> Unit,
    onVideoLongPress: (VideoItemEntity) -> Unit
) {
    val secondaryTextColor = if (isLightMode) TextSecondaryLight else TextSecondaryDark

    if (videos.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No videos found.\nVideos saved in your device storage will show up automatically.",
                color = secondaryTextColor,
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        }
    } else if (viewMode == "grid") {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(videos, key = { it.id }) { video ->
                VideoGridItem(
                    video = video,
                    isLightMode = isLightMode,
                    onClick = { onVideoClick(video) },
                    onLongPressThreshold = { onVideoLongPress(video) }
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(videos, key = { it.id }) { video ->
                VideoListItem(
                    video = video,
                    isLightMode = isLightMode,
                    onClick = { onVideoClick(video) },
                    onLongPressThreshold = { onVideoLongPress(video) }
                )
            }
        }
    }
}
