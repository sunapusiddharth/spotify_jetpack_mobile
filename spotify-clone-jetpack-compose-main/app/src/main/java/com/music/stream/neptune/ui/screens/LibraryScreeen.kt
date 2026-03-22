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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.music.stream.neptune.data.entity.PodcastEpisodeModel
import com.music.stream.neptune.data.entity.PodcastModel
import com.music.stream.neptune.data.entity.RadioStationModel
import com.music.stream.neptune.data.entity.SongsModel
import com.music.stream.neptune.data.entity.UserHistoryEntityModel
import com.music.stream.neptune.data.entity.UserHistoryPageModel
import com.music.stream.neptune.data.entity.UserLikedEntityModel
import com.music.stream.neptune.data.entity.UserLikesPageModel
import com.music.stream.neptune.data.entity.UserPlaylistModel
import com.music.stream.neptune.ui.components.Loader
import com.music.stream.neptune.ui.navigation.Routes
import com.music.stream.neptune.ui.theme.AppBackground
import com.music.stream.neptune.ui.viewmodel.HomeViewModel
import com.music.stream.neptune.ui.viewmodel.LocalSharedPlayerViewModel
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
        val playerViewModel: PlayerViewModel = LocalSharedPlayerViewModel.current
        val likesPage by libraryViewModel.libraryLikesPage.collectAsState()
        val historyPage by libraryViewModel.libraryHistoryPage.collectAsState()
        val playlistsState by libraryViewModel.libraryPlaylists.collectAsState()
        val hasMoreLikes by libraryViewModel.hasMoreLibraryLikes.collectAsState()
        val isLoadingMoreLikes by libraryViewModel.isLoadingMoreLibraryLikes.collectAsState()
        val hasMoreHistory by libraryViewModel.hasMoreLibraryHistory.collectAsState()
        val isLoadingMoreHistory by libraryViewModel.isLoadingMoreLibraryHistory.collectAsState()
        val context = LocalContext.current

        LaunchedEffect(Unit) {
            libraryViewModel.fetchLibraryContent(page = 1, limit = 20)
        }

        val likesResponse = (likesPage as? Response.Success)?.data
        val historyResponse = (historyPage as? Response.Success)?.data
        val playlistsResponse = (playlistsState as? Response.Success)?.data.orEmpty()
        val hasContent = likesResponse?.items?.isNotEmpty() == true ||
            historyResponse?.items?.isNotEmpty() == true ||
            playlistsResponse.isNotEmpty()
        val initialLoading = likesPage is Response.Loading &&
            historyPage is Response.Loading &&
            playlistsState is Response.Loading

        when {
            initialLoading -> {
                Log.d("LibraryScreen", "loading library content...")
                Loader()
            }
            hasContent || likesPage is Response.Success || historyPage is Response.Success || playlistsState is Response.Success -> {
                SumUpLibraryScreen(
                    padding = padding,
                    likesPage = likesResponse,
                    historyPage = historyResponse,
                    playlists = playlistsResponse,
                    hasMoreLikes = hasMoreLikes,
                    isLoadingMoreLikes = isLoadingMoreLikes,
                    hasMoreHistory = hasMoreHistory,
                    isLoadingMoreHistory = isLoadingMoreHistory,
                    onLoadMoreLikes = { libraryViewModel.loadMoreLibraryLikes(limit = 20) },
                    onLoadMoreHistory = { libraryViewModel.loadMoreLibraryHistory(limit = 20) },
                    navController = navController,
                    playerViewModel = playerViewModel,
                    context = context
                )
            }
            else -> {
                val error = (likesPage as? Response.Error)?.error
                    ?: (historyPage as? Response.Error)?.error
                    ?: (playlistsState as? Response.Error)?.error
                Log.d("LibraryScreen", "Error loading library: $error")
                Box(
                    Modifier.fillMaxSize().background(Color(AppBackground.toArgb())).padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Failed to load library", color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun SumUpLibraryScreen(
    padding: PaddingValues,
    likesPage: UserLikesPageModel?,
    historyPage: UserHistoryPageModel?,
    playlists: List<UserPlaylistModel>,
    hasMoreLikes: Boolean,
    isLoadingMoreLikes: Boolean,
    hasMoreHistory: Boolean,
    isLoadingMoreHistory: Boolean,
    onLoadMoreLikes: () -> Unit,
    onLoadMoreHistory: () -> Unit,
    navController: NavController,
    playerViewModel: PlayerViewModel,
    context: android.content.Context
) {
    val historyEntries = historyPage?.items.orEmpty()
    val likedEntries = likesPage?.items.orEmpty()
    val initialScroll = remember {
        ScreenScrollMemory.scrollOffsets[Routes.Library.route] ?: 0
    }
    val scrollState = rememberScrollState(initial = initialScroll)

    DisposableEffect(scrollState) {
        onDispose {
            ScreenScrollMemory.scrollOffsets[Routes.Library.route] = scrollState.value
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(padding)
            .background(Color(AppBackground.toArgb()))
    ) {
        Spacer(Modifier.height(8.dp))

        LibrarySectionHero(
            icon = Icons.Default.LibraryMusic,
            title = "Your Library",
            subtitle = buildString {
                append("${playlists.size} playlists")
                append(" • ")
                append("${historyPage?.total ?: historyEntries.size} history")
                append(" • ")
                append("${likesPage?.total ?: likedEntries.size} liked")
            }
        )

        if (playlists.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            LibrarySectionHeader(
                title = "Playlists",
                countText = playlists.size.toString()
            )
            Spacer(Modifier.height(10.dp))

            playlists.forEach { playlist ->
                LibraryEntityRow(
                    image = "",
                    title = playlist.name.ifBlank { "Playlist" },
                    subtitle = if (playlist.tracks.isEmpty()) "Playlist" else "${playlist.tracks.size} tracks",
                    icon = Icons.Default.LibraryMusic,
                    onClick = { navController.navigate("${Routes.Playlist.route}/${playlist.id}") }
                )
            }
        }

        if (historyEntries.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            LibrarySectionHeader(
                title = "Recently Played",
                countText = formatSectionCount(historyEntries.size, historyPage?.total ?: historyEntries.size, historyPage?.hasMore == true)
            )
            Spacer(Modifier.height(10.dp))

            historyEntries.forEach { entry ->
                LibraryEntityRow(
                    image = entry.image,
                    title = entry.title,
                    subtitle = historySubtitle(entry),
                    icon = Icons.Default.History,
                    onClick = {
                        when {
                            entry.isSong -> playerViewModel.playSongFromHistory(entry, historyEntries, context)
                            entry.isRadioStation -> playerViewModel.playRadioFromHistory(entry, context)
                            entry.isPodcastEpisode -> playerViewModel.playPodcastEpisodeFromHistory(entry, context)
                            entry.isPlaylist -> navController.navigate("${Routes.Playlist.route}/${entry.entityId}")
                            entry.isPlaylistCollection -> navController.navigate("${Routes.Playlist.route}/${entry.entityId}")
                        }
                    }
                )
            }

            if (hasMoreHistory || isLoadingMoreHistory) {
                LibraryLoadMoreRow(
                    isLoading = isLoadingMoreHistory,
                    onClick = onLoadMoreHistory
                )
            }
        }

        if (likedEntries.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            LibrarySectionHeader(
                title = "Liked",
                countText = formatSectionCount(likedEntries.size, likesPage?.total ?: likedEntries.size, likesPage?.hasMore == true)
            )
            Spacer(Modifier.height(10.dp))

            likedEntries.forEach { entry ->
                LibraryEntityRow(
                    image = entry.image,
                    title = entry.title,
                    subtitle = likedSubtitle(entry),
                    icon = Icons.Default.Favorite,
                    onClick = {
                        when {
                            entry.isSong && entry.s3link.isNotBlank() -> playerViewModel.playSongQueueFromPlaylist(
                                queueSongs = listOf(entry.toSongModel()),
                                startIndex = 0,
                                album = entry.subtitle.ifBlank { "Liked" },
                                context = context
                            )
                            entry.isRadioStation && entry.s3link.isNotBlank() -> playerViewModel.startRadioPlayback(
                                queue = listOf(entry.toRadioStationModel()),
                                startIndex = 0,
                                context = context
                            )
                            entry.isPodcastEpisode && entry.s3link.isNotBlank() -> playerViewModel.startPodcastPlayback(
                                podcast = entry.toPodcastModel(),
                                queue = listOf(entry.toPodcastEpisodeModel()),
                                startIndex = 0,
                                context = context
                            )
                            entry.isAlbum -> navController.navigate("${Routes.Album.route}/${entry.entityId}")
                            entry.isPlaylist -> navController.navigate("${Routes.Playlist.route}/${entry.entityId}")
                            entry.isPlaylistCollection -> navController.navigate("${Routes.Playlist.route}/${entry.entityId}")
                        }
                    }
                )
            }

            if (hasMoreLikes || isLoadingMoreLikes) {
                LibraryLoadMoreRow(
                    isLoading = isLoadingMoreLikes,
                    onClick = onLoadMoreLikes
                )
            }
        }

        if (playlists.isEmpty() && historyEntries.isEmpty() && likedEntries.isEmpty()) {
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
                        text = "Your library is empty",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Likes, history, and playlists will appear here",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(130.dp))
    }
}

@Composable
private fun LibraryLoadMoreRow(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        OutlinedButton(onClick = onClick, enabled = !isLoading) {
            Text(
                text = if (isLoading) "Loading..." else "Load More",
                color = Color.White
            )
        }
    }
}

@Composable
private fun LibrarySectionHero(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF203A43), Color(0xFF2C5364))
                )
            )
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
                    .background(Color.White.copy(alpha = 0.12f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
            Column {
                Text(text = title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun LibrarySectionHeader(title: String, countText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(text = countText, color = Color.Gray, fontSize = 13.sp)
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun LibraryEntityRow(
    image: String,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
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
            ) { onClick() }
            .padding(10.dp)
    ) {
        if (image.isNotBlank()) {
            GlideImage(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(6.dp)),
                model = image,
                contentScale = ContentScale.Crop,
                contentDescription = title
            )
        } else {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF262638)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = Color.White)
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title.ifBlank { "Untitled" },
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle.ifBlank { "Library item" },
                color = Color.Gray,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun historySubtitle(entry: UserHistoryEntityModel): String = when {
    entry.isSong -> entry.artists.joinToString(", ") { it.title }.ifBlank { entry.album.title.ifBlank { entry.subtitle.ifBlank { "Song" } } }
    entry.isRadioStation -> "Radio station"
    entry.isPodcastEpisode -> entry.subtitle.ifBlank { "Podcast episode" }
    entry.isPlaylist -> "Playlist"
    entry.isPlaylistCollection -> "Playlist"
    else -> entry.subtitle.ifBlank { entry.entityType }
}

private fun likedSubtitle(entry: UserLikedEntityModel): String = when {
    entry.isSong -> entry.artists.joinToString(", ") { it.title }.ifBlank { entry.album.title.ifBlank { entry.subtitle.ifBlank { "Song" } } }
    entry.isRadioStation -> "Radio station"
    entry.isPodcastEpisode -> entry.subtitle.ifBlank { "Podcast episode" }
    entry.isAlbum -> "Album"
    entry.isPlaylist -> "Playlist"
    entry.isPlaylistCollection -> "Playlist"
    else -> entry.subtitle.ifBlank { entry.entityType }
}

private fun formatSectionCount(visibleCount: Int, total: Int, hasMore: Boolean): String {
    return when {
        total <= 0 -> visibleCount.toString()
        hasMore && visibleCount < total -> "$visibleCount/$total"
        else -> total.toString()
    }
}

private val UserLikedEntityModel.isSong: Boolean get() = entityType == "song"
private val UserLikedEntityModel.isRadioStation: Boolean get() = entityType == "radio_station"
private val UserLikedEntityModel.isPodcastEpisode: Boolean get() = entityType == "podcast_episode"
private val UserLikedEntityModel.isAlbum: Boolean get() = entityType == "album"
private val UserLikedEntityModel.isPlaylist: Boolean get() = entityType == "playlist"
private val UserLikedEntityModel.isPlaylistCollection: Boolean get() = entityType == "playlist_collection"

private fun UserLikedEntityModel.toSongModel(): SongsModel = SongsModel(
    id = entityId,
    name = title,
    artists = artists,
    album = album.copy(title = album.title.ifBlank { subtitle }),
    thumbnail = image,
    s3link = s3link
)

private fun UserLikedEntityModel.toRadioStationModel(): RadioStationModel = RadioStationModel(
    id = entityId,
    name = title,
    stream_url = s3link,
    image = image,
    favicon = image
)

private fun UserLikedEntityModel.toPodcastModel(): PodcastModel = PodcastModel(
    id = entityId,
    title = subtitle.ifBlank { title },
    image = image,
    author = subtitle
)

private fun UserLikedEntityModel.toPodcastEpisodeModel(): PodcastEpisodeModel = PodcastEpisodeModel(
    id = entityId,
    title = title,
    s3link = s3link,
    thumbnail = image,
    episode_number = episodeNumber ?: 0
)
