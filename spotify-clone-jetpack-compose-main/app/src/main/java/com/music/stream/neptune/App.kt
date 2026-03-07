package com.music.stream.neptune

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.music.stream.neptune.ui.navigation.AppDrawer
import com.music.stream.neptune.ui.navigation.MainBottomNavigation
import com.music.stream.neptune.ui.navigation.MyNavHost
import com.music.stream.neptune.ui.navigation.Routes
import kotlinx.coroutines.launch


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun App() {
    val bottomBarState = rememberSaveable { (mutableStateOf(true)) }
    val bottomBarPlayerState = rememberSaveable { (mutableStateOf(true)) }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Control BottomBar visibility
    when (currentRoute) {
        Routes.Home.route -> bottomBarState.value = true
        Routes.Search.route -> bottomBarState.value = true
        Routes.Library.route -> bottomBarState.value = true
        Routes.Podcast.route -> bottomBarState.value = true
        Routes.Radio.route -> bottomBarState.value = true
        Routes.AvailableTracks.route -> bottomBarState.value = true
        Routes.Album.route -> bottomBarState.value = false
    }

    // Disable drawer gestures on fullscreen pages (player, album detail, artist detail, podcast detail)
    val drawerGesturesEnabled = currentRoute !in listOf(
        Routes.Player.route,
        "${Routes.Album.route}/{albumId}",
        "${Routes.Artist.route}/{artistId}",
        "${Routes.PodcastDetail.route}/{podcastId}",
        "${Routes.Playlist.route}/{playlistId}",
        "${Routes.PlaylistCollection.route}/{collectionId}"
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerGesturesEnabled,
        drawerContent = {
            AppDrawer(
                currentRoute = currentRoute,
                navController = navController,
                onClose = { coroutineScope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.navigationBarsPadding(),
            bottomBar = {
                MainBottomNavigation(
                    navController = navController,
                    bottomBarState = bottomBarState,
                    bottomBarPlayerState = bottomBarPlayerState,
                    onMenuClick = { coroutineScope.launch { drawerState.open() } }
                )
            }
        ) {
            MyNavHost(
                navHostController = navController,
                bottomBarState = bottomBarState,
                bottomBarPlayerState = bottomBarPlayerState
            )
        }
    }
}
