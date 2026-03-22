package com.music.stream.neptune.ui.viewmodel

import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.stream.neptune.auth.UserSessionManager
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
    private val currentSongState: CurrentSongState,
    private val userSessionManager: UserSessionManager
) : ViewModel() {

    // Local songs pool (from cached albums) for offline/quick search
    private val _songs: MutableStateFlow<Response<List<SongsModel>>> =
        MutableStateFlow(Response.Loading())
    val songs: StateFlow<Response<List<SongsModel>>> = _songs

    // Server-side search results
    private val _searchResults: MutableStateFlow<Response<SearchResultModel>?> =
        MutableStateFlow(null)
    val searchResults: StateFlow<Response<SearchResultModel>?> = _searchResults

    private val _recentSearches: MutableStateFlow<Response<List<String>>> =
        MutableStateFlow(Response.Success(emptyList()))
    val recentSearches: StateFlow<Response<List<String>>> = _recentSearches

    private val _autocompleteSuggestions: MutableStateFlow<Response<List<String>>> =
        MutableStateFlow(Response.Success(emptyList()))
    val autocompleteSuggestions: StateFlow<Response<List<String>>> = _autocompleteSuggestions

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
        loadRecentSearches()
    }

    private fun fetchSongs() = viewModelScope.launch(Dispatchers.IO) {
        repository.provideSongs().collect { songs ->
            _songs.value = songs
        }
    }

    fun loadRecentSearches() = viewModelScope.launch(Dispatchers.IO) {
        if (userSessionManager.userIdOrEmail().isBlank()) {
            _recentSearches.value = Response.Success(emptyList())
            return@launch
        }
        repository.provideRecentSearches().collect { response ->
            _recentSearches.value = response
        }
    }

    fun loadAutocomplete(query: String, limit: Int = 6) {
        if (query.isBlank()) {
            _autocompleteSuggestions.value = Response.Success(emptyList())
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.provideSearchAutocomplete(query, limit).collect { response ->
                _autocompleteSuggestions.value = response
            }
        }
    }

    fun addRecentSearch(query: String) = viewModelScope.launch(Dispatchers.IO) {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank() || userSessionManager.userIdOrEmail().isBlank()) return@launch

        repository.provideAddRecentSearch(normalizedQuery).collect { result ->
            if (result is Response.Success && result.data) {
                loadRecentSearches()
            }
        }
    }

    fun removeRecentSearch(query: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.provideRemoveRecentSearch(query).collect { result ->
            if (result is Response.Success && result.data) {
                loadRecentSearches()
            }
        }
    }

    fun clearRecentSearches() = viewModelScope.launch(Dispatchers.IO) {
        repository.provideClearRecentSearches().collect { result ->
            if (result is Response.Success && result.data) {
                _recentSearches.value = Response.Success(emptyList())
            }
        }
    }

    fun search(query: String, type: String? = null, page: Int = 1) {
        if (query.isBlank()) {
            _searchResults.value = null
            return
        }
        if (userSessionManager.userIdOrEmail().isBlank()) {
            _searchResults.value = Response.Success(SearchResultModel())
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _searchResults.value = Response.Loading()
            repository.provideSearch(query, type, page).collect { result ->
                _searchResults.value = result
            }
        }
    }

    fun submitSearch(query: String, type: String? = null, page: Int = 1) {
        addRecentSearch(query)
        search(query, type, page)
    }
}
