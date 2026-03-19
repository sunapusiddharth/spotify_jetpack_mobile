package com.music.stream.neptune.di

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.music.stream.neptune.data.entity.PodcastEpisodeModel
import com.music.stream.neptune.data.entity.PodcastModel
import com.music.stream.neptune.data.entity.RadioStationModel
import com.music.stream.neptune.data.entity.SongsModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentSongState @Inject constructor() {
    companion object {
        const val MAX_QUEUE_WINDOW_SIZE = 30
    }

    private val _title: MutableState<String> = mutableStateOf("")
    val title: State<String> get() = _title

    private val _album: MutableState<String> = mutableStateOf("")
    val album: State<String> get() = _album

    private val _albumTitle: MutableState<String> = mutableStateOf("")
    val albumTitle: State<String> get() = _albumTitle

    private val _albumId: MutableState<String> = mutableStateOf("")
    val albumId: State<String> get() = _albumId

    private val _singer: MutableState<String> = mutableStateOf("")
    val singer: State<String> get() = _singer

    private val _coverUri: MutableState<String> = mutableStateOf("")
    val coverUri: State<String> get() = _coverUri

    private val _playingState: MutableState<Boolean> = mutableStateOf(false)
    val playingState: State<Boolean> get() = _playingState

    private val _songIndex: MutableState<Int> = mutableStateOf(0)
    val songIndex: State<Int> get() = _songIndex

    // Changed from Int to String to match new SongType.id (String from API)
    private val _songId: MutableState<String> = mutableStateOf("")
    val songId: State<String> get() = _songId

    private val _mediaType: MutableState<PlaybackMediaType> = mutableStateOf(PlaybackMediaType.SONG)
    val mediaType: State<PlaybackMediaType> get() = _mediaType

    private val _songQueue: MutableState<List<SongsModel>> = mutableStateOf(emptyList())
    val songQueue: State<List<SongsModel>> get() = _songQueue

    private val _radioQueue: MutableState<List<RadioStationModel>> = mutableStateOf(emptyList())
    val radioQueue: State<List<RadioStationModel>> get() = _radioQueue

    private val _podcastQueue: MutableState<List<PodcastEpisodeModel>> = mutableStateOf(emptyList())
    val podcastQueue: State<List<PodcastEpisodeModel>> get() = _podcastQueue

    private val _radioIndex: MutableState<Int> = mutableStateOf(0)
    val radioIndex: State<Int> get() = _radioIndex

    private val _podcastIndex: MutableState<Int> = mutableStateOf(0)
    val podcastIndex: State<Int> get() = _podcastIndex

    private val _activePodcast: MutableState<PodcastModel?> = mutableStateOf(null)
    val activePodcast: State<PodcastModel?> get() = _activePodcast

    private var songSourceQueue: List<SongsModel> = emptyList()
    private var radioSourceQueue: List<RadioStationModel> = emptyList()
    private var podcastSourceQueue: List<PodcastEpisodeModel> = emptyList()

    val shuffle = mutableStateOf(false)
    val repeat = mutableStateOf(false)
    val likeState = mutableStateOf(false)

    fun updateShuffleState(newShuffleState: Boolean) {
        shuffle.value = newShuffleState
    }

    fun updateRepeatState(newRepeatState: Boolean) {
        repeat.value = newRepeatState
    }

    fun updateLikeState(newLikeState: Boolean) {
        likeState.value = newLikeState
    }

    fun updateSongState(
        coverUri: String,
        title: String,
        singer: String,
        playingState: Boolean,
        songId: String,
        songIndex: Int,
        album: String,
        albumTitle: String = _albumTitle.value,
        albumId: String = _albumId.value
    ) {
        _coverUri.value = coverUri
        _title.value = title
        _album.value = album
        _albumTitle.value = albumTitle
        _albumId.value = albumId
        _singer.value = singer
        _playingState.value = playingState
        _songIndex.value = songIndex
        _songId.value = songId
    }

    fun setMediaType(type: PlaybackMediaType) {
        _mediaType.value = type
    }

    fun setSongQueue(queue: List<SongsModel>, currentIndex: Int) {
        val playableQueue = queue.filter { it.hasPlayableAudio }
        songSourceQueue = playableQueue
        if (playableQueue.isEmpty()) {
            _songQueue.value = emptyList()
            _songIndex.value = 0
            _mediaType.value = PlaybackMediaType.SONG
            return
        }

        val safeIndex = currentIndex.coerceIn(0, playableQueue.lastIndex)
        _songIndex.value = safeIndex
        _songQueue.value = buildQueueWindow(playableQueue, safeIndex)
        _mediaType.value = PlaybackMediaType.SONG
    }

    fun setRadioQueue(queue: List<RadioStationModel>, currentIndex: Int) {
        radioSourceQueue = queue
        if (queue.isEmpty()) {
            _radioQueue.value = emptyList()
            _radioIndex.value = 0
            _mediaType.value = PlaybackMediaType.RADIO
            return
        }

        val safeIndex = currentIndex.coerceIn(0, queue.lastIndex)
        _radioIndex.value = safeIndex
        _radioQueue.value = buildQueueWindow(queue, safeIndex)
        _mediaType.value = PlaybackMediaType.RADIO
    }

    fun setPodcastQueue(podcast: PodcastModel, queue: List<PodcastEpisodeModel>, currentIndex: Int) {
        _activePodcast.value = podcast
        val playableQueue = queue.filter { it.hasAudio }
        podcastSourceQueue = playableQueue
        if (playableQueue.isEmpty()) {
            _podcastQueue.value = emptyList()
            _podcastIndex.value = 0
            _mediaType.value = PlaybackMediaType.PODCAST
            return
        }

        val safeIndex = currentIndex.coerceIn(0, playableQueue.lastIndex)
        _podcastIndex.value = safeIndex
        _podcastQueue.value = buildQueueWindow(playableQueue, safeIndex)
        _mediaType.value = PlaybackMediaType.PODCAST
    }

    fun setRadioIndex(index: Int) {
        if (radioSourceQueue.isEmpty()) return
        val safeIndex = index.coerceIn(0, radioSourceQueue.lastIndex)
        _radioIndex.value = safeIndex
        _radioQueue.value = buildQueueWindow(radioSourceQueue, safeIndex)
    }

    fun setPodcastIndex(index: Int) {
        if (podcastSourceQueue.isEmpty()) return
        val safeIndex = index.coerceIn(0, podcastSourceQueue.lastIndex)
        _podcastIndex.value = safeIndex
        _podcastQueue.value = buildQueueWindow(podcastSourceQueue, safeIndex)
    }

    fun setSongIndex(index: Int) {
        if (songSourceQueue.isEmpty()) return
        val safeIndex = index.coerceIn(0, songSourceQueue.lastIndex)
        _songIndex.value = safeIndex
        _songQueue.value = buildQueueWindow(songSourceQueue, safeIndex)
    }

    fun getSongSourceQueue(): List<SongsModel> = songSourceQueue

    fun getRadioSourceQueue(): List<RadioStationModel> = radioSourceQueue

    fun getPodcastSourceQueue(): List<PodcastEpisodeModel> = podcastSourceQueue

    private fun <T> buildQueueWindow(queue: List<T>, currentIndex: Int): List<T> {
        if (queue.isEmpty()) return emptyList()

        val safeIndex = currentIndex.coerceIn(0, queue.lastIndex)
        val windowSize = minOf(queue.size, MAX_QUEUE_WINDOW_SIZE)
        return List(windowSize) { offset ->
            queue[(safeIndex + offset) % queue.size]
        }
    }
}
