package com.music.stream.neptune.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.stream.neptune.auth.UserSessionManager
import com.music.stream.neptune.data.api.Response
import com.music.stream.neptune.data.entity.AlbumsModel
import com.music.stream.neptune.data.entity.ArtistsModel
import com.music.stream.neptune.data.entity.HomePageInfoModel
import com.music.stream.neptune.data.entity.HomePageSectionModel
import com.music.stream.neptune.data.entity.PodcastModel
import com.music.stream.neptune.data.entity.RadioStationModel
import com.music.stream.neptune.data.entity.SongsModel
import com.music.stream.neptune.data.entity.UserHistoryEntityModel
import com.music.stream.neptune.data.network.SongsPageResponse
import com.music.stream.neptune.ui.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AppRepository,
    private val userSessionManager: UserSessionManager
) : ViewModel() {
    private val loadedHomeSections = mutableListOf<HomePageSectionModel>()
    private var nextHomePage = 1

    private val _homePage: MutableStateFlow<Response<HomePageInfoModel>> =
        MutableStateFlow(Response.Loading())
    val homePage: StateFlow<Response<HomePageInfoModel>> = _homePage

    private val _isLoadingNextHomePage = MutableStateFlow(false)
    val isLoadingNextHomePage: StateFlow<Boolean> = _isLoadingNextHomePage

    private val _hasMoreHomePages = MutableStateFlow(true)
    val hasMoreHomePages: StateFlow<Boolean> = _hasMoreHomePages

    private val _albums: MutableStateFlow<Response<List<AlbumsModel>>> =
        MutableStateFlow(Response.Loading())
    val albums: StateFlow<Response<List<AlbumsModel>>> = _albums

    private val _artists: MutableStateFlow<Response<List<ArtistsModel>>> =
        MutableStateFlow(Response.Loading())
    val artists: StateFlow<Response<List<ArtistsModel>>> = _artists

    private val _topStations: MutableStateFlow<Response<List<RadioStationModel>>> =
        MutableStateFlow(Response.Loading())
    val topStations: StateFlow<Response<List<RadioStationModel>>> = _topStations

    private val _topPodcasts: MutableStateFlow<Response<List<PodcastModel>>> =
        MutableStateFlow(Response.Loading())
    val topPodcasts: StateFlow<Response<List<PodcastModel>>> = _topPodcasts

    private val _historyEntries: MutableStateFlow<Response<List<UserHistoryEntityModel>>> =
        MutableStateFlow(Response.Loading())
    val historyEntries: StateFlow<Response<List<UserHistoryEntityModel>>> = _historyEntries

    private val _availableSongsPage: MutableStateFlow<Response<SongsPageResponse>> =
        MutableStateFlow(Response.Loading())
    val availableSongsPage: StateFlow<Response<SongsPageResponse>> = _availableSongsPage

    // Optional home sections; exposed for screens that need these feeds.
    private val _topScoringSongs: MutableStateFlow<Response<List<SongsModel>>> =
        MutableStateFlow(Response.Loading())
    val topScoringSongs: StateFlow<Response<List<SongsModel>>> = _topScoringSongs

    init {
        fetchInitialHomePages()
        fetchUserHistory()
        fetchArtists()
        fetchTopStations()
        fetchTopScoringSongsForUser()
        fetchTopPodcasts()
    }

    private fun currentUserIdOrEmail(): String = userSessionManager.userIdOrEmail()

    private fun fetchInitialHomePages() = viewModelScope.launch(Dispatchers.IO) {
        fetchHomePageInternal(page = 1, reset = true)
        if (_hasMoreHomePages.value) {
            fetchHomePageInternal(page = 2, reset = false)
        }
        if (_hasMoreHomePages.value) {
            fetchHomePageInternal(page = 3, reset = false)
        }
    }

    fun fetchHomePage(page: Int = 1, reset: Boolean = page == 1) = viewModelScope.launch(Dispatchers.IO) {
        fetchHomePageInternal(page = page, reset = reset)
    }

    private suspend fun fetchHomePageInternal(page: Int = 1, reset: Boolean = page == 1) {
        if (_isLoadingNextHomePage.value) return
        if (!reset && !_hasMoreHomePages.value) return

        if (reset) {
            loadedHomeSections.clear()
            nextHomePage = page
            _hasMoreHomePages.value = true
            _homePage.value = Response.Loading()
        } else {
            _isLoadingNextHomePage.value = true
        }

        repository.provideHomePage(currentUserIdOrEmail(), page).collect { home ->
            when (home) {
                is Response.Loading -> {
                    if (reset) {
                        _homePage.value = Response.Loading()
                    }
                }
                is Response.Success -> {
                    val newSections = home.data.results
                    if (reset) {
                        loadedHomeSections.clear()
                    }
                    mergeHomeSections(newSections)
                    nextHomePage = page + 1
                    _hasMoreHomePages.value = newSections.isNotEmpty()
                    _homePage.value = Response.Success(
                        HomePageInfoModel(
                            results = loadedHomeSections.toList(),
                            page = home.data.page
                        )
                    )
                }
                is Response.Error -> {
                    if (loadedHomeSections.isEmpty()) {
                        _homePage.value = home
                    }
                }
            }
            if (!reset) {
                _isLoadingNextHomePage.value = false
            }
        }
    }

    fun loadNextHomePage() {
        fetchHomePage(page = nextHomePage, reset = false)
    }

    private fun mergeHomeSections(newSections: List<HomePageSectionModel>) {
        val existingKeys = loadedHomeSections
            .map { homeSectionKey(it) }
            .toMutableSet()

        newSections.forEach { section ->
            val key = homeSectionKey(section)
            if (key !in existingKeys) {
                loadedHomeSections.add(section)
                existingKeys.add(key)
            }
        }
    }

    private fun homeSectionKey(section: HomePageSectionModel): String {
        return listOf(
            section.id,
            section.path,
            section.label,
            section.cardType,
            section.cards.firstOrNull()?.id.orEmpty()
        ).joinToString("|")
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

    private fun fetchUserHistory(page: Int = 1, limit: Int = 20) = viewModelScope.launch(Dispatchers.IO) {
        repository.provideUserHistory(currentUserIdOrEmail(), page, limit).collect { response ->
            _historyEntries.value = when (response) {
                is Response.Success -> Response.Success(response.data.items)
                is Response.Loading -> Response.Loading()
                is Response.Error -> Response.Error(response.error)
            }
        }
    }

    private fun fetchTopStations(country: String = "US", page: Int = 1) = viewModelScope.launch(Dispatchers.IO) {
        repository.provideBrowseStations(country, page).collect { response ->
            _topStations.value = when (response) {
                is Response.Loading -> Response.Loading()
                is Response.Success -> Response.Success(response.data.results)
                is Response.Error -> Response.Error(response.error)
            }
        }
    }

    fun fetchTopScoringSongs(limit: Int = 10) = viewModelScope.launch(Dispatchers.IO) {
        repository.provideTopScoringSongs(limit).collect { songs ->
            _topScoringSongs.value = songs
        }
    }

    private fun fetchTopScoringSongsForUser(limit: Int = 30) = viewModelScope.launch(Dispatchers.IO) {
        repository.provideTopScoringSongsForUser(currentUserIdOrEmail(), limit).collect { songs ->
            _topScoringSongs.value = songs
        }
    }

    private fun fetchTopPodcasts(page: Int = 1) = viewModelScope.launch(Dispatchers.IO) {
        repository.provideBrowsePodcasts(page).collect { response ->
            _topPodcasts.value = when (response) {
                is Response.Loading -> Response.Loading()
                is Response.Success -> Response.Success(response.data.results)
                is Response.Error -> Response.Error(response.error)
            }
        }
    }

    fun fetchAvailableSongs(skip: Int = 0, limit: Int = 100) = viewModelScope.launch(Dispatchers.IO) {
        repository.provideAllAvailableSongs(skip, limit).collect { response ->
            _availableSongsPage.value = response
        }
    }
}
