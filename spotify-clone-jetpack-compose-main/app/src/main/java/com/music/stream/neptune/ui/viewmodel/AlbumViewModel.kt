package com.music.stream.neptune.ui.viewmodel

import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.stream.neptune.data.api.Response
import com.music.stream.neptune.data.entity.AlbumsModel
import com.music.stream.neptune.data.entity.SongsModel
import com.music.stream.neptune.di.CurrentSongState
import com.music.stream.neptune.ui.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val repository: AppRepository,
    private val currentSongState: CurrentSongState
) : ViewModel() {

    val currentSongPlayingState: State<Boolean> get() = currentSongState.playingState
    val currentSongId: State<String> get() = currentSongState.songId
    val currentSongCoverUri: State<String> get() = currentSongState.coverUri
    val currentSongTitle: State<String> get() = currentSongState.title
    val currentSongSinger: State<String> get() = currentSongState.singer
    val currentSongIndex: State<Int> get() = currentSongState.songIndex
    val currentSongAlbum: State<String> get() = currentSongState.album

    // Album data loaded by ID
    private val _album: MutableStateFlow<Response<AlbumsModel?>> =
        MutableStateFlow(Response.Loading())
    val album: StateFlow<Response<AlbumsModel?>> = _album

    // All songs (from fresh albums) — needed for liked songs screen / player queue
    private val _songs: MutableStateFlow<Response<List<SongsModel>>> =
        MutableStateFlow(Response.Loading())
    val songs: StateFlow<Response<List<SongsModel>>> = _songs

    // All albums — needed for liked songs "Liked Songs" special album
    private val _albums: MutableStateFlow<Response<List<AlbumsModel>>> =
        MutableStateFlow(Response.Loading())
    val albums: StateFlow<Response<List<AlbumsModel>>> = _albums

    val likeState = currentSongState.likeState

    fun updateLikeState(likeState: Boolean) {
        currentSongState.updateLikeState(likeState)
    }

    fun updateSongState(
        coverUri: String,
        title: String,
        singer: String,
        playingState: Boolean,
        songId: String,
        songIndex: Int = 0,
        album: String = ""
    ) {
        currentSongState.updateSongState(coverUri, title, singer, playingState, songId, songIndex, album)
    }

    fun loadAlbum(albumId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.provideAlbumById(albumId).collect { result ->
                _album.value = result
            }
        }
    }

    fun loadPlaylist(playlistId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.providePlaylistById(playlistId).collect { result ->
                _album.value = result
            }
        }
    }

    fun loadPlaylistCollection(collectionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.providePlaylistCollectionById(collectionId).collect { result ->
                _album.value = result
            }
        }
    }

    init {
        fetchSongs()
        fetchAlbums()
    }

    private fun fetchSongs() = viewModelScope.launch(Dispatchers.IO) {
        repository.provideSongs().collect { songs ->
            _songs.value = songs
        }
    }

    private fun fetchAlbums() = viewModelScope.launch(Dispatchers.IO) {
        repository.provideAlbums().collect { album ->
            _albums.value = album
        }
    }
}
