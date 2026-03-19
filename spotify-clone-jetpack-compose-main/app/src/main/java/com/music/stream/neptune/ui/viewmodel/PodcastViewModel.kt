package com.music.stream.neptune.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.stream.neptune.auth.UserSessionManager
import com.music.stream.neptune.data.api.Response
import com.music.stream.neptune.data.entity.PodcastEpisodeModel
import com.music.stream.neptune.data.entity.PodcastModel
import com.music.stream.neptune.data.entity.UserHistoryEntityModel
import com.music.stream.neptune.data.network.PodcastBrowseResponse
import com.music.stream.neptune.data.network.PodcastEpisodesResponse
import com.music.stream.neptune.ui.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PodcastViewModel @Inject constructor(
    private val repository: AppRepository,
    private val userSessionManager: UserSessionManager
) : ViewModel() {

    private val _podcasts: MutableStateFlow<Response<PodcastBrowseResponse>> =
        MutableStateFlow(Response.Loading())
    val podcasts: StateFlow<Response<PodcastBrowseResponse>> = _podcasts

    private val _genres: MutableStateFlow<Response<List<String>>> =
        MutableStateFlow(Response.Loading())
    val genres: StateFlow<Response<List<String>>> = _genres

    private val _selectedPodcast: MutableStateFlow<Response<PodcastModel>> =
        MutableStateFlow(Response.Loading())
    val selectedPodcast: StateFlow<Response<PodcastModel>> = _selectedPodcast

    private val _episodes: MutableStateFlow<Response<PodcastEpisodesResponse>> =
        MutableStateFlow(Response.Loading())
    val episodes: StateFlow<Response<PodcastEpisodesResponse>> = _episodes

    private val _genrePodcasts: MutableStateFlow<Response<PodcastBrowseResponse>> =
        MutableStateFlow(Response.Loading())
    val genrePodcasts: StateFlow<Response<PodcastBrowseResponse>> = _genrePodcasts

    private val _historyEntries: MutableStateFlow<Response<List<UserHistoryEntityModel>>> =
        MutableStateFlow(Response.Loading())
    val historyEntries: StateFlow<Response<List<UserHistoryEntityModel>>> = _historyEntries

    private val _isFetchingMoreGenrePodcasts = MutableStateFlow(false)
    val isFetchingMoreGenrePodcasts = _isFetchingMoreGenrePodcasts.asStateFlow()

    private val _hasMoreGenrePodcasts = MutableStateFlow(true)
    val hasMoreGenrePodcasts = _hasMoreGenrePodcasts.asStateFlow()

    private var activeGenre: String? = null
    private var currentGenrePage: Int = 0

    private val _selectedEpisodeId = MutableStateFlow<String?>(null)
    val selectedEpisodeId = _selectedEpisodeId.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage = _actionMessage.asStateFlow()

    init {
        fetchPodcasts(1)
        fetchGenres()
        fetchHistory()
    }

    private fun currentUserIdOrEmail(): String = userSessionManager.userIdOrEmail()

    fun fetchPodcasts(page: Int) = viewModelScope.launch(Dispatchers.IO) {
        repository.provideBrowsePodcasts(page).collect { _podcasts.value = it }
    }

    fun fetchPodcastsByGenre(genre: String, page: Int = 1) = viewModelScope.launch(Dispatchers.IO) {
        repository.provideBrowsePodcastsByGenre(genre, page).collect { _podcasts.value = it }
    }

    fun fetchGenrePage(genre: String, page: Int = 1, append: Boolean = false) = viewModelScope.launch(Dispatchers.IO) {
        if (append && _isFetchingMoreGenrePodcasts.value) return@launch

        if (!append) {
            activeGenre = genre
            currentGenrePage = 0
            _hasMoreGenrePodcasts.value = true
            _genrePodcasts.value = Response.Loading()
        } else {
            _isFetchingMoreGenrePodcasts.value = true
        }

        repository.provideBrowsePodcastsByGenre(genre, page).collect { incoming ->
            when (incoming) {
                is Response.Success -> {
                    val incomingResults = incoming.data.results
                    currentGenrePage = incoming.data.page

                    if (append && _genrePodcasts.value is Response.Success) {
                        val existing = (_genrePodcasts.value as Response.Success<PodcastBrowseResponse>).data
                        val merged = (existing.results + incomingResults).distinctBy { it.id }
                        val hasNewItems = merged.size > existing.results.size
                        _hasMoreGenrePodcasts.value = incomingResults.isNotEmpty() && hasNewItems
                        _genrePodcasts.value = Response.Success(
                            PodcastBrowseResponse(
                                results = merged,
                                page = incoming.data.page,
                                total = merged.size
                            )
                        )
                    } else {
                        _hasMoreGenrePodcasts.value = incomingResults.isNotEmpty()
                        _genrePodcasts.value = incoming
                    }
                }

                is Response.Error -> {
                    if (!append) {
                        _genrePodcasts.value = incoming
                        _hasMoreGenrePodcasts.value = false
                    }
                    _isFetchingMoreGenrePodcasts.value = false
                }

                is Response.Loading -> {
                    if (!append) _genrePodcasts.value = incoming
                }
            }
            if (incoming is Response.Success) {
                _isFetchingMoreGenrePodcasts.value = false
            }
        }
    }

    fun loadNextGenrePage() {
        val genre = activeGenre ?: return
        if (!_hasMoreGenrePodcasts.value || _isFetchingMoreGenrePodcasts.value) return
        fetchGenrePage(genre = genre, page = currentGenrePage + 1, append = true)
    }

    fun fetchGenres() = viewModelScope.launch(Dispatchers.IO) {
        repository.providePodcastGenres().collect { _genres.value = it }
    }

    fun fetchPodcastById(id: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.providePodcastById(id).collect { _selectedPodcast.value = it }
    }

    fun fetchEpisodes(id: String, page: Int = 1, append: Boolean = false) = viewModelScope.launch(Dispatchers.IO) {
        repository.providePodcastEpisodes(id, page).collect { incoming ->
            if (!append || incoming !is Response.Success || _episodes.value !is Response.Success) {
                _episodes.value = incoming
                return@collect
            }

            val oldData = (_episodes.value as Response.Success<PodcastEpisodesResponse>).data
            val newData = incoming.data
            val merged = oldData.results + newData.results
            _episodes.value = Response.Success(
                PodcastEpisodesResponse(
                    results = merged.distinctBy { it.id },
                    page = newData.page,
                    total = oldData.total + newData.total
                )
            )
        }
    }

    fun userListenedPodcasts(userId: String, podcastId: String, duration: Int, episodeId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val podcastState = _selectedPodcast.value as? Response.Success ?: return@launch
            val episode = (_episodes.value as? Response.Success)
                ?.data
                ?.results
                ?.firstOrNull { it.id == episodeId }
                ?: return@launch
            repository.provideUserListenedPodcastsAction(userId, podcastState.data, episode, duration).collect { }
        }

    fun fetchHistory(page: Int = 1, limit: Int = 20) = viewModelScope.launch(Dispatchers.IO) {
        val userId = currentUserIdOrEmail()
        repository.provideUserHistory(userId, page, limit).collect { response ->
            _historyEntries.value = when (response) {
                is Response.Success -> Response.Success(
                    response.data.items.filter { it.isPodcast || it.isPodcastEpisode }
                )
                is Response.Loading -> Response.Loading()
                is Response.Error -> Response.Error(response.error)
            }
        }
    }

    fun requestPodcastEpisodesPopulation(userId: String, podcastId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.provideRequestPodcastEpisodesPopulation(userId, podcastId).collect { result ->
                when (result) {
                    is Response.Success -> _actionMessage.value = "Podcast episodes requested successfully"
                    is Response.Error -> _actionMessage.value = "Podcast request failed: ${result.error}"
                    is Response.Loading -> Unit
                }
            }
        }

    fun requestPodcastEpisodesPopulation(podcastId: String) {
        val userId = currentUserIdOrEmail()
        if (userId.isBlank()) {
            _actionMessage.value = "Login required"
            return
        }
        requestPodcastEpisodesPopulation(userId, podcastId)
    }

    fun setSelectedEpisode(id: String) {
        _selectedEpisodeId.value = id
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }
}
