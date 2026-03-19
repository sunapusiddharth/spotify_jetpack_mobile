package com.music.stream.neptune.data.entity.web

// Mirrors sidflix-music-web-3-main/types/*.ts contracts.

data class WebArtistType(
    val image: String = "",
    val image_type: String = "",
    val id: String = "",
    val title: String = "",
    val monthly_listeners: Int = 0,
    val popular_songs: List<WebSongType> = emptyList(),
    val featured_albums: WebHomePageDataType = WebHomePageDataType(),
    val popular_releases: WebHomePageDataType = WebHomePageDataType(),
    val singles: WebHomePageDataType = WebHomePageDataType(),
    val albums_featuring_artist: WebHomePageDataType = WebHomePageDataType(),
    val fans_also_like: WebHomePageDataType = WebHomePageDataType(),
    val appears_on: WebHomePageDataType = WebHomePageDataType(),
    val dicovered_on: WebHomePageDataType = WebHomePageDataType(),
    val genres: List<String> = emptyList(),
    val similar: List<WebArtistListingType> = emptyList()
)

data class WebArtistListingType(
    val image: String = "",
    val id: String = "",
    val title: String = ""
)

data class WebAllPlayListType(
    val image: String = "",
    val id: String = "",
    val title: String = ""
)

data class WebCardContentType(
    val image: String = "",
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val type: String = "",
    val path: String = "",
    val song: WebSongType? = null
)

data class WebHomePageDataType(
    val cardType: String = "",
    val label: String = "",
    val path: String = "",
    val cards: List<WebCardContentType> = emptyList(),
    val id: String = ""
)

data class WebPlayListType(
    val id: String = "",
    val image: String = "",
    val title: String = "",
    val artists: List<WebPlaylistArtistRef> = emptyList(),
    val songs: List<WebSongType> = emptyList()
)

data class WebPlaylistArtistRef(
    val id: String = "",
    val name: String = ""
)

data class WebPodcastDto(
    val id: String = "",
    val image: String = "",
    val title: String = "",
    val genre_ids: List<Int> = emptyList(),
    val publisher: String = "",
    val total_episodes: Int = 0,
    val description: String = "",
    val episodes: List<WebPodcastInlineEpisode> = emptyList()
)

data class WebPodcastInlineEpisode(
    val id: String = "",
    val name: String = "",
    val summary: String = "",
    val thumbnail: String = "",
    val duration: Int = 0
)

data class WebPodcastCardDto(
    val id: String = "",
    val image: String = "",
    val publisher: String = "",
    val uuid: String = "",
    val url: String = "",
    val title: String = "",
    val link: String = "",
    val itunes_id: Long = 0,
    val itunes_author: String = "",
    val image_url: String = "",
    val language: String = "",
    val episode_count: Int = 0,
    val popularity_score: Int = 0,
    val newest_enclosure_url: String = "",
    val description: String = "",
    val category: String = "",
    val newest_enclosure_duration: Int = 0,
    val genres: String = ""
)

data class WebPodcastEpisodeDto(
    val id: String = "",
    val summary: String = "",
    val thumbnail: String = "",
    val uuid: String = "",
    val name: String = "",
    val description: String = "",
    val audio_url: String = "",
    val image_url: String = "",
    val subtitle: String = "",
    val video_url: String = "",
    val file_type: String = "",
    val duration: Int = 0,
    val episode_type: String = "",
    val episode_number: Int = 0,
    val season_number: Int = 0,
    val date_published: String = ""
)

data class WebRadioStation(
    val id: String = "",
    val clickCount: Int = 0,
    val countryCode: String = "",
    val favicon: String = "",
    val name: String = "",
    val tags: List<String> = emptyList(),
    val url: String = "",
    val created_at: String = ""
)

// Alias in TS is same shape; keep explicit Kotlin type for readability.
typealias WebRadioStationType = WebRadioStation

data class WebSongType(
    val id: String = "",
    val name: String = "",
    val playlist_id: String = "",
    val playlist_name: String = "",
    val artists: List<WebSongArtistRef> = emptyList(),
    val duration: Int = 0,
    val likes: Int = 0,
    val genres: List<String> = emptyList(),
    val album: WebSongAlbumRef? = null,
    val thumbnail: String = "",
    val view_count: Int = 0,
    val preview_url: String? = null,
    val s3link: String? = null
)

data class WebSongArtistRef(
    val title: String = "",
    val id: String = "",
    val path: String = ""
)

data class WebSongAlbumRef(
    val title: String = "",
    val id: String = "",
    val path: String = ""
)

enum class WebSearchTypeEnum {
    album,
    songs,
    artist,
    radio,
    podcast
}

data class WebSearchPageResType(
    val total: Int = 0,
    val took: Int = 0,
    val cards: List<WebSearchCard> = emptyList(),
    val type: String = ""
)

data class WebSearchCard(
    val play_url: String? = null,
    val s3link: String? = null,
    val id: String = "",
    val image: String = "",
    val name: String = "",
    val artist: String = "",
    val type: String = ""
)

data class WebUserType(
    val id: String = "",
    val name: String = "",
    val playlists: List<WebUserPlayListType> = emptyList(),
    val liked_songs: List<String> = emptyList(),
    val liked_podcast: List<String> = emptyList(),
    val liked_radio: List<String> = emptyList(),
    val tracks: List<String> = emptyList(),
    val artists: List<String> = emptyList()
)

data class WebUserPlayListType(
    val id: String = "",
    val name: String = "",
    val image: String = "",
    val tracks: List<String> = emptyList(),
    val user_id: String = "",
    val followers: List<String> = emptyList(),
    val created_at: String = "",
    val updated_at: String = ""
)

data class WebRadioCountryAgg(
    val name: String = "",
    val count: Int = 0,
    val code: String = ""
)

data class WebMeiliSearchAggType(
    val value: String = "",
    val count: Int = 0
)

data class WebPodcastsPageInfo(
    val results: List<WebPodcastCardDto> = emptyList(),
    val page: Int = 0
)

data class WebPodcastsByGenrePageInfo(
    val results: List<WebPodcastsCardCarousel> = emptyList(),
    val page: Int = 0
)

data class WebPodcastsCardCarousel(
    val cards: List<WebPodcastCardDto> = emptyList(),
    val label: String = ""
)

data class WebPodcastEpisodesPageInfo(
    val results: List<WebPodcastEpisodeDto> = emptyList(),
    val page: Int = 0
)

data class WebArtistSongsPagination(
    val results: List<WebSongType> = emptyList(),
    val page: Int = 0
)

data class WebRadioStationsPageInfo(
    val results: List<WebRadioStation> = emptyList(),
    val page: Int = 0
)
