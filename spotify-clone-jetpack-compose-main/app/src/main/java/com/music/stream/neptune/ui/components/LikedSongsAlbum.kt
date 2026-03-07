package com.music.stream.neptune.ui.components

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.music.stream.neptune.R
import com.music.stream.neptune.data.entity.SongsModel
import com.music.stream.neptune.data.preferences.addLikedSongId
import com.music.stream.neptune.data.preferences.getLikedSongIds
import com.music.stream.neptune.data.preferences.getSongsByIds
import com.music.stream.neptune.data.preferences.isSongLiked
import com.music.stream.neptune.data.preferences.removeLikedSongId
import com.music.stream.neptune.di.SongPlayer
import com.music.stream.neptune.ui.theme.AppBackground
import com.music.stream.neptune.ui.theme.AppPalette
import com.music.stream.neptune.ui.viewmodel.AlbumViewModel

/**
 * Liked Songs pseudo-album screen.
 * No longer needs the albums list — uses a purple gradient placeholder instead.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun LikedSongsScreen(
    songs: List<SongsModel>,
    navController: NavController,
    context: Context
) {
    val albumViewModel: AlbumViewModel = hiltViewModel()
    val likedSongIds = getLikedSongIds(context)
    var likedSongs by remember { mutableStateOf(emptyList<SongsModel>()) }

    val likeState = albumViewModel.likeState.value
    LaunchedEffect(likeState) {
        likedSongs = getSongsByIds(getLikedSongIds(context), songs).sortedBy { it.name }
    }
    // Initial load
    LaunchedEffect(Unit) {
        likedSongs = getSongsByIds(likedSongIds, songs).sortedBy { it.name }
    }

    val gradientStart = Color(0xFF4A0880)

    Scaffold(
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
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(gradientStart, Color(AppBackground.toArgb())),
                            startY = -100f
                        )
                    ),
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.padding(25.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF6A11CB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Liked Songs",
                            tint = Color.White,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.padding(20.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(25.dp, 0.dp)
                ) {
                    Column(modifier = Modifier.width(200.dp)) {
                        Text(
                            text = "Liked Songs",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${likedSongs.size} songs",
                            color = Color.Gray,
                            letterSpacing = 0.sp,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (!albumViewModel.currentSongPlayingState.value && likedSongs.isNotEmpty()) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color.White)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    SongPlayer.playSong(likedSongs[0].url, context)
                                    albumViewModel.updateSongState(
                                        likedSongs[0].thumbnail,
                                        likedSongs[0].name,
                                        likedSongs[0].singer,
                                        true,
                                        likedSongs[0].id,
                                        0,
                                        "Liked Songs"
                                    )
                                }
                        ) {
                            Icon(
                                modifier = Modifier.size(25.dp),
                                tint = Color.Black,
                                painter = painterResource(id = R.drawable.play_svgrepo_com),
                                contentDescription = "Play"
                            )
                        }
                    }
                }
            }

            if (likedSongs.isNotEmpty()) {
                repeat(likedSongs.size) { index ->
                    var isLiked by remember {
                        mutableStateOf(isSongLiked(context, likedSongs[index].id))
                    }
                    val song = likedSongs[index]
                    val currentPlayingIndicatorColor =
                        if (song.id == albumViewModel.currentSongId.value) Color(AppPalette.toArgb())
                        else Color.White

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp, 8.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                SongPlayer.playSong(song.url, context)
                                albumViewModel.updateSongState(
                                    song.thumbnail,
                                    song.name,
                                    song.singer,
                                    true,
                                    song.id,
                                    index,
                                    "Liked Songs"
                                )
                            }
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.width(280.dp)
                        ) {
                            GlideImage(
                                modifier = Modifier
                                    .padding(0.dp, 0.dp, 10.dp, 0.dp)
                                    .size(50.dp),
                                model = song.thumbnail,
                                contentScale = ContentScale.Crop,
                                failure = placeholder(R.drawable.placeholder),
                                loading = placeholder(R.drawable.placeholder),
                                contentDescription = ""
                            )
                            Column {
                                Text(
                                    text = song.name,
                                    color = currentPlayingIndicatorColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = song.singer,
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Icon(
                            modifier = Modifier
                                .size(20.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (isLiked) removeLikedSongId(context, song.id)
                                    else addLikedSongId(context, song.id)
                                    albumViewModel.updateLikeState(!albumViewModel.likeState.value)
                                },
                            painter = if (isLiked) painterResource(id = R.drawable.added)
                            else painterResource(id = R.drawable.ic_add),
                            tint = Color.LightGray,
                            contentDescription = ""
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.padding(20.dp, 20.dp).fillMaxWidth()) {
                    Snackbar(showMessage = "No liked songs yet. Heart a song to save it.")
                }
            }

            Spacer(modifier = Modifier.padding(80.dp))
        }
    }
}
