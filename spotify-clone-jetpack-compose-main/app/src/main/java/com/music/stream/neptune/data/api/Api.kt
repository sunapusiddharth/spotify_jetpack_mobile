package com.music.stream.neptune.data.api

import android.util.Log
import com.music.stream.neptune.data.entity.AlbumsModel
import com.music.stream.neptune.data.entity.ArtistsModel
import com.music.stream.neptune.data.entity.QueueUpdateModel
import com.music.stream.neptune.data.entity.PodcastEpisodeModel
import com.music.stream.neptune.data.entity.PodcastModel
import com.music.stream.neptune.data.entity.RadioCountryAggModel
import com.music.stream.neptune.data.entity.RadioGenreAggModel
import com.music.stream.neptune.data.entity.RadioStationModel
import com.music.stream.neptune.data.entity.SearchResultModel
import com.music.stream.neptune.data.entity.SongsModel
import com.music.stream.neptune.data.entity.UserPlaylistModel
import com.music.stream.neptune.data.entity.web.toDomain
import com.music.stream.neptune.data.network.AddMultipleTracksRequest
import com.music.stream.neptune.data.network.AddSongToPlaylistRequest
import com.music.stream.neptune.data.network.ArtistSongsPaginationResponse
import com.music.stream.neptune.data.network.NetworkApi
import com.music.stream.neptune.data.network.PodcastBrowseResponse
import com.music.stream.neptune.data.network.PodcastEpisodesResponse
import com.music.stream.neptune.data.network.SongsPageResponse
import com.music.stream.neptune.data.network.StationsBrowseResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Api @Inject constructor(private val networkApi: NetworkApi) {

    // In-memory caches to avoid repeated network calls
    private var cachedAlbums: List<AlbumsModel>? = null
    private var cachedArtists: List<ArtistsModel>? = null
    private var cachedSongs: List<SongsModel>? = null

    suspend fun getAlbums(): Flow<Response<List<AlbumsModel>>> = flow {
        emit(Response.Loading())
        try {
            val cached = cachedAlbums
            if (cached != null) {
                emit(Response.Success(cached))
                return@flow
            }
            val albums = networkApi.getFreshAlbums().map { it.toDomain() }
            cachedAlbums = albums
            Log.d("Api", "Fetched ${albums.size} albums from network")
            emit(Response.Success(albums))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching albums: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun getArtists(): Flow<Response<List<ArtistsModel>>> = flow {
        emit(Response.Loading())
        try {
            val cached = cachedArtists
            if (cached != null) {
                emit(Response.Success(cached))
                return@flow
            }
            val artists = networkApi.getTopArtists().map { it.toDomain() }
            cachedArtists = artists
            Log.d("Api", "Fetched ${artists.size} artists from network")
            emit(Response.Success(artists))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching artists: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun getSongs(): Flow<Response<List<SongsModel>>> = flow {
        emit(Response.Loading())
        try {
            val cached = cachedSongs
            if (cached != null) {
                emit(Response.Success(cached))
                return@flow
            }
            // Load all songs from fresh albums (embedded in each album)
            val albums = cachedAlbums ?: networkApi.getFreshAlbums().map { it.toDomain() }.also { cachedAlbums = it }
            val songs = albums.flatMap { it.songs }.distinctBy { it.id }
            cachedSongs = songs
            Log.d("Api", "Derived ${songs.size} songs from ${albums.size} albums")
            emit(Response.Success(songs))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching songs: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun getAlbumById(id: String): Flow<Response<AlbumsModel?>> = flow {
        emit(Response.Loading())
        try {
            // Try cache first
            val fromCache = cachedAlbums?.find { it.id.toString() == id }
            if (fromCache != null) {
                emit(Response.Success(fromCache))
                return@flow
            }
            val album = networkApi.getAlbumById(id).toDomain()
            emit(Response.Success(album))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching album $id: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun getArtistById(id: String): Flow<Response<ArtistsModel?>> = flow {
        emit(Response.Loading())
        try {
            val artist = networkApi.getArtistById(id).toDomain()
            emit(Response.Success(artist))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching artist $id: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun getArtistSongs(id: String, page: Int): Flow<Response<ArtistSongsPaginationResponse>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.getArtistSongs(id, page)
            val mapped = ArtistSongsPaginationResponse(
                results = result.results.map { it.toDomain() },
                page = result.page
            )
            emit(Response.Success(mapped))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching artist songs for $id: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun searchAll(query: String, type: String, page: Int): Flow<Response<SearchResultModel>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.searchAll(query, type, page).toDomain()
            emit(Response.Success(result))
        } catch (e: Exception) {
            Log.e("Api", "Error searching '$query': ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun getTopScoringSongs(limit: Int): Flow<Response<List<SongsModel>>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.getTopScoringSongs(limit).map { it.toDomain() }
            emit(Response.Success(result))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching top scoring songs: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    // ── Available tracks ──────────────────────────────────────────────────────
    suspend fun getAllSongs(page: Int, limit: Int): Flow<Response<SongsPageResponse>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.getAllSongs(page, limit)
            val mapped = SongsPageResponse(
                results = result.results.map { it.toDomain() },
                page = result.page,
                total = result.total
            )
            emit(Response.Success(mapped))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching all songs page=$page: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun addPlaylistToQueue(userId: String, trackIds: List<String>): Flow<Response<Boolean>> = flow {
        emit(Response.Loading())
        try {
            networkApi.addPlaylistToQueue(userId, AddMultipleTracksRequest(songs = trackIds))
            emit(Response.Success(true))
        } catch (e: Exception) {
            Log.e("Api", "Error adding playlist to queue: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun getNextQueueItem(userId: String): Flow<Response<QueueUpdateModel>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.getNextQueueItem(userId)
            emit(
                Response.Success(
                    QueueUpdateModel(
                        song = result.song.toDomain(),
                        updatedQueue = result.updated_queue.map { it.toDomain() }
                    )
                )
            )
        } catch (e: Exception) {
            Log.e("Api", "Error fetching next queue item: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun getPrevQueueItem(userId: String): Flow<Response<QueueUpdateModel>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.getPrevQueueItem(userId)
            emit(
                Response.Success(
                    QueueUpdateModel(
                        song = result.song.toDomain(),
                        updatedQueue = result.updated_queue.map { it.toDomain() }
                    )
                )
            )
        } catch (e: Exception) {
            Log.e("Api", "Error fetching prev queue item: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun requestTrackAddition(userId: String, songId: String): Flow<Response<Boolean>> = flow {
        emit(Response.Loading())
        try {
            networkApi.requestTrackAddition(userId, songId)
            emit(Response.Success(true))
        } catch (e: Exception) {
            Log.e("Api", "Error requesting track addition: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun likeDislikeSong(userId: String, likeDislike: Boolean, trackId: String): Flow<Response<Boolean>> = flow {
        emit(Response.Loading())
        try {
            networkApi.likeDislikeSong(userId, trackId, likeDislike)
            emit(Response.Success(true))
        } catch (e: Exception) {
            Log.e("Api", "Error like/dislike song: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun likeDislikeRadio(userId: String, likeDislike: Boolean, trackId: String): Flow<Response<Boolean>> = flow {
        emit(Response.Loading())
        try {
            networkApi.likeDislikeRadio(userId, trackId, likeDislike)
            emit(Response.Success(true))
        } catch (e: Exception) {
            Log.e("Api", "Error like/dislike radio: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun likeDislikePodcast(userId: String, likeDislike: Boolean, trackId: String): Flow<Response<Boolean>> = flow {
        emit(Response.Loading())
        try {
            networkApi.likeDislikePodcast(userId, trackId, likeDislike)
            emit(Response.Success(true))
        } catch (e: Exception) {
            Log.e("Api", "Error like/dislike podcast: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    // ── Podcast ───────────────────────────────────────────────────────────────
    suspend fun browsePodcasts(page: Int): Flow<Response<PodcastBrowseResponse>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.browsePodcasts(page)
            val podcasts = result.results.map { it.toDomain() }.distinctBy { it.id }
            val mapped = PodcastBrowseResponse(results = podcasts, page = result.page, total = podcasts.size)
            emit(Response.Success(mapped))
        } catch (e: Exception) {
            Log.e("Api", "Error browsing podcasts: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun browsePodcastsByGenre(genre: String, page: Int): Flow<Response<PodcastBrowseResponse>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.browsePodcastsByGenre(genre, page)
            val podcasts = result.results.flatMap { it.cards }.map { it.toDomain() }.distinctBy { it.id }
            val mapped = PodcastBrowseResponse(results = podcasts, page = result.page, total = podcasts.size)
            emit(Response.Success(mapped))
        } catch (e: Exception) {
            Log.e("Api", "Error browsing podcasts by genre: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun getPodcastById(id: String): Flow<Response<PodcastModel>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.getPodcastById(id).toDomain()
            emit(Response.Success(result))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching podcast $id: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun getPodcastEpisodes(id: String, page: Int): Flow<Response<PodcastEpisodesResponse>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.getPodcastEpisodes(id, page)
            val mapped = PodcastEpisodesResponse(
                results = result.results.map { it.toDomain() },
                page = result.page,
                total = result.results.size
            )
            emit(Response.Success(mapped))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching podcast episodes: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun getPodcastGenres(): Flow<Response<List<String>>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.getPodcastGenres().map { it.value }
            emit(Response.Success(result))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching podcast genres: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun getUserLikedPodcasts(userId: String): Flow<Response<List<PodcastModel>>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.getUserLikedPodcasts(userId).map { it.toDomain() }
            emit(Response.Success(result))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching liked podcasts: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun topPodcastsByUserActivity(userId: String): Flow<Response<List<PodcastModel>>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.topPodcastsByUserActivity(userId).map { it.toDomain() }
            emit(Response.Success(result))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching top podcasts by activity: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun userLikedPodcasts(userId: String, podcastId: String, episodeId: String): Flow<Response<Boolean>> = flow {
        emit(Response.Loading())
        try {
            networkApi.userLikedPodcasts(userId, podcastId, episodeId)
            emit(Response.Success(true))
        } catch (e: Exception) {
            Log.e("Api", "Error liking podcast: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun userListenedPodcasts(
        userId: String,
        podcastId: String,
        duration: Int,
        episodeId: String
    ): Flow<Response<List<PodcastModel>>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.userListenedPodcasts(userId, podcastId, episodeId, duration).map { it.toDomain() }
            emit(Response.Success(result))
        } catch (e: Exception) {
            Log.e("Api", "Error sending podcast listened event: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun requestPodcastEpisodesPopulation(userId: String, podcastId: String): Flow<Response<Boolean>> = flow {
        emit(Response.Loading())
        try {
            networkApi.requestPodcastEpisodesPopulation(userId, podcastId)
            emit(Response.Success(true))
        } catch (e: Exception) {
            Log.e("Api", "Error requesting podcast episodes: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    // ── Radio ─────────────────────────────────────────────────────────────────
    suspend fun getRadioCountries(): Flow<Response<List<String>>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.getRadioCountries().map { it.code.ifBlank { it.name } }
            emit(Response.Success(result))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching radio countries: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun getRadioCountryAggs(): Flow<Response<List<RadioCountryAggModel>>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.getRadioCountries().map {
                RadioCountryAggModel(name = it.name, count = it.count, code = it.code)
            }
            emit(Response.Success(result))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching radio country aggs: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun getRadioGenres(country: String): Flow<Response<List<String>>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.getRadioGenres(country).map { it.value }
            emit(Response.Success(result))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching radio genres for $country: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun getRadioGenreAggs(country: String): Flow<Response<List<RadioGenreAggModel>>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.getRadioGenres(country).map {
                RadioGenreAggModel(value = it.value, count = it.count)
            }
            emit(Response.Success(result))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching radio genre aggs for $country: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun getTrendingStations(country: String): Flow<Response<List<RadioStationModel>>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.getTrendingStations(country).map { it.toDomain() }
            emit(Response.Success(result))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching trending stations: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun getUserLikedStations(userId: String): Flow<Response<List<RadioStationModel>>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.getUserLikedStations(userId).map { it.toDomain() }
            emit(Response.Success(result))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching liked stations: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun getLastPlayedStations(userId: String): Flow<Response<List<RadioStationModel>>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.getLastPlayedStations(userId).map { it.toDomain() }
            emit(Response.Success(result))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching last played stations: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun userListenedStation(userId: String, stationId: String, duration: Int): Flow<Response<Boolean>> = flow {
        emit(Response.Loading())
        try {
            networkApi.userListenedStation(userId, stationId, duration)
            emit(Response.Success(true))
        } catch (e: Exception) {
            Log.e("Api", "Error sending station listened event: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun browseStations(country: String, page: Int): Flow<Response<StationsBrowseResponse>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.browseStations(country, page)
            val mapped = StationsBrowseResponse(
                results = result.results.map { it.toDomain() },
                page = result.page,
                total = result.results.size
            )
            emit(Response.Success(mapped))
        } catch (e: Exception) {
            Log.e("Api", "Error browsing stations: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun browseStationsByCountryAndGenre(
        country: String,
        genre: String,
        page: Int
    ): Flow<Response<StationsBrowseResponse>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.browseStationsByCountryAndGenre(country, page, genre)
            val mapped = StationsBrowseResponse(
                results = result.results.map { it.toDomain() },
                page = result.page,
                total = result.results.size
            )
            emit(Response.Success(mapped))
        } catch (e: Exception) {
            Log.e("Api", "Error browsing stations by country+genre: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    // ── Playlist / PlaylistCollection ─────────────────────────────────────────
    suspend fun getPlaylistById(id: String): Flow<Response<AlbumsModel?>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.getPlaylistById(id).toDomain()
            emit(Response.Success(result))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching playlist $id: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun getPlaylistCollectionById(id: String): Flow<Response<AlbumsModel?>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.getPlaylistCollectionById(id).toDomain()
            emit(Response.Success(result))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching playlist collection $id: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun getLatestPlaylistCollections(): Flow<Response<List<AlbumsModel>>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.getLatestPlaylistCollections().results.map { it.toDomain() }
            emit(Response.Success(result))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching latest playlist collections: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun getUserPlaylists(userId: String): Flow<Response<List<UserPlaylistModel>>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.getUserPlaylists(userId).map {
                UserPlaylistModel(id = it.id, name = it.name, image = it.image)
            }
            emit(Response.Success(result))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching user playlists: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun addSongToPlaylist(userId: String, songId: String, playlistIds: List<String>, posterPath: String): Flow<Response<Boolean>> = flow {
        emit(Response.Loading())
        try {
            networkApi.addSongToPlaylist(
                userId = userId,
                songId = songId,
                body = AddSongToPlaylistRequest(playlists = playlistIds, posterpath = posterPath)
            )
            emit(Response.Success(true))
        } catch (e: Exception) {
            Log.e("Api", "Error adding song to playlist: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    fun clearCache() {
        cachedAlbums = null
        cachedArtists = null
        cachedSongs = null
    }
}
