package com.music.stream.neptune.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.music.stream.neptune.data.api.Response
import com.music.stream.neptune.data.entity.SongsModel
import com.music.stream.neptune.ui.components.UnavailableAudioBadge
import com.music.stream.neptune.ui.components.Loader
import com.music.stream.neptune.ui.components.unavailableArtworkColorFilter
import com.music.stream.neptune.ui.navigation.Routes
import com.music.stream.neptune.ui.theme.AppBackground
import com.music.stream.neptune.ui.viewmodel.HomeViewModel
import com.music.stream.neptune.ui.viewmodel.LocalSharedPlayerViewModel
import com.music.stream.neptune.ui.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailableTracksScreen() {
    val homeViewModel: HomeViewModel = hiltViewModel()
    val playerViewModel: PlayerViewModel = LocalSharedPlayerViewModel.current
    val bgColor = Color(AppBackground.toArgb())
    val availableSongsPage by homeViewModel.availableSongsPage.collectAsState()
    val context = LocalContext.current
    val initialListPosition = remember {
        ScreenScrollMemory.lazyListPositions[Routes.AvailableTracks.route] ?: SavedLazyListPosition()
    }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState(
        initialFirstVisibleItemIndex = initialListPosition.index,
        initialFirstVisibleItemScrollOffset = initialListPosition.offset
    )

    androidx.compose.runtime.DisposableEffect(listState) {
        onDispose {
            ScreenScrollMemory.lazyListPositions[Routes.AvailableTracks.route] = SavedLazyListPosition(
                index = listState.firstVisibleItemIndex,
                offset = listState.firstVisibleItemScrollOffset
            )
        }
    }

    LaunchedEffect(Unit) {
        homeViewModel.fetchAvailableSongs(skip = 0, limit = 50)
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        modifier = Modifier.padding(0.dp, 40.dp, 0.dp, 0.dp),
                        text = "Available Tracks",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor),
                modifier = Modifier.height(120.dp)
            )
        }
    ) { padding ->
        when (availableSongsPage) {
            is Response.Loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) { Loader() }
            }
            is Response.Error -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Failed to load tracks", color = Color.White)
                }
            }
            is Response.Success -> {
                val displaySongs = (availableSongsPage as Response.Success).data.results
                val playableSongs = remember(displaySongs) { displaySongs.filter { it.hasPlayableAudio } }
                Log.d("AvailableTracks", "Loaded ${displaySongs.size} songs")

                if (displaySongs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No available songs returned from API", color = Color.White)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .background(bgColor),
                        state = listState
                    ) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "All Songs",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${displaySongs.size} tracks",
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                        }

                        itemsIndexed(displaySongs, key = { _, song -> song.id.ifBlank { song.name } }) { index, song ->
                            AvailableTrackRow(
                                song = song,
                                index = index + 1,
                                onPlay = {
                                    val startIndex = playableSongs.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
                                    playerViewModel.startSongPlayback(
                                        queueSongs = playableSongs,
                                        startIndex = startIndex,
                                        album = "available_tracks",
                                        context = context
                                    )
                                }
                            )
                        }

                        item {
                            Spacer(Modifier.height(130.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun AvailableTrackRow(song: SongsModel, index: Int, onPlay: () -> Unit) {
    val isPlayable = song.hasPlayableAudio

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (index % 2 == 0) Color(0xFF161620) else Color.Transparent)
            .clickable(
                enabled = isPlayable,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onPlay()
            }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$index",
            color = Color.Gray,
            fontSize = 13.sp,
            modifier = Modifier.width(28.dp)
        )
        Box {
            GlideImage(
                model = song.thumbnail,
                contentDescription = song.name,
                contentScale = ContentScale.Crop,
                colorFilter = unavailableArtworkColorFilter(isPlayable),
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
            if (!isPlayable) {
                UnavailableAudioBadge(modifier = Modifier.align(Alignment.Center))
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = song.name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artists.joinToString(", ") { it.title }.ifBlank { "Unknown artist" },
                color = Color.Gray,
                fontSize = 12.sp,
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
        if (song.duration > 0) {
            Text(
                text = formatMillis(song.duration),
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}
