package com.music.stream.neptune.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.music.stream.neptune.data.preferences.getLikedSongIds
import com.music.stream.neptune.data.preferences.toggleLikedSong
import com.music.stream.neptune.di.SongPlayer
import com.music.stream.neptune.ui.components.Loader
import com.music.stream.neptune.ui.navigation.Routes
import com.music.stream.neptune.ui.theme.AppBackground
import com.music.stream.neptune.ui.theme.AppPalette
import com.music.stream.neptune.ui.viewmodel.AlbumViewModel
import com.music.stream.neptune.ui.viewmodel.PlayerViewModel

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PlaylistScreen(navController: NavController, playlistId: String) {
    val viewModel: AlbumViewModel = hiltViewModel()
    val albumState by viewModel.album.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(playlistId) {
        viewModel.loadPlaylist(playlistId)
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
                    PlaylistContent(
                        playlist = playlist,
                        isCollection = false,
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
            else -> {}
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
                imageVector = Icons.Default.ArrowBack,
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
                    PlaylistContent(
                        playlist = collection,
                        isCollection = true,
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
            else -> {}
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
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun PlaylistContent(
    playlist: AlbumsModel,
    isCollection: Boolean,
    navController: NavController,
    viewModel: AlbumViewModel,
    context: Context
) {
    val playingState by viewModel.currentSongPlayingState
    val currentSongId by viewModel.currentSongId
    val currentAlbum by viewModel.currentSongAlbum
    val paletteColor = Color(AppPalette.toArgb())
    val playerViewModel: PlayerViewModel = hiltViewModel()

    val playlistSongs = playlist.songs
    val isThisPlaying = playingState && currentAlbum == playlist.id.toString()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Hero header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
        ) {
            GlideImage(
                model = playlist.image,
                contentDescription = playlist.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0f to Color.Transparent,
                                0.6f to Color.Black.copy(alpha = 0.4f),
                                1f to Color(AppBackground.toArgb())
                            )
                        )
                    )
            )
            // Labels
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                // Type badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(paletteColor.copy(alpha = 0.85f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isCollection) "PLAYLIST COLLECTION" else "PLAYLIST",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = playlist.title,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (playlist.artists.isNotEmpty()) {
                    Text(
                        text = playlist.artists.take(3).joinToString(" • ") { it.name },
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Metadata + Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${playlistSongs.size} song${if (playlistSongs.size != 1) "s" else ""}",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
                if (playlistSongs.isNotEmpty()) {
                    val totalMs = playlistSongs.sumOf { it.duration }
                    Text(
                        text = formatPlaylistDuration(totalMs),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Shuffle
                IconButton(
                    onClick = {
                        if (playlistSongs.isNotEmpty()) {
                            val shuffled = playlistSongs.shuffled()
                            playerViewModel.playSongQueueFromPlaylist(
                                queueSongs = shuffled,
                                startIndex = 0,
                                album = playlist.id.toString(),
                                context = context
                            )
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A2A3A))
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                // Play/Pause
                IconButton(
                    onClick = {
                        if (playlistSongs.isNotEmpty()) {
                            if (isThisPlaying) {
                                SongPlayer.pause()
                                viewModel.updateSongState(
                                    coverUri = viewModel.currentSongCoverUri.value,
                                    title = viewModel.currentSongTitle.value,
                                    singer = viewModel.currentSongSinger.value,
                                    playingState = false,
                                    songId = currentSongId,
                                    songIndex = viewModel.currentSongIndex.value,
                                    album = playlist.id.toString()
                                )
                            } else {
                                playerViewModel.playSongQueueFromPlaylist(
                                    queueSongs = playlistSongs,
                                    startIndex = 0,
                                    album = playlist.id.toString(),
                                    context = context
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(paletteColor)
                ) {
                    Icon(
                        imageVector = if (isThisPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isThisPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        // Song list
        if (playlistSongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No songs in this playlist", color = Color.Gray)
            }
        } else {
            playlistSongs.forEachIndexed { index, song ->
                PlaylistSongRow(
                    song = song,
                    trackNumber = index + 1,
                    isPlaying = currentSongId == song.id && playingState,
                    paletteColor = paletteColor,
                    context = context,
                    onClick = {
                        playerViewModel.playSongQueueFromPlaylist(
                            queueSongs = playlistSongs,
                            startIndex = index,
                            album = playlist.id.toString(),
                            context = context
                        )
                        navController.navigate(Routes.Player.route)
                    }
                )
            }
        }

        Spacer(Modifier.height(130.dp))
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun PlaylistSongRow(
    song: SongsModel,
    trackNumber: Int,
    isPlaying: Boolean,
    paletteColor: Color,
    context: Context,
    onClick: () -> Unit
) {
    val likedSongs = remember { getLikedSongIds(context).toMutableSet() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isPlaying) paletteColor.copy(alpha = 0.15f) else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
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
            Text(
                text = song.artists.joinToString(", ") { it.title },
                color = Color.Gray,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        // Like button
        IconButton(
            onClick = { toggleLikedSong(context, song.id) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (likedSongs.contains(song.id)) Icons.Default.Favorite
                else Icons.Default.FavoriteBorder,
                contentDescription = "Like",
                tint = if (likedSongs.contains(song.id)) Color(0xFF1DB954) else Color.Gray,
                modifier = Modifier.size(16.dp)
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
        totalMinutes < 60 -> "$totalMinutes min"
        else -> "${totalMinutes / 60}h ${totalMinutes % 60}m"
    }
}
