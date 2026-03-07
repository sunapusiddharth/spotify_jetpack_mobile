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
    private val _title: MutableState<String> = mutableStateOf("")
    val title: State<String> get() = _title

    private val _album: MutableState<String> = mutableStateOf("")
    val album: State<String> get() = _album

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
        album: String
    ) {
        _coverUri.value = coverUri
        _title.value = title
        _album.value = album
        _singer.value = singer
        _playingState.value = playingState
        _songIndex.value = songIndex
        _songId.value = songId
    }

    fun setMediaType(type: PlaybackMediaType) {
        _mediaType.value = type
    }

    fun setSongQueue(queue: List<SongsModel>, currentIndex: Int) {
        _songQueue.value = queue
        _songIndex.value = currentIndex.coerceAtLeast(0)
        _mediaType.value = PlaybackMediaType.SONG
    }

    fun setRadioQueue(queue: List<RadioStationModel>, currentIndex: Int) {
        _radioQueue.value = queue
        _radioIndex.value = currentIndex.coerceAtLeast(0)
        _mediaType.value = PlaybackMediaType.RADIO
    }

    fun setPodcastQueue(podcast: PodcastModel, queue: List<PodcastEpisodeModel>, currentIndex: Int) {
        _activePodcast.value = podcast
        _podcastQueue.value = queue
        _podcastIndex.value = currentIndex.coerceAtLeast(0)
        _mediaType.value = PlaybackMediaType.PODCAST
    }

    fun setRadioIndex(index: Int) {
        _radioIndex.value = index.coerceAtLeast(0)
    }

    fun setPodcastIndex(index: Int) {
        _podcastIndex.value = index.coerceAtLeast(0)
    }
}
