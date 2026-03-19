package com.music.stream.neptune.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.stream.neptune.auth.UserSessionManager
import com.music.stream.neptune.data.api.Response
import com.music.stream.neptune.data.entity.RadioCountryAggModel
import com.music.stream.neptune.data.entity.RadioGenreAggModel
import com.music.stream.neptune.data.entity.toRadioStationModel
import com.music.stream.neptune.data.entity.RadioStationModel
import com.music.stream.neptune.data.network.StationsBrowseResponse
import com.music.stream.neptune.ui.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RadioViewModel @Inject constructor(
    private val repository: AppRepository,
    private val userSessionManager: UserSessionManager
) : ViewModel() {

    private val _countries: MutableStateFlow<Response<List<RadioCountryAggModel>>> =
        MutableStateFlow(Response.Loading())
    val countries: StateFlow<Response<List<RadioCountryAggModel>>> = _countries

    private val _genres: MutableStateFlow<Response<List<RadioGenreAggModel>>> =
        MutableStateFlow(Response.Loading())
    val genres: StateFlow<Response<List<RadioGenreAggModel>>> = _genres

    private val _lastPlayedStations: MutableStateFlow<Response<List<RadioStationModel>>> =
        MutableStateFlow(Response.Loading())
    val lastPlayedStations: StateFlow<Response<List<RadioStationModel>>> = _lastPlayedStations

    private val _topStationsByVotes: MutableStateFlow<Response<StationsBrowseResponse>> =
        MutableStateFlow(Response.Loading())
    val topStationsByVotes: StateFlow<Response<StationsBrowseResponse>> = _topStationsByVotes

    private val _genreListing: MutableStateFlow<Response<StationsBrowseResponse>> =
        MutableStateFlow(Response.Loading())
    val genreListing: StateFlow<Response<StationsBrowseResponse>> = _genreListing

    private val _isFetchingMoreGenreListing = MutableStateFlow(false)
    val isFetchingMoreGenreListing = _isFetchingMoreGenreListing.asStateFlow()

    private val _hasMoreGenreListing = MutableStateFlow(true)
    val hasMoreGenreListing = _hasMoreGenreListing.asStateFlow()

    // Default to US
    private var currentCountry = "US"
    private var currentGenre = ""
    private var currentGenrePage = 0
    private var currentUserId = ""

    private fun currentUserIdOrEmail(): String = userSessionManager.userIdOrEmail()

    init {
        fetchCountries()
        onCountrySelected("US")
    }

    fun fetchCountries() = viewModelScope.launch(Dispatchers.IO) {
        repository.provideRadioCountryAggs().collect { _countries.value = it }
    }

    fun fetchGenres(country: String) = viewModelScope.launch(Dispatchers.IO) {
        currentCountry = country
        repository.provideRadioGenreAggs(country).collect { _genres.value = it }
    }

    fun fetchLastPlayedStations(userId: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.provideUserHistory(userId, page = 1, limit = 20).collect { response ->
            _lastPlayedStations.value = when (response) {
                is Response.Success -> Response.Success(
                    response.data.items
                        .filter { it.isRadioStation && it.s3link.isNotBlank() }
                        .map { it.toRadioStationModel() }
                )
                is Response.Loading -> Response.Loading()
                is Response.Error -> Response.Error(response.error)
            }
        }
    }

    fun browseStations(country: String, page: Int = 1) = viewModelScope.launch(Dispatchers.IO) {
        repository.provideBrowseStations(country, page).collect { _topStationsByVotes.value = it }
    }

    fun fetchGenreListing(genre: String, page: Int = 1, append: Boolean = false) =
        viewModelScope.launch(Dispatchers.IO) {
            if (append && _isFetchingMoreGenreListing.value) return@launch

            if (!append) {
                currentGenre = genre
                currentGenrePage = 0
                _hasMoreGenreListing.value = true
                _genreListing.value = Response.Loading()
            } else {
                _isFetchingMoreGenreListing.value = true
            }

            val flow = if (genre.isBlank()) {
                repository.provideBrowseStations(currentCountry, page)
            } else {
                repository.provideBrowseStationsByCountryAndGenre(currentCountry, genre, page)
            }

            flow.collect { incoming ->
                when (incoming) {
                    is Response.Success -> {
                        val incomingRows = incoming.data.results
                        currentGenrePage = incoming.data.page
                        if (append && _genreListing.value is Response.Success) {
                                val existing = (_genreListing.value as Response.Success<StationsBrowseResponse>).data
                            val merged = (existing.results + incomingRows).distinctBy { it.id }
                            val hasNewItems = merged.size > existing.results.size
                            _hasMoreGenreListing.value = incomingRows.isNotEmpty() && hasNewItems
                            _genreListing.value = Response.Success(
                                StationsBrowseResponse(
                                    results = merged,
                                    page = incoming.data.page,
                                    total = merged.size
                                )
                            )
                        } else {
                            _hasMoreGenreListing.value = incomingRows.isNotEmpty()
                            _genreListing.value = incoming
                        }
                        _isFetchingMoreGenreListing.value = false
                    }

                    is Response.Error -> {
                        if (!append) {
                            _genreListing.value = incoming
                            _hasMoreGenreListing.value = false
                        }
                        _isFetchingMoreGenreListing.value = false
                    }

                    is Response.Loading -> {
                        if (!append) _genreListing.value = incoming
                    }
                }
            }
        }

    fun loadNextGenrePage() {
        if (!_hasMoreGenreListing.value || _isFetchingMoreGenreListing.value) return
        fetchGenreListing(currentGenre, currentGenrePage + 1, append = true)
    }

    fun onCountrySelected(country: String) {
        currentCountry = country
        currentUserId = currentUserIdOrEmail()
        if (currentUserId.isNotBlank()) {
            fetchLastPlayedStations(currentUserId)
        }
        fetchGenres(country)
        browseStations(country, 1)
        fetchGenreListing(genre = "", page = 1, append = false)
    }
}
