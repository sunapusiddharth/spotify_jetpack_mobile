package com.music.stream.neptune.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.music.stream.neptune.data.preferences.getAlbumsByIds
import com.music.stream.neptune.ui.components.Loader
import com.music.stream.neptune.ui.navigation.Routes
import com.music.stream.neptune.ui.theme.AppBackground
import com.music.stream.neptune.ui.viewmodel.HomeViewModel
import com.music.stream.neptune.ui.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(navController: NavController) {
    Scaffold(
        containerColor = Color(AppBackground.toArgb()),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        modifier = Modifier.padding(0.dp, 40.dp, 0.dp, 0.dp),
                        text = "Your Library",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(AppBackground.toArgb()),
                ),
                modifier = Modifier.height(120.dp)
            )
        }
    ) { padding ->
        val libraryViewModel: HomeViewModel = hiltViewModel()
        val playerViewModel: PlayerViewModel = hiltViewModel()
        val albums by libraryViewModel.albums.collectAsState()
        val likedSongIds by playerViewModel.likedSongIds.collectAsState()
        val likedAlbumIds by playerViewModel.likedAlbumIds.collectAsState()

        when (albums) {
            is Response.Loading -> {
                Log.d("LibraryScreen", "loading albums...")
                Loader()
            }
            is Response.Success -> {
                val albumsResponse = (albums as Response.Success).data
                SumUpLibraryScreen(padding, albumsResponse, navController, likedSongIds, likedAlbumIds)
            }
            is Response.Error -> {
                Log.d("LibraryScreen", "Error loading albums")
                Box(
                    Modifier.fillMaxSize().background(Color(AppBackground.toArgb())).padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Failed to load library", color = Color.White)
                }
            }
            else -> {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun SumUpLibraryScreen(
    padding: PaddingValues,
    albums: List<AlbumsModel>,
    navController: NavController,
    likedSongIds: Set<String>,
    likedAlbumIds: Set<String>
) {
    val libraryAlbums = getAlbumsByIds(likedAlbumIds, albums)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .background(Color(AppBackground.toArgb()))
    ) {
        Spacer(Modifier.height(8.dp))

        // ── Pinned: Liked Songs ───────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF6A1B9A), Color(0xFF4A148C))
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { navController.navigate("${Routes.Album.route}/liked_songs") }
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF9C27B0).copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Liked Songs",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Column {
                    Text(
                        text = "Liked Songs",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (likedSongIds.isEmpty()) "No songs liked yet"
                        else "${likedSongIds.size} song${if (likedSongIds.size != 1) "s" else ""}",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Saved Albums header ───────────────────────────────────────────
        if (libraryAlbums.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Saved Albums",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${libraryAlbums.size}",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
            Spacer(Modifier.height(10.dp))
        }

        // ── Album rows ────────────────────────────────────────────────────
        libraryAlbums.forEachIndexed { index, album ->
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF161620))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { navController.navigate("${Routes.Album.route}/${album.id}") }
                    .padding(10.dp)
            ) {
                GlideImage(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    model = album.image,
                    contentScale = ContentScale.Crop,
                    contentDescription = ""
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = album.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = album.artists.take(2).joinToString(", ") { it.name }
                            .ifEmpty { "Album" },
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (album.songs.isNotEmpty()) {
                        Text(
                            text = "${album.songs.size} songs",
                            color = Color(0xFF888888),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // ── Empty state ───────────────────────────────────────────────────
        if (libraryAlbums.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 32.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF161620))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No saved albums yet",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Browse albums and tap the save icon",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(130.dp))
    }
}
