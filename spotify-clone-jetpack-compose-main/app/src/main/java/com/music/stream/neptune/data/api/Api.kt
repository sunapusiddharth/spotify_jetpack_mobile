package com.music.stream.neptune.data.api

import android.util.Log
import com.music.stream.neptune.data.entity.AlbumsModel
import com.music.stream.neptune.data.entity.ArtistsModel
import com.music.stream.neptune.data.entity.HomePageInfoModel
import com.music.stream.neptune.data.entity.HomePageSectionModel
import com.music.stream.neptune.data.entity.PodcastEpisodeModel
import com.music.stream.neptune.data.entity.PodcastModel
import com.music.stream.neptune.data.entity.PaginatedItems
import com.music.stream.neptune.data.entity.RadioCountryAggModel
import com.music.stream.neptune.data.entity.RadioGenreAggModel
import com.music.stream.neptune.data.entity.RadioStationModel
import com.music.stream.neptune.data.entity.SearchResultModel
import com.music.stream.neptune.data.entity.SongsModel
import com.music.stream.neptune.data.entity.UserHistoryEntityModel
import com.music.stream.neptune.data.entity.UserHistoryPageModel
import com.music.stream.neptune.data.entity.UserLikedEntityModel
import com.music.stream.neptune.data.entity.UserLikesPageModel
import com.music.stream.neptune.data.entity.UserModel
import com.music.stream.neptune.data.entity.UserPlaylistModel
import com.music.stream.neptune.data.entity.web.toDomain
import com.music.stream.neptune.data.network.AddSongToPlaylistRequest
import com.music.stream.neptune.data.network.ArtistSongsPaginationResponse
import com.music.stream.neptune.data.network.CreatePlaylistRequest
import com.music.stream.neptune.data.network.HistoryEntityResponseDto
import com.music.stream.neptune.data.network.LikeEntityRequest
import com.music.stream.neptune.data.network.LikedEntityResponseDto
import com.music.stream.neptune.data.network.NetworkApi
import com.music.stream.neptune.data.network.PodcastBrowseResponse
import com.music.stream.neptune.data.network.PodcastEpisodesResponse
import com.music.stream.neptune.data.network.SongsPageResponse
import com.music.stream.neptune.data.network.StationsBrowseResponse
import com.music.stream.neptune.data.network.AlbumsBrowseResponse
import com.music.stream.neptune.data.network.UpdateHistoryRequest
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Api @Inject constructor(private val networkApi: NetworkApi) {
    private val gson = Gson()

    // In-memory caches to avoid repeated network calls
    private var cachedAlbums: List<AlbumsModel>? = null
    private var cachedArtists: List<ArtistsModel>? = null
    private var cachedSongs: List<SongsModel>? = null
    private var cachedUser: UserModel? = null
    private val likedSongIds = MutableStateFlow<Set<String>>(emptySet())
    private val likedAlbumIds = MutableStateFlow<Set<String>>(emptySet())
    private val likedEntityKeys = MutableStateFlow<Set<String>>(emptySet())

    fun observeLikedSongIds(): StateFlow<Set<String>> = likedSongIds.asStateFlow()
    fun observeLikedAlbumIds(): StateFlow<Set<String>> = likedAlbumIds.asStateFlow()
    fun observeLikedEntityKeys(): StateFlow<Set<String>> = likedEntityKeys.asStateFlow()

    private fun entityLikeKey(entityType: String, entityId: String): String = "$entityType:$entityId"

    fun clearCachedUser() {
        cachedUser = null
        likedSongIds.value = emptySet()
        likedAlbumIds.value = emptySet()
        likedEntityKeys.value = emptySet()
    }

    private fun LikedEntityResponseDto.toDomain(): UserLikedEntityModel = UserLikedEntityModel(
        entityId = entityId,
        entityType = entityType,
        title = title,
        image = image.orEmpty(),
        s3link = s3link.orEmpty(),
        subtitle = albumName ?: podcastName.orEmpty(),
        episodeNumber = episodeNumber,
        likedAt = likedAt
    )

    private fun HistoryEntityResponseDto.toDomain(): UserHistoryEntityModel = UserHistoryEntityModel(
        entityId = entityId,
        entityType = entityType,
        title = title,
        image = image.orEmpty(),
        s3link = s3link.orEmpty(),
        subtitle = albumName ?: podcastName.orEmpty(),
        episodeNumber = episodeNumber,
        watchedDuration = watchedDuration,
        totalDuration = totalDuration,
        watchedPercentage = watchedPercentage,
        lastPlayedAt = lastPlayedAt
    )

    fun updateCachedSongLike(userId: String, trackId: String, liked: Boolean) {
        if (userId.isBlank() || trackId.isBlank()) return

        val nextIds = likedSongIds.value.toMutableSet().apply {
            if (liked) add(trackId) else remove(trackId)
        }
        likedSongIds.value = nextIds
        updateCachedEntityLike("song", trackId, liked)

        val existing = cachedUser
        if (existing != null && existing.id == userId) {
            cachedUser = existing.copy(likedSongs = nextIds.toList())
        }
    }

    fun updateCachedAlbumLike(albumId: String, liked: Boolean) {
        if (albumId.isBlank()) return

        val nextIds = likedAlbumIds.value.toMutableSet().apply {
            if (liked) add(albumId) else remove(albumId)
        }
        likedAlbumIds.value = nextIds
        updateCachedEntityLike("album", albumId, liked)
    }

    fun updateCachedEntityLike(entityType: String, entityId: String, liked: Boolean) {
        if (entityType.isBlank() || entityId.isBlank()) return

        val key = entityLikeKey(entityType, entityId)
        likedEntityKeys.value = likedEntityKeys.value.toMutableSet().apply {
            if (liked) add(key) else remove(key)
        }
    }

    suspend fun getUserById(userId: String): Flow<Response<UserModel>> = flow {
        emit(Response.Loading())
        try {
            if (userId.isBlank()) {
                clearCachedUser()
                emit(Response.Error("Login required"))
                return@flow
            }

            val cached = cachedUser
            if (cached != null && cached.id == userId) {
                likedSongIds.value = cached.likedSongs.toSet()
                emit(Response.Success(cached))
                return@flow
            }

            val user = networkApi.getUserById(userId).toDomain()
            cachedUser = user
            likedSongIds.value = user.likedSongs.toSet()
            emit(Response.Success(user))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching user $userId: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun refreshLikedSongs(userId: String, limit: Int = 100): Flow<Response<Set<String>>> = flow {
        emit(Response.Loading())
        try {
            if (userId.isBlank()) {
                likedSongIds.value = emptySet()
                likedAlbumIds.value = emptySet()
                likedEntityKeys.value = emptySet()
                emit(Response.Success(emptySet()))
                return@flow
            }

            val likes = networkApi.getUserLikes(userId, page = 1, limit = limit)
            val ids = likes.items
                .filter { it.entityType == "song" }
                .map { it.entityId }
                .toSet()
            val albumIds = likes.items
                .filter { it.entityType == "album" }
                .map { it.entityId }
                .toSet()
            val entityKeys = likes.items
                .map { entityLikeKey(it.entityType, it.entityId) }
                .toSet()
            likedSongIds.value = ids
            likedAlbumIds.value = albumIds
            likedEntityKeys.value = entityKeys
            emit(Response.Success(ids))
        } catch (e: Exception) {
            Log.e("Api", "Error refreshing liked songs: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun getUserLikes(userId: String, page: Int, limit: Int): Flow<Response<UserLikesPageModel>> = flow {
        emit(Response.Loading())
        try {
            if (userId.isBlank()) {
                emit(Response.Success(UserLikesPageModel()))
                return@flow
            }

            val result = networkApi.getUserLikes(userId, page, limit)
            emit(
                Response.Success(
                    PaginatedItems(
                        items = result.items.map { it.toDomain() },
                        total = result.total,
                        page = result.page,
                        limit = result.limit,
                        hasMore = result.hasMore
                    )
                )
            )
        } catch (e: Exception) {
            Log.e("Api", "Error fetching user likes: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun getUserHistory(userId: String, page: Int, limit: Int): Flow<Response<UserHistoryPageModel>> = flow {
        emit(Response.Loading())
        try {
            if (userId.isBlank()) {
                emit(Response.Success(UserHistoryPageModel()))
                return@flow
            }

            val result = networkApi.getUserHistory(userId, page, limit)
            emit(
                Response.Success(
                    PaginatedItems(
                        items = result.items.map { it.toDomain() },
                        total = result.total,
                        page = result.page,
                        limit = result.limit,
                        hasMore = result.hasMore
                    )
                )
            )
        } catch (e: Exception) {
            Log.e("Api", "Error fetching user history: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

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

    suspend fun getHomePage(userId: String, page: Int): Flow<Response<HomePageInfoModel>> = flow {
        emit(Response.Loading())
        try {
            if (userId.isBlank()) {
                emit(Response.Success(HomePageInfoModel(page = page)))
                return@flow
            }
            val result = networkApi.getHomePage(userId, page)
            val sections = parseHomeSections(result.get("results"))
            emit(
                Response.Success(
                    HomePageInfoModel(
                        results = sections.map { it.toDomain() },
                        page = result.get("page")?.takeIf { !it.isJsonNull }?.asInt ?: page
                    )
                )
            )
        } catch (e: Exception) {
            Log.e("Api", "Error fetching home page for $userId: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    private fun parseHomeSections(element: JsonElement?): List<com.music.stream.neptune.data.entity.web.WebHomePageDataType> {
        if (element == null || element.isJsonNull) return emptyList()

        return when {
            element.isJsonArray -> element.asJsonArray.flatMap { parseHomeSections(it) }
            element.isJsonObject -> parseHomeSectionObject(element.asJsonObject)?.let(::listOf).orEmpty()
            else -> emptyList()
        }
    }

    private fun parseHomeSectionObject(element: JsonObject): com.music.stream.neptune.data.entity.web.WebHomePageDataType? {
        if (element.entrySet().isEmpty()) return null

        val section = gson.fromJson(element, com.music.stream.neptune.data.entity.web.WebHomePageDataType::class.java)
        val hasMeaningfulData = section.cardType.isNotBlank() ||
            section.label.isNotBlank() ||
            section.path.isNotBlank() ||
            section.id.isNotBlank() ||
            section.cards.isNotEmpty()

        return section.takeIf { hasMeaningfulData }
    }

    suspend fun getSongs(): Flow<Response<List<SongsModel>>> = flow {
        emit(Response.Loading())
        try {
            val cached = cachedSongs
            if (cached != null) {
                emit(Response.Success(cached))
                return@flow
            }
            val songs = networkApi.getAllAvailableSongs(0, 500).results.map { it.toDomain() }.distinctBy { it.id }
            cachedSongs = songs
            Log.d("Api", "Fetched ${songs.size} songs from available songs endpoint")
            emit(Response.Success(songs))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching songs: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun getAllAlbums(page: Int): Flow<Response<AlbumsBrowseResponse>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.getAllAlbums(page)
            val mappedAlbums = result.results.map { it.toDomain() }
            emit(
                Response.Success(
                    AlbumsBrowseResponse(
                        results = mappedAlbums,
                        page = result.page,
                        total = mappedAlbums.size
                    )
                )
            )
        } catch (e: Exception) {
            Log.e("Api", "Error fetching all albums page=$page: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun getAlbumById(id: String): Flow<Response<AlbumsModel?>> = flow {
        emit(Response.Loading())
        try {
            // Try cache first
            val fromCache = cachedAlbums?.find { it.id == id }
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

    suspend fun getTopScoringSongsForUser(userId: String, limit: Int): Flow<Response<List<SongsModel>>> = flow {
        emit(Response.Loading())
        try {
            if (userId.isBlank()) {
                emit(Response.Success(emptyList()))
                return@flow
            }
            val result = networkApi.getTopScoringSongsForUser(userId, limit).map { it.toDomain() }
            emit(Response.Success(result))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching user top scoring songs: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun getAllAvailableSongs(skip: Int, limit: Int): Flow<Response<SongsPageResponse>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.getAllAvailableSongs(skip, limit)
            val mapped = SongsPageResponse(
                results = result.results.map { it.toDomain() },
                page = result.page,
                total = if (result.total > 0) result.total else result.results.size
            )
            emit(Response.Success(mapped))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching all available songs skip=$skip limit=$limit: ${e.message}")
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

    suspend fun likeDislikeSong(userId: String, likeDislike: Boolean, song: SongsModel): Flow<Response<Boolean>> = flow {
        emit(Response.Loading())
        try {
            if (song.id.isBlank()) {
                emit(Response.Error("Song id is missing"))
                return@flow
            }
            if (likeDislike) {
                networkApi.likeEntity(
                    userId,
                    song.id,
                    LikeEntityRequest(
                        entityId = song.id,
                        entityType = "song",
                        title = song.title,
                        image = song.coverUri.takeIf { it.isNotBlank() },
                        s3link = song.s3link.takeIf { it.isNotBlank() },
                        albumName = song.album.title.takeIf { it.isNotBlank() }
                    )
                )
            } else {
                networkApi.unlikeEntity(userId, song.id)
            }
            emit(Response.Success(true))
        } catch (e: Exception) {
            Log.e("Api", "Error like/dislike song: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun likeDislikeAlbum(userId: String, likeDislike: Boolean, album: AlbumsModel): Flow<Response<Boolean>> = flow {
        emit(Response.Loading())
        try {
            if (album.id.isBlank()) {
                emit(Response.Error("Album id is missing"))
                return@flow
            }
            if (likeDislike) {
                networkApi.likeEntity(
                    userId,
                    album.id,
                    LikeEntityRequest(
                        entityId = album.id,
                        entityType = "album",
                        title = album.title,
                        image = album.image.takeIf { it.isNotBlank() },
                        albumName = album.title.takeIf { it.isNotBlank() }
                    )
                )
            } else {
                networkApi.unlikeEntity(userId, album.id)
            }
            emit(Response.Success(true))
        } catch (e: Exception) {
            Log.e("Api", "Error like/dislike album: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun likeDislikeRadioStation(userId: String, likeDislike: Boolean, station: RadioStationModel): Flow<Response<Boolean>> = flow {
        emit(Response.Loading())
        try {
            if (station.id.isBlank()) {
                emit(Response.Error("Station id is missing"))
                return@flow
            }
            if (likeDislike) {
                networkApi.likeEntity(
                    userId,
                    station.id,
                    LikeEntityRequest(
                        entityId = station.id,
                        entityType = "radio_station",
                        title = station.name,
                        image = station.coverUri.takeIf { it.isNotBlank() },
                        s3link = station.stream_url.takeIf { it.isNotBlank() }
                    )
                )
            } else {
                networkApi.unlikeEntity(userId, station.id)
            }
            emit(Response.Success(true))
        } catch (e: Exception) {
            Log.e("Api", "Error like/dislike station: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun likeDislikePodcastEpisode(
        userId: String,
        likeDislike: Boolean,
        podcast: PodcastModel,
        episode: PodcastEpisodeModel
    ): Flow<Response<Boolean>> = flow {
        emit(Response.Loading())
        try {
            if (episode.id.isBlank()) {
                emit(Response.Error("Episode id is missing"))
                return@flow
            }
            if (likeDislike) {
                networkApi.likeEntity(
                    userId,
                    episode.id,
                    LikeEntityRequest(
                        entityId = episode.id,
                        entityType = "podcast_episode",
                        title = episode.title,
                        image = episode.thumbnail.takeIf { it.isNotBlank() } ?: podcast.image.takeIf { it.isNotBlank() },
                        s3link = episode.s3link.takeIf { it.isNotBlank() } ?: episode.preview_url.takeIf { it.isNotBlank() },
                        podcastName = podcast.title.takeIf { it.isNotBlank() },
                        episodeNumber = episode.episode_number.takeIf { it > 0 }
                    )
                )
            } else {
                networkApi.unlikeEntity(userId, episode.id)
            }
            emit(Response.Success(true))
        } catch (e: Exception) {
            Log.e("Api", "Error like/dislike podcast episode: ${e.message}")
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

    suspend fun userListenedPodcasts(
        userId: String,
        podcast: PodcastModel,
        episode: PodcastEpisodeModel,
        duration: Int,
    ): Flow<Response<Boolean>> = flow {
        emit(Response.Loading())
        try {
            val totalDuration = episode.duration.coerceAtLeast(0)
            val watchedPercentage = if (totalDuration > 0) {
                ((duration.toDouble() / totalDuration.toDouble()) * 100.0).toInt().coerceIn(0, 100)
            } else {
                0
            }
            networkApi.updateHistory(
                userId = userId,
                entityId = episode.id,
                body = UpdateHistoryRequest(
                    entityId = episode.id,
                    entityType = "podcast_episode",
                    title = episode.title,
                    image = episode.thumbnail.takeIf { it.isNotBlank() } ?: podcast.image.takeIf { it.isNotBlank() },
                    s3link = episode.s3link.takeIf { it.isNotBlank() } ?: episode.preview_url.takeIf { it.isNotBlank() },
                    podcastName = podcast.title.takeIf { it.isNotBlank() },
                    episodeNumber = episode.episode_number.takeIf { it > 0 },
                    watchedDuration = duration.coerceAtLeast(0),
                    totalDuration = totalDuration,
                    watchedPercentage = watchedPercentage
                )
            )
            emit(Response.Success(true))
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

    suspend fun userListenedStation(userId: String, station: RadioStationModel, duration: Int): Flow<Response<Boolean>> = flow {
        emit(Response.Loading())
        try {
            networkApi.updateHistory(
                userId = userId,
                entityId = station.id,
                body = UpdateHistoryRequest(
                    entityId = station.id,
                    entityType = "radio_station",
                    title = station.name,
                    image = station.coverUri.takeIf { it.isNotBlank() },
                    s3link = station.stream_url.takeIf { it.isNotBlank() },
                    watchedDuration = duration.coerceAtLeast(0),
                    totalDuration = 0,
                    watchedPercentage = 0
                )
            )
            emit(Response.Success(true))
        } catch (e: Exception) {
            Log.e("Api", "Error sending station listened event: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun userPlayedSong(userId: String, song: SongsModel, duration: Int): Flow<Response<Boolean>> = flow {
        emit(Response.Loading())
        try {
            val totalDuration = song.duration.coerceAtLeast(0)
            val watchedPercentage = if (totalDuration > 0) {
                ((duration.toDouble() / totalDuration.toDouble()) * 100.0).toInt().coerceIn(0, 100)
            } else {
                0
            }
            networkApi.updateHistory(
                userId = userId,
                entityId = song.id,
                body = UpdateHistoryRequest(
                    entityId = song.id,
                    entityType = "song",
                    title = song.title,
                    image = song.coverUri.takeIf { it.isNotBlank() },
                    s3link = song.s3link.takeIf { it.isNotBlank() },
                    albumName = song.album.title.takeIf { it.isNotBlank() },
                    watchedDuration = duration.coerceAtLeast(0),
                    totalDuration = totalDuration,
                    watchedPercentage = watchedPercentage
                )
            )
            emit(Response.Success(true))
        } catch (e: Exception) {
            Log.e("Api", "Error sending song played event: ${e.message}")
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

    suspend fun getUserPlaylists(userId: String): Flow<Response<List<UserPlaylistModel>>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.getUserPlaylists(userId).map {
                UserPlaylistModel(id = it.id, name = it.name, image = it.image, tracks = it.tracks)
            }
            emit(Response.Success(result))
        } catch (e: Exception) {
            Log.e("Api", "Error fetching user playlists: ${e.message}")
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun createNewPlaylist(userId: String, name: String, image: String): Flow<Response<UserPlaylistModel>> = flow {
        emit(Response.Loading())
        try {
            val result = networkApi.createNewPlaylist(
                userId = userId,
                name = name,
                body = CreatePlaylistRequest(image = image)
            )
            emit(
                Response.Success(
                    UserPlaylistModel(
                        id = result.id,
                        name = result.name,
                        image = result.image,
                        tracks = result.tracks
                    )
                )
            )
        } catch (e: Exception) {
            Log.e("Api", "Error creating playlist: ${e.message}")
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
