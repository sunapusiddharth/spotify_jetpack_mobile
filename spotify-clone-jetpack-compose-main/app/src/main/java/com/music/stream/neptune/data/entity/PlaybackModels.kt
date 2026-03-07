package com.music.stream.neptune.data.entity

data class QueueUpdateModel(
    val song: SongsModel = SongsModel(),
    val updatedQueue: List<SongsModel> = emptyList()
)

data class UserPlaylistModel(
    val id: String = "",
    val name: String = "",
    val image: String = ""
)
