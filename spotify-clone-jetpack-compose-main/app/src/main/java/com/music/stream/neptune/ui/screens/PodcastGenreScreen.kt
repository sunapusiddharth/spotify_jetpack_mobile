package com.music.stream.neptune.ui.screens

import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.music.stream.neptune.data.api.Response
import com.music.stream.neptune.data.entity.PodcastModel
import com.music.stream.neptune.ui.components.Loader
import com.music.stream.neptune.ui.navigation.Routes
import com.music.stream.neptune.ui.theme.AppBackground
import com.music.stream.neptune.ui.viewmodel.PodcastViewModel

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PodcastGenreScreen(navController: NavController, genreName: String) {
    val viewModel: PodcastViewModel = hiltViewModel()
    val podcastsState by viewModel.genrePodcasts.collectAsState()
    val hasMore by viewModel.hasMoreGenrePodcasts.collectAsState()
    val isFetchingMore by viewModel.isFetchingMoreGenrePodcasts.collectAsState()

    val decodedGenre = Uri.decode(genreName)
    val bgColor = Color(AppBackground.toArgb())

    LaunchedEffect(decodedGenre) {
        viewModel.fetchGenrePage(genre = decodedGenre, page = 1, append = false)
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = decodedGenre,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        }
    ) { padding ->
        when (podcastsState) {
            is Response.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Loader()
                }
            }

            is Response.Error -> {
                val errorMessage = (podcastsState as Response.Error).error
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Error! $errorMessage", color = Color.White)
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = {
                        viewModel.fetchGenrePage(genre = decodedGenre, page = 1, append = false)
                    }) {
                        Text("Retry")
                    }
                }
            }

            is Response.Success -> {
                val podcasts = (podcastsState as Response.Success).data.results
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(bgColor),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Browse All",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    items(podcasts) { podcast ->
                        PodcastGenreRowItem(podcast = podcast, navController = navController)
                    }

                    item {
                        if (hasMore) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(
                                    onClick = { viewModel.loadNextGenrePage() },
                                    enabled = !isFetchingMore
                                ) {
                                    Text(if (isFetchingMore) "Fetching..." else "Load More")
                                }
                            }
                        }
                        Spacer(Modifier.height(120.dp))
                    }
                }
            }

            else -> Unit
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun PodcastGenreRowItem(podcast: PodcastModel, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF161620))
            .clickable(
                interactionSource = androidx.compose.runtime.remember { MutableInteractionSource() },
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
    }
}
