package com.music.stream.neptune.data.entity

data class RadioStationModel(
    val id: String = "",
    val name: String = "",
    val country: String = "",
    val stream_url: String = "",
    val image: String = "",
    val favicon: String = "",
    val tags: String = "",
    val votes: Int = 0,
    val bitrate: Int = 0,
    val codec: String = ""
) {
    val genres: List<String> get() = tags.takeIf { it.isNotEmpty() }
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?: emptyList()
    val coverUri: String get() = if (image.isNotEmpty()) image else favicon
}
