package com.music.stream.neptune.data.network

import com.music.stream.neptune.data.entity.PodcastEpisodeModel
import com.music.stream.neptune.data.entity.PodcastModel
import com.music.stream.neptune.data.entity.RadioStationModel
import com.music.stream.neptune.data.entity.SongsModel
import com.music.stream.neptune.data.entity.web.WebArtistListingType
import com.music.stream.neptune.data.entity.web.WebArtistType
import com.music.stream.neptune.data.entity.web.WebAllPlayListType
import com.music.stream.neptune.data.entity.web.WebFreshAlbumType
import com.music.stream.neptune.data.entity.web.WebHomePageDataType
import com.music.stream.neptune.data.entity.web.WebMeiliSearchAggType
import com.music.stream.neptune.data.entity.web.WebPlayListType
import com.music.stream.neptune.data.entity.web.WebPodcastCardDto
import com.music.stream.neptune.data.entity.web.WebPodcastsByGenrePageInfo
import com.music.stream.neptune.data.entity.web.WebPodcastEpisodesPageInfo
import com.music.stream.neptune.data.entity.web.WebPodcastsPageInfo
import com.music.stream.neptune.data.entity.web.WebRadioCountryAgg
import com.music.stream.neptune.data.entity.web.WebRadioStation
import com.music.stream.neptune.data.entity.web.WebRadioStationsPageInfo
import com.music.stream.neptune.data.entity.web.WebSongType
import com.music.stream.neptune.data.entity.web.WebUserType
import com.music.stream.neptune.data.entity.web.WebUserPlayListType
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface NetworkApi {

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") userId: String): WebUserType

    @GET("users/{id}/home/{page}")
    suspend fun getHomePage(
        @Path("id") userId: String,
        @Path("page") page: Int
    ): JsonElement

    // Album endpoints
    @GET("album/freshAlbums")
    suspend fun getFreshAlbums(): List<WebFreshAlbumType>

    @GET("album/all/{page}")
    suspend fun getAllAlbums(@Path("page") page: Int): WebAllPlayListPageResponse

    @GET("album/{id}")
    suspend fun getAlbumById(@Path("id") id: String): WebPlayListType

    // Artist endpoints
    @GET("artist/top")
    suspend fun getTopArtists(): List<WebArtistListingType>

    @GET("artist/{id}")
    suspend fun getArtistById(@Path("id") id: String): WebArtistType

    @GET("artist/songs/{id}/{page}")
    suspend fun getArtistSongs(
        @Path("id") id: String,
        @Path("page") page: Int
    ): WebArtistSongsPaginationResponse

    // Track / song endpoints
    @POST("tracks/requestTrackAddition/{userid}/{songid}")
    suspend fun requestTrackAddition(
        @Path("userid") userId: String,
        @Path("songid") songId: String
    ): RequestTrackAdditionResponse

    @POST("users/{userId}/likes/{entityId}")
    suspend fun likeEntity(
        @Path("userId") userId: String,
        @Path("entityId") entityId: String,
        @Body body: LikeEntityRequest
    ): ApiStatusResponse

    @DELETE("users/{userId}/likes/{entityId}")
    suspend fun unlikeEntity(
        @Path("userId") userId: String,
        @Path("entityId") entityId: String
    ): ApiStatusResponse

    @GET("users/{userId}/likes")
    suspend fun getUserLikes(
        @Path("userId") userId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): PaginatedApiResponse<LikedEntityResponseDto>

    @PUT("users/{userId}/history/{entityId}")
    suspend fun updateHistory(
        @Path("userId") userId: String,
        @Path("entityId") entityId: String,
        @Body body: UpdateHistoryRequest
    ): ApiStatusResponse

    @GET("users/{userId}/history")
    suspend fun getUserHistory(
        @Path("userId") userId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): PaginatedApiResponse<HistoryEntityResponseDto>

    @GET("stream/all_available_songs/{skip}/{limit}")
    suspend fun getAllAvailableSongs(
        @Path("skip") skip: Int,
        @Path("limit") limit: Int
    ): WebSongsPageResponse

    // Search endpoint
    @GET("search/{userid}/{query}/{page}")
    suspend fun searchAll(
        @Path("userid") userId: String,
        @Path("query") query: String,
        @Path("page") page: Int,
        @Query("type") type: String?
    ): JsonElement

    @GET("search/{userId}/recent")
    suspend fun getRecentSearches(
        @Path("userId") userId: String
    ): JsonElement

    @POST("search/{userId}/recent")
    suspend fun addRecentSearch(
        @Path("userId") userId: String,
        @Body body: AddSearchQueryRequest
    ): ApiStatusResponse

    @DELETE("search/{userId}/recent")
    suspend fun clearRecentSearches(
        @Path("userId") userId: String
    ): ApiStatusResponse

    @DELETE("search/{userId}/recent/{query}")
    suspend fun removeRecentSearch(
        @Path("userId") userId: String,
        @Path("query") query: String
    ): ApiStatusResponse

    @GET("search/autocomplete")
    suspend fun getSearchAutocomplete(
        @Query("q") query: String,
        @Query("limit") limit: Int
    ): JsonElement

    // Podcast endpoints
    @GET("podcast/browse/{page}")
    suspend fun browsePodcasts(@Path("page") page: Int): WebPodcastsPageInfo

    @GET("podcast/browsePodcastsByGenre/{genre}/{page}")
    suspend fun browsePodcastsByGenre(
        @Path("genre") genre: String,
        @Path("page") page: Int
    ): WebPodcastsByGenrePageInfo

    @GET("podcast/{id}")
    suspend fun getPodcastById(@Path("id") id: String): WebPodcastCardDto

    @GET("podcast/{id}/episodes/{page}")
    suspend fun getPodcastEpisodes(
        @Path("id") id: String,
        @Path("page") page: Int
    ): WebPodcastEpisodesPageInfo

    @POST("podcast/requestPodcastEpisodesPopulation/{userid}/{id}")
    suspend fun requestPodcastEpisodesPopulation(
        @Path("userid") userId: String,
        @Path("id") podcastId: String
    ): ApiStatusResponse

    @GET("podcast/all_genres")
    suspend fun getPodcastGenres(): List<WebMeiliSearchAggType>

    // Radio endpoints
    @GET("radio/all_countries")
    suspend fun getRadioCountries(): List<WebRadioCountryAgg>

    @GET("radio/all_genres/{country}")
    suspend fun getRadioGenres(@Path("country") country: String): List<WebMeiliSearchAggType>

    @GET("radio/{countryCode}/browseStationsByCountry/{page}")
    suspend fun browseStations(
        @Path("countryCode") country: String,
        @Path("page") page: Int
    ): WebRadioStationsPageInfo

    @GET("radio/{countryCode}/browseStationsByCountryAndGenre/{page}")
    suspend fun browseStationsByCountryAndGenre(
        @Path("countryCode") country: String,
        @Path("page") page: Int,
        @Query("genre") genre: String
    ): WebRadioStationsPageInfo

    // Playlist endpoints
    @GET("playlist/{id}")
    suspend fun getPlaylistById(@Path("id") id: String): WebPlayListType

    @GET("playlist_collection/{id}")
    suspend fun getPlaylistCollectionById(@Path("id") id: String): WebPlayListType

    @GET("playlist/{id}/playlists")
    suspend fun getUserPlaylists(@Path("id") userId: String): List<WebUserPlayListType>

    @POST("playlist/{id}/create-new-playlist/{name}")
    suspend fun createNewPlaylist(
        @Path("id") userId: String,
        @Path("name") name: String,
        @Body body: CreatePlaylistRequest
    ): JsonElement

    @PUT("playlist/{userid}/{songid}")
    suspend fun addSongToPlaylist(
        @Path("userid") userId: String,
        @Path("songid") songId: String,
        @Body body: AddSongToPlaylistRequest
    ): WebPlayListType
}

data class LikeEntityRequest(
    val entityId: String,
    val entityType: String,
    val title: String,
    val image: String? = null,
    val s3link: String? = null,
    val albumName: String? = null,
    val podcastName: String? = null,
    val episodeNumber: Int? = null
)

data class UpdateHistoryRequest(
    val entityId: String,
    val entityType: String,
    val title: String,
    val image: String? = null,
    val s3link: String? = null,
    val albumName: String? = null,
    val podcastName: String? = null,
    val episodeNumber: Int? = null,
    val watchedDuration: Int = 0,
    val totalDuration: Int = 0,
    val watchedPercentage: Int = 0
)

data class WebHomePageInfo(
    val results: List<WebHomePageDataType> = emptyList(),
    val page: Int = 0
)

data class WebAllPlayListPageResponse(
    val results: List<WebAllPlayListType> = emptyList(),
    val page: Int = 0
)

data class PaginatedApiResponse<T>(
    val items: List<T> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 20,
    val hasMore: Boolean = false
)

data class LikedEntityResponseDto(
    val entityId: String = "",
    val entityType: String = "",
    val image: String? = null,
    val title: String = "",
    val s3link: String? = null,
    val albumName: String? = null,
    val album: ActivityAlbumDto? = null,
    val artists: List<ActivityArtistDto> = emptyList(),
    val podcastName: String? = null,
    val episodeNumber: Int? = null,
    val likedAt: String = ""
)

data class HistoryEntityResponseDto(
    val entityId: String = "",
    val entityType: String = "",
    val title: String = "",
    val image: String? = null,
    val s3link: String? = null,
    val watchedDuration: Int = 0,
    val totalDuration: Int = 0,
    val watchedPercentage: Int = 0,
    val lastPlayedAt: String = "",
    val albumName: String? = null,
    val album: ActivityAlbumDto? = null,
    val artists: List<ActivityArtistDto> = emptyList(),
    val podcastName: String? = null,
    val episodeNumber: Int? = null
)

data class ActivityArtistDto(
    @SerializedName(value = "title", alternate = ["name"])
    val title: String = "",
    val id: String = "",
    val path: String = ""
)

data class ActivityAlbumDto(
    @SerializedName(value = "title", alternate = ["name"])
    val title: String = "",
    val id: String = "",
    val path: String = ""
)

data class RequestTrackAdditionResponse(
    val id: String = "",
    val name: String = "",
    val album: String = "",
    val artist: String = ""
)

data class AddSongToPlaylistRequest(
    val playlists: List<String> = emptyList(),
    val posterpath: String = ""
)

data class CreatePlaylistRequest(
    val image: String = ""
)

data class AddSearchQueryRequest(
    val query: String
)

data class WebArtistSongsPaginationResponse(
    val results: List<WebSongType> = emptyList(),
    val page: Int = 0
)

data class WebSongsPageResponse(
    val results: List<WebSongType> = emptyList(),
    val page: Int = 0,
    val total: Int = 0
)

data class ApiStatusResponse(
    val status: String = ""
)

// Existing app-facing wrappers kept for compatibility with current viewmodels.
data class ArtistSongsPaginationResponse(
    val results: List<SongsModel> = emptyList(),
    val page: Int = 0
)

data class SongsPageResponse(
    val results: List<SongsModel> = emptyList(),
    val page: Int = 0,
    val total: Int = 0
)

data class PodcastBrowseResponse(
    val results: List<PodcastModel> = emptyList(),
    val page: Int = 0,
    val total: Int = 0
)

data class PodcastEpisodesResponse(
    val results: List<PodcastEpisodeModel> = emptyList(),
    val page: Int = 0,
    val total: Int = 0
)

data class StationsBrowseResponse(
    val results: List<RadioStationModel> = emptyList(),
    val page: Int = 0,
    val total: Int = 0
)

data class AlbumsBrowseResponse(
    val results: List<com.music.stream.neptune.data.entity.AlbumsModel> = emptyList(),
    val page: Int = 0,
    val total: Int = 0
)
