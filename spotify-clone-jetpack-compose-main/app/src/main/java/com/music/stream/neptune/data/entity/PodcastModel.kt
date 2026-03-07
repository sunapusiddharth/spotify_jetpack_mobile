package com.music.stream.neptune.data.entity

data class PodcastModel(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val image: String = "",
    val author: String = "",
    val genres: List<String> = emptyList(),
    val episode_count: Int = 0,
    val episodes: List<PodcastEpisodeModel> = emptyList()
)

data class PodcastEpisodeModel(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val duration: Int = 0,
    val preview_url: String = "",
    val s3link: String = "",
    val thumbnail: String = "",
    val episode_number: Int = 0
) {
    val url: String get() = if (s3link.isNotEmpty()) s3link else preview_url
    val hasAudio: Boolean get() = url.isNotEmpty()
}
