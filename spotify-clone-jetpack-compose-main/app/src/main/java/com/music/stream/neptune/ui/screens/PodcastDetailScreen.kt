package com.music.stream.neptune.ui.screens

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
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
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
import com.music.stream.neptune.ui.components.Loader
import com.music.stream.neptune.ui.components.Snackbar
import com.music.stream.neptune.ui.theme.AppBackground
import com.music.stream.neptune.ui.theme.AppPalette
import com.music.stream.neptune.ui.viewmodel.LocalSharedPlayerViewModel
import com.music.stream.neptune.ui.viewmodel.PlayerViewModel
import com.music.stream.neptune.ui.viewmodel.PodcastViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PodcastDetailScreen(navController: NavController, podcastId: String?) {
    if (podcastId.isNullOrEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Invalid podcast ID", color = Color.White)
        }
        return
    }

    val viewModel: PodcastViewModel = hiltViewModel()
    val playerViewModel: PlayerViewModel = LocalSharedPlayerViewModel.current
    val podcastState by viewModel.selectedPodcast.collectAsState()
    val episodesState by viewModel.episodes.collectAsState()
    val selectedEpisodeId by viewModel.selectedEpisodeId.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()

    var currentPage by remember { mutableStateOf(1) }
    var infoDialogText by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(podcastId) {
        currentPage = 1
        viewModel.fetchPodcastById(podcastId)
        viewModel.fetchEpisodes(podcastId, 1, append = false)
    }

    LaunchedEffect(actionMessage) {
        if (actionMessage != null) {
            delay(1500)
            viewModel.clearActionMessage()
        }
    }

    val bgColor = Color(AppBackground.toArgb())
    val scrollState = rememberSaveable(podcastId, saver = ScrollState.Saver) {
        ScrollState(0)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        when (podcastState) {
            is Response.Loading -> Loader()
            is Response.Success -> {
                val podcast = (podcastState as Response.Success).data
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    if (actionMessage != null) {
                        Snackbar(showMessage = actionMessage ?: "")
                    }

                    // Hero header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp)
                    ) {
                        GlideImage(
                            model = podcast.image,
                            contentDescription = podcast.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.3f),
                                            bgColor
                                        )
                                    )
                                )
                        )
                        // Back button
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .statusBarsPadding()
                                .padding(8.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.4f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        // Title overlay
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = podcast.title,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (podcast.author.isNotEmpty()) {
                                Text(
                                    text = podcast.author,
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // Metadata row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (podcast.episode_count > 0) {
                            MetaChip("${podcast.episode_count} episodes")
                        }
                        podcast.genres.take(2).forEach { genre ->
                            MetaChip(genre)
                        }
                    }

                    // Actions row (Play Latest + Request)
                    val latestEpisode = (episodesState as? Response.Success)?.data?.results?.firstOrNull()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (latestEpisode != null) {
                                    val allEpisodes = (episodesState as? Response.Success)?.data?.results.orEmpty()
                                    if (latestEpisode.hasAudio && allEpisodes.isNotEmpty()) {
                                        playerViewModel.startPodcastPlayback(
                                            podcast = podcast,
                                            queue = allEpisodes,
                                            startIndex = 0,
                                            context = context
                                        )
                                    }
                                    viewModel.setSelectedEpisode(latestEpisode.id)
                                }
                            }
                        ) {
                            Text("Play Latest")
                        }
                        Button(
                            onClick = {
                                viewModel.requestPodcastEpisodesPopulation(podcast.id)
                            }
                        ) {
                            Text("Request")
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Description
                    if (podcast.description.isNotEmpty()) {
                        Text(
                            text = podcast.description,
                            color = Color.Gray,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(20.dp))
                    }

                    // Episodes header
                    Text(
                        text = "Episodes",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(10.dp))

                    // Episodes list
                    when (episodesState) {
                        is Response.Loading -> Loader()
                        is Response.Success -> {
                            val episodes = (episodesState as Response.Success).data.results
                            if (episodes.isEmpty()) {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) { Text("No episodes available", color = Color.Gray) }
                            } else {
                                episodes.forEachIndexed { index, episode ->
                                    EpisodeRowItem(
                                        episode = episode,
                                        episodeNumber = index + 1,
                                        isActive = selectedEpisodeId == episode.id,
                                        onPlay = {
                                            if (episode.hasAudio) {
                                                playerViewModel.startPodcastPlayback(
                                                    podcast = podcast,
                                                    queue = episodes,
                                                    startIndex = index,
                                                    context = context
                                                )
                                            }
                                            viewModel.setSelectedEpisode(episode.id)
                                        },
                                        onShowInfo = { infoDialogText = episode.description }
                                    )
                                    if (index < episodes.lastIndex) {
                                        Divider(
                                            color = Color(0xFF2A2A3A),
                                            thickness = 0.5.dp,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }
                                }
                            }

                            if (episodes.isNotEmpty()) {
                                Spacer(Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        currentPage += 1
                                        viewModel.fetchEpisodes(podcastId, currentPage, append = true)
                                    },
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .padding(bottom = 8.dp)
                                ) {
                                    Text("Load More")
                                }
                            }
                        }
                        else -> {}
                    }

                    Spacer(Modifier.height(130.dp))
                }

                if (!infoDialogText.isNullOrBlank()) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { infoDialogText = null },
                        title = { Text("Episode Info") },
                        text = { Text(infoDialogText ?: "") },
                        confirmButton = {
                            Button(onClick = { infoDialogText = null }) { Text("Close") }
                        }
                    )
                }
            }
            is Response.Error -> {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text("Failed to load podcast", color = Color.White) }
            }
            else -> {}
        }
    }
}

@Composable
private fun MetaChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF2A2A3A))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = label, color = Color(0xFF1DB954), fontSize = 12.sp)
    }
}

@Composable
private fun EpisodeRowItem(
    episode: PodcastEpisodeModel,
    episodeNumber: Int,
    isActive: Boolean,
    onPlay: () -> Unit,
    onShowInfo: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isActive) Color(0x3329A86B) else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onPlay() },
        verticalAlignment = Alignment.Top
    ) {
        // Episode number
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF2A2A3A)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$episodeNumber",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = episode.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (episode.description.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = episode.description,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (episode.duration > 0) {
                    Text(
                        text = formatMillis(episode.duration),
                        color = Color(0xFF888888),
                        fontSize = 11.sp
                    )
                }
                if (episode.hasAudio) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(AppPalette.toArgb())),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = Color.LightGray,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onShowInfo() }
                )
            }
        }
    }
}
