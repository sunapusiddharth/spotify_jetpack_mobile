package com.music.stream.neptune.ui.screens

import android.annotation.SuppressLint
import android.util.Log
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
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
import com.music.stream.neptune.data.entity.AlbumsModel
import com.music.stream.neptune.data.entity.SongsModel
import com.music.stream.neptune.di.Palette
import com.music.stream.neptune.di.PlaybackMediaType
import com.music.stream.neptune.di.SongPlayer
import com.music.stream.neptune.ui.components.Loader
import com.music.stream.neptune.ui.theme.AppBackground
import com.music.stream.neptune.ui.theme.AppPalette
import com.music.stream.neptune.ui.viewmodel.AlbumViewModel
import com.music.stream.neptune.ui.viewmodel.LocalSharedPlayerViewModel
import com.music.stream.neptune.ui.viewmodel.PlayerViewModel

@Composable
fun AlbumScreen(navController: NavController, albumId: String) {
    val albumViewModel: AlbumViewModel = hiltViewModel()
    val playerViewModel: PlayerViewModel = LocalSharedPlayerViewModel.current
    val albumState by albumViewModel.album.collectAsState()
    val songsState by albumViewModel.songs.collectAsState()

    // Liked Songs special route
    if (albumId == "liked_songs") {
        when (songsState) {
            is Response.Loading -> Loader()
            is Response.Success -> {
                val allSongs = (songsState as Response.Success).data
                com.music.stream.neptune.ui.components.LikedSongsScreen(
                    songs = allSongs,
                    navController = navController,
                    context = LocalContext.current,
                    playerViewModel = playerViewModel
                )
            }
            is Response.Error -> {}
        }
        return
    }

    LaunchedEffect(albumId) {
        albumViewModel.loadAlbum(albumId)
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(AppBackground.toArgb()))
    ) {
        when (albumState) {
            is Response.Loading -> Loader()
            is Response.Success -> {
                val album = (albumState as Response.Success).data
                if (album != null) {
                    Log.d("AlbumScreen", "Album loaded: ${album.title}, songs=${album.songs.size}")
                    SumUpAlbumScreen(
                        navController = navController,
                        albumViewModel = albumViewModel,
                        playerViewModel = playerViewModel,
                        album = album,
                        albumSongs = album.songs.sortedBy { it.name }
                    )
                } else {
                    Box(
                        Modifier.fillMaxSize().background(Color(AppBackground.toArgb())),
                        contentAlignment = Alignment.Center
                    ) { Text("Album not found", color = Color.White) }
                }
            }
            is Response.Error -> {
                Log.d("AlbumScreen", "Error loading album $albumId")
                Box(
                    Modifier.fillMaxSize().background(Color(AppBackground.toArgb())),
                    contentAlignment = Alignment.Center
                ) { Text("Failed to load album", color = Color.White) }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun SumUpAlbumScreen(
    navController: NavController,
    albumViewModel: AlbumViewModel,
    playerViewModel: PlayerViewModel,
    album: AlbumsModel,
    albumSongs: List<SongsModel>
) {
    val context = LocalContext.current
    val likedSongIds by playerViewModel.likedSongIds.collectAsState()
    val likedAlbumIds by playerViewModel.likedAlbumIds.collectAsState()

    var dominantColor by remember { mutableStateOf(Color(AppBackground.toArgb())) }
    Palette().extractSecondColorFromCoverUrl(context = context, album.image) { color ->
        dominantColor = color
    }
    val isAlbumSaved = likedAlbumIds.contains(album.id)
    val isCurrentAlbumPlaying = playerViewModel.mediaType.value == PlaybackMediaType.SONG &&
        playerViewModel.currentSongPlayingState.value &&
        playerViewModel.currentSongAlbumTitle.value == album.title
    val scrollKey = remember(album.id) { "album:${album.id}" }
    val initialScroll = remember(scrollKey) { ScreenScrollMemory.scrollOffsets[scrollKey] ?: 0 }
    val scrollState = rememberScrollState(initial = initialScroll)

    DisposableEffect(scrollKey, scrollState) {
        onDispose {
            ScreenScrollMemory.scrollOffsets[scrollKey] = scrollState.value
        }
    }

    val totalDurationMs = albumSongs.sumOf { it.duration }
    val totalDurationText = formatTotalDuration(totalDurationMs)

    Scaffold(
        containerColor = Color(AppBackground.toArgb()),
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.padding(16.dp, 0.dp),
                navigationIcon = {
                    Icon(
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { navController.navigateUp() },
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                ),
                title = { Text("") }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(AppBackground.toArgb()))
                .verticalScroll(scrollState)
        ) {
            // ── Album Header ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
            ) {
                // Background blurred color
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(dominantColor, Color(AppBackground.toArgb())),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(60.dp))

                    // Album art with shadow effect
                    Box(
                        modifier = Modifier
                            .size(210.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.3f))
                    ) {
                        GlideImage(
                            modifier = Modifier.fillMaxSize(),
                            model = album.image,
                            contentScale = ContentScale.Crop,
                            loading = placeholder(R.drawable.placeholder),
                            failure = placeholder(R.drawable.placeholder),
                            contentDescription = "Album art"
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Album title + metadata
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Text(
                            text = album.title,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = album.artists.joinToString(", ") { it.name },
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${albumSongs.size} songs",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                            if (totalDurationMs > 0) {
                                Text(text = "·", color = Color.Gray, fontSize = 12.sp)
                                Text(
                                    text = totalDurationText,
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Action buttons row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Save / unsave
                            Icon(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        playerViewModel.toggleAlbumLike(album)
                                    },
                                painter = if (isAlbumSaved) painterResource(R.drawable.added)
                                else painterResource(R.drawable.ic_add),
                                tint = if (isAlbumSaved) Color.White else Color.Gray,
                                contentDescription = "Save album"
                            )

                            Spacer(Modifier.weight(1f))

                            // Shuffle button
                            if (albumSongs.isNotEmpty()) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            val shuffled = albumSongs.shuffled()
                                            playerViewModel.startSongPlayback(
                                                queueSongs = shuffled,
                                                startIndex = 0,
                                                album = album.title,
                                                context = context
                                            )
                                        }
                                ) {
                                    Icon(
                                        modifier = Modifier.size(22.dp),
                                        tint = Color.White,
                                        painter = painterResource(R.drawable.ic_player_shuffle),
                                        contentDescription = "Shuffle"
                                    )
                                }
                            }

                            // Play button
                            if (albumSongs.isNotEmpty()) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            if (isCurrentAlbumPlaying) {
                                                SongPlayer.pause()
                                                playerViewModel.updateSongState(
                                                    playerViewModel.currentSongCoverUri.value,
                                                    playerViewModel.currentSongTitle.value,
                                                    playerViewModel.currentSongSinger.value,
                                                    false,
                                                    playerViewModel.currentSongId.value,
                                                    playerViewModel.currentSongIndex.value,
                                                    album.title
                                                )
                                            } else {
                                                playerViewModel.startSongPlayback(
                                                    queueSongs = albumSongs,
                                                    startIndex = 0,
                                                    album = album.title,
                                                    context = context
                                                )
                                            }
                                        }
                                ) {
                                    Icon(
                                        modifier = Modifier.size(26.dp),
                                        tint = Color.Black,
                                        painter = if (isCurrentAlbumPlaying)
                                            painterResource(R.drawable.ic_playing)
                                        else
                                            painterResource(R.drawable.play_svgrepo_com),
                                        contentDescription = "Play"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Song List ────────────────────────────────────────────────────
            albumSongs.forEachIndexed { index, song ->
                val isLiked = likedSongIds.contains(song.id)

                val isPlaying = song.id == albumViewModel.currentSongId.value
                val textColor = if (isPlaying) Color(AppPalette.toArgb()) else Color.White

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            playerViewModel.startSongPlayback(
                                queueSongs = albumSongs,
                                startIndex = index,
                                album = album.title,
                                context = context
                            )
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Track number
                        Text(
                            text = "${index + 1}",
                            color = if (isPlaying) Color(AppPalette.toArgb()) else Color.Gray,
                            fontSize = 13.sp,
                            modifier = Modifier.width(28.dp),
                            fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal
                        )

                        Box {
                            GlideImage(
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                model = song.thumbnail,
                                contentScale = ContentScale.Crop,
                                loading = placeholder(R.drawable.placeholder),
                                failure = placeholder(R.drawable.placeholder),
                                contentDescription = ""
                            )
                            // Playing indicator overlay
                            if (isPlaying) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(Color.Black.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_playing),
                                        tint = Color(AppPalette.toArgb()),
                                        contentDescription = "Playing",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.name,
                                color = textColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
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

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (song.duration > 0) {
                            Text(
                                text = formatMillis(song.duration),
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                        Icon(
                            modifier = Modifier
                                .size(20.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    playerViewModel.toggleSongLike(song.id)
                                },
                            painter = if (isLiked) painterResource(R.drawable.added)
                            else painterResource(R.drawable.ic_add),
                            tint = if (isLiked) Color.White else Color.Gray,
                            contentDescription = ""
                        )
                    }
                }
            }

            Spacer(Modifier.height(120.dp))
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

private fun formatTotalDuration(totalMs: Int): String {
    val totalSeconds = totalMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes} min"
        else -> "${totalSeconds}s"
    }
}
