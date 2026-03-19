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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.music.stream.neptune.data.entity.UserHistoryEntityModel
import com.music.stream.neptune.di.SongPlayer
import com.music.stream.neptune.ui.components.Loader
import com.music.stream.neptune.ui.components.StaggeredReveal
import com.music.stream.neptune.ui.components.UnavailableAudioBadge
import com.music.stream.neptune.ui.components.pressScale
import com.music.stream.neptune.ui.components.unavailableArtworkColorFilter
import com.music.stream.neptune.ui.navigation.Routes
import com.music.stream.neptune.ui.theme.AppBackground
import com.music.stream.neptune.ui.theme.GridBackground
import com.music.stream.neptune.ui.viewmodel.HomeViewModel
import com.music.stream.neptune.ui.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay

private val HomeSectionTitleSize = 16.sp
private val HomeSectionHeaderBottomGap = 9.5.dp
private val HomeStandardCardSize = 98.dp
private val HomeStandardCardWidth = 112.dp
private val HomeSongCardSize = 101.dp
private val HomeSongCardWidth = 114.dp
private val HomeArtistCardSize = 85.dp
private val HomeArtistCardWidth = 96.dp

@Composable
fun HomeScreen(navController: NavController) {
    val homeViewModel: HomeViewModel = hiltViewModel()
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val homePage by homeViewModel.homePage.collectAsState()
    val isLoadingNextHomePage by homeViewModel.isLoadingNextHomePage.collectAsState()
    val hasMoreHomePages by homeViewModel.hasMoreHomePages.collectAsState()
    val artists by homeViewModel.artists.collectAsState()
    val topStations by homeViewModel.topStations.collectAsState()
    val topScoringSongs by homeViewModel.topScoringSongs.collectAsState()
    val topPodcasts by homeViewModel.topPodcasts.collectAsState()
    val historyEntries by homeViewModel.historyEntries.collectAsState()

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
        val topSongsResponse = (topScoringSongs as? Response.Success)?.data.orEmpty()
        val podcastsResponse = (topPodcasts as? Response.Success)?.data.orEmpty()
        val historyResponse = (historyEntries as? Response.Success)?.data.orEmpty()
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
            topScoringSongs is Response.Loading &&
            topPodcasts is Response.Loading

        val hasAnyContent = homeSections.isNotEmpty() ||
            historyResponse.isNotEmpty() ||
            artistsResponse.isNotEmpty() ||
            stationsResponse.isNotEmpty() ||
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
                    historyEntries = historyResponse,
                    homeSections = homeSections,
                    artists = artistsResponse,
                    topStations = stationsResponse,
                    topScoringSongs = topSongsResponse,
                    topPodcasts = podcastsResponse,
                    isLoadingNextHomePage = isLoadingNextHomePage
                )
            }
            else -> {
                val error = (homePage as? Response.Error)?.error
                    ?: (artists as? Response.Error)?.error
                    ?: (topStations as? Response.Error)?.error
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
    historyEntries: List<UserHistoryEntityModel>,
    homeSections: List<HomePageSectionModel>,
    artists: List<ArtistsModel>,
    topStations: List<RadioStationModel>,
    topScoringSongs: List<SongsModel>,
    topPodcasts: List<PodcastModel>,
    isLoadingNextHomePage: Boolean
) {
    val displaySections = remember(homeSections) { homeSections.filter { it.cards.isNotEmpty() } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(AppBackground.toArgb())),
        state = listState
    ) {
        item {
            StaggeredReveal(index = 0) {
                HomeArtists(artists = artists, navController)
            }
        }

        if (historyEntries.isNotEmpty()) {
            item(key = "history_entries") {
                StaggeredReveal(index = 1) {
                    HomeHistorySection(
                        navController = navController,
                        playerViewModel = playerViewModel,
                        entries = historyEntries
                    )
                }
            }
        }

        item {
            StaggeredReveal(index = 2) {
                HomePodcastsSection(navController = navController, podcasts = topPodcasts)
            }
        }

        items(
            count = displaySections.size,
            key = { index -> "home_section_${homeSectionUiKey(displaySections[index])}" }
        ) { index ->
            val section = displaySections[index]
            val revealIndex = 3 + index
            StaggeredReveal(index = revealIndex) {
                HomeCardSection(navController = navController, section = section, playerViewModel = playerViewModel)
            }
        }

        item {
            StaggeredReveal(index = 13) {
                HomeSongsSection(title = "Top Tracks For You", songs = topScoringSongs, playerViewModel = playerViewModel)
            }
        }

        item {
            StaggeredReveal(index = 14) {
                HomeStationsSection(navController = navController, stations = topStations)
            }
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

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun HomeHistorySection(
    navController: NavController,
    playerViewModel: PlayerViewModel,
    entries: List<UserHistoryEntityModel>
) {
    if (entries.isEmpty()) return

    val context = LocalContext.current

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp, 16.dp, 16.dp, 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "History", color = Color.White, fontSize = HomeSectionTitleSize, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(HomeSectionHeaderBottomGap))

        LazyRow(modifier = Modifier.padding(horizontal = 6.dp, vertical = 0.dp)) {
            items(entries.size) { index ->
                val entry = entries[index]
                val isPlayable = entry.isSong || entry.isRadioStation || entry.isPodcastEpisode
                val interactionSource = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .width(HomeStandardCardWidth)
                        .height(160.dp)
                        .pressScale(interactionSource, pressedScale = 0.94f)
                        .clickable(
                            enabled = isPlayable || entry.isPodcast || entry.isPlaylist || entry.isPlaylistCollection,
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            when {
                                entry.isSong -> playerViewModel.playSongFromHistory(entry, context)
                                entry.isRadioStation -> playerViewModel.playRadioFromHistory(entry, context)
                                entry.isPodcastEpisode -> playerViewModel.playPodcastEpisodeFromHistory(entry, context)
                                entry.isPodcast -> navController.navigate("${Routes.PodcastDetail.route}/${entry.entityId}")
                                entry.isPlaylist || entry.isPlaylistCollection -> navController.navigate("${Routes.Playlist.route}/${entry.entityId}")
                            }
                        }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        GlideImage(
                            modifier = Modifier
                                .size(HomeStandardCardSize)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                            model = entry.image,
                            loading = placeholder(R.drawable.placeholder),
                            failure = placeholder(R.drawable.placeholder),
                            contentDescription = entry.title
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = entry.title,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            lineHeight = 15.sp,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = entry.subtitle.ifBlank { entry.entityType.replace('_', ' ') },
                            color = Color.Gray,
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private fun homeSectionUiKey(section: HomePageSectionModel): String {
    return listOf(
        section.id,
        section.path,
        section.label,
        section.cardType,
        section.cards.firstOrNull()?.id.orEmpty()
    ).joinToString("|")
}

@Composable
fun GreetingSection() {
    Spacer(modifier = Modifier.height(6.dp))
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun HomeCardSection(navController: NavController, section: HomePageSectionModel, playerViewModel: PlayerViewModel) {
    val context = LocalContext.current
    val playableSongs = remember(section.cards) {
        section.cards.mapNotNull { it.song }.filter { it.hasPlayableAudio }
    }
    val titleClickable = section.path.isNotBlank() || section.id.isNotBlank()
    val showsPlaylistCollectionIndicator = remember(section) { sectionNavigatesToPlaylistCollection(section) }
    val titleInteractionSource = remember { MutableInteractionSource() }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp, 16.dp, 16.dp, 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(
                    enabled = titleClickable,
                    interactionSource = titleInteractionSource,
                    indication = null
                ) {
                    navigateToHomeSection(navController, section)
                }
            ) {
                Text(
                    text = section.label.ifBlank { "Recommended" },
                    color = Color.White,
                    fontSize = HomeSectionTitleSize,
                    fontWeight = FontWeight.Bold
                )
                if (showsPlaylistCollectionIndicator) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Opens playlist collection",
                        tint = Color.White.copy(alpha = 0.72f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(HomeSectionHeaderBottomGap))

        LazyRow(modifier = Modifier.padding(horizontal = 6.dp, vertical = 0.dp)) {
            items(section.cards.size) { index ->
                val card = section.cards[index]
                val isPlayableSong = card.song?.hasPlayableAudio == true
                val interactionSource = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .width(HomeStandardCardWidth)
                        .height(172.dp)
                        .pressScale(interactionSource, pressedScale = 0.94f)
                        .clickable(
                            enabled = isPlayableSong,
                            interactionSource = interactionSource,
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
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box {
                            GlideImage(
                                modifier = Modifier
                                    .size(HomeStandardCardSize)
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
                            modifier = Modifier.fillMaxWidth(),
                            text = card.title,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            lineHeight = 15.sp,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (card.subtitle.isNotEmpty() && card.song == null) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = card.subtitle,
                                color = Color.Gray,
                                fontSize = 11.sp,
                                lineHeight = 13.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (!isPlayableSong) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
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
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun HomeSongsSection(songs: List<SongsModel>, playerViewModel: PlayerViewModel) {
    HomeSongsSection(title = "Top Tracks For You", songs = songs, playerViewModel = playerViewModel)
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun HomeSongsSection(title: String, songs: List<SongsModel>, playerViewModel: PlayerViewModel) {
    if (songs.isEmpty()) return

    val context = LocalContext.current
    val playableSongs = remember(songs) { songs.filter { it.hasPlayableAudio } }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp, 16.dp, 16.dp, 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, color = Color.White, fontSize = HomeSectionTitleSize, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(HomeSectionHeaderBottomGap))

        LazyRow(modifier = Modifier.padding(horizontal = 6.dp, vertical = 0.dp)) {
            items(songs.size) { index ->
                val song = songs[index]
                val isPlayable = song.hasPlayableAudio
                val interactionSource = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .width(HomeSongCardWidth)
                        .height(150.dp)
                        .pressScale(interactionSource, pressedScale = 0.94f)
                        .clickable(
                            enabled = isPlayable,
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            val startIndex = playableSongs.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
                            playerViewModel.startSongPlayback(
                                queueSongs = playableSongs,
                                startIndex = startIndex,
                                album = title,
                                context = context
                            )
                        }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box {
                            GlideImage(
                                modifier = Modifier
                                    .size(HomeSongCardSize)
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
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = song.title,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            lineHeight = 15.sp,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!isPlayable) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
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
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun HomeStationsSection(navController: NavController, stations: List<RadioStationModel>) {
    if (stations.isEmpty()) return

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp, 16.dp, 16.dp, 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Top Stations", color = Color.White, fontSize = HomeSectionTitleSize, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(HomeSectionHeaderBottomGap))

        LazyRow(modifier = Modifier.padding(horizontal = 6.dp, vertical = 0.dp)) {
            items(stations.size) { index ->
                val station = stations[index]
                val interactionSource = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .width(HomeStandardCardWidth)
                        .height(150.dp)
                        .pressScale(interactionSource, pressedScale = 0.94f)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            navController.navigate(Routes.Radio.route)
                        }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        GlideImage(
                            modifier = Modifier
                                .size(HomeStandardCardSize)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                            model = station.coverUri,
                            loading = placeholder(R.drawable.placeholder),
                            failure = placeholder(R.drawable.placeholder),
                            contentDescription = station.name
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = station.name,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            lineHeight = 15.sp,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = station.country,
                            color = Color.Gray,
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
private fun HomePodcastsSection(navController: NavController, podcasts: List<PodcastModel>) {
    if (podcasts.isEmpty()) return

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp, 16.dp, 16.dp, 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Top Podcasts", color = Color.White, fontSize = HomeSectionTitleSize, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(HomeSectionHeaderBottomGap))

        LazyRow(modifier = Modifier.padding(horizontal = 6.dp, vertical = 0.dp)) {
            items(podcasts.size) { index ->
                val podcast = podcasts[index]
                val interactionSource = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .width(HomeStandardCardWidth)
                        .height(150.dp)
                        .pressScale(interactionSource, pressedScale = 0.94f)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            navController.navigate("${Routes.PodcastDetail.route}/${podcast.id}")
                        }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        GlideImage(
                            modifier = Modifier
                                .size(HomeStandardCardSize)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                            model = podcast.image,
                            loading = placeholder(R.drawable.placeholder),
                            failure = placeholder(R.drawable.placeholder),
                            contentDescription = podcast.title
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = podcast.title,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            lineHeight = 15.sp,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = podcast.author,
                            color = Color.Gray,
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

private fun sectionNavigatesToPlaylistCollection(section: HomePageSectionModel): Boolean {
    return section.path.startsWith("/playlist_collection")
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

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp, 16.dp, 16.dp, 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Albums", color = Color.White, fontSize = HomeSectionTitleSize, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(HomeSectionHeaderBottomGap))

        LazyRow(modifier = Modifier.padding(horizontal = 6.dp, vertical = 0.dp)) {
            items(displayAlbums.size) { index ->
                val a = displayAlbums[index]
                val interactionSource = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .width(HomeStandardCardWidth)
                        .height(152.dp)
                        .pressScale(interactionSource, pressedScale = 0.94f)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            navController.navigate("${Routes.Album.route}/${a.id}")
                        }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        GlideImage(
                            modifier = Modifier
                                .size(HomeStandardCardSize)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop,
                            model = a.image,
                            loading = placeholder(R.drawable.placeholder),
                            failure = placeholder(R.drawable.placeholder),
                            contentDescription = "Album"
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = 12.sp,
                            text = a.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            lineHeight = 15.sp,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = 11.sp,
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
}

// ─── Artists Row ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun HomeArtists(artists: List<ArtistsModel>, navController: NavController) {
    Column {
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
                fontSize = HomeSectionTitleSize,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(HomeSectionHeaderBottomGap))

        LazyRow(modifier = Modifier.padding(horizontal = 6.dp, vertical = 0.dp)) {
            items(artists.size) { index ->
                val artist = artists[index]
                val interactionSource = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .width(HomeArtistCardWidth)
                        .height(132.dp)
                        .pressScale(interactionSource, pressedScale = 0.94f)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            Log.d("check", "navigating to artist id=${artist.id}")
                            navController.navigate("${Routes.Artist.route}/${artist.id}")
                        }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GlideImage(
                            modifier = Modifier
                                .size(HomeArtistCardSize)
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
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            lineHeight = 15.sp,
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
                                modifier = Modifier.fillMaxWidth(),
                                text = "$formatted listeners",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
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
    Column(
        modifier = modifier.padding(0.dp, 0.dp, 0.dp, 50.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp, 16.dp, 16.dp, 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Discover", color = Color.White, fontSize = HomeSectionTitleSize, fontWeight = FontWeight.Bold)
        }
        repeat(albums.size) { index ->
            val a = albums[index]
            val interactionSource = remember { MutableInteractionSource() }
            Card(
                shape = RoundedCornerShape(15.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                modifier = Modifier
                    .padding(15.dp)
                    .fillMaxWidth()
                    .height(380.dp)
                    .pressScale(interactionSource, pressedScale = 0.98f)
                    .clickable(
                        interactionSource = interactionSource,
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
