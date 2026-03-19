package com.music.stream.neptune.ui.screens

import android.util.Log
import java.util.Calendar
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import com.music.stream.neptune.data.entity.AlbumsModel
import com.music.stream.neptune.data.entity.ArtistsModel
import com.music.stream.neptune.data.entity.HomePageCardModel
import com.music.stream.neptune.data.entity.HomePageSectionModel
import com.music.stream.neptune.data.entity.PodcastModel
import com.music.stream.neptune.data.entity.RadioStationModel
import com.music.stream.neptune.data.entity.SongsModel
import com.music.stream.neptune.di.SongPlayer
import com.music.stream.neptune.ui.components.Loader
import com.music.stream.neptune.ui.components.UnavailableAudioBadge
import com.music.stream.neptune.ui.components.unavailableArtworkColorFilter
import com.music.stream.neptune.ui.navigation.Routes
import com.music.stream.neptune.ui.theme.AppBackground
import com.music.stream.neptune.ui.theme.GridBackground
import com.music.stream.neptune.ui.viewmodel.HomeViewModel
import com.music.stream.neptune.ui.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(navController: NavController) {
    val homeViewModel: HomeViewModel = hiltViewModel()
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val homePage by homeViewModel.homePage.collectAsState()
    val isLoadingNextHomePage by homeViewModel.isLoadingNextHomePage.collectAsState()
    val hasMoreHomePages by homeViewModel.hasMoreHomePages.collectAsState()
    val artists by homeViewModel.artists.collectAsState()
    val topStations by homeViewModel.topStations.collectAsState()
    val editorsPlayList by homeViewModel.editorsPlayList.collectAsState()
    val topScoringSongs by homeViewModel.topScoringSongs.collectAsState()
    val topPodcasts by homeViewModel.topPodcasts.collectAsState()

    val listState = rememberLazyListState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(AppBackground.toArgb()))
            .statusBarsPadding()
    ) {
        val homeSections = (homePage as? Response.Success)?.data?.results.orEmpty()
        val artistsResponse = (artists as? Response.Success)?.data.orEmpty()
        val stationsResponse = (topStations as? Response.Success)?.data.orEmpty()
        val editorsSection = (editorsPlayList as? Response.Success)?.data
        val topSongsResponse = (topScoringSongs as? Response.Success)?.data.orEmpty()
        val podcastsResponse = (topPodcasts as? Response.Success)?.data.orEmpty()
        val shouldLoadMore by remember(listState, hasMoreHomePages, isLoadingNextHomePage, homeSections) {
            derivedStateOf {
                val totalItemsCount = listState.layoutInfo.totalItemsCount
                val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                homeSections.isNotEmpty() &&
                    hasMoreHomePages &&
                    !isLoadingNextHomePage &&
                    totalItemsCount > 0 &&
                    lastVisibleItem >= totalItemsCount - 3
            }
        }

        LaunchedEffect(shouldLoadMore) {
            if (shouldLoadMore) {
                homeViewModel.loadNextHomePage()
            }
        }

        val isLoading = homePage is Response.Loading &&
            artists is Response.Loading &&
            topStations is Response.Loading &&
            editorsPlayList is Response.Loading &&
            topScoringSongs is Response.Loading &&
            topPodcasts is Response.Loading

        val hasAnyContent = homeSections.isNotEmpty() ||
            artistsResponse.isNotEmpty() ||
            stationsResponse.isNotEmpty() ||
            editorsSection?.cards?.isNotEmpty() == true ||
            topSongsResponse.isNotEmpty() ||
            podcastsResponse.isNotEmpty()

        when {
            isLoading -> {
                Log.d("homeMain", "loading real home feed...")
                Loader()
            }
            hasAnyContent -> {
                SumUpHomeScreen(
                    navController = navController,
                    playerViewModel = playerViewModel,
                    listState = listState,
                    homeSections = homeSections,
                    artists = artistsResponse,
                    topStations = stationsResponse,
                    editorsPlayList = editorsSection,
                    topScoringSongs = topSongsResponse,
                    topPodcasts = podcastsResponse,
                    isLoadingNextHomePage = isLoadingNextHomePage
                )
            }
            else -> {
                val error = (homePage as? Response.Error)?.error
                    ?: (artists as? Response.Error)?.error
                    ?: (topStations as? Response.Error)?.error
                    ?: (editorsPlayList as? Response.Error)?.error
                    ?: (topScoringSongs as? Response.Error)?.error
                    ?: (topPodcasts as? Response.Error)?.error
                Log.d("homeMain", "Error!! $error")
                Box(
                    modifier = Modifier.fillMaxSize().background(Color(AppBackground.toArgb())),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Failed to load. Check connection.", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SumUpHomeScreen(
    navController: NavController,
    playerViewModel: PlayerViewModel,
    listState: androidx.compose.foundation.lazy.LazyListState,
    homeSections: List<HomePageSectionModel>,
    artists: List<ArtistsModel>,
    topStations: List<RadioStationModel>,
    editorsPlayList: HomePageSectionModel?,
    topScoringSongs: List<SongsModel>,
    topPodcasts: List<PodcastModel>,
    isLoadingNextHomePage: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(AppBackground.toArgb())),
        state = listState
    ) {
        item {
            GreetingSection()
        }

        item {
            HomeArtists(artists = artists, navController)
        }

        item {
            HomePodcastsSection(navController = navController, podcasts = topPodcasts)
        }

        if (editorsPlayList?.cards?.isNotEmpty() == true) {
            item(key = "editors_playlist") {
                HomeCardSection(navController = navController, section = editorsPlayList, playerViewModel = playerViewModel)
            }
        }

        items(
            items = homeSections.filter { it.cards.isNotEmpty() },
            key = { section -> "home_section_${section.id.ifBlank { section.label + section.path }}" }
        ) { section ->
            HomeCardSection(navController = navController, section = section, playerViewModel = playerViewModel)
        }

        item {
            HomeSongsSection(songs = topScoringSongs, playerViewModel = playerViewModel)
        }

        item {
            HomeStationsSection(navController = navController, stations = topStations)
        }

        if (isLoadingNextHomePage) {
            item(key = "home_paging_loader") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Loader()
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
fun GreetingSection(name: String = "User") {
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        currentHour < 12 -> "Good Morning"
        currentHour < 17 -> "Good Afternoon"
        else -> "Good Evening"
    }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 12.dp, 16.dp, 0.dp)
    ) {
        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Have a Nice Day",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray,
                fontSize = 13.sp
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun HomeCardSection(navController: NavController, section: HomePageSectionModel, playerViewModel: PlayerViewModel) {
    val context = LocalContext.current
    val playableSongs = remember(section.cards) {
        section.cards.mapNotNull { it.song }.filter { it.hasPlayableAudio }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp, 16.dp, 16.dp, 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = section.label.ifBlank { "Recommended" },
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(
                enabled = section.path.isNotBlank() || section.id.isNotBlank(),
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                navigateToHomeSection(navController, section)
            }
        )
    }

    LazyRow(modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)) {
        items(section.cards.size) { index ->
            val card = section.cards[index]
            val isPlayableSong = card.song?.hasPlayableAudio == true
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .width(150.dp)
                    .clickable(
                        enabled = isPlayableSong,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        card.song?.let { song ->
                            val startIndex = playableSongs.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
                            playerViewModel.startSongPlayback(
                                queueSongs = playableSongs,
                                startIndex = startIndex,
                                album = section.label.ifBlank { section.id.ifBlank { "home" } },
                                context = context
                            )
                        }
                    }
            ) {
                Column {
                    Box {
                        GlideImage(
                            modifier = Modifier
                                .size(150.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                            model = card.image,
                            colorFilter = unavailableArtworkColorFilter(isPlayableSong),
                            loading = placeholder(R.drawable.placeholder),
                            failure = placeholder(R.drawable.placeholder),
                            contentDescription = card.title
                        )
                        if (!isPlayableSong) {
                            UnavailableAudioBadge(modifier = Modifier.align(Alignment.Center))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = card.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (card.subtitle.isNotEmpty()) {
                        Text(
                            text = card.subtitle,
                            color = Color.Gray,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (!isPlayableSong) {
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

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun HomeSongsSection(songs: List<SongsModel>, playerViewModel: PlayerViewModel) {
    if (songs.isEmpty()) return

    val context = LocalContext.current
    val playableSongs = remember(songs) { songs.filter { it.hasPlayableAudio } }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp, 16.dp, 16.dp, 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Top Tracks For You", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }

    LazyRow(modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)) {
        items(songs.size) { index ->
            val song = songs[index]
            val isPlayable = song.hasPlayableAudio
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .width(155.dp)
                    .clickable(
                        enabled = isPlayable,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        val startIndex = playableSongs.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
                        playerViewModel.startSongPlayback(
                            queueSongs = playableSongs,
                            startIndex = startIndex,
                            album = "Top Tracks For You",
                            context = context
                        )
                    }
            ) {
                Column {
                    Box {
                        GlideImage(
                            modifier = Modifier
                                .size(155.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                            model = song.thumbnail,
                            colorFilter = unavailableArtworkColorFilter(isPlayable),
                            loading = placeholder(R.drawable.placeholder),
                            failure = placeholder(R.drawable.placeholder),
                            contentDescription = song.title
                        )
                        if (!isPlayable) {
                            UnavailableAudioBadge(modifier = Modifier.align(Alignment.Center))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(text = song.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(
                        text = song.artists.joinToString(", ") { it.title },
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
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun HomeStationsSection(navController: NavController, stations: List<RadioStationModel>) {
    if (stations.isEmpty()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp, 16.dp, 16.dp, 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Top Stations", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }

    LazyRow(modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)) {
        items(stations.size) { index ->
            val station = stations[index]
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .width(150.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        navController.navigate(Routes.Radio.route)
                    }
            ) {
                Column {
                    GlideImage(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        model = station.coverUri,
                        loading = placeholder(R.drawable.placeholder),
                        failure = placeholder(R.drawable.placeholder),
                        contentDescription = station.name
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(text = station.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(text = station.country, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun HomePodcastsSection(navController: NavController, podcasts: List<PodcastModel>) {
    if (podcasts.isEmpty()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp, 16.dp, 16.dp, 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Top Podcasts", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }

    LazyRow(modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)) {
        items(podcasts.size) { index ->
            val podcast = podcasts[index]
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .width(150.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        navController.navigate("${Routes.PodcastDetail.route}/${podcast.id}")
                    }
            ) {
                Column {
                    GlideImage(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        model = podcast.image,
                        loading = placeholder(R.drawable.placeholder),
                        failure = placeholder(R.drawable.placeholder),
                        contentDescription = podcast.title
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(text = podcast.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(text = podcast.author, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                }
            }
        }
    }
}

private fun navigateToHomeCard(navController: NavController, card: HomePageCardModel) {
    when {
        card.path.startsWith("/artist") || card.type == "artists_card" -> {
            navController.navigate("${Routes.Artist.route}/${card.id}")
        }
        card.path.startsWith("/podcast") -> {
            navController.navigate("${Routes.PodcastDetail.route}/${card.id}")
        }
        card.path.contains("playlist/") -> {
            navController.navigate("${Routes.Playlist.route}/${card.id}")
        }
        card.path.startsWith("/playlist_collection") || card.type == "playlist_card" -> {
            navController.navigate("${Routes.Playlist.route}/${card.id}")
        }
        card.song?.album?.id?.isNotEmpty() == true -> {
            navController.navigate("${Routes.Album.route}/${card.song.album.id}")
        }
        else -> {
            navController.navigate("${Routes.Playlist.route}/${card.id}")
        }
    }
}

private fun navigateToHomeSection(navController: NavController, section: HomePageSectionModel) {
    when {
        section.path.contains("playlist/") -> {
            navController.navigate("${Routes.Playlist.route}/${extractTrailingId(section.path, section.id)}")
        }
        section.path.startsWith("/playlist_collection") -> {
            navController.navigate("${Routes.Playlist.route}/${extractTrailingId(section.path, section.id)}")
        }
        section.path.startsWith("/artist") -> {
            navController.navigate("${Routes.Artist.route}/${extractTrailingId(section.path, section.id)}")
        }
        section.path.startsWith("/podcast") -> {
            navController.navigate("${Routes.Podcast.route}")
        }
    }
}

private fun extractTrailingId(path: String, fallbackId: String): String {
    val trimmed = path.trim().trimEnd('/')
    val trailing = trimmed.substringAfterLast('/', "")
    return trailing.ifBlank { fallbackId }
}

// ─── Featured Banner (Hero Carousel) ─────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun FeaturedBanner(albums: List<AlbumsModel>, navController: NavController) {
    val featured = albums.take(5)
    if (featured.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { featured.size })

    // Auto-scroll every 4 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000L)
            val next = (pagerState.currentPage + 1) % pagerState.pageCount
            pagerState.animateScrollToPage(next, animationSpec = tween(600))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        HorizontalPager(state = pagerState) { page ->
            val album = featured[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { navController.navigate("${Routes.Album.route}/${album.id}") }
            ) {
                GlideImage(
                    modifier = Modifier.fillMaxSize(),
                    model = album.image,
                    contentScale = ContentScale.Crop,
                    loading = placeholder(R.drawable.placeholder),
                    failure = placeholder(R.drawable.placeholder),
                    contentDescription = album.title
                )
                // Dark gradient overlay at bottom
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.75f)
                                ),
                                startY = 80f
                            )
                        )
                )
                // Title + artist at bottom left
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp, 0.dp, 16.dp, 40.dp)
                ) {
                    Text(
                        text = album.title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (album.artists.isNotEmpty()) {
                        Text(
                            text = album.artists.take(3).joinToString(", ") { it.name },
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Indicator dots
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(featured.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .width(if (isSelected) 18.dp else 6.dp)
                        .height(6.dp)
                        .background(
                            if (isSelected) Color.White else Color.White.copy(alpha = 0.4f)
                        )
                )
            }
        }
    }
}

// ─── Home Grid ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun HomePlaylistGrid(navController: NavController, albums: List<AlbumsModel>) {
    val gridAlbums = albums.take(8)
    val chunkedAlbums = gridAlbums.chunked(2)

    Column(modifier = Modifier.padding(0.dp, 10.dp)) {
        repeat(chunkedAlbums.size) { rowIndex ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(15.dp, 5.dp, 7.dp, 0.dp)
                    .fillMaxWidth()
            ) {
                repeat(chunkedAlbums[rowIndex].size) { colIndex ->
                    val album = chunkedAlbums[rowIndex][colIndex]
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(GridBackground.toArgb()))
                            .width(180.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                Log.d("check", "navigating to album id=${album.id}")
                                navController.navigate("${Routes.Album.route}/${album.id}")
                            }
                    ) {
                        GlideImage(
                            modifier = Modifier.size(55.dp),
                            contentScale = ContentScale.Crop,
                            model = album.image,
                            loading = placeholder(R.drawable.placeholder),
                            failure = placeholder(R.drawable.placeholder),
                            contentDescription = "Album cover"
                        )
                        Text(
                            modifier = Modifier.padding(5.dp),
                            text = album.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ─── Albums Row ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun HomeAlbums(album: List<AlbumsModel>, navController: NavController) {
    val displayAlbums = album.reversed()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp, 16.dp, 16.dp, 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Albums", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }

    LazyRow(modifier = Modifier.padding(6.dp)) {
        items(displayAlbums.size) { index ->
            val a = displayAlbums[index]
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .width(150.dp)
                    .height(195.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        navController.navigate("${Routes.Album.route}/${a.id}")
                    }
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    GlideImage(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop,
                        model = a.image,
                        loading = placeholder(R.drawable.placeholder),
                        failure = placeholder(R.drawable.placeholder),
                        contentDescription = "Album"
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        fontSize = 13.sp,
                        text = a.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        fontSize = 12.sp,
                        text = a.artists.firstOrNull()?.name ?: "",
                        color = Color.LightGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ─── Artists Row ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun HomeArtists(artists: List<ArtistsModel>, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp, 16.dp, 16.dp, 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Best of Artists",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }

    LazyRow(modifier = Modifier.padding(6.dp)) {
        items(artists.size) { index ->
            val artist = artists[index]
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .width(130.dp)
                    .height(180.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        Log.d("check", "navigating to artist id=${artist.id}")
                        navController.navigate("${Routes.Artist.route}/${artist.id}")
                    }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    GlideImage(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        model = artist.image,
                        loading = placeholder(R.drawable.placeholder),
                        failure = placeholder(R.drawable.placeholder),
                        contentDescription = "Artist"
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = artist.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val listeners = artist.monthly_listeners
                    if (listeners > 0) {
                        val formatted = when {
                            listeners >= 1_000_000 -> "${listeners / 1_000_000}M"
                            listeners >= 1_000 -> "${listeners / 1_000}K"
                            else -> "$listeners"
                        }
                        Text(
                            text = "$formatted listeners",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ─── Discover Cards ───────────────────────────────────────────────────────────

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ImageCard(
    navController: NavController,
    allAlbums: List<AlbumsModel>,
    modifier: Modifier = Modifier
) {
    val albums = allAlbums.takeLast(3)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp, 16.dp, 16.dp, 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Discover", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
    Column(modifier = Modifier.padding(0.dp, 10.dp, 0.dp, 50.dp)) {
        repeat(albums.size) { index ->
            val a = albums[index]
            Card(
                shape = RoundedCornerShape(15.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                modifier = Modifier
                    .padding(15.dp)
                    .fillMaxWidth()
                    .height(380.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        navController.navigate("${Routes.Album.route}/${a.id}")
                    }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    GlideImage(
                        modifier = Modifier.fillMaxSize(),
                        model = a.image,
                        contentDescription = "Album card",
                        loading = placeholder(R.drawable.placeholder),
                        failure = placeholder(R.drawable.placeholder),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(AppBackground.toArgb())),
                                    startY = 150f
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(0.dp, 0.dp, 0.dp, 30.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = a.title,
                                style = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            if (a.artists.isNotEmpty()) {
                                Text(
                                    text = a.artists.take(2).joinToString(", ") { it.name },
                                    color = Color.LightGray,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}
