package com.music.stream.neptune.data.preferences

import android.content.Context
import com.music.stream.neptune.data.entity.SongsModel

fun addLikedSongId(context: Context, songId: String) {
    val sharedPreferences = context.getSharedPreferences("LikedSongs", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString(songId, songId).apply()
}

fun removeLikedSongId(context: Context, songId: String) {
    val sharedPreferences = context.getSharedPreferences("LikedSongs", Context.MODE_PRIVATE)
    sharedPreferences.edit().remove(songId).apply()
}

fun isSongLiked(context: Context, songId: String): Boolean {
    val sharedPreferences = context.getSharedPreferences("LikedSongs", Context.MODE_PRIVATE)
    return sharedPreferences.contains(songId)
}

fun getLikedSongIds(context: Context): Set<String> {
    val sharedPreferences = context.getSharedPreferences("LikedSongs", Context.MODE_PRIVATE)
    return sharedPreferences.all.keys.toSet()
}

fun getSongsByIds(songIds: Set<String>, songs: List<SongsModel>): List<SongsModel> {
    return songs.filter { song -> song.id in songIds }
}

fun toggleLikedSong(context: Context, songId: String) {
    if (isSongLiked(context, songId)) removeLikedSongId(context, songId)
    else addLikedSongId(context, songId)
}
