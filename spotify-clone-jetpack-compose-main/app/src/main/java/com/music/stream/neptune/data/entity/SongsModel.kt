package com.music.stream.neptune.data.entity

data class SongsModel(
    val id: String = "",
    val name: String = "",
    val playlist_id: String = "",
    val playlist_name: String = "",
    val artists: List<ArtistRef> = emptyList(),
    val duration: Int = 0,
    val likes: Int = 0,
    val genres: List<String> = emptyList(),
    val album: AlbumRef = AlbumRef(),
    val thumbnail: String = "",
    val view_count: Int = 0,
    val preview_url: String = "",
    val s3link: String = ""
) {
    data class ArtistRef(
        val title: String = "",
        val id: String = "",
        val path: String = ""
    )

    data class AlbumRef(
        val title: String = "",
        val id: String = "",
        val path: String = ""
    )

    // Convenience getters for backward compat with screens
    val title: String get() = name
    val singer: String get() = artists.firstOrNull()?.title ?: ""
    val coverUri: String get() = thumbnail
    val hasPlayableAudio: Boolean get() = s3link.isNotBlank()
    // Use s3link as primary play URL, fall back to preview_url
    val url: String get() = if (s3link.isNotEmpty()) s3link else preview_url
}
