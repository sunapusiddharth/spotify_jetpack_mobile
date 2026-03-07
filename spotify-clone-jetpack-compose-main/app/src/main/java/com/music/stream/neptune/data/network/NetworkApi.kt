package com.music.stream.neptune.data.network

import com.music.stream.neptune.data.entity.PodcastEpisodeModel
import com.music.stream.neptune.data.entity.PodcastModel
import com.music.stream.neptune.data.entity.RadioStationModel
import com.music.stream.neptune.data.entity.SongsModel
import com.music.stream.neptune.data.entity.web.WebArtistType
import com.music.stream.neptune.data.entity.web.WebMeiliSearchAggType
import com.music.stream.neptune.data.entity.web.WebPlayListType
import com.music.stream.neptune.data.entity.web.WebPodcastCardDto
import com.music.stream.neptune.data.entity.web.WebPodcastsByGenrePageInfo
import com.music.stream.neptune.data.entity.web.WebPodcastEpisodesPageInfo
import com.music.stream.neptune.data.entity.web.WebPodcastsPageInfo
import com.music.stream.neptune.data.entity.web.WebRadioCountryAgg
import com.music.stream.neptune.data.entity.web.WebRadioStation
import com.music.stream.neptune.data.entity.web.WebRadioStationsPageInfo
import com.music.stream.neptune.data.entity.web.WebSearchPageResType
import com.music.stream.neptune.data.entity.web.WebSongType
import com.music.stream.neptune.data.entity.web.WebUserPlayListType
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface NetworkApi {

    // Album endpoints
    @GET("album/freshAlbums")
    suspend fun getFreshAlbums(): List<WebPlayListType>

    @GET("album/{id}")
    suspend fun getAlbumById(@Path("id") id: String): WebPlayListType

    // Artist endpoints
    @GET("artist/top")
    suspend fun getTopArtists(): List<WebArtistType>

    @GET("artist/{id}")
    suspend fun getArtistById(@Path("id") id: String): WebArtistType

    @GET("artist/songs/{id}/{page}")
    suspend fun getArtistSongs(
        @Path("id") id: String,
        @Path("page") page: Int
    ): WebArtistSongsPaginationResponse

    // Track / song endpoints
    @GET("tracks/top_scoring_songs/{limit}")
    suspend fun getTopScoringSongs(@Path("limit") limit: Int): List<WebSongType>

    @POST("tracks/{id}/add-multiple")
    suspend fun addPlaylistToQueue(
        @Path("id") userId: String,
        @Body body: AddMultipleTracksRequest
    ): ApiStatusResponse

    @GET("tracks/{userid}/next_item_in_queue")
    suspend fun getNextQueueItem(@Path("userid") userId: String): QueueItemResponse

    @GET("tracks/{userid}/prev_item_in_queue")
    suspend fun getPrevQueueItem(@Path("userid") userId: String): QueueItemResponse

    @POST("tracks/requestTrackAddition/{userid}/{songid}")
    suspend fun requestTrackAddition(
        @Path("userid") userId: String,
        @Path("songid") songId: String
    ): RequestTrackAdditionResponse

    @PUT("users/{id}/likedislike/song/{trackid}/{likedislike}")
    suspend fun likeDislikeSong(
        @Path("id") userId: String,
        @Path("trackid") trackId: String,
        @Path("likedislike") likeDislike: Boolean
    ): ApiStatusResponse

    @PUT("users/{id}/likedislike/radio/{trackid}/{likedislike}")
    suspend fun likeDislikeRadio(
        @Path("id") userId: String,
        @Path("trackid") trackId: String,
        @Path("likedislike") likeDislike: Boolean
    ): ApiStatusResponse

    @PUT("users/{id}/likedislike/podcast/{trackid}/{likedislike}")
    suspend fun likeDislikePodcast(
        @Path("id") userId: String,
        @Path("trackid") trackId: String,
        @Path("likedislike") likeDislike: Boolean
    ): ApiStatusResponse

    @GET("tracks/allSongs/{page}/{limit}")
    suspend fun getAllSongs(
        @Path("page") page: Int,
        @Path("limit") limit: Int
    ): WebSongsPageResponse

    // Search endpoint
    @GET("search/test/{query}/{type}/{page}")
    suspend fun searchAll(
        @Path("query") query: String,
        @Path("type") type: String,
        @Path("page") page: Int
    ): WebSearchPageResType

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

    @GET("podcast/{userid}/liked_podcasts")
    suspend fun getUserLikedPodcasts(@Path("userid") userId: String): List<WebPodcastCardDto>

    @PUT("podcast/{userid}/liked_podcast/{podcastid}/{episodeid}")
    suspend fun userLikedPodcasts(
        @Path("userid") userId: String,
        @Path("podcastid") podcastId: String,
        @Path("episodeid") episodeId: String
    ): ApiStatusResponse

    @PUT("podcast/{userid}/listened_station/{podcastid}/{episodeid}/{duration}")
    suspend fun userListenedPodcasts(
        @Path("userid") userId: String,
        @Path("podcastid") podcastId: String,
        @Path("episodeid") episodeId: String,
        @Path("duration") duration: Int
    ): List<WebPodcastCardDto>

    @GET("podcast/{id}/topPodcastsByUserActivity")
    suspend fun topPodcastsByUserActivity(@Path("id") userId: String): List<WebPodcastCardDto>

    @POST("podcast/requestPodcastEpisodesPopulation/{userid}/{songid}")
    suspend fun requestPodcastEpisodesPopulation(
        @Path("userid") userId: String,
        @Path("songid") podcastId: String
    ): ApiStatusResponse

    @GET("podcast/all_genres")
    suspend fun getPodcastGenres(): List<WebMeiliSearchAggType>

    // Radio endpoints
    @GET("radio/all_countries")
    suspend fun getRadioCountries(): List<WebRadioCountryAgg>

    @GET("radio/all_genres/{country}")
    suspend fun getRadioGenres(@Path("country") country: String): List<WebMeiliSearchAggType>

    @GET("radio/{country}/trendingStations")
    suspend fun getTrendingStations(@Path("country") country: String): List<WebRadioStation>

    @GET("radio/{userid}/liked_stations")
    suspend fun getUserLikedStations(@Path("userid") userId: String): List<WebRadioStation>

    @GET("radio/{userid}/last_played_stations")
    suspend fun getLastPlayedStations(@Path("userid") userId: String): List<WebRadioStation>

    @PUT("radio/{userid}/listened_station/{stationid}/{duration}")
    suspend fun userListenedStation(
        @Path("userid") userId: String,
        @Path("stationid") stationId: String,
        @Path("duration") duration: Int
    ): ApiStatusResponse

    @GET("radio/{country}/browseStationsByCountry/{page}")
    suspend fun browseStations(
        @Path("country") country: String,
        @Path("page") page: Int
    ): WebRadioStationsPageInfo

    @GET("radio/{country}/browseStationsByCountryAndGenre/{page}")
    suspend fun browseStationsByCountryAndGenre(
        @Path("country") country: String,
        @Path("page") page: Int,
        @Query("genre") genre: String
    ): WebRadioStationsPageInfo

    // Playlist endpoints
    @GET("playlist/{id}")
    suspend fun getPlaylistById(@Path("id") id: String): WebPlayListType

    @GET("playlist_collection/{id}")
    suspend fun getPlaylistCollectionById(@Path("id") id: String): WebPlayListType

    @GET("playlist_collection/latest")
    suspend fun getLatestPlaylistCollections(): WebPlaylistCollectionBrowseResponse

    @GET("playlist/{userid}/playlists")
    suspend fun getUserPlaylists(@Path("userid") userId: String): List<WebUserPlayListType>

    @PUT("playlist/{userid}/{songid}")
    suspend fun addSongToPlaylist(
        @Path("userid") userId: String,
        @Path("songid") songId: String,
        @Body body: AddSongToPlaylistRequest
    ): WebPlayListType
}

data class AddMultipleTracksRequest(
    val songs: List<String> = emptyList()
)

data class QueueItemResponse(
    val song: WebSongType = WebSongType(),
    val updated_queue: List<WebSongType> = emptyList()
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

data class WebArtistSongsPaginationResponse(
    val results: List<WebSongType> = emptyList(),
    val page: Int = 0
)

data class WebSongsPageResponse(
    val results: List<WebSongType> = emptyList(),
    val page: Int = 0,
    val total: Int = 0
)

data class WebPlaylistCollectionBrowseResponse(
    val results: List<WebPlayListType> = emptyList(),
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
