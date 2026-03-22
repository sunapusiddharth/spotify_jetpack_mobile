package com.music.stream.neptune.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.stream.neptune.data.entity.PersistedPlaybackSnapshot
import com.music.stream.neptune.data.api.Response
import com.music.stream.neptune.data.entity.AlbumsModel
import com.music.stream.neptune.data.entity.PodcastEpisodeModel
import com.music.stream.neptune.data.entity.PodcastModel
import com.music.stream.neptune.data.entity.RadioStationModel
import com.music.stream.neptune.data.entity.SongsModel
import com.music.stream.neptune.data.entity.UserHistoryEntityModel
import com.music.stream.neptune.data.entity.UserPlaylistModel
import com.music.stream.neptune.data.entity.toPodcastEpisodeModel
import com.music.stream.neptune.data.entity.toPodcastModel
import com.music.stream.neptune.data.entity.toRadioStationModel
import com.music.stream.neptune.data.entity.toSongModel
import com.music.stream.neptune.data.preferences.PlaybackPreferences
import com.music.stream.neptune.di.CurrentSongState
import com.music.stream.neptune.di.PlaybackMediaType
import com.music.stream.neptune.di.SongPlayer
import com.music.stream.neptune.auth.UserSessionManager
import com.music.stream.neptune.ui.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val appContext: Context,
    private val currentSongState: CurrentSongState,
    private val repository: AppRepository,
    private val userSessionManager: UserSessionManager,
    private val playbackPreferences: PlaybackPreferences
) : ViewModel() {
    private fun currentUserIdOrEmail(): String {
        return userSessionManager.userIdOrEmail()
    }

    val currentSongTitle: State<String> get() = currentSongState.title
    val currentSongSinger: State<String> get() = currentSongState.singer
    val currentSongCoverUri: State<String> get() = currentSongState.coverUri
    val currentSongPlayingState: State<Boolean> get() = currentSongState.playingState
    val currentSongIndex: State<Int> get() = currentSongState.songIndex
    val currentSongAlbum: State<String> get() = currentSongState.album
    val currentSongAlbumTitle: State<String> get() = currentSongState.albumTitle
    val currentSongAlbumId: State<String> get() = currentSongState.albumId
    val mediaType: State<PlaybackMediaType> get() = currentSongState.mediaType
    val songQueue: State<List<SongsModel>> get() = currentSongState.songQueue
    val radioQueue: State<List<RadioStationModel>> get() = currentSongState.radioQueue
    val podcastQueue: State<List<PodcastEpisodeModel>> get() = currentSongState.podcastQueue
    val radioIndex: State<Int> get() = currentSongState.radioIndex
    val podcastIndex: State<Int> get() = currentSongState.podcastIndex
    val activePodcast: State<PodcastModel?> get() = currentSongState.activePodcast
    val currentSongId: State<String> get() = currentSongState.songId
    val lastPlayedSongs: StateFlow<List<SongsModel>> = playbackPreferences.observeRecentSongs()

    val shuffleState = currentSongState.shuffle
    val repeatState = currentSongState.repeat
    val likeState = currentSongState.likeState
    val likedSongIds: StateFlow<Set<String>> = repository.observeLikedSongIds()
    val likedAlbumIds: StateFlow<Set<String>> = repository.observeLikedAlbumIds()
    val likedEntityKeys: StateFlow<Set<String>> = repository.observeLikedEntityKeys()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage

    private val _isPlayerExpanded = MutableStateFlow(false)
    val isPlayerExpanded: StateFlow<Boolean> = _isPlayerExpanded

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
        restorePersistedPlayback()
        fetchSongs()
        observeUserLikes()
        observeLikedEntityState()
    }

    private fun restorePersistedPlayback() {
        if (currentSongId.value.isNotBlank() || SongPlayer.isPrepared()) {
            syncCurrentMediaLikeState()
            return
        }

        val snapshot = playbackPreferences.loadSnapshot() ?: return
        val restoredType = runCatching {
            PlaybackMediaType.valueOf(snapshot.mediaType)
        }.getOrDefault(PlaybackMediaType.SONG)

        when (restoredType) {
            PlaybackMediaType.SONG -> restoreSongSnapshot(snapshot)
            PlaybackMediaType.RADIO -> restoreRadioSnapshot(snapshot)
            PlaybackMediaType.PODCAST -> restorePodcastSnapshot(snapshot)
        }
    }

    private fun restoreSongSnapshot(snapshot: PersistedPlaybackSnapshot) {
        val queue = snapshot.songQueue.filter { it.hasPlayableAudio }
        if (queue.isEmpty()) return

        val safeIndex = snapshot.currentIndex.coerceIn(0, queue.lastIndex)
        val song = queue[safeIndex]
        currentSongState.setSongQueue(queue, safeIndex)
        updateSongState(
            coverUri = snapshot.coverUri.ifBlank { song.coverUri },
            title = snapshot.title.ifBlank { song.title },
            singer = snapshot.singer.ifBlank { song.singer },
            playingState = false,
            songId = snapshot.songId.ifBlank { song.id },
            songIndex = safeIndex,
            album = snapshot.album,
            albumTitle = snapshot.albumTitle.ifBlank { song.album.title },
            albumId = snapshot.albumId.ifBlank { song.album.id }
        )
        SongPlayer.prepareSong(song, appContext)
    }

    private fun restoreRadioSnapshot(snapshot: PersistedPlaybackSnapshot) {
        val queue = snapshot.radioQueue
        if (queue.isEmpty()) return

        val safeIndex = snapshot.currentIndex.coerceIn(0, queue.lastIndex)
        val station = queue[safeIndex]
        currentSongState.setRadioQueue(queue, safeIndex)
        updateSongState(
            coverUri = snapshot.coverUri.ifBlank { station.coverUri },
            title = snapshot.title.ifBlank { station.name },
            singer = snapshot.singer.ifBlank { station.country },
            playingState = false,
            songId = snapshot.songId.ifBlank { station.id },
            songIndex = safeIndex,
            album = snapshot.album.ifBlank { "radio" },
            albumTitle = "",
            albumId = ""
        )
        SongPlayer.prepareMedia(
            station.stream_url,
            appContext,
            station.name,
            station.country,
            station.coverUri
        )
    }

    private fun restorePodcastSnapshot(snapshot: PersistedPlaybackSnapshot) {
        val podcast = snapshot.activePodcast ?: return
        val queue = snapshot.podcastQueue.filter { it.hasAudio }
        if (queue.isEmpty()) return

        val safeIndex = snapshot.currentIndex.coerceIn(0, queue.lastIndex)
        val episode = queue[safeIndex]
        currentSongState.setPodcastQueue(podcast, queue, safeIndex)
        updateSongState(
            coverUri = snapshot.coverUri.ifBlank { if (episode.thumbnail.isNotEmpty()) episode.thumbnail else podcast.image },
            title = snapshot.title.ifBlank { episode.title },
            singer = snapshot.singer.ifBlank { podcast.author },
            playingState = false,
            songId = snapshot.songId.ifBlank { episode.id },
            songIndex = safeIndex,
            album = snapshot.album.ifBlank { podcast.id },
            albumTitle = "",
            albumId = ""
        )
        SongPlayer.prepareMedia(
            episode.url,
            appContext,
            episode.title,
            podcast.author,
            if (episode.thumbnail.isNotEmpty()) episode.thumbnail else podcast.image
        )
    }

    private fun persistCurrentPlaybackSnapshot() {
        if (currentSongId.value.isBlank() && currentSongTitle.value.isBlank()) return

        playbackPreferences.saveSnapshot(
            PersistedPlaybackSnapshot(
                mediaType = mediaType.value.name,
                coverUri = currentSongCoverUri.value,
                title = currentSongTitle.value,
                singer = currentSongSinger.value,
                songId = currentSongId.value,
                album = currentSongAlbum.value,
                albumTitle = currentSongAlbumTitle.value,
                albumId = currentSongAlbumId.value,
                currentIndex = when (mediaType.value) {
                    PlaybackMediaType.SONG -> currentSongIndex.value
                    PlaybackMediaType.RADIO -> radioIndex.value
                    PlaybackMediaType.PODCAST -> podcastIndex.value
                },
                songQueue = currentSongState.getSongSourceQueue(),
                radioQueue = currentSongState.getRadioSourceQueue(),
                podcastQueue = currentSongState.getPodcastSourceQueue(),
                activePodcast = activePodcast.value
            )
        )
    }

    private fun observeUserLikes() = viewModelScope.launch {
        userSessionManager.session.collectLatest { session ->
            val userId = session?.idOrEmail.orEmpty()
            if (userId == observedUserId) return@collectLatest

            observedUserId = userId
            if (userId.isBlank()) {
                repository.clearCachedUserProfile()
                syncCurrentMediaLikeState()
                return@collectLatest
            }

            repository.refreshLikedSongs(userId).collect { result ->
                if (result is Response.Success) {
                    syncCurrentMediaLikeState()
                }
            }
        }
    }

    private fun observeLikedEntityState() = viewModelScope.launch {
        likedEntityKeys.collectLatest {
            syncCurrentMediaLikeState()
        }
    }

    private fun syncCurrentMediaLikeState() {
        val key = currentEntityLikeKey()
        currentSongState.updateLikeState(key != null && likedEntityKeys.value.contains(key))
    }

    private fun currentEntityLikeKey(): String? {
        val entityId = currentSongId.value
        if (entityId.isBlank()) return null

        val entityType = when (mediaType.value) {
            PlaybackMediaType.SONG -> "song"
            PlaybackMediaType.RADIO -> "radio_station"
            PlaybackMediaType.PODCAST -> "podcast_episode"
        }
        return "$entityType:$entityId"
    }

    fun startSongPlayback(
        queueSongs: List<SongsModel>,
        startIndex: Int,
        album: String,
        context: Context
    ) {
        val playableQueue = queueSongs.filter { it.hasPlayableAudio }
        if (playableQueue.isEmpty()) return
        val safeIndex = startIndex.coerceIn(0, playableQueue.lastIndex)
        val song = playableQueue[safeIndex]
        currentSongState.setSongQueue(playableQueue, safeIndex)
        updateSongState(
            coverUri = song.coverUri,
            title = song.title,
            singer = song.singer,
            playingState = true,
            songId = song.id,
            songIndex = safeIndex,
            album = album,
            albumTitle = song.album.title.ifBlank { album },
            albumId = song.album.id
        )
        SongPlayer.playSong(song, context)
        playbackPreferences.addRecentSong(song)
        viewModelScope.launch(Dispatchers.IO) {
            val userId = currentUserIdOrEmail()
            if (userId.isNotBlank()) {
                repository.provideUserPlayedSong(userId, song, 0).collect { }
            }
        }
    }

    fun playSongById(
        songId: String,
        context: Context,
        queueSongs: List<SongsModel> = currentSongState.getSongSourceQueue(),
        album: String = currentSongAlbum.value
    ) {
        if (songId.isBlank() || queueSongs.isEmpty()) return
        val targetIndex = queueSongs.indexOfFirst { it.id == songId }
        if (targetIndex >= 0) {
            startSongPlayback(queueSongs, targetIndex, album, context)
        }
    }

    fun playSongQueueFromPlaylist(
        queueSongs: List<SongsModel>,
        startIndex: Int,
        album: String,
        context: Context
    ) = viewModelScope.launch(Dispatchers.IO) {
        if (queueSongs.isEmpty()) return@launch

        withContext(Dispatchers.Main) {
            startSongPlayback(queueSongs, startIndex, album, context)
        }
    }

    fun syncSongQueue(queueSongs: List<SongsModel>, currentSongId: String) {
        if (queueSongs.isEmpty()) return
        val idx = queueSongs.indexOfFirst { it.id == currentSongId }.coerceAtLeast(0)
        currentSongState.setSongQueue(queueSongs, idx)
        persistCurrentPlaybackSnapshot()
    }

    private fun playFromLocalSongQueue(offset: Int, context: Context): Boolean {
        val queue = currentSongState.getSongSourceQueue().filter { it.hasPlayableAudio }
        if (queue.isEmpty()) return false

        val currentIndexInQueue = queue.indexOfFirst { it.id == currentSongId.value }
            .takeIf { it >= 0 }
            ?: currentSongIndex.value.coerceIn(0, queue.lastIndex)

        val targetIndex = when {
            queue.size == 1 -> 0
            offset > 0 -> if (currentIndexInQueue < queue.lastIndex) currentIndexInQueue + 1 else 0
            else -> if (currentIndexInQueue > 0) currentIndexInQueue - 1 else queue.lastIndex
        }

        startSongPlayback(queue, targetIndex, currentSongAlbum.value, context)
        return true
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
            album = "radio",
            albumTitle = "",
            albumId = ""
        )
        SongPlayer.playSong(station.stream_url, context, station.name, station.country, station.coverUri)
        viewModelScope.launch(Dispatchers.IO) {
            val userId = currentUserIdOrEmail()
            if (userId.isNotBlank()) {
                repository.provideUserListenedStation(userId, station, 0).collect { }
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
            album = podcast.id,
            albumTitle = "",
            albumId = ""
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
                repository.provideUserListenedPodcastsAction(userId, podcast, episode, 0).collect { }
            }
        }
    }

    fun playNext(context: Context) {
        when (mediaType.value) {
            PlaybackMediaType.SONG -> {
                playFromLocalSongQueue(offset = 1, context = context)
            }
            PlaybackMediaType.RADIO -> {
                val queue = currentSongState.getRadioSourceQueue()
                if (queue.isEmpty()) return
                val nextIndex = if (radioIndex.value < queue.lastIndex) radioIndex.value + 1 else 0
                startRadioPlayback(queue, nextIndex, context)
            }
            PlaybackMediaType.PODCAST -> {
                val queue = currentSongState.getPodcastSourceQueue()
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
                playFromLocalSongQueue(offset = -1, context = context)
            }
            PlaybackMediaType.RADIO -> {
                val queue = currentSongState.getRadioSourceQueue()
                if (queue.isEmpty()) return
                val prevIndex = if (radioIndex.value > 0) radioIndex.value - 1 else queue.lastIndex
                startRadioPlayback(queue, prevIndex, context)
            }
            PlaybackMediaType.PODCAST -> {
                val queue = currentSongState.getPodcastSourceQueue()
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
        val userId = currentUserIdOrEmail()
        if (userId.isBlank()) {
            _actionMessage.value = "Login required"
            return@launch
        }

        when (mediaType.value) {
            PlaybackMediaType.SONG -> {
                val trackId = currentSongId.value
                if (trackId.isBlank()) return@launch
                toggleSongLikeInternal(userId, trackId, likedSongIds.value.contains(trackId))
            }
            PlaybackMediaType.RADIO -> {
                val queue = currentSongState.getRadioSourceQueue()
                if (queue.isEmpty()) return@launch
                val station = queue[radioIndex.value.coerceIn(0, queue.lastIndex)]
                toggleRadioStationLikeInternal(userId, station)
            }
            PlaybackMediaType.PODCAST -> {
                val podcast = activePodcast.value ?: return@launch
                val queue = currentSongState.getPodcastSourceQueue()
                if (queue.isEmpty()) return@launch
                val episode = queue[podcastIndex.value.coerceIn(0, queue.lastIndex)]
                togglePodcastEpisodeLikeInternal(userId, podcast, episode)
            }
        }
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

    fun toggleAlbumLike(album: AlbumsModel) = viewModelScope.launch(Dispatchers.IO) {
        val userId = currentUserIdOrEmail()
        if (userId.isBlank()) {
            _actionMessage.value = "Login required"
            return@launch
        }
        if (album.id.isBlank()) return@launch

        val currentlyLiked = likedAlbumIds.value.contains(album.id)
        val nextState = !currentlyLiked
        repository.provideLikeDislikeAlbum(userId, nextState, album).collect { result ->
            when (result) {
                is Response.Success -> {
                    repository.updateCachedAlbumLike(album.id, nextState)
                    _actionMessage.value = if (nextState) "Album saved" else "Album removed"
                }
                is Response.Error -> _actionMessage.value = "Album save failed"
                else -> Unit
            }
        }
    }

    private suspend fun toggleRadioStationLikeInternal(userId: String, station: RadioStationModel) {
        val key = "radio_station:${station.id}"
        val nextState = !likedEntityKeys.value.contains(key)
        repository.provideLikeDislikeRadioStation(userId, nextState, station).collect { result ->
            when (result) {
                is Response.Success -> {
                    repository.updateCachedEntityLike("radio_station", station.id, nextState)
                    if (currentSongId.value == station.id && mediaType.value == PlaybackMediaType.RADIO) {
                        updateLikeState(nextState)
                    }
                    _actionMessage.value = if (nextState) "Added to favorites" else "Removed from favorites"
                }
                is Response.Error -> _actionMessage.value = "Favorite update failed"
                else -> Unit
            }
        }
    }

    private suspend fun togglePodcastEpisodeLikeInternal(
        userId: String,
        podcast: PodcastModel,
        episode: PodcastEpisodeModel
    ) {
        val key = "podcast_episode:${episode.id}"
        val nextState = !likedEntityKeys.value.contains(key)
        repository.provideLikeDislikePodcastEpisode(userId, nextState, podcast, episode).collect { result ->
            when (result) {
                is Response.Success -> {
                    repository.updateCachedEntityLike("podcast_episode", episode.id, nextState)
                    if (currentSongId.value == episode.id && mediaType.value == PlaybackMediaType.PODCAST) {
                        updateLikeState(nextState)
                    }
                    _actionMessage.value = if (nextState) "Added to favorites" else "Removed from favorites"
                }
                is Response.Error -> _actionMessage.value = "Favorite update failed"
                else -> Unit
            }
        }
    }

    private suspend fun toggleSongLikeInternal(userId: String, songId: String, currentlyLiked: Boolean) {
        val song = currentSongState.getSongSourceQueue().firstOrNull { it.id == songId }
            ?: (_songs.value as? Response.Success)?.data?.firstOrNull { it.id == songId }
            ?: SongsModel(id = songId)
        val nextState = !currentlyLiked
        repository.provideLikeDislikeSong(userId, nextState, song).collect { result ->
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
                repository.provideUserListenedPodcastsAction(userId, podcast, episode, secondsPlayed.toInt()).collect { }
            }
        }
    }

    fun playSongFromHistory(entry: UserHistoryEntityModel, context: Context) {
        if (!entry.isSong || entry.s3link.isBlank()) return
        playSongFromHistory(
            entry,
            historyEntries = listOf(entry),
            context = context
        )
    }
    fun playSongFromHistory(
        entry: UserHistoryEntityModel,
        historyEntries: List<UserHistoryEntityModel> = listOf(entry),
        context: Context
    ) {
        if (!entry.isSong || entry.s3link.isBlank()) return

        val queueSongs = historyEntries
            .asSequence()
            .filter { it.isSong && it.s3link.isNotBlank() }
            .map { it.toSongModel() }
            .distinctBy { it.id }
            .toList()

        if (queueSongs.isEmpty()) return

        val startIndex = queueSongs.indexOfFirst { it.id == entry.entityId }
        if (startIndex < 0) return

        startSongPlayback(queueSongs, startIndex, entry.subtitle.ifBlank { "History" }, context)
    }

    fun playRadioFromHistory(entry: UserHistoryEntityModel, context: Context) {
        if (!entry.isRadioStation || entry.s3link.isBlank()) return
        startRadioPlayback(listOf(entry.toRadioStationModel()), 0, context)
    }

    fun playPodcastEpisodeFromHistory(entry: UserHistoryEntityModel, context: Context) {
        if (!entry.isPodcastEpisode || entry.s3link.isBlank()) return
        val podcast = entry.toPodcastModel().copy(title = entry.subtitle.ifBlank { entry.title })
        val episode = entry.toPodcastEpisodeModel()
        startPodcastPlayback(podcast, listOf(episode), 0, context)
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
        album: String = "",
        albumTitle: String = currentSongAlbumTitle.value,
        albumId: String = currentSongAlbumId.value
    ) {
        currentSongState.updateSongState(coverUri, title, singer, playingState, songId, songIndex, album, albumTitle, albumId)
        syncCurrentMediaLikeState()
        persistCurrentPlaybackSnapshot()
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

    fun expandPlayer() {
        _isPlayerExpanded.value = true
    }

    fun collapsePlayer() {
        _isPlayerExpanded.value = false
    }
}
