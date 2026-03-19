package com.music.stream.neptune.data.entity.web

import com.music.stream.neptune.data.entity.AlbumsModel
import com.music.stream.neptune.data.entity.ArtistsModel
import com.music.stream.neptune.data.entity.HomePageCardModel
import com.music.stream.neptune.data.entity.HomePageSectionModel
import com.music.stream.neptune.data.entity.PodcastEpisodeModel
import com.music.stream.neptune.data.entity.PodcastModel
import com.music.stream.neptune.data.entity.RadioStationModel
import com.music.stream.neptune.data.entity.SearchCardModel
import com.music.stream.neptune.data.entity.SearchResultModel
import com.music.stream.neptune.data.entity.SongsModel
import com.music.stream.neptune.data.entity.UserModel
import com.music.stream.neptune.data.entity.UserPlaylistModel

fun WebSongType.toDomain(): SongsModel = SongsModel(
    id = id,
    name = name,
    playlist_id = playlist_id,
    playlist_name = playlist_name,
    artists = artists.map { SongsModel.ArtistRef(title = it.title, id = it.id, path = it.path) },
    duration = duration,
    likes = likes,
    genres = genres,
    album = SongsModel.AlbumRef(
        title = album?.title.orEmpty(),
        id = album?.id.orEmpty(),
        path = album?.path.orEmpty()
    ),
    thumbnail = thumbnail,
    view_count = view_count,
    preview_url = preview_url.orEmpty(),
    s3link = s3link.orEmpty()
)

fun WebPlayListType.toDomain(): AlbumsModel = AlbumsModel(
    id = id,
    image = image,
    title = title,
    artists = artists.map { AlbumsModel.PlaylistArtistRef(id = it.id, name = it.name) },
    songs = songs.map { it.toDomain() }
)

fun WebArtistType.toDomain(): ArtistsModel = ArtistsModel(
    id = id,
    title = title,
    image = image,
    image_type = image_type,
    monthly_listeners = monthly_listeners,
    popular_songs = popular_songs.map { it.toDomain() },
    genres = genres
)

fun WebCardContentType.toDomain(): HomePageCardModel = HomePageCardModel(
    image = image,
    id = id,
    title = title,
    subtitle = subtitle,
    type = type,
    path = path,
    song = song?.toDomain()
)

fun WebHomePageDataType.toDomain(): HomePageSectionModel = HomePageSectionModel(
    cardType = cardType,
    label = label,
    path = path,
    cards = cards.map { it.toDomain() },
    id = id
)

fun WebPodcastCardDto.toDomain(): PodcastModel = PodcastModel(
    id = uuid.ifBlank { id },
    title = title,
    description = description,
    image = image_url.ifBlank { image },
    author = itunes_author.ifBlank { publisher },
    genres = genres.split(',').map { it.trim() }.filter { it.isNotEmpty() },
    episode_count = episode_count,
    episodes = emptyList()
)

fun WebPodcastEpisodeDto.toDomain(): PodcastEpisodeModel = PodcastEpisodeModel(
    id = uuid.ifBlank { id },
    title = name,
    description = description.ifBlank { summary },
    duration = duration,
    preview_url = audio_url.ifBlank { video_url },
    s3link = audio_url,
    thumbnail = image_url.ifBlank { thumbnail },
    episode_number = episode_number
)

fun WebRadioStation.toDomain(): RadioStationModel = RadioStationModel(
    id = id,
    name = name,
    country = countryCode,
    stream_url = url,
    image = favicon,
    favicon = favicon,
    tags = tags.joinToString(","),
    votes = clickCount
)

fun WebSearchPageResType.toDomain(): SearchResultModel = SearchResultModel(
    total = total,
    took = took,
    cards = cards.map {
        SearchCardModel(
            play_url = it.play_url.orEmpty(),
            s3link = it.s3link.orEmpty(),
            id = it.id,
            image = it.image,
            name = it.name,
            artist = it.artist,
            type = it.type
        )
    },
    type = type
)

fun WebUserType.toDomain(): UserModel = UserModel(
    id = id,
    name = name,
    playlists = playlists.map {
        UserPlaylistModel(
            id = it.id,
            name = it.name,
            image = it.image,
            tracks = it.tracks
        )
    },
    likedSongs = liked_songs,
    tracks = tracks,
    artists = artists
)
