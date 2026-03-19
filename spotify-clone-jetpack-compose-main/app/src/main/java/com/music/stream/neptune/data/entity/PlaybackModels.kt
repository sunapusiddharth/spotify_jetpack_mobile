package com.music.stream.neptune.data.entity

data class QueueUpdateModel(
    val song: SongsModel = SongsModel(),
    val updatedQueue: List<SongsModel> = emptyList()
)

data class UserPlaylistModel(
    val id: String = "",
    val name: String = "",
    val image: String = "",
    val tracks: List<String> = emptyList()
)

data class UserModel(
    val id: String = "",
    val name: String = "",
    val playlists: List<UserPlaylistModel> = emptyList(),
    val likedSongs: List<String> = emptyList(),
    val tracks: List<String> = emptyList(),
    val artists: List<String> = emptyList()
)
