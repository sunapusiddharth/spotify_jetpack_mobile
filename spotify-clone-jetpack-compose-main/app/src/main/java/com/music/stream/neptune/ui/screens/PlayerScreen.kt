package com.music.stream.neptune.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Checkbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.music.stream.neptune.R
import com.music.stream.neptune.data.api.Response
import com.music.stream.neptune.data.entity.SongsModel
import com.music.stream.neptune.di.PlaybackMediaType
import com.music.stream.neptune.di.Palette
import com.music.stream.neptune.di.SongPlayer
import com.music.stream.neptune.ui.components.CustomSlider
import com.music.stream.neptune.ui.components.Snackbar
import com.music.stream.neptune.ui.components.pressScale
import com.music.stream.neptune.ui.navigation.Routes
import com.music.stream.neptune.ui.theme.AppBackground
import com.music.stream.neptune.ui.theme.AppPalette
import com.music.stream.neptune.ui.viewmodel.LocalSharedPlayerViewModel
import com.music.stream.neptune.ui.viewmodel.PlayerViewModel
import kotlin.math.abs
import kotlinx.coroutines.delay

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PlayerScreen(navController: NavController) {
    val playerViewModel: PlayerViewModel = LocalSharedPlayerViewModel.current
    val songTitle = playerViewModel.currentSongTitle.value
    val songSinger = playerViewModel.currentSongSinger.value
    val songCoverUri = playerViewModel.currentSongCoverUri.value
    val songPlayingState = playerViewModel.currentSongPlayingState.value
    val songId = playerViewModel.currentSongId.value
    val mediaType = playerViewModel.mediaType.value
    val songAlbumTitle = playerViewModel.currentSongAlbumTitle.value
    val songAlbumId = playerViewModel.currentSongAlbumId.value
    val context = LocalContext.current
    val actionMessage by playerViewModel.actionMessage.collectAsState()
    val userPlaylistsState by playerViewModel.userPlaylists.collectAsState()
    var showPlaylistPicker by remember { mutableStateOf(false) }
    var selectedPlaylistIds by remember(songId) { mutableStateOf(setOf<String>()) }
    var newPlaylistName by remember(songId) { mutableStateOf("") }
    var artworkDragOffset by remember(songId) { mutableStateOf(0f) }

    var songProgress by remember { mutableStateOf(maxOf(0f, SongPlayer.getCurrentPosition().toFloat())) }
    var songDurationText by remember { mutableStateOf("0:00") }
    var songProgressText by remember { mutableStateOf("0:00") }

    songDurationText = if (SongPlayer.getDuration() < 0) "0:00"
    else playerViewModel.formatDuration(SongPlayer.getDuration())
    songProgressText = if (SongPlayer.getCurrentPosition() < 0) "0:00"
    else playerViewModel.formatDuration(SongPlayer.getCurrentPosition())

    var dominantColor by remember { mutableStateOf(Color(AppBackground.toArgb())) }
    Palette().extractSecondColorFromCoverUrl(context = context, songCoverUri) { color ->
        dominantColor = color
    }

    val shuffle = playerViewModel.shuffleState.value
    val repeat = playerViewModel.repeatState.value

    if ((songProgressText != "0:00") && (songDurationText == songProgressText)) {
        if (repeat) {
            SongPlayer.seekTo(0)
        } else {
            playerViewModel.playNext(context)
        }
    }

    LaunchedEffect(actionMessage) {
        if (actionMessage != null) {
            delay(1600)
            playerViewModel.clearActionMessage()
        }
    }

    LaunchedEffect(showPlaylistPicker, userPlaylistsState, songId) {
        if (!showPlaylistPicker) return@LaunchedEffect
        val playlists = (userPlaylistsState as? Response.Success)?.data ?: return@LaunchedEffect
        selectedPlaylistIds = playlists
            .filter { it.tracks.contains(songId) }
            .map { it.id }
            .toSet()
    }

    LaunchedEffect(key1 = songPlayingState) {
        while (songPlayingState) {
            songProgress = SongPlayer.getCurrentPosition().toFloat()
            songProgressText = playerViewModel.formatDuration(songProgress.toLong())
            playerViewModel.onPlaybackProgress(songProgress.toLong())
            delay(300L)
        }
    }

    // Queue sheet state
    var showQueue by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(dominantColor, Color.Black),
                    startY = 100f
                )
            )
            .statusBarsPadding()
    ) {
        // Main player content
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PlayerTopBar(navController)

            GlideImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(410.dp)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .graphicsLayer(translationX = artworkDragOffset)
                    .pointerInput(songId, mediaType) {
                        var totalDrag = 0f
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                totalDrag += dragAmount
                                artworkDragOffset = (artworkDragOffset + dragAmount).coerceIn(-180f, 180f)
                            },
                            onDragEnd = {
                                when {
                                    totalDrag <= -120f -> playerViewModel.playNext(context)
                                    totalDrag >= 120f -> playerViewModel.playPrevious(context)
                                }
                                totalDrag = 0f
                                artworkDragOffset = 0f
                            },
                            onDragCancel = {
                                totalDrag = 0f
                                artworkDragOffset = 0f
                            }
                        )
                    }
                    .clip(RoundedCornerShape(12.dp)),
                model = songCoverUri,
                contentScale = ContentScale.Crop,
                contentDescription = ""
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 56.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_player_back),
                    contentDescription = "Previous hint",
                    tint = if (artworkDragOffset > 24f) {
                        Color.White.copy(alpha = 0.28f + (abs(artworkDragOffset) / 220f).coerceIn(0f, 0.37f))
                    } else {
                        Color.White.copy(alpha = 0.14f)
                    },
                    modifier = Modifier.size(18.dp)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_player_skip),
                    contentDescription = "Next hint",
                    tint = if (artworkDragOffset < -24f) {
                        Color.White.copy(alpha = 0.28f + (abs(artworkDragOffset) / 220f).coerceIn(0f, 0.37f))
                    } else {
                        Color.White.copy(alpha = 0.14f)
                    },
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(horizontal = 0.dp, vertical = 0.dp)
                    .padding(bottom = 40.dp)
            ) {
                if (actionMessage != null) {
                    Snackbar(showMessage = actionMessage ?: "")
                }

                PlayerInfo(
                    songTitle = songTitle,
                    songSinger = songSinger,
                    albumTitle = songAlbumTitle,
                    showAlbumLink = mediaType == PlaybackMediaType.SONG && songAlbumId.isNotBlank(),
                    onAlbumClick = {
                        if (songAlbumId.isNotBlank()) {
                            navController.navigate("${Routes.Album.route}/$songAlbumId")
                        }
                    },
                    isLiked = playerViewModel.likeState.value,
                    showLike = songId.isNotBlank() && mediaType in setOf(
                        PlaybackMediaType.SONG,
                        PlaybackMediaType.RADIO,
                        PlaybackMediaType.PODCAST
                    ),
                    onLike = { playerViewModel.toggleLikeCurrentMedia() }
                )

                CustomSlider(
                    value = if (SongPlayer.getDuration() > 0)
                        SongPlayer.getCurrentPosition().toFloat() / SongPlayer.getDuration().toFloat()
                    else 0f,
                    onValueChange = { newValue ->
                        SongPlayer.seekTo((newValue * SongPlayer.getDuration()).toLong())
                        if (!songPlayingState) {
                            SongPlayer.play()
                            playerViewModel.updateSongState(
                                playerViewModel.currentSongCoverUri.value,
                                playerViewModel.currentSongTitle.value,
                                playerViewModel.currentSongSinger.value,
                                true,
                                playerViewModel.currentSongId.value,
                                playerViewModel.currentSongIndex.value,
                                playerViewModel.currentSongAlbum.value
                            )
                        }
                    },
                    valueRange = 0f..1f,
                    steps = 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.Gray
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = songProgressText, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Text(text = songDurationText, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }

                PlayerFull(songPlayingState, playerViewModel, context, shuffle, repeat)

                if (mediaType == PlaybackMediaType.SONG) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.10f))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    playerViewModel.loadUserPlaylists()
                                    showPlaylistPicker = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LibraryAdd,
                                contentDescription = "Add to playlist",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(18.dp))
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.10f))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    playerViewModel.requestCurrentTrackAddition()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Request song",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // ── Bottom row: devices · queue ──────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(id = R.drawable.ic_devices),
                        tint = Color.White,
                        contentDescription = "Devices"
                    )
                    // Queue toggle button
                    if (mediaType == PlaybackMediaType.SONG) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showQueue = true }
                        ) {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(id = R.drawable.ic_share),
                                tint = Color.White,
                                contentDescription = "Queue"
                            )
                            Text(
                                text = "Up Next",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // ── Queue Sheet Overlay ──────────────────────────────────────────────
        val activeSongQueue = playerViewModel.songQueue.value
        AnimatedVisibility(
            visible = showQueue && mediaType == PlaybackMediaType.SONG,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            QueueSheet(
                queueSongs = activeSongQueue,
                currentSongId = songId,
                playerViewModel = playerViewModel,
                context = context,
                onDismiss = { showQueue = false }
            )
        }

        if (showPlaylistPicker && mediaType == PlaybackMediaType.SONG) {
            AlertDialog(
                onDismissRequest = {
                    showPlaylistPicker = false
                    newPlaylistName = ""
                },
                title = { Text("Your Playlists") },
                text = {
                    when (userPlaylistsState) {
                        is Response.Loading -> Text("Loading playlists...")
                        is Response.Success -> {
                            val playlists = (userPlaylistsState as Response.Success).data
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = newPlaylistName,
                                    onValueChange = { newPlaylistName = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    label = { Text("New playlist name") }
                                )
                                Text(
                                    text = "Choose existing playlists or create a new one.",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                                if (playlists.isEmpty()) {
                                    Text("No playlists found", color = Color.White)
                                }
                                playlists.forEach { playlist ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedPlaylistIds = if (selectedPlaylistIds.contains(playlist.id)) {
                                                    selectedPlaylistIds - playlist.id
                                                } else {
                                                    selectedPlaylistIds + playlist.id
                                                }
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = selectedPlaylistIds.contains(playlist.id),
                                            onCheckedChange = { checked ->
                                                selectedPlaylistIds = if (checked) {
                                                    selectedPlaylistIds + playlist.id
                                                } else {
                                                    selectedPlaylistIds - playlist.id
                                                }
                                            }
                                        )
                                        Column {
                                            Text(text = playlist.name)
                                            if (playlist.tracks.contains(songId)) {
                                                Text(
                                                    text = "Already contains this song",
                                                    color = Color.Gray,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        is Response.Error -> Text("Failed to load playlists")
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        playerViewModel.saveCurrentSongToPlaylists(selectedPlaylistIds, newPlaylistName)
                        showPlaylistPicker = false
                        newPlaylistName = ""
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showPlaylistPicker = false
                        newPlaylistName = ""
                    }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

// ─── Queue Sheet ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun QueueSheet(
    queueSongs: List<SongsModel>,
    currentSongId: String,
    playerViewModel: PlayerViewModel,
    context: Context,
    onDismiss: () -> Unit
) {
    val listState = rememberLazyListState()
    val currentIndex = queueSongs.indexOfFirst { it.id == currentSongId }

    LaunchedEffect(currentSongId) {
        if (currentIndex >= 0) {
            listState.scrollToItem(maxOf(0, currentIndex - 1))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.97f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* absorb clicks */ }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp, 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Up Next",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (queueSongs.isNotEmpty()) {
                        Text(
                            text = "${queueSongs.size} songs",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                        contentDescription = "Close"
                    )
                }
            }

            if (queueSongs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No songs in queue", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(state = listState) {
                    itemsIndexed(queueSongs) { _, song ->
                        val isCurrentSong = song.id == currentSongId
                        val interactionSource = remember { MutableInteractionSource() }
                        val bgColor by animateColorAsState(
                            targetValue = if (isCurrentSong) Color.White.copy(alpha = 0.08f) else Color.Transparent,
                            animationSpec = spring(stiffness = 320f),
                            label = "queueHighlight"
                        )
                        val itemScale by animateFloatAsState(
                            targetValue = if (isCurrentSong) 1.015f else 1f,
                            animationSpec = spring(dampingRatio = 0.75f, stiffness = 380f),
                            label = "queueScale"
                        )

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer(scaleX = itemScale, scaleY = itemScale)
                                .background(bgColor)
                                .pressScale(interactionSource, pressedScale = 0.985f)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    playerViewModel.playSongById(song.id, context)
                                }
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box {
                                    GlideImage(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(6.dp)),
                                        model = song.thumbnail,
                                        contentScale = ContentScale.Crop,
                                        loading = placeholder(R.drawable.placeholder),
                                        failure = placeholder(R.drawable.placeholder),
                                        contentDescription = ""
                                    )
                                    if (isCurrentSong) {
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color.Black.copy(alpha = 0.5f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_playing),
                                                tint = Color(AppPalette.toArgb()),
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.name,
                                        color = if (isCurrentSong) Color(AppPalette.toArgb()) else Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = if (isCurrentSong) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = song.singer,
                                        color = Color.Gray,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            if (song.duration > 0) {
                                Text(
                                    text = formatMillis(song.duration),
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }

                    item { Spacer(Modifier.height(40.dp)) }
                }
            }
        }
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────

@Composable
fun PlayerTopBar(navController: NavController) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Icon(
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { navController.navigateUp() },
            painter = painterResource(id = R.drawable.ic_down),
            tint = Color.White,
            contentDescription = "Down"
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "NOW PLAYING", color = Color.LightGray, fontSize = 10.sp, letterSpacing = 1.5.sp)
        }
        // Spacer to balance layout
        Spacer(Modifier.size(24.dp))
    }
}

// ─── Song Info + Like ────────────────────────────────────────────────────────

@Composable
fun PlayerInfo(
    songTitle: String,
    songSinger: String,
    albumTitle: String,
    showAlbumLink: Boolean,
    onAlbumClick: () -> Unit,
    isLiked: Boolean,
    showLike: Boolean,
    onLike: () -> Unit
) {
    val likeScale by animateFloatAsState(
        targetValue = if (isLiked) 1.18f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 420f),
        label = "playerLikeScale"
    )
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = songTitle,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = songSinger,
                color = Color.LightGray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1
            )
            if (showAlbumLink) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = albumTitle,
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onAlbumClick() }
                )
            }
        }
        if (showLike) {
            Spacer(Modifier.width(12.dp))
            IconButton(onClick = onLike) {
                Icon(
                    painter = if (isLiked) painterResource(id = R.drawable.added)
                    else painterResource(id = R.drawable.ic_add),
                    tint = if (isLiked) Color.Red else Color.White,
                    contentDescription = if (isLiked) "Unlike" else "Like",
                    modifier = Modifier
                        .size(28.dp)
                        .graphicsLayer(scaleX = likeScale, scaleY = likeScale)
                )
            }
        }
    }
}

// ─── Controls ────────────────────────────────────────────────────────────────

@Composable
fun PlayerFull(
    songPlayingState: Boolean,
    playerViewModel: PlayerViewModel,
    context: Context,
    shuffle: Boolean,
    repeat: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
    ) {
        // Shuffle
        Icon(
            modifier = Modifier
                .size(22.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { playerViewModel.updateShuffleState(!shuffle) },
            tint = if (shuffle) Color(AppPalette.toArgb()) else Color.White,
            painter = painterResource(id = R.drawable.ic_player_shuffle),
            contentDescription = "Shuffle"
        )

        // Previous
        Icon(
            modifier = Modifier
                .size(30.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    playerViewModel.playPrevious(context)
                },
            tint = Color.White,
            painter = painterResource(id = R.drawable.ic_player_back),
            contentDescription = "Previous"
        )

        // Play / Pause
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable {
                    if (songPlayingState) {
                        SongPlayer.pause()
                        playerViewModel.updateSongState(
                            playerViewModel.currentSongCoverUri.value,
                            playerViewModel.currentSongTitle.value,
                            playerViewModel.currentSongSinger.value,
                            false,
                            playerViewModel.currentSongId.value,
                            playerViewModel.currentSongIndex.value,
                            playerViewModel.currentSongAlbum.value
                        )
                    } else {
                        SongPlayer.play()
                        playerViewModel.updateSongState(
                            playerViewModel.currentSongCoverUri.value,
                            playerViewModel.currentSongTitle.value,
                            playerViewModel.currentSongSinger.value,
                            true,
                            playerViewModel.currentSongId.value,
                            playerViewModel.currentSongIndex.value,
                            playerViewModel.currentSongAlbum.value
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(26.dp),
                tint = Color.Black,
                painter = if (songPlayingState) painterResource(id = R.drawable.ic_playing)
                else painterResource(id = R.drawable.play_svgrepo_com),
                contentDescription = "Play/Pause"
            )
        }

        // Next
        Icon(
            modifier = Modifier
                .size(30.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    playerViewModel.playNext(context)
                },
            tint = Color.White,
            painter = painterResource(id = R.drawable.ic_player_skip),
            contentDescription = "Next"
        )

        // Repeat
        Icon(
            modifier = Modifier
                .size(22.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { playerViewModel.updateRepeatState(!repeat) },
            tint = if (repeat) Color(AppPalette.toArgb()) else Color.White,
            painter = painterResource(id = R.drawable.ic_repeat),
            contentDescription = "Repeat"
        )
    }
}
