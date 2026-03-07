package com.music.stream.neptune.data.preferences

import android.content.Context
import com.music.stream.neptune.data.entity.AlbumsModel

fun addLikedAlbumId(context: Context, albumId: String) {
    val sharedPreferences = context.getSharedPreferences("LikedAlbums", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString(albumId, albumId).apply()
}

fun removeLikedAlbumId(context: Context, albumId: String) {
    val sharedPreferences = context.getSharedPreferences("LikedAlbums", Context.MODE_PRIVATE)
    sharedPreferences.edit().remove(albumId).apply()
}

fun isAlbumLiked(context: Context, albumId: String): Boolean {
    val sharedPreferences = context.getSharedPreferences("LikedAlbums", Context.MODE_PRIVATE)
    return sharedPreferences.contains(albumId)
}

fun getLikedAlbumIds(context: Context): Set<String> {
    val sharedPreferences = context.getSharedPreferences("LikedAlbums", Context.MODE_PRIVATE)
    return sharedPreferences.all.keys.toSet()
}

fun getAlbumsByIds(albumIds: Set<String>, albums: List<AlbumsModel>): List<AlbumsModel> {
    return albums.filter { album -> album.id.toString() in albumIds }
}
