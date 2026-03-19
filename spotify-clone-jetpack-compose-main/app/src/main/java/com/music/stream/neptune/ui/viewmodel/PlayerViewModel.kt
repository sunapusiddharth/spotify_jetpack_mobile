package com.music.stream.neptune.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.stream.neptune.data.api.Response
import com.music.stream.neptune.data.entity.PodcastEpisodeModel
import com.music.stream.neptune.data.entity.PodcastModel
import com.music.stream.neptune.data.entity.QueueUpdateModel
import com.music.stream.neptune.data.entity.RadioStationModel
import com.music.stream.neptune.data.entity.SongsModel
import com.music.stream.neptune.data.entity.UserPlaylistModel
import com.music.stream.neptune.di.CurrentSongState
import com.music.stream.neptune.di.PlaybackMediaType
import com.music.stream.neptune.di.SongPlayer
import com.music.stream.neptune.auth.UserSessionManager
import com.music.stream.neptune.ui.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val currentSongState: CurrentSongState,
    private val repository: AppRepository,
    private val userSessionManager: UserSessionManager
) : ViewModel() {
    private var syncedPlaylistAlbumId: String? = null

    private fun currentUserIdOrEmail(): String {
        return userSessionManager.userIdOrEmail()
    }

    val currentSongTitle: State<String> get() = currentSongState.title
    val currentSongSinger: State<String> get() = currentSongState.singer
    val currentSongCoverUri: State<String> get() = currentSongState.coverUri
    val currentSongPlayingState: State<Boolean> get() = currentSongState.playingState
    val currentSongIndex: State<Int> get() = currentSongState.songIndex
    val currentSongAlbum: State<String> get() = currentSongState.album
    val mediaType: State<PlaybackMediaType> get() = currentSongState.mediaType
    val songQueue: State<List<SongsModel>> get() = currentSongState.songQueue
    val radioQueue: State<List<RadioStationModel>> get() = currentSongState.radioQueue
    val podcastQueue: State<List<PodcastEpisodeModel>> get() = currentSongState.podcastQueue
    val radioIndex: State<Int> get() = currentSongState.radioIndex
    val podcastIndex: State<Int> get() = currentSongState.podcastIndex
    val activePodcast: State<PodcastModel?> get() = currentSongState.activePodcast
    val currentSongId: State<String> get() = currentSongState.songId

    val shuffleState = currentSongState.shuffle
    val repeatState = currentSongState.repeat
    val likeState = currentSongState.likeState
    val likedSongIds: StateFlow<Set<String>> = repository.observeLikedSongIds()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage

    private val _userPlaylists: MutableStateFlow<Response<List<UserPlaylistModel>>> =
        MutableStateFlow(Response.Loading())
    val userPlaylists: StateFlow<Response<List<UserPlaylistModel>>> = _userPlaylists

    private val _songs: MutableStateFlow<Response<List<SongsModel>>> =
        MutableStateFlow(Response.Loading())
    val songs: StateFlow<Response<List<SongsModel>>> = _songs

    val playingArtist by mutableStateOf(currentSongSinger.value)
    private var lastPodcastProgressCheckpointSec: Long = 0
    private var observedUserId: String? = null

    init {
        fetchSongs()
        observeUserLikes()
        observeLikedSongState()
    }

    private fun observeUserLikes() = viewModelScope.launch {
        userSessionManager.session.collectLatest { session ->
            val userId = session?.idOrEmail.orEmpty()
            if (userId == observedUserId) return@collectLatest

            observedUserId = userId
            if (userId.isBlank()) {
                repository.clearCachedUserProfile()
                syncCurrentSongLikeState()
                return@collectLatest
            }

            repository.provideUserById(userId).collect { result ->
                if (result is Response.Success) {
                    syncCurrentSongLikeState()
                }
            }
        }
    }

    private fun observeLikedSongState() = viewModelScope.launch {
        likedSongIds.collectLatest {
            syncCurrentSongLikeState()
        }
    }

    private fun syncCurrentSongLikeState() {
        val isLikedSong = mediaType.value == PlaybackMediaType.SONG &&
            currentSongId.value.isNotBlank() &&
            likedSongIds.value.contains(currentSongId.value)
        currentSongState.updateLikeState(isLikedSong)
    }

    fun startSongPlayback(
        queueSongs: List<SongsModel>,
        startIndex: Int,
        album: String,
        context: Context
    ) {
        if (queueSongs.isEmpty()) return
        val safeIndex = startIndex.coerceIn(0, queueSongs.lastIndex)
        val song = queueSongs[safeIndex]
        currentSongState.setSongQueue(queueSongs, safeIndex)
        updateSongState(
            coverUri = song.coverUri,
            title = song.title,
            singer = song.singer,
            playingState = true,
            songId = song.id,
            songIndex = safeIndex,
            album = album
        )
        SongPlayer.playSong(song, context)
    }

    fun playSongQueueFromPlaylist(
        queueSongs: List<SongsModel>,
        startIndex: Int,
        album: String,
        context: Context
    ) = viewModelScope.launch(Dispatchers.IO) {
        if (queueSongs.isEmpty()) return@launch

        val userId = currentUserIdOrEmail()
        if (userId.isBlank()) {
            _actionMessage.value = "Login required"
            return@launch
        }

        if (syncedPlaylistAlbumId == album) {
            withContext(Dispatchers.Main) {
                startSongPlayback(queueSongs, startIndex, album, context)
            }
            return@launch
        }

        repository.provideAddPlaylistToQueue(userId, queueSongs.map { it.id }).collect { result ->
            when (result) {
                is Response.Success -> {
                    syncedPlaylistAlbumId = album
                    withContext(Dispatchers.Main) {
                        startSongPlayback(queueSongs, startIndex, album, context)
                        _actionMessage.value = "Queue synced from playlist"
                    }
                }
                is Response.Error -> {
                    syncedPlaylistAlbumId = album
                    withContext(Dispatchers.Main) {
                        startSongPlayback(queueSongs, startIndex, album, context)
                        _actionMessage.value = "Queue sync failed, playing locally"
                    }
                }
                else -> Unit
            }
        }
    }

    fun syncSongQueue(queueSongs: List<SongsModel>, currentSongId: String) {
        if (queueSongs.isEmpty()) return
        val idx = queueSongs.indexOfFirst { it.id == currentSongId }.coerceAtLeast(0)
        currentSongState.setSongQueue(queueSongs, idx)
    }

    private fun applyQueueUpdate(update: QueueUpdateModel, context: Context) {
        val backendQueue = if (update.updatedQueue.isNotEmpty()) update.updatedQueue else listOf(update.song)
        val nextIndex = backendQueue.indexOfFirst { it.id == update.song.id }.coerceAtLeast(0)
        startSongPlayback(backendQueue, nextIndex, currentSongAlbum.value, context)
    }

    fun startRadioPlayback(
        queue: List<RadioStationModel>,
        startIndex: Int,
        context: Context
    ) {
        if (queue.isEmpty()) return
        val safeIndex = startIndex.coerceIn(0, queue.lastIndex)
        val station = queue[safeIndex]
        currentSongState.setRadioQueue(queue, safeIndex)
        updateSongState(
            coverUri = station.coverUri,
            title = station.name,
            singer = station.country,
            playingState = true,
            songId = station.id,
            songIndex = safeIndex,
            album = "radio"
        )
        SongPlayer.playSong(station.stream_url, context, station.name, station.country, station.coverUri)
        viewModelScope.launch(Dispatchers.IO) {
            val userId = currentUserIdOrEmail()
            if (userId.isNotBlank()) {
                repository.provideUserListenedStation(userId, station.id, 0).collect { }
            }
        }
    }

    fun startPodcastPlayback(
        podcast: PodcastModel,
        queue: List<PodcastEpisodeModel>,
        startIndex: Int,
        context: Context
    ) {
        if (queue.isEmpty()) return
        val safeIndex = startIndex.coerceIn(0, queue.lastIndex)
        val episode = queue[safeIndex]
        currentSongState.setPodcastQueue(podcast, queue, safeIndex)
        updateSongState(
            coverUri = if (episode.thumbnail.isNotEmpty()) episode.thumbnail else podcast.image,
            title = episode.title,
            singer = podcast.author,
            playingState = true,
            songId = episode.id,
            songIndex = safeIndex,
            album = podcast.id
        )
        SongPlayer.playSong(
            episode.url,
            context,
            episode.title,
            podcast.author,
            if (episode.thumbnail.isNotEmpty()) episode.thumbnail else podcast.image
        )
        viewModelScope.launch(Dispatchers.IO) {
            val userId = currentUserIdOrEmail()
            if (userId.isNotBlank()) {
                repository.provideUserListenedPodcastsAction(userId, podcast.id, 0, episode.id).collect { }
            }
        }
    }

    fun playNext(context: Context) {
        when (mediaType.value) {
            PlaybackMediaType.SONG -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val userId = currentUserIdOrEmail()
                    if (userId.isBlank()) {
                        _actionMessage.value = "Login required"
                        return@launch
                    }
                    repository.provideNextQueueItem(userId).collect { result ->
                        when (result) {
                            is Response.Success -> withContext(Dispatchers.Main) {
                                applyQueueUpdate(result.data, context)
                            }
                            is Response.Error -> _actionMessage.value = "Failed to fetch next song"
                            else -> Unit
                        }
                    }
                }
            }
            PlaybackMediaType.RADIO -> {
                val queue = radioQueue.value
                if (queue.isEmpty()) return
                val nextIndex = if (radioIndex.value < queue.lastIndex) radioIndex.value + 1 else 0
                startRadioPlayback(queue, nextIndex, context)
            }
            PlaybackMediaType.PODCAST -> {
                val queue = podcastQueue.value
                val podcast = activePodcast.value ?: return
                if (queue.isEmpty()) return
                val nextIndex = if (podcastIndex.value < queue.lastIndex) podcastIndex.value + 1 else 0
                startPodcastPlayback(podcast, queue, nextIndex, context)
            }
        }
    }

    fun playPrevious(context: Context) {
        when (mediaType.value) {
            PlaybackMediaType.SONG -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val userId = currentUserIdOrEmail()
                    if (userId.isBlank()) {
                        _actionMessage.value = "Login required"
                        return@launch
                    }
                    repository.providePrevQueueItem(userId).collect { result ->
                        when (result) {
                            is Response.Success -> withContext(Dispatchers.Main) {
                                applyQueueUpdate(result.data, context)
                            }
                            is Response.Error -> _actionMessage.value = "Failed to fetch previous song"
                            else -> Unit
                        }
                    }
                }
            }
            PlaybackMediaType.RADIO -> {
                val queue = radioQueue.value
                if (queue.isEmpty()) return
                val prevIndex = if (radioIndex.value > 0) radioIndex.value - 1 else queue.lastIndex
                startRadioPlayback(queue, prevIndex, context)
            }
            PlaybackMediaType.PODCAST -> {
                val queue = podcastQueue.value
                val podcast = activePodcast.value ?: return
                if (queue.isEmpty()) return
                val prevIndex = if (podcastIndex.value > 0) podcastIndex.value - 1 else queue.lastIndex
                startPodcastPlayback(podcast, queue, prevIndex, context)
            }
        }
    }

    fun loadUserPlaylists() = viewModelScope.launch(Dispatchers.IO) {
        val userId = currentUserIdOrEmail()
        if (userId.isBlank()) {
            _actionMessage.value = "Login required"
            return@launch
        }
        repository.provideUserPlaylists(userId).collect { _userPlaylists.value = it }
    }

    fun addCurrentSongToPlaylist(playlistId: String) = viewModelScope.launch(Dispatchers.IO) {
            val userId = currentUserIdOrEmail()
            if (userId.isBlank()) {
                _actionMessage.value = "Login required"
                return@launch
            }
            val songId = currentSongId.value
            if (songId.isBlank()) return@launch
            repository.provideAddSongToPlaylist(
                userId = userId,
                songId = songId,
                playlistIds = listOf(playlistId),
                posterPath = currentSongCoverUri.value
            ).collect { result ->
                _actionMessage.value = when (result) {
                    is Response.Success -> "Added to playlist"
                    is Response.Error -> "Add to playlist failed"
                    else -> _actionMessage.value
                }
            }
        }

    fun saveCurrentSongToPlaylists(selectedPlaylistIds: Set<String>, newPlaylistName: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val userId = currentUserIdOrEmail()
            if (userId.isBlank()) {
                _actionMessage.value = "Login required"
                return@launch
            }

            val songId = currentSongId.value
            if (songId.isBlank()) return@launch

            val targetPlaylistIds = selectedPlaylistIds.toMutableSet()
            val trimmedName = newPlaylistName.trim()

            if (trimmedName.isNotEmpty()) {
                var createFailed = false
                repository.provideCreateNewPlaylist(userId, trimmedName, currentSongCoverUri.value).collect { result ->
                    when (result) {
                        is Response.Success -> {
                            val createdPlaylist = result.data
                            targetPlaylistIds.add(createdPlaylist.id)
                            val currentPlaylists = (_userPlaylists.value as? Response.Success)?.data.orEmpty()
                            _userPlaylists.value = Response.Success(
                                currentPlaylists + createdPlaylist
                            )
                        }
                        is Response.Error -> {
                            createFailed = true
                            _actionMessage.value = "Create playlist failed"
                        }
                        else -> Unit
                    }
                }
                if (createFailed) return@launch
            }

            if (targetPlaylistIds.isEmpty()) {
                _actionMessage.value = "Select or create a playlist"
                return@launch
            }

            repository.provideAddSongToPlaylist(
                userId = userId,
                songId = songId,
                playlistIds = targetPlaylistIds.toList(),
                posterPath = currentSongCoverUri.value
            ).collect { result ->
                when (result) {
                    is Response.Success -> {
                        val currentPlaylists = (_userPlaylists.value as? Response.Success)?.data.orEmpty()
                        _userPlaylists.value = Response.Success(
                            currentPlaylists.map { playlist ->
                                if (targetPlaylistIds.contains(playlist.id) && !playlist.tracks.contains(songId)) {
                                    playlist.copy(tracks = playlist.tracks + songId)
                                } else {
                                    playlist
                                }
                            }
                        )
                        _actionMessage.value = if (trimmedName.isNotEmpty()) {
                            "Playlist created and song added"
                        } else {
                            "Added to playlist"
                        }
                    }
                    is Response.Error -> {
                        _actionMessage.value = if (trimmedName.isNotEmpty()) {
                            "Playlist created, but song add failed"
                        } else {
                            "Add to playlist failed"
                        }
                    }
                    else -> Unit
                }
            }
        }

    fun requestCurrentTrackAddition() = viewModelScope.launch(Dispatchers.IO) {
        val userId = currentUserIdOrEmail()
        if (userId.isBlank()) {
            _actionMessage.value = "Login required"
            return@launch
        }
        val songId = currentSongId.value
        if (songId.isBlank() || mediaType.value != PlaybackMediaType.SONG) return@launch
        repository.provideRequestTrackAddition(userId, songId).collect { result ->
            _actionMessage.value = when (result) {
                is Response.Success -> "Track request sent"
                is Response.Error -> "Track request failed"
                else -> _actionMessage.value
            }
        }
    }

    fun toggleLikeCurrentMedia() = viewModelScope.launch(Dispatchers.IO) {
        if (mediaType.value != PlaybackMediaType.SONG) return@launch

        val userId = currentUserIdOrEmail()
        if (userId.isBlank()) {
            _actionMessage.value = "Login required"
            return@launch
        }
        val trackId = currentSongId.value
        if (trackId.isBlank()) return@launch

        toggleSongLikeInternal(userId, trackId, likedSongIds.value.contains(trackId))
    }

    fun toggleSongLike(songId: String) = viewModelScope.launch(Dispatchers.IO) {
        val userId = currentUserIdOrEmail()
        if (userId.isBlank()) {
            _actionMessage.value = "Login required"
            return@launch
        }
        if (songId.isBlank()) return@launch

        toggleSongLikeInternal(userId, songId, likedSongIds.value.contains(songId))
    }

    private suspend fun toggleSongLikeInternal(userId: String, songId: String, currentlyLiked: Boolean) {
        val nextState = !currentlyLiked
        repository.provideLikeDislikeSong(userId, nextState, songId).collect { result ->
            when (result) {
                is Response.Success -> {
                    repository.updateCachedSongLike(userId, songId, nextState)
                    if (currentSongId.value == songId) {
                        updateLikeState(nextState)
                    }
                    _actionMessage.value = if (nextState) "Added to favorites" else "Removed from favorites"
                }
                is Response.Error -> _actionMessage.value = "Favorite update failed"
                else -> Unit
            }
        }
    }

    fun onPlaybackProgress(secondsPlayed: Long) {
        if (mediaType.value != PlaybackMediaType.PODCAST) return
        if (secondsPlayed <= 0L || secondsPlayed % 30L != 0L || secondsPlayed == lastPodcastProgressCheckpointSec) return

        val podcast = activePodcast.value ?: return
        val queue = podcastQueue.value
        if (queue.isEmpty()) return
        val idx = podcastIndex.value.coerceIn(0, queue.lastIndex)
        val episode = queue[idx]

        lastPodcastProgressCheckpointSec = secondsPlayed
        viewModelScope.launch(Dispatchers.IO) {
            val userId = currentUserIdOrEmail()
            if (userId.isNotBlank()) {
                repository.provideUserListenedPodcastsAction(userId, podcast.id, secondsPlayed.toInt(), episode.id).collect { }
            }
        }
    }

    private fun fetchSongs() = viewModelScope.launch(Dispatchers.IO) {
        repository.provideSongs().collect { songs ->
            _songs.value = songs
        }
    }

    fun formatDuration(durationMillis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis)
        val seconds =
            TimeUnit.MILLISECONDS.toSeconds(durationMillis) - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%01d:%02d", minutes, seconds)
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
        syncCurrentSongLikeState()
    }

    fun updateShuffleState(shuffleState: Boolean) {
        currentSongState.updateShuffleState(shuffleState)
    }

    fun updateRepeatState(repeatState: Boolean) {
        currentSongState.updateRepeatState(repeatState)
    }

    fun updateLikeState(likeState: Boolean) {
        currentSongState.updateLikeState(likeState)
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }
}
