package com.music.stream.neptune.data.preferences

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.music.stream.neptune.data.entity.PersistedPlaybackSnapshot
import com.music.stream.neptune.data.entity.SongsModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val _recentSongs = MutableStateFlow(loadRecentSongs())

    fun observeRecentSongs(): StateFlow<List<SongsModel>> = _recentSongs.asStateFlow()

    fun saveSnapshot(snapshot: PersistedPlaybackSnapshot) {
        sharedPreferences.edit()
            .putString(KEY_SNAPSHOT, gson.toJson(snapshot))
            .apply()
    }

    fun loadSnapshot(): PersistedPlaybackSnapshot? {
        val rawSnapshot = sharedPreferences.getString(KEY_SNAPSHOT, null) ?: return null
        return runCatching {
            gson.fromJson(rawSnapshot, PersistedPlaybackSnapshot::class.java)
        }.getOrNull()
    }

    fun addRecentSong(song: SongsModel) {
        if (!song.hasPlayableAudio) return

        val updatedSongs = buildList {
            add(song)
            addAll(_recentSongs.value.filterNot { it.id == song.id })
        }.take(MAX_RECENT_SONGS)

        persistRecentSongs(updatedSongs)
    }

    private fun persistRecentSongs(songs: List<SongsModel>) {
        _recentSongs.value = songs
        sharedPreferences.edit()
            .putString(KEY_RECENT_SONGS, gson.toJson(songs))
            .apply()
    }

    private fun loadRecentSongs(): List<SongsModel> {
        val rawSongs = sharedPreferences.getString(KEY_RECENT_SONGS, null) ?: return emptyList()
        val listType = object : TypeToken<List<SongsModel>>() {}.type
        return runCatching {
            gson.fromJson<List<SongsModel>>(rawSongs, listType).orEmpty()
        }.getOrDefault(emptyList())
            .filter { it.hasPlayableAudio }
            .take(MAX_RECENT_SONGS)
    }

    private companion object {
        const val PREFS_NAME = "PlaybackPrefs"
        const val KEY_SNAPSHOT = "playback_snapshot"
        const val KEY_RECENT_SONGS = "recent_songs"
        const val MAX_RECENT_SONGS = 20
    }
}