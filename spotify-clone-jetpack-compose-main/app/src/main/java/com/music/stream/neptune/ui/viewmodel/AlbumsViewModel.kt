package com.music.stream.neptune.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.stream.neptune.data.api.Response
import com.music.stream.neptune.data.entity.AlbumsModel
import com.music.stream.neptune.data.network.AlbumsBrowseResponse
import com.music.stream.neptune.ui.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _freshAlbums = MutableStateFlow<Response<List<AlbumsModel>>>(Response.Loading())
    val freshAlbums: StateFlow<Response<List<AlbumsModel>>> = _freshAlbums

    private val _allAlbums = MutableStateFlow<Response<AlbumsBrowseResponse>>(Response.Loading())
    val allAlbums: StateFlow<Response<AlbumsBrowseResponse>> = _allAlbums

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private var currentPage = 0

    init {
        fetchFreshAlbums()
        fetchAllAlbums(page = 1, append = false)
    }

    fun fetchFreshAlbums() = viewModelScope.launch(Dispatchers.IO) {
        repository.provideAlbums().collect { _freshAlbums.value = it }
    }

    fun fetchAllAlbums(page: Int, append: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        if (append && _isLoadingMore.value) return@launch
        if (append) {
            _isLoadingMore.value = true
        } else {
            _allAlbums.value = Response.Loading()
            _hasMore.value = true
            currentPage = 0
        }

        repository.provideAllAlbums(page).collect { incoming ->
            when (incoming) {
                is Response.Success -> {
                    currentPage = incoming.data.page
                    if (append && _allAlbums.value is Response.Success) {
                        val existing = (_allAlbums.value as Response.Success).data
                        val merged = (existing.results + incoming.data.results).distinctBy { it.id }
                        _hasMore.value = incoming.data.results.isNotEmpty() && merged.size > existing.results.size
                        _allAlbums.value = Response.Success(
                            AlbumsBrowseResponse(
                                results = merged,
                                page = incoming.data.page,
                                total = merged.size
                            )
                        )
                    } else {
                        _hasMore.value = incoming.data.results.isNotEmpty()
                        _allAlbums.value = incoming
                    }
                    _isLoadingMore.value = false
                }
                is Response.Error -> {
                    if (!append) {
                        _allAlbums.value = incoming
                    }
                    _hasMore.value = false
                    _isLoadingMore.value = false
                }
                is Response.Loading -> {
                    if (!append) {
                        _allAlbums.value = incoming
                    }
                }
            }
        }
    }

    fun loadNextPage() {
        if (_isLoadingMore.value || !_hasMore.value) return
        fetchAllAlbums(page = currentPage + 1, append = true)
    }
}