package com.music.stream.neptune.ui.viewmodel

import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.stream.neptune.data.api.Response
import com.music.stream.neptune.data.entity.SearchResultModel
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
class SearchViewModel @Inject constructor(
    private val repository: AppRepository,
    private val currentSongState: CurrentSongState
) : ViewModel() {

    // Local songs pool (from cached albums) for offline/quick search
    private val _songs: MutableStateFlow<Response<List<SongsModel>>> =
        MutableStateFlow(Response.Loading())
    val songs: StateFlow<Response<List<SongsModel>>> = _songs

    // Server-side search results
    private val _searchResults: MutableStateFlow<Response<SearchResultModel>?> =
        MutableStateFlow(null)
    val searchResults: StateFlow<Response<SearchResultModel>?> = _searchResults

    val likeState = currentSongState.likeState
    val currentSongId: State<String> get() = currentSongState.songId

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

    init {
        fetchSongs()
    }

    private fun fetchSongs() = viewModelScope.launch(Dispatchers.IO) {
        repository.provideSongs().collect { songs ->
            _songs.value = songs
        }
    }

    fun search(query: String, type: String = "null", page: Int = 1) {
        if (query.isBlank()) {
            _searchResults.value = null
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _searchResults.value = Response.Loading()
            repository.provideSearch(query, type, page).collect { result ->
                _searchResults.value = result
            }
        }
    }
}
