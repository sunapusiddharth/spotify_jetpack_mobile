package com.music.stream.neptune.ui.screens

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.music.stream.neptune.data.api.Response
import com.music.stream.neptune.data.entity.AlbumsModel
import com.music.stream.neptune.data.entity.SongsModel
import com.music.stream.neptune.di.SongPlayer
import com.music.stream.neptune.ui.components.Loader
import com.music.stream.neptune.ui.components.UnavailableAudioBadge
import com.music.stream.neptune.ui.components.pressScale
import com.music.stream.neptune.ui.navigation.Routes
import com.music.stream.neptune.ui.theme.AppBackground
import com.music.stream.neptune.ui.theme.AppPalette
import com.music.stream.neptune.ui.viewmodel.AlbumViewModel
import com.music.stream.neptune.ui.viewmodel.LocalSharedPlayerViewModel
import com.music.stream.neptune.ui.viewmodel.PlayerViewModel

private val playlistScrollPositions = mutableMapOf<String, Int>()

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PlaylistScreen(navController: NavController, playlistId: String) {
    val viewModel: AlbumViewModel = hiltViewModel()
    val albumState by viewModel.album.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(playlistId) {
        viewModel.loadPlaylistCollection(playlistId)
    }

    val bgColor = Color(AppBackground.toArgb())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        when (albumState) {
            is Response.Loading -> Loader()
            is Response.Success -> {
                val playlist = (albumState as Response.Success).data
                if (playlist != null) {
                    PlaylistCollectionContent(
                        playlist = playlist,
                        navController = navController,
                        viewModel = viewModel,
                        context = context
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No playlist data available", color = Color.White)
                    }
                }
            }
            is Response.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Failed to load playlist", color = Color.White)
                }
            }
        }

        // Back button
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .statusBarsPadding()
                .padding(8.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PlaylistCollectionScreen(navController: NavController, collectionId: String) {
    val viewModel: AlbumViewModel = hiltViewModel()
    val albumState by viewModel.album.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(collectionId) {
        viewModel.loadPlaylistCollection(collectionId)
    }

    val bgColor = Color(AppBackground.toArgb())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        when (albumState) {
            is Response.Loading -> Loader()
            is Response.Success -> {
                val collection = (albumState as Response.Success).data
                if (collection != null) {
                    PlaylistCollectionContent(
                        playlist = collection,
                        navController = navController,
                        viewModel = viewModel,
                        context = context
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No collection data available", color = Color.White)
                    }
                }
            }
            is Response.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Failed to load collection", color = Color.White)
                }
            }
        }

        // Back button
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .statusBarsPadding()
                .padding(8.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun PlaylistCollectionContent(
    playlist: AlbumsModel,
    navController: NavController,
    viewModel: AlbumViewModel,
    context: Context
) {
    val playingState by viewModel.currentSongPlayingState
    val currentSongId by viewModel.currentSongId
    val currentAlbum by viewModel.currentSongAlbum
    val paletteColor = Color(AppPalette.toArgb())
    val playerViewModel: PlayerViewModel = LocalSharedPlayerViewModel.current
    val likedSongIds by playerViewModel.likedSongIds.collectAsState()

    val playlistSongs = playlist.songs
    val playableSongs = playlistSongs.filter { it.hasPlayableAudio }
    val isThisPlaying = playingState && currentAlbum == playlist.id
    val initialScroll = remember(playlist.id) { playlistScrollPositions[playlist.id] ?: 0 }
    val scrollState = rememberScrollState(initial = initialScroll)

    DisposableEffect(playlist.id, scrollState) {
        onDispose {
            playlistScrollPositions[playlist.id] = scrollState.value
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(Color(AppBackground.toArgb()))
    ) {
        PlaylistHeroHeader(
            playlist = playlist,
            paletteColor = paletteColor,
            scrollOffset = scrollState.value
        )

        Surface(
            color = Color(AppBackground.toArgb()),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "${playlistSongs.size} tracks",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = buildString {
                                append(formatPlaylistDuration(playlistSongs.sumOf { it.duration }))
                                if (playableSongs.size != playlistSongs.size) {
                                    append(" • ")
                                    append("${playableSongs.size} playable")
                                }
                            },
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        IconButton(
                            onClick = {
                                if (playableSongs.isNotEmpty()) {
                                    val shuffled = playableSongs.shuffled()
                                    playerViewModel.playSongQueueFromPlaylist(
                                        queueSongs = shuffled,
                                        startIndex = 0,
                                        album = playlist.id,
                                        context = context
                                    )
                                }
                            },
                            enabled = playableSongs.isNotEmpty(),
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (playableSongs.isNotEmpty()) Color(0xFF20242C) else Color(0xFF16181D))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (playableSongs.isNotEmpty()) Color.White else Color.Gray
                            )
                        }
                        IconButton(
                            onClick = {
                                if (playableSongs.isNotEmpty()) {
                                    if (isThisPlaying) {
                                        SongPlayer.pause()
                                        viewModel.updateSongState(
                                            coverUri = viewModel.currentSongCoverUri.value,
                                            title = viewModel.currentSongTitle.value,
                                            singer = viewModel.currentSongSinger.value,
                                            playingState = false,
                                            songId = currentSongId,
                                            songIndex = viewModel.currentSongIndex.value,
                                            album = playlist.id
                                        )
                                    } else {
                                        playerViewModel.playSongQueueFromPlaylist(
                                            queueSongs = playableSongs,
                                            startIndex = 0,
                                            album = playlist.id,
                                            context = context
                                        )
                                    }
                                }
                            },
                            enabled = playableSongs.isNotEmpty(),
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(if (playableSongs.isNotEmpty()) paletteColor else Color(0xFF1B3A28))
                        ) {
                            Icon(
                                imageVector = if (isThisPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isThisPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                if (playlistSongs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No songs in this playlist", color = Color.Gray)
                    }
                } else {
                    playlistSongs.forEachIndexed { index, song ->
                        val playableQueue = playlistSongs.filter { it.hasPlayableAudio }
                        val playableIndex = playableQueue.indexOfFirst { it.id == song.id }
                        PlaylistSongRow(
                            song = song,
                            trackNumber = index + 1,
                            isPlaying = currentSongId == song.id && playingState,
                            isLiked = likedSongIds.contains(song.id),
                            paletteColor = paletteColor,
                            onToggleLike = { playerViewModel.toggleSongLike(song.id) },
                            onClick = {
                                if (song.hasPlayableAudio && playableIndex >= 0) {
                                    playerViewModel.playSongQueueFromPlaylist(
                                        queueSongs = playableQueue,
                                        startIndex = playableIndex,
                                        album = playlist.id,
                                        context = context
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(130.dp))
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun PlaylistHeroHeader(
    playlist: AlbumsModel,
    paletteColor: Color,
    scrollOffset: Int
) {
    val heroTranslation = (scrollOffset * 0.35f).coerceAtMost(140f)
    val heroScale = (1f + (scrollOffset / 1800f)).coerceAtMost(1.08f)
    val contentAlpha = (1f - (scrollOffset / 520f)).coerceIn(0.55f, 1f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
    ) {
        GlideImage(
            model = playlist.image,
            contentDescription = playlist.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    translationY = heroTranslation,
                    scaleX = heroScale,
                    scaleY = heroScale
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.12f),
                            Color.Black.copy(alpha = 0.35f),
                            paletteColor.copy(alpha = 0.45f),
                            Color(AppBackground.toArgb())
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .graphicsLayer(alpha = contentAlpha)
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Text(
                text = playlist.title,
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun PlaylistSongRow(
    song: SongsModel,
    trackNumber: Int,
    isPlaying: Boolean,
    isLiked: Boolean,
    paletteColor: Color,
    onToggleLike: () -> Unit,
    onClick: () -> Unit
) {
    val isPlayable = song.hasPlayableAudio
    val interactionSource = remember { MutableInteractionSource() }
    val likeScale by animateFloatAsState(
        targetValue = if (isLiked) 1.16f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 420f),
        label = "playlistLikeScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isPlaying) paletteColor.copy(alpha = 0.15f) else Color.Transparent)
            .pressScale(interactionSource, pressedScale = 0.985f)
            .clickable(
                enabled = isPlayable,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Track number or playing indicator
        Box(
            modifier = Modifier.width(28.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isPlaying) {
                Text("▶", color = paletteColor, fontSize = 12.sp)
            } else {
                Text(
                    text = "$trackNumber",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
        }
        // Thumbnail
        Box(modifier = Modifier.size(50.dp)) {
            GlideImage(
                model = song.thumbnail,
                contentDescription = song.name,
                contentScale = ContentScale.Crop,
                colorFilter = if (isPlayable) null else ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(6.dp))
            )
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("▶", color = paletteColor, fontSize = 14.sp)
                }
            }
            if (!isPlayable) {
                UnavailableAudioBadge(modifier = Modifier.align(Alignment.Center))
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = song.name,
                color = if (isPlaying) paletteColor else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!isPlayable) {
                Text(
                    text = "Audio unavailable",
                    color = Color(0xFFBDBDBD),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        // Like button
        IconButton(
            onClick = onToggleLike,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (isLiked) Icons.Default.Favorite
                else Icons.Default.FavoriteBorder,
                contentDescription = "Like",
                tint = if (isLiked) Color.Red else Color.Gray,
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer(scaleX = likeScale, scaleY = likeScale)
            )
        }
        if (song.duration > 0) {
            Text(
                text = formatMillis(song.duration),
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

private fun formatPlaylistDuration(totalMs: Int): String {
    val totalMinutes = totalMs / 60000
    return when {
        totalMinutes <= 0 -> "0 min"
        totalMinutes < 60 -> "$totalMinutes min"
        else -> "${totalMinutes / 60}h ${totalMinutes % 60}m"
    }
}
