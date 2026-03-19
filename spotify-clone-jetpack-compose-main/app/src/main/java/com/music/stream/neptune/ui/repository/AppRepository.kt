package com.music.stream.neptune.ui.repository

import com.music.stream.neptune.data.api.Api
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(private val api: Api) {

    fun observeLikedSongIds(): StateFlow<Set<String>> = api.observeLikedSongIds()
    fun observeLikedAlbumIds(): StateFlow<Set<String>> = api.observeLikedAlbumIds()
    fun observeLikedEntityKeys(): StateFlow<Set<String>> = api.observeLikedEntityKeys()
    fun clearCachedUserProfile() = api.clearCachedUser()
    fun updateCachedSongLike(userId: String, trackId: String, liked: Boolean) =
        api.updateCachedSongLike(userId, trackId, liked)
    fun updateCachedAlbumLike(albumId: String, liked: Boolean) =
        api.updateCachedAlbumLike(albumId, liked)
    fun updateCachedEntityLike(entityType: String, entityId: String, liked: Boolean) =
        api.updateCachedEntityLike(entityType, entityId, liked)

    suspend fun refreshLikedSongs(userId: String) = api.refreshLikedSongs(userId)
    suspend fun provideUserById(userId: String) = api.getUserById(userId)
    suspend fun provideUserLikes(userId: String, page: Int, limit: Int) = api.getUserLikes(userId, page, limit)
    suspend fun provideUserHistory(userId: String, page: Int, limit: Int) = api.getUserHistory(userId, page, limit)
    suspend fun provideHomePage(userId: String, page: Int) = api.getHomePage(userId, page)
    suspend fun provideAlbums() = api.getAlbums()
    suspend fun provideArtists() = api.getArtists()
    suspend fun provideSongs() = api.getSongs()
    suspend fun provideAllAlbums(page: Int) = api.getAllAlbums(page)
    suspend fun provideAlbumById(id: String) = api.getAlbumById(id)
    suspend fun provideArtistById(id: String) = api.getArtistById(id)
    suspend fun provideArtistSongs(id: String, page: Int) = api.getArtistSongs(id, page)
    suspend fun provideSearch(query: String, type: String, page: Int) = api.searchAll(query, type, page)
    suspend fun provideTopScoringSongs(limit: Int) = api.getTopScoringSongs(limit)
    suspend fun provideTopScoringSongsForUser(userId: String, limit: Int) = api.getTopScoringSongsForUser(userId, limit)

    suspend fun provideRequestTrackAddition(userId: String, songId: String) = api.requestTrackAddition(userId, songId)
    suspend fun provideLikeDislikeSong(userId: String, likeDislike: Boolean, song: com.music.stream.neptune.data.entity.SongsModel) =
        api.likeDislikeSong(userId, likeDislike, song)
    suspend fun provideLikeDislikeAlbum(userId: String, likeDislike: Boolean, album: com.music.stream.neptune.data.entity.AlbumsModel) =
        api.likeDislikeAlbum(userId, likeDislike, album)
    suspend fun provideLikeDislikeRadioStation(userId: String, likeDislike: Boolean, station: com.music.stream.neptune.data.entity.RadioStationModel) =
        api.likeDislikeRadioStation(userId, likeDislike, station)
    suspend fun provideLikeDislikePodcastEpisode(
        userId: String,
        likeDislike: Boolean,
        podcast: com.music.stream.neptune.data.entity.PodcastModel,
        episode: com.music.stream.neptune.data.entity.PodcastEpisodeModel
    ) = api.likeDislikePodcastEpisode(userId, likeDislike, podcast, episode)
    suspend fun provideUserPlayedSong(userId: String, song: com.music.stream.neptune.data.entity.SongsModel, duration: Int) =
        api.userPlayedSong(userId, song, duration)
    suspend fun provideAllAvailableSongs(skip: Int, limit: Int) = api.getAllAvailableSongs(skip, limit)

    suspend fun provideBrowsePodcasts(page: Int) = api.browsePodcasts(page)
    suspend fun provideBrowsePodcastsByGenre(genre: String, page: Int) = api.browsePodcastsByGenre(genre, page)
    suspend fun providePodcastById(id: String) = api.getPodcastById(id)
    suspend fun providePodcastEpisodes(id: String, page: Int) = api.getPodcastEpisodes(id, page)
    suspend fun providePodcastGenres() = api.getPodcastGenres()
    suspend fun provideRequestPodcastEpisodesPopulation(userId: String, podcastId: String) =
        api.requestPodcastEpisodesPopulation(userId, podcastId)
    suspend fun provideUserListenedPodcastsAction(
        userId: String,
        podcast: com.music.stream.neptune.data.entity.PodcastModel,
        episode: com.music.stream.neptune.data.entity.PodcastEpisodeModel,
        duration: Int
    ) = api.userListenedPodcasts(userId, podcast, episode, duration)

    suspend fun provideRadioCountries() = api.getRadioCountries()
    suspend fun provideRadioCountryAggs() = api.getRadioCountryAggs()
    suspend fun provideRadioGenres(country: String) = api.getRadioGenres(country)
    suspend fun provideRadioGenreAggs(country: String) = api.getRadioGenreAggs(country)
    suspend fun provideUserListenedStation(userId: String, station: com.music.stream.neptune.data.entity.RadioStationModel, duration: Int) =
        api.userListenedStation(userId, station, duration)
    suspend fun provideBrowseStations(country: String, page: Int) = api.browseStations(country, page)
    suspend fun provideBrowseStationsByCountryAndGenre(country: String, genre: String, page: Int) =
        api.browseStationsByCountryAndGenre(country, genre, page)

    suspend fun providePlaylistById(id: String) = api.getPlaylistById(id)
    suspend fun providePlaylistCollectionById(id: String) = api.getPlaylistCollectionById(id)
    suspend fun provideUserPlaylists(userId: String) = api.getUserPlaylists(userId)
    suspend fun provideCreateNewPlaylist(userId: String, name: String, image: String) =
        api.createNewPlaylist(userId, name, image)
    suspend fun provideAddSongToPlaylist(userId: String, songId: String, playlistIds: List<String>, posterPath: String) =
        api.addSongToPlaylist(userId, songId, playlistIds, posterPath)
}
