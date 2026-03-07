package com.music.stream.neptune.data.entity

data class AlbumsModel(
    val id: Int = 0,
    val image: String = "",
    val title: String = "",
    val artists: List<PlaylistArtistRef> = emptyList(),
    val songs: List<SongsModel> = emptyList()
) {
    data class PlaylistArtistRef(
        val id: String = "",
        val name: String = ""
    )

    // Convenience getters for backward compat with screens
    val name: String get() = title
    val coverUri: String get() = image
    val time: String get() = ""
}
