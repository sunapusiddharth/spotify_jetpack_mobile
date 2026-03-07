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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.music.stream.neptune.ui.components.Loader
import com.music.stream.neptune.ui.navigation.Routes
import com.music.stream.neptune.ui.theme.AppBackground
import com.music.stream.neptune.ui.theme.GridBackground
import com.music.stream.neptune.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(navController: NavController) {
    val homeViewModel: HomeViewModel = hiltViewModel()
    val albums by homeViewModel.albums.collectAsState()
    val artists by homeViewModel.artists.collectAsState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(AppBackground.toArgb()))
            .statusBarsPadding()
    ) {
        when (albums) {
            is Response.Loading -> {
                Log.d("homeMain", "loading...")
                Loader()
            }
            is Response.Success -> {
                val albumsResponse = (albums as Response.Success).data
                val artistsResponse = when (artists) {
                    is Response.Success -> (artists as Response.Success).data
                    else -> emptyList()
                }
                Log.d("homeMain", "Success. albums=${albumsResponse.size} artists=${artistsResponse.size}")
                SumUpHomeScreen(navController = navController, albums = albumsResponse, artists = artistsResponse)
            }
            is Response.Error -> {
                Log.d("homeMain", "Error!! ${(albums as Response.Error).error}")
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
    albums: List<AlbumsModel>,
    artists: List<ArtistsModel>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(AppBackground.toArgb()))
    ) {
        // Featured hero carousel
        if (albums.isNotEmpty()) {
            FeaturedBanner(albums = albums, navController = navController)
        }
        GreetingSection()
        if (albums.size >= 8) {
            HomePlaylistGrid(navController, albums)
        }
        HomeAlbums(album = albums, navController)
        HomeArtists(artists = artists, navController)
        ImageCard(navController, albums)
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
