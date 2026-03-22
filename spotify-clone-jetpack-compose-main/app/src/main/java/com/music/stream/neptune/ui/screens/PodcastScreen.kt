package com.music.stream.neptune.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import android.net.Uri
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.music.stream.neptune.data.api.Response
import com.music.stream.neptune.data.entity.PodcastModel
import com.music.stream.neptune.data.entity.UserHistoryEntityModel
import com.music.stream.neptune.ui.components.Loader
import com.music.stream.neptune.ui.navigation.Routes
import com.music.stream.neptune.ui.viewmodel.LocalSharedPlayerViewModel
import com.music.stream.neptune.ui.viewmodel.PlayerViewModel
import com.music.stream.neptune.ui.theme.AppBackground
import com.music.stream.neptune.ui.viewmodel.PodcastViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastScreen(navController: NavController) {
    val podcastViewModel: PodcastViewModel = hiltViewModel()
    val playerViewModel: PlayerViewModel = LocalSharedPlayerViewModel.current
    val podcastsState by podcastViewModel.podcasts.collectAsState()
    val genresState by podcastViewModel.genres.collectAsState()
    val historyState by podcastViewModel.historyEntries.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val bgColor = Color(AppBackground.toArgb())
    val initialScroll = remember { ScreenScrollMemory.scrollOffsets[Routes.Podcast.route] ?: 0 }
    val scrollState = rememberScrollState(initial = initialScroll)

    DisposableEffect(scrollState) {
        onDispose {
            ScreenScrollMemory.scrollOffsets[Routes.Podcast.route] = scrollState.value
        }
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        modifier = Modifier.padding(0.dp, 40.dp, 0.dp, 0.dp),
                        text = "Podcasts",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padding)
                .background(bgColor)
        ) {
            Spacer(Modifier.height(8.dp))

            // Genre filter chips
            val genres = if (genresState is Response.Success)
                listOf("All") + (genresState as Response.Success).data
            else listOf("All")
            var selectedGenre by remember { mutableStateOf("All") }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(genres) { genre ->
                    FilterChip(
                        selected = selectedGenre == genre,
                        onClick = {
                            selectedGenre = genre
                            if (genre == "All") {
                                podcastViewModel.fetchPodcasts(1)
                            } else {
                                navController.navigate("${Routes.PodcastGenre.route}/${Uri.encode(genre)}")
                            }
                        },
                        label = { Text(genre, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1DB954),
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF2A2A3A),
                            labelColor = Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Spacer(Modifier.height(12.dp))

            PodcastHistorySection(
                entries = (historyState as? Response.Success)?.data.orEmpty(),
                navController = navController,
                playerViewModel = playerViewModel,
                context = context
            )

            when (podcastsState) {
                is Response.Loading -> Loader()
                is Response.Success -> {
                    val podcasts = (podcastsState as Response.Success).data.results
                    val filtered = if (selectedGenre == "All") podcasts
                    else podcasts.filter { it.genres.contains(selectedGenre) }

                    if (filtered.isNotEmpty()) {
                        // Featured top podcast
                        Text(
                            "Featured",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        PodcastFeaturedCard(filtered.first(), navController)
                        Spacer(Modifier.height(24.dp))

                        // All podcasts grid
                        Text(
                            "All Podcasts",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        for (podcast in filtered) {
                            PodcastRowItem(podcast, navController)
                        }
                    } else {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No podcasts found", color = Color.Gray)
                        }
                    }
                }
                is Response.Error -> {
                    Box(
                        Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("Failed to load podcasts", color = Color.Gray) }
                }
            }

            Spacer(Modifier.height(130.dp))
        }
    }
}

@Composable
private fun PodcastHistorySection(
    entries: List<UserHistoryEntityModel>,
    navController: NavController,
    playerViewModel: PlayerViewModel,
    context: android.content.Context
) {
    if (entries.isEmpty()) return

    Text(
        "History",
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    Spacer(Modifier.height(10.dp))

    entries.forEach { entry ->
        PodcastHistoryRow(
            entry = entry,
            onClick = {
                when {
                    entry.isPodcastEpisode -> playerViewModel.playPodcastEpisodeFromHistory(entry, context)
                    entry.isPodcast -> navController.navigate("${Routes.PodcastDetail.route}/${entry.entityId}")
                }
            }
        )
    }

    Spacer(Modifier.height(20.dp))
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun PodcastHistoryRow(entry: UserHistoryEntityModel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF161620))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlideImage(
            model = entry.image,
            contentDescription = entry.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(62.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = entry.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = entry.subtitle.ifBlank { if (entry.isPodcastEpisode) "Episode" else "Podcast" },
                color = Color.Gray,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PodcastHorizontalSection(
    title: String,
    state: Response<List<PodcastModel>>,
    navController: NavController
) {
    if (state !is Response.Success || state.data.isEmpty()) return

    Text(
        title,
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(state.data) { podcast ->
            PodcastMiniCard(podcast = podcast, navController = navController)
        }
    }

    Spacer(Modifier.height(12.dp))
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun PodcastMiniCard(podcast: PodcastModel, navController: NavController) {
    Column(
        modifier = Modifier
            .width(130.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { navController.navigate("${Routes.PodcastDetail.route}/${podcast.id}") }
    ) {
        GlideImage(
            model = podcast.image,
            contentDescription = podcast.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = podcast.title,
            color = Color.White,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun PodcastFeaturedCard(podcast: PodcastModel, navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { navController.navigate("${Routes.PodcastDetail.route}/${podcast.id}") }
    ) {
        GlideImage(
            model = podcast.image,
            contentDescription = podcast.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = podcast.title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (podcast.author.isNotEmpty()) {
                Text(
                    text = "by ${podcast.author}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
            if (podcast.episode_count > 0) {
                Text(
                    text = "${podcast.episode_count} episodes",
                    color = Color(0xFF1DB954),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun PodcastRowItem(podcast: PodcastModel, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF161620))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { navController.navigate("${Routes.PodcastDetail.route}/${podcast.id}") }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlideImage(
            model = podcast.image,
            contentDescription = podcast.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(62.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = podcast.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (podcast.author.isNotEmpty()) {
                Text(
                    text = podcast.author,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (podcast.genres.isNotEmpty()) {
                Text(
                    text = podcast.genres.take(2).joinToString(" · "),
                    color = Color(0xFF1DB954),
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
        if (podcast.episode_count > 0) {
            Text(
                text = "${podcast.episode_count} eps",
                color = Color.Gray,
                fontSize = 11.sp
            )
        }
    }
}
