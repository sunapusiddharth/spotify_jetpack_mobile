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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.music.stream.neptune.data.api.Response
import com.music.stream.neptune.data.entity.RadioGenreAggModel
import com.music.stream.neptune.data.entity.RadioStationModel
import com.music.stream.neptune.ui.components.Loader
import com.music.stream.neptune.ui.theme.AppBackground
import com.music.stream.neptune.ui.theme.AppPalette
import com.music.stream.neptune.ui.viewmodel.PlayerViewModel
import com.music.stream.neptune.ui.viewmodel.RadioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioScreen(navController: NavController) {
    val radioViewModel: RadioViewModel = hiltViewModel()
    val playerViewModel: PlayerViewModel = hiltViewModel()

    val countriesState by radioViewModel.countries.collectAsState()
    val genresState by radioViewModel.genres.collectAsState()
    val lastPlayedState by radioViewModel.lastPlayedStations.collectAsState()
    val likedStationsState by radioViewModel.likedStations.collectAsState()
    val topStationsState by radioViewModel.topStationsByVotes.collectAsState()
    val trendingState by radioViewModel.trendingStations.collectAsState()
    val genreListingState by radioViewModel.genreListing.collectAsState()
    val hasMoreGenreListing by radioViewModel.hasMoreGenreListing.collectAsState()
    val isFetchingMoreGenreListing by radioViewModel.isFetchingMoreGenreListing.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val bgColor = Color(AppBackground.toArgb())

    var selectedCountry by remember { mutableStateOf("US") }
    var selectedGenreTab by remember { mutableStateOf(0) }

    val genres = if (genresState is Response.Success) {
        listOf(RadioGenreAggModel(value = "", count = 0)) + (genresState as Response.Success).data
    } else {
        listOf(RadioGenreAggModel(value = "", count = 0))
    }

    LaunchedEffect(selectedCountry) {
        radioViewModel.onCountrySelected(selectedCountry, "test")
        selectedGenreTab = 0
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        modifier = Modifier.padding(0.dp, 40.dp, 0.dp, 0.dp),
                        text = "Radio",
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
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .background(bgColor)
        ) {
            Spacer(Modifier.height(8.dp))

            if (countriesState is Response.Success) {
                Text(
                    text = "Countries",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items((countriesState as Response.Success).data) { country ->
                        val countryCode = country.code.ifBlank { country.name }
                        FilterChip(
                            selected = selectedCountry == countryCode,
                            onClick = { selectedCountry = countryCode },
                            label = {
                                Text(
                                    text = "${countryCode.uppercase()} (${country.count})",
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(AppPalette.toArgb()),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF2A2A3A),
                                labelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
            }

            RadioCarouselSection(
                title = "Last Played Stations",
                state = lastPlayedState,
                onStationClick = { queue, index ->
                    playerViewModel.startRadioPlayback(queue = queue, startIndex = index, context = context)
                }
            )
            RadioCarouselSection(
                title = "Liked Stations",
                state = likedStationsState,
                onStationClick = { queue, index ->
                    playerViewModel.startRadioPlayback(queue = queue, startIndex = index, context = context)
                }
            )
            RadioCarouselSection(
                title = "TopStations By Votes",
                state = mapStationsFromBrowse(topStationsState),
                onStationClick = { queue, index ->
                    playerViewModel.startRadioPlayback(queue = queue, startIndex = index, context = context)
                }
            )
            RadioCarouselSection(
                title = "Trending Stations",
                state = trendingState,
                onStationClick = { queue, index ->
                    playerViewModel.startRadioPlayback(queue = queue, startIndex = index, context = context)
                }
            )

            Text(
                text = "Browse By Genres",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            TabRow(
                selectedTabIndex = selectedGenreTab,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedGenreTab]),
                        color = Color(AppPalette.toArgb())
                    )
                },
                divider = { Spacer(Modifier.height(0.dp)) }
            ) {
                genres.forEachIndexed { index, genre ->
                    Tab(
                        selected = selectedGenreTab == index,
                        onClick = {
                            selectedGenreTab = index
                            radioViewModel.fetchGenreListing(genre.value, page = 1, append = false)
                        },
                        text = {
                            Text(
                                text = "${genre.value.ifBlank { "All" }.uppercase()} (${genre.count})",
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        selectedContentColor = Color.White,
                        unselectedContentColor = Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            when (genreListingState) {
                is Response.Loading -> Loader()
                is Response.Error -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Failed to load stations", color = Color.Gray)
                    }
                }
                is Response.Success -> {
                    val stations = (genreListingState as Response.Success).data.results
                    if (stations.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No stations found", color = Color.Gray)
                        }
                    } else {
                        stations.forEach { station ->
                            val index = stations.indexOfFirst { it.id == station.id }
                            RadioStationRow(
                                station = station,
                                onPlay = {
                                    playerViewModel.startRadioPlayback(
                                        queue = stations,
                                        startIndex = if (index >= 0) index else 0,
                                        context = context
                                    )
                                }
                            )
                        }
                        if (hasMoreGenreListing) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(
                                    onClick = { radioViewModel.loadNextGenrePage() },
                                    enabled = !isFetchingMoreGenreListing
                                ) {
                                    Text(if (isFetchingMoreGenreListing) "Fetching..." else "Load More")
                                }
                            }
                        }
                    }
                }
                else -> Unit
            }

            Spacer(Modifier.height(130.dp))
        }
    }
}

@Composable
private fun mapStationsFromBrowse(
    state: Response<com.music.stream.neptune.data.network.StationsBrowseResponse>
): Response<List<RadioStationModel>> {
    return when (state) {
        is Response.Success -> Response.Success(state.data.results)
        is Response.Error -> Response.Error(state.error)
        is Response.Loading -> Response.Loading()
        else -> Response.Loading()
    }
}

@Composable
private fun RadioCarouselSection(
    title: String,
    state: Response<List<RadioStationModel>>,
    onStationClick: (List<RadioStationModel>, Int) -> Unit
) {
    if (state !is Response.Success || state.data.isEmpty()) return

    Text(
        text = title,
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(state.data) { station ->
            val index = state.data.indexOfFirst { it.id == station.id }
            RadioStationCard(station = station, onPlay = { onStationClick(state.data, if (index >= 0) index else 0) })
        }
    }

    Spacer(Modifier.height(8.dp))
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun RadioStationCard(station: RadioStationModel, onPlay: () -> Unit) {
    Column(
        modifier = Modifier
            .width(130.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onPlay() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2A2A3A)),
            contentAlignment = Alignment.Center
        ) {
            if (station.coverUri.isNotEmpty()) {
                GlideImage(
                    model = station.coverUri,
                    contentDescription = station.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Radio,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = station.name,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun RadioStationRow(station: RadioStationModel, onPlay: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF161620))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onPlay() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF2A2A3A)),
            contentAlignment = Alignment.Center
        ) {
            if (station.coverUri.isNotEmpty()) {
                GlideImage(
                    model = station.coverUri,
                    contentDescription = station.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Radio,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = station.name,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (station.country.isNotEmpty()) {
                Text(text = station.country, color = Color.Gray, fontSize = 12.sp)
            }
            if (station.genres.isNotEmpty()) {
                Text(
                    text = station.genres.take(2).joinToString(" · "),
                    color = Color(0xFF888888),
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
        if (station.votes > 0) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${station.votes}",
                    color = Color(AppPalette.toArgb()),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(text = "votes", color = Color.Gray, fontSize = 10.sp)
            }
        }
    }
}
