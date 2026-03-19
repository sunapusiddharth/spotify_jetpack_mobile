package com.music.stream.neptune.data.entity

data class PersistedPlaybackSnapshot(
    val mediaType: String = "SONG",
    val coverUri: String = "",
    val title: String = "",
    val singer: String = "",
    val songId: String = "",
    val album: String = "",
    val albumTitle: String = "",
    val albumId: String = "",
    val currentIndex: Int = 0,
    val songQueue: List<SongsModel> = emptyList(),
    val radioQueue: List<RadioStationModel> = emptyList(),
    val podcastQueue: List<PodcastEpisodeModel> = emptyList(),
    val activePodcast: PodcastModel? = null
)