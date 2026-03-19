package com.music.stream.neptune.ui.screens

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.music.stream.neptune.data.entity.SearchCardModel
import com.music.stream.neptune.data.entity.SongsModel
import com.music.stream.neptune.di.SongPlayer
import com.music.stream.neptune.ui.components.Loader
import com.music.stream.neptune.ui.components.StaggeredReveal
import com.music.stream.neptune.ui.components.UnavailableAudioBadge
import com.music.stream.neptune.ui.components.pressScale
import com.music.stream.neptune.ui.components.unavailableArtworkColorFilter
import com.music.stream.neptune.ui.navigation.Routes
import com.music.stream.neptune.ui.theme.AppBackground
import com.music.stream.neptune.ui.theme.AppPalette
import com.music.stream.neptune.ui.viewmodel.LocalSharedPlayerViewModel
import com.music.stream.neptune.ui.viewmodel.PlayerViewModel
import com.music.stream.neptune.ui.viewmodel.SearchViewModel
import kotlinx.coroutines.delay

enum class SearchFilter(val label: String, val apiType: String) {
    ALL("All", "null"),
    SONGS("Songs", "songs"),
    ALBUMS("Albums", "album"),
    ARTISTS("Artists", "artist")
}

@Composable
fun SearchScreen(navController: NavController) {
    val searchViewModel: SearchViewModel = hiltViewModel()
    val playerViewModel: PlayerViewModel = LocalSharedPlayerViewModel.current
    val songs by searchViewModel.songs.collectAsState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(AppBackground.toArgb()))
    ) {
        when (songs) {
            is Response.Loading -> {
                Log.d("SearchScreen", "loading songs pool...")
                Loader()
            }
            is Response.Success -> {
                val songsPool = (songs as Response.Success).data
                SumUpSearchScreen(
                    navController = navController,
                    localSongs = songsPool,
                    searchViewModel = searchViewModel,
                    playerViewModel = playerViewModel
                )
            }
            is Response.Error -> {
                SumUpSearchScreen(
                    navController = navController,
                    localSongs = emptyList(),
                    searchViewModel = searchViewModel,
                    playerViewModel = playerViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun SumUpSearchScreen(
    navController: NavController,
    localSongs: List<SongsModel>,
    searchViewModel: SearchViewModel,
    playerViewModel: PlayerViewModel
) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(SearchFilter.ALL) }
    val searchResults by searchViewModel.searchResults.collectAsState()

    LaunchedEffect(text, selectedFilter) {
        if (text.isNotBlank()) {
            delay(400)
            searchViewModel.search(text, selectedFilter.apiType)
        }
    }

    val localFiltered = remember(text, localSongs) {
        if (text.isBlank()) localSongs
        else localSongs.filter {
            it.name.lowercase().contains(text.lowercase()) ||
                    it.singer.lowercase().contains(text.lowercase()) ||
                    it.album.title.lowercase().contains(text.lowercase())
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(AppBackground.toArgb()))
            .statusBarsPadding()
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp, 16.dp, 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Search",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            SearchStickyBar(text) { newText ->
                text = newText
                if (newText.isBlank()) searchViewModel.search("")
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchFilter.values().forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter.label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.White,
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF2A2A2A),
                            labelColor = Color.White
                        )
                    )
                }
            }
        }

        if (text.isNotBlank() && searchResults != null) {
            when (val result = searchResults) {
                is Response.Loading -> {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("Searching...", color = Color.Gray) }
                    }
                }
                is Response.Success -> {
                    val cards = result.data.cards
                    val searchSongQueue = cards.filter { it.type == "songs" && it.hasPlayableAudio }.map { it.toSongsModel() }
                    if (cards.isEmpty()) {
                        item {
                            Box(
                                Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("No results for", color = Color.Gray, fontSize = 14.sp)
                                    Text(
                                        "\"$text\"",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else if (selectedFilter == SearchFilter.ALL) {
                        // ── ALL: Top Result + grouped sections ──────────────
                        val topResult = cards.first()
                        val songCards = cards.filter { it.type == "songs" }
                        val albumCards = cards.filter { it.type == "album" }
                        val artistCards = cards.filter { it.type == "artist" }

                        item {
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                TopResultCard(
                                    card = topResult,
                                    navController = navController,
                                    playerViewModel = playerViewModel,
                                    songQueue = searchSongQueue,
                                    context = context,
                                    revealIndex = 0,
                                    modifier = Modifier.weight(1f)
                                )
                                if (songCards.isNotEmpty()) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            "Songs",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        songCards.take(4).forEachIndexed { index, song ->
                                            SearchSongMiniRow(
                                                card = song,
                                                playerViewModel = playerViewModel,
                                                songQueue = searchSongQueue,
                                                context = context,
                                                revealIndex = index + 1
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (artistCards.isNotEmpty()) {
                            item {
                                Spacer(Modifier.height(16.dp))
                                SearchSectionHeader("Artists")
                                LazyRow(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(artistCards.size) { i ->
                                        SearchArtistCard(artistCards[i], navController, revealIndex = i)
                                    }
                                }
                            }
                        }

                        if (albumCards.isNotEmpty()) {
                            item {
                                Spacer(Modifier.height(16.dp))
                                SearchSectionHeader("Albums")
                                LazyRow(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(albumCards.size) { i ->
                                        SearchAlbumCard(albumCards[i], navController, revealIndex = i)
                                    }
                                }
                            }
                        }

                        if (songCards.size > 4) {
                            item {
                                Spacer(Modifier.height(16.dp))
                                SearchSectionHeader("All Songs")
                            }
                            items(songCards.size) { i ->
                                SearchCardRow(songCards[i], navController, playerViewModel, searchSongQueue, context, revealIndex = i)
                            }
                        }

                    } else {
                        // ── Filtered: flat list ─────────────────────────────
                        items(cards.size) { i ->
                            SearchCardRow(cards[i], navController, playerViewModel, searchSongQueue, context, revealIndex = i)
                        }
                    }
                }
                is Response.Error -> {
                    items(localFiltered.size) { i ->
                        LocalSongRow(localFiltered[i], localFiltered, playerViewModel, context, revealIndex = i)
                    }
                }
                else -> {}
            }
        } else if (text.isBlank()) {
            items(localFiltered.size) { i ->
                LocalSongRow(localFiltered[i], localFiltered, playerViewModel, context, revealIndex = i)
            }
        }

        item { Spacer(modifier = Modifier.height(130.dp)) }
    }
}

// ─── Top Result Card ──────────────────────────────────────────────────────────

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun TopResultCard(
    card: SearchCardModel,
    navController: NavController,
    playerViewModel: PlayerViewModel,
    songQueue: List<SongsModel>,
    context: android.content.Context,
    revealIndex: Int,
    modifier: Modifier = Modifier
) {
    val isPlayableSong = card.type != "songs" || card.hasPlayableAudio
    val interactionSource = remember { MutableInteractionSource() }

    StaggeredReveal(index = revealIndex, modifier = modifier) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1E1E1E))
                .pressScale(interactionSource, pressedScale = 0.97f)
                .clickable(
                    enabled = isPlayableSong,
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    when (card.type) {
                        "album" -> navController.navigate("${Routes.Album.route}/${card.id}")
                        "artist" -> navController.navigate("${Routes.Artist.route}/${card.id}")
                        "songs" -> if (card.hasPlayableAudio) {
                            val queue = if (songQueue.isNotEmpty()) songQueue else listOf(card.toSongsModel())
                            val startIndex = queue.indexOfFirst { it.id == card.id }.coerceAtLeast(0)
                            playerViewModel.startSongPlayback(
                                queueSongs = queue,
                                startIndex = startIndex,
                                album = "Search",
                                context = context
                            )
                        }
                    }
                }
                .padding(12.dp)
        ) {
            Column {
                Text(
                    "Top Result",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(10.dp))
                Box {
                    GlideImage(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(if (card.type == "artist") CircleShape else RoundedCornerShape(8.dp)),
                        model = card.image,
                        contentScale = ContentScale.Crop,
                        colorFilter = unavailableArtworkColorFilter(isPlayableSong),
                        loading = placeholder(R.drawable.placeholder),
                        failure = placeholder(R.drawable.placeholder),
                        contentDescription = card.name
                    )
                    if (card.type == "songs" && !card.hasPlayableAudio) {
                        UnavailableAudioBadge(modifier = Modifier.align(Alignment.Center))
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = card.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (card.artist.isNotEmpty()) {
                        Text(
                            text = card.artist,
                            color = Color.Gray,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Text("·", color = Color.Gray, fontSize = 11.sp)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF333333))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = card.type.replaceFirstChar { it.uppercase() },
                            color = Color.LightGray,
                            fontSize = 10.sp
                        )
                    }
                }
                if (card.type == "songs" && !card.hasPlayableAudio) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Audio unavailable",
                        color = Color(0xFFBDBDBD),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ─── Song Mini Row ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SearchSongMiniRow(
    card: SearchCardModel,
    playerViewModel: PlayerViewModel,
    songQueue: List<SongsModel>,
    context: android.content.Context,
    revealIndex: Int
) {
    val isPlayable = card.hasPlayableAudio
    val interactionSource = remember { MutableInteractionSource() }

    StaggeredReveal(index = revealIndex) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .pressScale(interactionSource, pressedScale = 0.98f)
                .clickable(
                    enabled = isPlayable,
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    if (isPlayable) {
                        val queue = if (songQueue.isNotEmpty()) songQueue else listOf(card.toSongsModel())
                        val startIndex = queue.indexOfFirst { it.id == card.id }.coerceAtLeast(0)
                        playerViewModel.startSongPlayback(
                            queueSongs = queue,
                            startIndex = startIndex,
                            album = "Search",
                            context = context
                        )
                    }
                }
                .padding(4.dp)
        ) {
            Box {
                GlideImage(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    model = card.image,
                    contentScale = ContentScale.Crop,
                    colorFilter = unavailableArtworkColorFilter(isPlayable),
                    loading = placeholder(R.drawable.placeholder),
                    failure = placeholder(R.drawable.placeholder),
                    contentDescription = ""
                )
                if (!isPlayable) {
                    UnavailableAudioBadge(modifier = Modifier.align(Alignment.Center))
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.name,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = card.artist,
                    color = Color.Gray,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!isPlayable) {
                    Text(
                        text = "Audio unavailable",
                        color = Color(0xFFBDBDBD),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ─── Section Header ───────────────────────────────────────────────────────────

@Composable
fun SearchSectionHeader(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

// ─── Artist Card ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SearchArtistCard(card: SearchCardModel, navController: NavController, revealIndex: Int) {
    val interactionSource = remember { MutableInteractionSource() }
    StaggeredReveal(index = revealIndex) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(100.dp)
                .pressScale(interactionSource, pressedScale = 0.96f)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { navController.navigate("${Routes.Artist.route}/${card.id}") }
        ) {
            GlideImage(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape),
                model = card.image,
                contentScale = ContentScale.Crop,
                loading = placeholder(R.drawable.placeholder),
                failure = placeholder(R.drawable.placeholder),
                contentDescription = card.name
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = card.name,
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Artist",
                color = Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}

// ─── Album Card ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SearchAlbumCard(card: SearchCardModel, navController: NavController, revealIndex: Int) {
    val interactionSource = remember { MutableInteractionSource() }
    StaggeredReveal(index = revealIndex) {
        Column(
            modifier = Modifier
                .width(110.dp)
                .pressScale(interactionSource, pressedScale = 0.96f)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { navController.navigate("${Routes.Album.route}/${card.id}") }
        ) {
            GlideImage(
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(6.dp)),
                model = card.image,
                contentScale = ContentScale.Crop,
                loading = placeholder(R.drawable.placeholder),
                failure = placeholder(R.drawable.placeholder),
                contentDescription = card.name
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = card.name,
                color = Color.White,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = card.artist,
                color = Color.Gray,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─── Full Search Card Row ─────────────────────────────────────────────────────

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SearchCardRow(
    card: SearchCardModel,
    navController: NavController,
    playerViewModel: PlayerViewModel,
    songQueue: List<SongsModel>,
    context: android.content.Context,
    revealIndex: Int
) {
    val isPlayableSong = card.type != "songs" || card.hasPlayableAudio
    val interactionSource = remember { MutableInteractionSource() }

    StaggeredReveal(index = revealIndex) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 6.dp)
                .pressScale(interactionSource, pressedScale = 0.985f)
                .clickable(
                    enabled = isPlayableSong,
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    when (card.type) {
                        "album" -> navController.navigate("${Routes.Album.route}/${card.id}")
                        "artist" -> navController.navigate("${Routes.Artist.route}/${card.id}")
                        "songs" -> if (card.hasPlayableAudio) {
                            val queue = if (songQueue.isNotEmpty()) songQueue else listOf(card.toSongsModel())
                            val startIndex = queue.indexOfFirst { it.id == card.id }.coerceAtLeast(0)
                            playerViewModel.startSongPlayback(
                                queueSongs = queue,
                                startIndex = startIndex,
                                album = "Search",
                                context = context
                            )
                        }
                    }
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(modifier = Modifier.padding(end = 10.dp)) {
                    GlideImage(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(if (card.type == "artist") CircleShape else RoundedCornerShape(6.dp)),
                        model = card.image,
                        contentScale = ContentScale.Crop,
                        colorFilter = unavailableArtworkColorFilter(isPlayableSong),
                        failure = placeholder(R.drawable.placeholder),
                        loading = placeholder(R.drawable.placeholder),
                        contentDescription = ""
                    )
                    if (card.type == "songs" && !card.hasPlayableAudio) {
                        UnavailableAudioBadge(modifier = Modifier.align(Alignment.Center))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.name,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = when (card.type) {
                            "album" -> "Album · ${card.artist}"
                            "artist" -> "Artist"
                            "songs" -> "Song · ${card.artist}"
                            else -> card.type
                        },
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                    if (card.type == "songs" && !card.hasPlayableAudio) {
                        Text(
                            text = "Audio unavailable",
                            color = Color(0xFFBDBDBD),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ─── Local Song Row ───────────────────────────────────────────────────────────

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun LocalSongRow(
    song: SongsModel,
    localQueue: List<SongsModel>,
    playerViewModel: PlayerViewModel,
    context: android.content.Context,
    revealIndex: Int
) {
    val likedSongIds by playerViewModel.likedSongIds.collectAsState()
    val isLiked = likedSongIds.contains(song.id)
    val interactionSource = remember { MutableInteractionSource() }
    val likeScale by animateFloatAsState(
        targetValue = if (isLiked) 1.16f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 420f),
        label = "searchLikeScale"
    )

    val isPlaying = song.id == playerViewModel.currentSongId.value
    val isPlayable = song.hasPlayableAudio
    val textColor = if (isPlaying) Color(AppPalette.toArgb()) else Color.White

    StaggeredReveal(index = revealIndex) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 8.dp)
                .pressScale(interactionSource, pressedScale = 0.985f)
                .clickable(
                    enabled = isPlayable,
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    val startIndex = localQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
                    playerViewModel.startSongPlayback(
                        queueSongs = localQueue,
                        startIndex = startIndex,
                        album = song.album.title.ifBlank { "Search" },
                        context = context
                    )
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(modifier = Modifier.padding(end = 10.dp)) {
                    GlideImage(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        model = song.thumbnail,
                        contentScale = ContentScale.Crop,
                        colorFilter = unavailableArtworkColorFilter(isPlayable),
                        failure = placeholder(R.drawable.placeholder),
                        loading = placeholder(R.drawable.placeholder),
                        contentDescription = ""
                    )
                    if (!isPlayable) {
                        UnavailableAudioBadge(modifier = Modifier.align(Alignment.Center))
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
                    Text(text = song.singer, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
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
            }
            Icon(
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer(scaleX = likeScale, scaleY = likeScale)
                    .padding(start = 4.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        playerViewModel.toggleSongLike(song.id)
                    },
                painter = if (isLiked) painterResource(R.drawable.added) else painterResource(R.drawable.ic_add),
                tint = if (isLiked) Color.White else Color.Gray,
                contentDescription = ""
            )
        }
    }
}

private fun SearchCardModel.toSongsModel(): SongsModel {
    return SongsModel(
        id = id,
        name = name,
        artists = listOf(SongsModel.ArtistRef(title = artist)),
        thumbnail = image,
        preview_url = play_url,
        s3link = s3link
    )
}

// ─── Search Bar ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchStickyBar(text: String, onTextChange: (String) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clip(RoundedCornerShape(10.dp))
            .height(55.dp)
            .background(Color.White)
            .padding(10.dp, 0.dp)
    ) {
        Icon(
            painterResource(id = R.drawable.ic_search_big),
            tint = Color.Black,
            contentDescription = ""
        )
        TextField(
            enabled = true,
            value = text,
            textStyle = TextStyle.Default.copy(
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight(500)
            ),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.Black
            ),
            singleLine = true,
            onValueChange = onTextChange,
            placeholder = {
                Text(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    text = "What do you want to listen to?"
                )
            }
        )
    }
}
