package com.music.stream.neptune.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.stream.neptune.data.api.Response
import com.music.stream.neptune.data.entity.AlbumsModel
import com.music.stream.neptune.data.entity.ArtistsModel
import com.music.stream.neptune.data.entity.SongsModel
import com.music.stream.neptune.ui.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {

    private val _albums: MutableStateFlow<Response<List<AlbumsModel>>> =
        MutableStateFlow(Response.Loading())
    val albums: StateFlow<Response<List<AlbumsModel>>> = _albums

    private val _artists: MutableStateFlow<Response<List<ArtistsModel>>> =
        MutableStateFlow(Response.Loading())
    val artists: StateFlow<Response<List<ArtistsModel>>> = _artists

    // Optional home sections; exposed for screens that need these feeds.
    private val _topScoringSongs: MutableStateFlow<Response<List<SongsModel>>> =
        MutableStateFlow(Response.Loading())
    val topScoringSongs: StateFlow<Response<List<SongsModel>>> = _topScoringSongs

    private val _latestPlaylistCollections: MutableStateFlow<Response<List<AlbumsModel>>> =
        MutableStateFlow(Response.Loading())
    val latestPlaylistCollections: StateFlow<Response<List<AlbumsModel>>> = _latestPlaylistCollections

    init {
        fetchAlbums()
        fetchArtists()
    }

    private fun fetchAlbums() = viewModelScope.launch(Dispatchers.IO) {
        repository.provideAlbums().collect { album ->
            _albums.value = album
        }
    }

    private fun fetchArtists() = viewModelScope.launch(Dispatchers.IO) {
        repository.provideArtists().collect { artist ->
            _artists.value = artist
        }
    }

    fun fetchTopScoringSongs(limit: Int = 10) = viewModelScope.launch(Dispatchers.IO) {
        repository.provideTopScoringSongs(limit).collect { songs ->
            _topScoringSongs.value = songs
        }
    }

    fun fetchLatestPlaylistCollections() = viewModelScope.launch(Dispatchers.IO) {
        repository.provideLatestPlaylistCollections().collect { collections ->
            _latestPlaylistCollections.value = collections
        }
    }
}
