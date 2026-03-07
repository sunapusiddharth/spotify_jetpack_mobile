package com.music.stream.neptune.data.entity

data class ArtistsModel(
    val id: String = "",
    val title: String = "",
    val image: String = "",
    val image_type: String = "",
    val monthly_listeners: Int = 0,
    val popular_songs: List<SongsModel> = emptyList(),
    val genres: List<String> = emptyList()
) {
    // Convenience getters for backward compat
    val name: String get() = title
    val coverUri: String get() = image
}
