package com.music.stream.neptune.ui.navigation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.music.stream.neptune.ui.screens.AlbumScreen
import com.music.stream.neptune.ui.screens.ArtistScreen
import com.music.stream.neptune.ui.screens.AvailableTracksScreen
import com.music.stream.neptune.ui.screens.HomeScreen
import com.music.stream.neptune.ui.screens.LibraryScreen
import com.music.stream.neptune.ui.screens.PlayerScreen
import com.music.stream.neptune.ui.screens.PlaylistCollectionScreen
import com.music.stream.neptune.ui.screens.PlaylistScreen
import com.music.stream.neptune.ui.screens.PodcastDetailScreen
import com.music.stream.neptune.ui.screens.PodcastGenreScreen
import com.music.stream.neptune.ui.screens.PodcastScreen
import com.music.stream.neptune.ui.screens.RadioScreen
import com.music.stream.neptune.ui.screens.SearchScreen
import com.music.stream.neptune.ui.viewmodel.PlayerViewModel

@Composable
fun MyNavHost(
    navHostController: NavHostController,
    bottomBarState: MutableState<Boolean>,
    bottomBarPlayerState: MutableState<Boolean>
) {
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val playerState by playerViewModel.currentSongTitle

    NavHost(navController = navHostController, startDestination = Routes.Home.route) {

        composable(Routes.Home.route) {
            LaunchedEffect(playerState) {
                bottomBarState.value = true
                bottomBarPlayerState.value = playerState.isNotEmpty()
            }
            HomeScreen(navHostController)
        }

        composable(Routes.Search.route) {
            LaunchedEffect(playerState) {
                bottomBarState.value = true
                bottomBarPlayerState.value = playerState.isNotEmpty()
            }
            SearchScreen(navHostController)
        }

        composable(Routes.Library.route) {
            LaunchedEffect(playerState) {
                bottomBarState.value = true
                bottomBarPlayerState.value = playerState.isNotEmpty()
            }
            LibraryScreen(navHostController)
        }

        composable(Routes.Player.route) {
            LaunchedEffect(playerState) {
                bottomBarState.value = false
                bottomBarPlayerState.value = playerState.isNotEmpty()
            }
            PlayerScreen(navHostController)
        }

        // Album route — takes albumId (Int as String, or "liked_songs")
        composable("${Routes.Album.route}/{albumId}") { navBackStackEntry ->
            LaunchedEffect(playerState) {
                bottomBarState.value = true
                bottomBarPlayerState.value = playerState.isNotEmpty()
            }
            val albumId = navBackStackEntry.arguments?.getString("albumId")
            albumId?.let {
                AlbumScreen(navController = navHostController, albumId = it)
            }
        }

        // Artist route
        composable("${Routes.Artist.route}/{artistId}") { navBackStackEntry ->
            LaunchedEffect(playerState) {
                bottomBarState.value = true
                bottomBarPlayerState.value = playerState.isNotEmpty()
            }
            val artistId = navBackStackEntry.arguments?.getString("artistId")
            artistId?.let {
                ArtistScreen(navHostController, it)
            }
        }

        // Podcast browse
        composable(Routes.Podcast.route) {
            LaunchedEffect(playerState) {
                bottomBarState.value = true
                bottomBarPlayerState.value = playerState.isNotEmpty()
            }
            PodcastScreen(navHostController)
        }

        // Podcast detail
        composable("${Routes.PodcastDetail.route}/{podcastId}") { navBackStackEntry ->
            LaunchedEffect(playerState) {
                bottomBarState.value = true
                bottomBarPlayerState.value = playerState.isNotEmpty()
            }
            val podcastId = navBackStackEntry.arguments?.getString("podcastId")
            podcastId?.let {
                PodcastDetailScreen(navController = navHostController, podcastId = it)
            }
        }

        // Podcast genre listing
        composable("${Routes.PodcastGenre.route}/{genreName}") { navBackStackEntry ->
            LaunchedEffect(playerState) {
                bottomBarState.value = true
                bottomBarPlayerState.value = playerState.isNotEmpty()
            }
            val genreName = navBackStackEntry.arguments?.getString("genreName")
            genreName?.let {
                PodcastGenreScreen(navController = navHostController, genreName = it)
            }
        }

        // Radio
        composable(Routes.Radio.route) {
            LaunchedEffect(playerState) {
                bottomBarState.value = true
                bottomBarPlayerState.value = playerState.isNotEmpty()
            }
            RadioScreen(navHostController)
        }

        // Available tracks
        composable(Routes.AvailableTracks.route) {
            LaunchedEffect(playerState) {
                bottomBarState.value = true
                bottomBarPlayerState.value = playerState.isNotEmpty()
            }
            AvailableTracksScreen()
        }

        // Playlist
        composable("${Routes.Playlist.route}/{playlistId}") { navBackStackEntry ->
            LaunchedEffect(playerState) {
                bottomBarState.value = true
                bottomBarPlayerState.value = playerState.isNotEmpty()
            }
            val playlistId = navBackStackEntry.arguments?.getString("playlistId")
            playlistId?.let {
                PlaylistScreen(navController = navHostController, playlistId = it)
            }
        }

        // Playlist collection
        composable("${Routes.PlaylistCollection.route}/{collectionId}") { navBackStackEntry ->
            LaunchedEffect(playerState) {
                bottomBarState.value = true
                bottomBarPlayerState.value = playerState.isNotEmpty()
            }
            val collectionId = navBackStackEntry.arguments?.getString("collectionId")
            collectionId?.let {
                PlaylistCollectionScreen(navController = navHostController, collectionId = it)
            }
        }
    }
}
