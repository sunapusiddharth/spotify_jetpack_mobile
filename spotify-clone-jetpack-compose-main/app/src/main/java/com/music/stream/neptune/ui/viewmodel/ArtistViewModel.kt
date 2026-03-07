package com.music.stream.neptune.ui.viewmodel

import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.stream.neptune.data.api.Response
import com.music.stream.neptune.data.entity.ArtistsModel
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
class ArtistViewModel @Inject constructor(
    private val repository: AppRepository,
    private val currentSongState: CurrentSongState
) : ViewModel() {

    val currentSongPlayingState: State<Boolean> get() = currentSongState.playingState
    val currentSongId: State<String> get() = currentSongState.songId

    // Full artist data (includes popular_songs)
    private val _artist: MutableStateFlow<Response<ArtistsModel?>> =
        MutableStateFlow(Response.Loading())
    val artist: StateFlow<Response<ArtistsModel?>> = _artist

    // Paginated artist songs (page 0)
    private val _artistSongs: MutableStateFlow<Response<List<SongsModel>>> =
        MutableStateFlow(Response.Loading())
    val artistSongs: StateFlow<Response<List<SongsModel>>> = _artistSongs

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

    fun loadArtist(artistId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.provideArtistById(artistId).collect { result ->
                _artist.value = result
                // If artist data loaded successfully, also load songs
                if (result is Response.Success && result.data != null) {
                    loadArtistSongs(artistId)
                }
            }
        }
    }

    private fun loadArtistSongs(artistId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.provideArtistSongs(artistId, 0).collect { result ->
                when (result) {
                    is Response.Success -> _artistSongs.value = Response.Success(result.data.results)
                    is Response.Loading -> _artistSongs.value = Response.Loading()
                    is Response.Error -> _artistSongs.value = Response.Error(result.error)
                }
            }
        }
    }
}
