package com.music.stream.neptune.data.entity

data class PaginatedItems<T>(
    val items: List<T> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 20,
    val hasMore: Boolean = false
)

typealias UserLikesPageModel = PaginatedItems<UserLikedEntityModel>
typealias UserHistoryPageModel = PaginatedItems<UserHistoryEntityModel>

data class UserLikedEntityModel(
    val entityId: String = "",
    val entityType: String = "",
    val title: String = "",
    val image: String = "",
    val s3link: String = "",
    val subtitle: String = "",
    val artists: List<SongsModel.ArtistRef> = emptyList(),
    val album: SongsModel.AlbumRef = SongsModel.AlbumRef(),
    val episodeNumber: Int? = null,
    val likedAt: String = ""
)

data class UserHistoryEntityModel(
    val entityId: String = "",
    val entityType: String = "",
    val title: String = "",
    val image: String = "",
    val s3link: String = "",
    val subtitle: String = "",
    val artists: List<SongsModel.ArtistRef> = emptyList(),
    val album: SongsModel.AlbumRef = SongsModel.AlbumRef(),
    val episodeNumber: Int? = null,
    val watchedDuration: Int = 0,
    val totalDuration: Int = 0,
    val watchedPercentage: Int = 0,
    val lastPlayedAt: String = ""
) {
    val isSong: Boolean get() = entityType == "song"
    val isRadioStation: Boolean get() = entityType == "radio_station"
    val isPodcast: Boolean get() = entityType == "podcast"
    val isPodcastEpisode: Boolean get() = entityType == "podcast_episode"
    val isPlaylist: Boolean get() = entityType == "playlist"
    val isPlaylistCollection: Boolean get() = entityType == "playlist_collection"
}

fun UserHistoryEntityModel.toSongModel(): SongsModel = SongsModel(
    id = entityId,
    name = title,
    artists = artists,
    album = album.copy(title = album.title.ifBlank { subtitle }),
    thumbnail = image,
    duration = totalDuration,
    s3link = s3link
)

fun UserHistoryEntityModel.toRadioStationModel(): RadioStationModel = RadioStationModel(
    id = entityId,
    name = title,
    stream_url = s3link,
    image = image,
    favicon = image
)

fun UserHistoryEntityModel.toPodcastModel(): PodcastModel = PodcastModel(
    id = entityId,
    title = title,
    image = image,
    author = subtitle
)

fun UserHistoryEntityModel.toPodcastEpisodeModel(): PodcastEpisodeModel = PodcastEpisodeModel(
    id = entityId,
    title = title,
    duration = totalDuration,
    s3link = s3link,
    thumbnail = image,
    episode_number = episodeNumber ?: 0
)