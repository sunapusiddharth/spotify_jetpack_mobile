package com.music.stream.neptune.ui.repository

import com.music.stream.neptune.data.api.Api
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(private val api: Api) {

    fun observeLikedSongIds(): StateFlow<Set<String>> = api.observeLikedSongIds()
    fun clearCachedUserProfile() = api.clearCachedUser()
    fun updateCachedSongLike(userId: String, trackId: String, liked: Boolean) = api.updateCachedSongLike(userId, trackId, liked)

    suspend fun provideUserById(userId: String) = api.getUserById(userId)
    suspend fun provideHomePage(userId: String, page: Int) = api.getHomePage(userId, page)
    suspend fun provideAlbums() = api.getAlbums()
    suspend fun provideArtists() = api.getArtists()
    suspend fun provideSongs() = api.getSongs()
    suspend fun provideAlbumById(id: String) = api.getAlbumById(id)
    suspend fun provideArtistById(id: String) = api.getArtistById(id)
    suspend fun provideArtistSongs(id: String, page: Int) = api.getArtistSongs(id, page)
    suspend fun provideSearch(query: String, type: String, page: Int) = api.searchAll(query, type, page)
    suspend fun provideTopScoringSongs(limit: Int) = api.getTopScoringSongs(limit)
    suspend fun provideTopScoringSongsForUser(userId: String, limit: Int) = api.getTopScoringSongsForUser(userId, limit)
    suspend fun provideAddPlaylistToQueue(userId: String, trackIds: List<String>) = api.addPlaylistToQueue(userId, trackIds)
    suspend fun provideNextQueueItem(userId: String) = api.getNextQueueItem(userId)
    suspend fun providePrevQueueItem(userId: String) = api.getPrevQueueItem(userId)
    suspend fun provideRequestTrackAddition(userId: String, songId: String) = api.requestTrackAddition(userId, songId)
    suspend fun provideLikeDislikeSong(userId: String, likeDislike: Boolean, trackId: String) =
        api.likeDislikeSong(userId, likeDislike, trackId)
    suspend fun provideLikeDislikeRadio(userId: String, likeDislike: Boolean, trackId: String) =
        api.likeDislikeRadio(userId, likeDislike, trackId)
    suspend fun provideLikeDislikePodcast(userId: String, likeDislike: Boolean, trackId: String) =
        api.likeDislikePodcast(userId, likeDislike, trackId)

    // Available tracks
    suspend fun provideAllSongs(page: Int, limit: Int) = api.getAllSongs(page, limit)
    suspend fun provideAllAvailableSongs(skip: Int, limit: Int) = api.getAllAvailableSongs(skip, limit)

    // Podcast
    suspend fun provideBrowsePodcasts(page: Int) = api.browsePodcasts(page)
    suspend fun provideBrowsePodcastsByGenre(genre: String, page: Int) = api.browsePodcastsByGenre(genre, page)
    suspend fun providePodcastById(id: String) = api.getPodcastById(id)
    suspend fun providePodcastEpisodes(id: String, page: Int) = api.getPodcastEpisodes(id, page)
    suspend fun providePodcastGenres() = api.getPodcastGenres()
    suspend fun provideUserLikedPodcasts(userId: String) = api.getUserLikedPodcasts(userId)
    suspend fun provideTopPodcastsByUserActivity(userId: String) = api.topPodcastsByUserActivity(userId)
    suspend fun provideRequestPodcastEpisodesPopulation(userId: String, podcastId: String) =
        api.requestPodcastEpisodesPopulation(userId, podcastId)
    suspend fun provideUserLikedPodcastsAction(userId: String, podcastId: String, episodeId: String) =
        api.userLikedPodcasts(userId, podcastId, episodeId)
    suspend fun provideUserListenedPodcastsAction(userId: String, podcastId: String, duration: Int, episodeId: String) =
        api.userListenedPodcasts(userId, podcastId, duration, episodeId)

    // Radio
    suspend fun provideRadioCountries() = api.getRadioCountries()
    suspend fun provideRadioCountryAggs() = api.getRadioCountryAggs()
    suspend fun provideRadioGenres(country: String) = api.getRadioGenres(country)
    suspend fun provideRadioGenreAggs(country: String) = api.getRadioGenreAggs(country)
    suspend fun provideTrendingStations(country: String) = api.getTrendingStations(country)
    suspend fun provideUserLikedStations(userId: String) = api.getUserLikedStations(userId)
    suspend fun provideLastPlayedStations(userId: String) = api.getLastPlayedStations(userId)
    suspend fun provideUserListenedStation(userId: String, stationId: String, duration: Int) =
        api.userListenedStation(userId, stationId, duration)
    suspend fun provideBrowseStations(country: String, page: Int) = api.browseStations(country, page)
    suspend fun provideBrowseStationsByCountryAndGenre(country: String, genre: String, page: Int) =
        api.browseStationsByCountryAndGenre(country, genre, page)

    // Playlist / PlaylistCollection
    suspend fun providePlaylistById(id: String) = api.getPlaylistById(id)
    suspend fun providePlaylistCollectionById(id: String) = api.getPlaylistCollectionById(id)
    suspend fun provideEditorsPlayList(limit: Int) = api.getEditorsPlayList(limit)
    suspend fun provideLatestPlaylistCollections() = api.getLatestPlaylistCollections()
    suspend fun provideUserPlaylists(userId: String) = api.getUserPlaylists(userId)
    suspend fun provideCreateNewPlaylist(userId: String, name: String, image: String) =
        api.createNewPlaylist(userId, name, image)
    suspend fun provideAddSongToPlaylist(userId: String, songId: String, playlistIds: List<String>, posterPath: String) =
        api.addSongToPlaylist(userId, songId, playlistIds, posterPath)
}
