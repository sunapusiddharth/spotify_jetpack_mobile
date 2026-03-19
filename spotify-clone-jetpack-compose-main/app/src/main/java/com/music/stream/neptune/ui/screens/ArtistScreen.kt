package com.music.stream.neptune.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.music.stream.neptune.data.entity.ArtistsModel
import com.music.stream.neptune.data.entity.SongsModel
import com.music.stream.neptune.di.Palette
import com.music.stream.neptune.ui.components.Loader
import com.music.stream.neptune.ui.theme.AppBackground
import com.music.stream.neptune.ui.theme.AppPalette
import com.music.stream.neptune.ui.viewmodel.ArtistViewModel
import com.music.stream.neptune.ui.viewmodel.PlayerViewModel
import kotlinx.coroutines.launch

@Composable
fun ArtistScreen(navController: NavController, artistId: String) {
    val artistViewModel: ArtistViewModel = hiltViewModel()
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val artistState by artistViewModel.artist.collectAsState()
    val artistSongsState by artistViewModel.artistSongs.collectAsState()

    Log.d("ArtistScreen", "artistId=$artistId")

    LaunchedEffect(artistId) {
        artistViewModel.loadArtist(artistId)
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(AppBackground.toArgb()))
    ) {
        when (artistState) {
            is Response.Loading -> Loader()
            is Response.Success -> {
                val artist = (artistState as Response.Success).data
                if (artist != null) {
                    val songs = when (artistSongsState) {
                        is Response.Success -> (artistSongsState as Response.Success).data
                        else -> artist.popular_songs
                    }
                    Log.d("ArtistScreen", "Artist loaded: ${artist.title}, songs=${songs.size}")
                    SumUpArtistScreen(
                        navController = navController,
                        artistViewModel = artistViewModel,
                        playerViewModel = playerViewModel,
                        artist = artist,
                        songs = songs
                    )
                } else {
                    Box(
                        Modifier.fillMaxSize().background(Color(AppBackground.toArgb())),
                        contentAlignment = Alignment.Center
                    ) { Text("Artist not found", color = Color.White) }
                }
            }
            is Response.Error -> {
                Log.d("ArtistScreen", "Error loading artist $artistId")
                Box(
                    Modifier.fillMaxSize().background(Color(AppBackground.toArgb())),
                    contentAlignment = Alignment.Center
                ) { Text("Failed to load artist", color = Color.White) }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class,
    ExperimentalFoundationApi::class)
@Composable
fun SumUpArtistScreen(
    navController: NavController,
    artistViewModel: ArtistViewModel,
    playerViewModel: PlayerViewModel,
    artist: ArtistsModel,
    songs: List<SongsModel>
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var dominantColor by remember { mutableStateOf(Color(AppBackground.toArgb())) }
    Palette().extractSecondColorFromCoverUrl(context = context, artist.image) { color ->
        dominantColor = color
    }

    val tabTitles = listOf("Popular Songs", "About")
    val pagerState = rememberPagerState(pageCount = { tabTitles.size })

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
                .verticalScroll(rememberScrollState())
        ) {

            // ── Hero Header ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                // Full-width artist photo
                GlideImage(
                    modifier = Modifier.fillMaxSize(),
                    model = artist.image,
                    contentScale = ContentScale.Crop,
                    loading = placeholder(R.drawable.placeholder),
                    failure = placeholder(R.drawable.placeholder),
                    contentDescription = artist.title
                )

                // Gradient overlay: bottom half fades to AppBackground
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(AppBackground.toArgb())
                                ),
                                startY = 120f
                            )
                        )
                )

                // Artist info at bottom of hero
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp, 0.dp, 20.dp, 16.dp)
                ) {
                    Text(
                        text = artist.title,
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (artist.monthly_listeners > 0) {
                            val formatted = when {
                                artist.monthly_listeners >= 1_000_000 ->
                                    "${artist.monthly_listeners / 1_000_000}M monthly listeners"
                                artist.monthly_listeners >= 1_000 ->
                                    "${artist.monthly_listeners / 1_000}K monthly listeners"
                                else -> "${artist.monthly_listeners} monthly listeners"
                            }
                            Text(
                                text = formatted,
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    // Play All button
                    if (songs.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        playerViewModel.startSongPlayback(
                                            queueSongs = songs,
                                            startIndex = 0,
                                            album = artist.title,
                                            context = context
                                        )
                                    }
                            ) {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.Black,
                                    painter = painterResource(id = R.drawable.play_svgrepo_com),
                                    contentDescription = "Play"
                                )
                            }

                            // Shuffle
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
                                        val shuffled = songs.shuffled()
                                        if (shuffled.isNotEmpty()) {
                                                playerViewModel.startSongPlayback(
                                                    queueSongs = shuffled,
                                                    startIndex = 0,
                                                    album = artist.title,
                                                    context = context
                                                )
                                        }
                                    }
                            ) {
                                Icon(
                                    modifier = Modifier.size(22.dp),
                                    tint = Color.White,
                                    painter = painterResource(id = R.drawable.ic_player_shuffle),
                                    contentDescription = "Shuffle"
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Tabs ─────────────────────────────────────────────────────────
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color(AppBackground.toArgb()),
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = Color(AppPalette.toArgb()),
                        height = 2.dp
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index, animationSpec = tween(300))
                            }
                        },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (pagerState.currentPage == index) Color.White else Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    )
                }
            }

            // ── Pager Content ─────────────────────────────────────────────────
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = true
            ) { page ->
                when (page) {
                    0 -> ArtistSongsTab(
                        songs = songs,
                        artist = artist,
                        artistViewModel = artistViewModel,
                        playerViewModel = playerViewModel,
                        context = context
                    )
                    1 -> ArtistAboutTab(artist = artist)
                }
            }

            Spacer(Modifier.height(120.dp))
        }
    }
}

// ─── Popular Songs Tab ────────────────────────────────────────────────────────

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ArtistSongsTab(
    songs: List<SongsModel>,
    artist: ArtistsModel,
    artistViewModel: ArtistViewModel,
    playerViewModel: PlayerViewModel,
    context: android.content.Context
) {
    val likedSongIds by playerViewModel.likedSongIds.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(AppBackground.toArgb()))
    ) {
        if (songs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No songs available", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            songs.forEachIndexed { index, song ->
                val isLiked = likedSongIds.contains(song.id)

                val isPlaying = song.id == artistViewModel.currentSongId.value
                val textColor = if (isPlaying) Color(AppPalette.toArgb()) else Color.White

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 8.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            playerViewModel.startSongPlayback(
                                queueSongs = songs,
                                startIndex = index,
                                album = artist.title,
                                context = context
                            )
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box {
                            GlideImage(
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                model = song.thumbnail,
                                contentScale = ContentScale.Crop,
                                loading = placeholder(R.drawable.placeholder),
                                failure = placeholder(R.drawable.placeholder),
                                contentDescription = ""
                            )
                            if (isPlaying) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_playing),
                                        tint = Color(AppPalette.toArgb()),
                                        contentDescription = "Playing",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        Column {
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
                                maxLines = 1
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                                .size(22.dp)
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
        }
    }
}

// ─── About Tab ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArtistAboutTab(artist: ArtistsModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(AppBackground.toArgb()))
            .padding(20.dp, 24.dp)
    ) {
        // Artist photo (circular) centered
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            GlideAboutPhoto(artist.image)
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = artist.title,
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        if (artist.monthly_listeners > 0) {
            Spacer(Modifier.height(6.dp))
            val formatted = when {
                artist.monthly_listeners >= 1_000_000 ->
                    "${artist.monthly_listeners / 1_000_000}M"
                artist.monthly_listeners >= 1_000 ->
                    "${artist.monthly_listeners / 1_000}K"
                else -> "${artist.monthly_listeners}"
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatted,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "monthly listeners",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
        }

        if (artist.genres.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Genres",
                color = Color.LightGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                artist.genres.forEach { genre ->
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                text = genre,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Color.White.copy(alpha = 0.12f)
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = Color.White.copy(alpha = 0.25f)
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        if (artist.popular_songs.isNotEmpty()) {
            Text(
                text = "Known For",
                color = Color.LightGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(10.dp))
            artist.popular_songs.take(3).forEach { song ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(AppPalette.toArgb()))
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = song.name,
                        color = Color.White,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun GlideAboutPhoto(imageUrl: String) {
    GlideImage(
        modifier = Modifier
            .size(160.dp)
            .clip(CircleShape),
        model = imageUrl,
        contentScale = ContentScale.Crop,
        loading = placeholder(R.drawable.placeholder),
        failure = placeholder(R.drawable.placeholder),
        contentDescription = ""
    )
}

// ─── Utility ──────────────────────────────────────────────────────────────────

fun formatMillis(ms: Int): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
