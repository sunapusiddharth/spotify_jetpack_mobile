package com.music.stream.neptune.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.stream.neptune.auth.UserSessionManager
import com.music.stream.neptune.data.api.Response
import com.music.stream.neptune.data.entity.AlbumsModel
import com.music.stream.neptune.data.entity.HomePageInfoModel
import com.music.stream.neptune.data.entity.HomePageSectionModel
import com.music.stream.neptune.data.entity.PodcastModel
import com.music.stream.neptune.data.entity.RadioStationModel
import com.music.stream.neptune.data.entity.UserLikedEntityModel
import com.music.stream.neptune.data.entity.UserHistoryEntityModel
import com.music.stream.neptune.data.entity.UserHistoryPageModel
import com.music.stream.neptune.data.entity.UserLikesPageModel
import com.music.stream.neptune.data.entity.UserPlaylistModel
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
    private val loadedLibraryLikes = mutableListOf<UserLikedEntityModel>()
    private val loadedLibraryHistory = mutableListOf<UserHistoryEntityModel>()
    private var nextHomePage = 1
    private var nextLibraryLikesPage = 1
    private var nextLibraryHistoryPage = 1

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

    private val _topStations: MutableStateFlow<Response<List<RadioStationModel>>> =
        MutableStateFlow(Response.Loading())
    val topStations: StateFlow<Response<List<RadioStationModel>>> = _topStations

    private val _topPodcasts: MutableStateFlow<Response<List<PodcastModel>>> =
        MutableStateFlow(Response.Loading())
    val topPodcasts: StateFlow<Response<List<PodcastModel>>> = _topPodcasts

    private val _historyEntries: MutableStateFlow<Response<List<UserHistoryEntityModel>>> =
        MutableStateFlow(Response.Loading())
    val historyEntries: StateFlow<Response<List<UserHistoryEntityModel>>> = _historyEntries

    private val _libraryLikesPage: MutableStateFlow<Response<UserLikesPageModel>> =
        MutableStateFlow(Response.Loading())
    val libraryLikesPage: StateFlow<Response<UserLikesPageModel>> = _libraryLikesPage

    private val _libraryHistoryPage: MutableStateFlow<Response<UserHistoryPageModel>> =
        MutableStateFlow(Response.Loading())
    val libraryHistoryPage: StateFlow<Response<UserHistoryPageModel>> = _libraryHistoryPage

    private val _libraryPlaylists: MutableStateFlow<Response<List<UserPlaylistModel>>> =
        MutableStateFlow(Response.Loading())
    val libraryPlaylists: StateFlow<Response<List<UserPlaylistModel>>> = _libraryPlaylists

    private val _isLoadingMoreLibraryLikes = MutableStateFlow(false)
    val isLoadingMoreLibraryLikes: StateFlow<Boolean> = _isLoadingMoreLibraryLikes

    private val _hasMoreLibraryLikes = MutableStateFlow(true)
    val hasMoreLibraryLikes: StateFlow<Boolean> = _hasMoreLibraryLikes

    private val _isLoadingMoreLibraryHistory = MutableStateFlow(false)
    val isLoadingMoreLibraryHistory: StateFlow<Boolean> = _isLoadingMoreLibraryHistory

    private val _hasMoreLibraryHistory = MutableStateFlow(true)
    val hasMoreLibraryHistory: StateFlow<Boolean> = _hasMoreLibraryHistory

    private val _availableSongsPage: MutableStateFlow<Response<SongsPageResponse>> =
        MutableStateFlow(Response.Loading())
    val availableSongsPage: StateFlow<Response<SongsPageResponse>> = _availableSongsPage

    init {
        fetchInitialHomePages()
        fetchUserHistory()
    }

    private fun currentUserIdOrEmail(): String = userSessionManager.userIdOrEmail()

    private fun fetchInitialHomePages() = viewModelScope.launch(Dispatchers.IO) {
        fetchTopStations(page = 1)
        fetchTopPodcasts(page = 1)
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

    private fun fetchUserHistory(page: Int = 1, limit: Int = 20) = viewModelScope.launch(Dispatchers.IO) {
        repository.provideUserHistory(currentUserIdOrEmail(), page, limit).collect { response ->
            _historyEntries.value = when (response) {
                is Response.Success -> Response.Success(response.data.items)
                is Response.Loading -> Response.Loading()
                is Response.Error -> Response.Error(response.error)
            }
        }
    }

    fun fetchLibraryContent(page: Int = 1, limit: Int = 20) = viewModelScope.launch(Dispatchers.IO) {
        fetchLibraryLikes(page = page, limit = limit, reset = true)
        fetchLibraryHistory(page = page, limit = limit, reset = true)
        fetchLibraryPlaylists()
    }

    fun fetchLibraryLikes(page: Int = 1, limit: Int = 20, reset: Boolean = page == 1) = viewModelScope.launch(Dispatchers.IO) {
        if (_isLoadingMoreLibraryLikes.value) return@launch
        if (!reset && !_hasMoreLibraryLikes.value) return@launch

        if (reset) {
            loadedLibraryLikes.clear()
            nextLibraryLikesPage = page
            _hasMoreLibraryLikes.value = true
            _libraryLikesPage.value = Response.Loading()
        } else {
            _isLoadingMoreLibraryLikes.value = true
        }

        repository.provideUserLikes(currentUserIdOrEmail(), page, limit).collect { response ->
            _libraryLikesPage.value = when (response) {
                is Response.Success -> {
                    if (reset) {
                        loadedLibraryLikes.clear()
                    }
                    response.data.items.forEach { item ->
                        val exists = loadedLibraryLikes.any { it.entityId == item.entityId && it.entityType == item.entityType }
                        if (!exists) loadedLibraryLikes.add(item)
                    }
                    nextLibraryLikesPage = response.data.page + 1
                    _hasMoreLibraryLikes.value = response.data.hasMore
                    Response.Success(
                        response.data.copy(
                            items = loadedLibraryLikes.toList()
                        )
                    )
                }
                is Response.Loading -> if (reset) Response.Loading() else _libraryLikesPage.value
                is Response.Error -> response
            }

            if (!reset) {
                _isLoadingMoreLibraryLikes.value = false
            }
        }
    }

    fun fetchLibraryHistory(page: Int = 1, limit: Int = 20, reset: Boolean = page == 1) = viewModelScope.launch(Dispatchers.IO) {
        if (_isLoadingMoreLibraryHistory.value) return@launch
        if (!reset && !_hasMoreLibraryHistory.value) return@launch

        if (reset) {
            loadedLibraryHistory.clear()
            nextLibraryHistoryPage = page
            _hasMoreLibraryHistory.value = true
            _libraryHistoryPage.value = Response.Loading()
        } else {
            _isLoadingMoreLibraryHistory.value = true
        }

        repository.provideUserHistory(currentUserIdOrEmail(), page, limit).collect { response ->
            _libraryHistoryPage.value = when (response) {
                is Response.Success -> {
                    if (reset) {
                        loadedLibraryHistory.clear()
                    }
                    response.data.items.forEach { item ->
                        val exists = loadedLibraryHistory.any { it.entityId == item.entityId && it.entityType == item.entityType }
                        if (!exists) loadedLibraryHistory.add(item)
                    }
                    nextLibraryHistoryPage = response.data.page + 1
                    _hasMoreLibraryHistory.value = response.data.hasMore
                    Response.Success(
                        response.data.copy(
                            items = loadedLibraryHistory.toList()
                        )
                    )
                }
                is Response.Loading -> if (reset) Response.Loading() else _libraryHistoryPage.value
                is Response.Error -> response
            }

            if (!reset) {
                _isLoadingMoreLibraryHistory.value = false
            }
        }
    }

    fun loadMoreLibraryLikes(limit: Int = 20) {
        fetchLibraryLikes(page = nextLibraryLikesPage, limit = limit, reset = false)
    }

    fun loadMoreLibraryHistory(limit: Int = 20) {
        fetchLibraryHistory(page = nextLibraryHistoryPage, limit = limit, reset = false)
    }

    fun fetchLibraryPlaylists() = viewModelScope.launch(Dispatchers.IO) {
        repository.provideUserPlaylists(currentUserIdOrEmail()).collect { response ->
            _libraryPlaylists.value = response
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

    private fun fetchTopPodcasts(page: Int = 1) = viewModelScope.launch(Dispatchers.IO) {
        repository.provideBrowsePodcasts(page).collect { response ->
            _topPodcasts.value = when (response) {
                is Response.Loading -> Response.Loading()
                is Response.Success -> Response.Success(response.data.results)
                is Response.Error -> Response.Error(response.error)
            }
        }
    }

    fun fetchAvailableSongs(skip: Int = 0, limit: Int = 50) = viewModelScope.launch(Dispatchers.IO) {
        repository.provideAllAvailableSongs(skip, limit).collect { response ->
            _availableSongsPage.value = response
        }
    }
}
