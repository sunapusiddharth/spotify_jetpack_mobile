package com.music.stream.neptune.di

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.media3.common.PlaybackException
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.datasource.HttpDataSource
import com.music.stream.neptune.BuildConfig
import com.music.stream.neptune.data.entity.SongsModel
import com.music.stream.neptune.playback.PlaybackService

object SongPlayer {
    private var appContext: Context? = null
    private var player: ExoPlayer? = null
    private var playerListener: Player.Listener? = null

    fun getOrCreatePlayer(context: Context): ExoPlayer {
        val applicationContext = context.applicationContext
        appContext = applicationContext
        return player ?: ExoPlayer.Builder(applicationContext).build().also { exoPlayer ->
            val listener = object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    val message = when (val cause = error.cause) {
                        is HttpDataSource.InvalidResponseCodeException -> {
                            if (cause.responseCode == 404) {
                                "This song source is unavailable right now"
                            } else {
                                "Playback failed with HTTP ${cause.responseCode}"
                            }
                        }
                        else -> "Unable to play this media"
                    }
                    Log.e("SongPlayer", "Playback error: ${error.message}", error)
                    Handler(Looper.getMainLooper()).post {
                        appContext?.let {
                            Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                    exoPlayer.pause()
                }
            }
            exoPlayer.addListener(listener)
            playerListener = listener
            player = exoPlayer
        }
    }

    private fun startPlaybackService(context: Context) {
        val applicationContext = context.applicationContext
        appContext = applicationContext
        ContextCompat.startForegroundService(
            applicationContext,
            Intent(applicationContext, PlaybackService::class.java)
        )
    }

    fun buildSongStreamUrl(s3link: String): String {
        val normalizedPath = s3link.trim().trimStart('/')
        return "${AppModule.HLS_BASE_URL}/hls/spotify_transcoded/$normalizedPath"
    }

    fun playSong(
        song: SongsModel,
        context: Context,
        title: String? = song.title,
        artist: String? = song.singer,
        artworkUri: String? = song.coverUri
    ) {
        if (!song.hasPlayableAudio) return
        val streamUrl = buildSongStreamUrl(song.s3link)
        if (BuildConfig.ENABLE_HTTP_LOGGING) {
            Log.d("SongPlayer", "Playing song id=${song.id} title=${song.title} streamUrl=$streamUrl cover=${song.coverUri}")
        }
        playSong(streamUrl, context, title, artist, artworkUri)
    }

    fun playSong(
        song: String,
        context: Context,
        title: String? = null,
        artist: String? = null,
        artworkUri: String? = null
    ) {
        if (BuildConfig.ENABLE_HTTP_LOGGING) {
            Log.d("SongPlayer", "Setting media item url=$song title=${title.orEmpty()} artist=${artist.orEmpty()} artwork=${artworkUri.orEmpty()}")
        }
        val exoPlayer = getOrCreatePlayer(context)
        startPlaybackService(context)

        val mediaMetadataBuilder = MediaMetadata.Builder()
        if (!title.isNullOrBlank()) mediaMetadataBuilder.setTitle(title)
        if (!artist.isNullOrBlank()) mediaMetadataBuilder.setArtist(artist)
        if (!artworkUri.isNullOrBlank()) mediaMetadataBuilder.setArtworkUri(Uri.parse(artworkUri))

        val mediaItemBuilder = MediaItem.Builder().setUri(song)
        if (!title.isNullOrBlank() || !artist.isNullOrBlank() || !artworkUri.isNullOrBlank()) {
            mediaItemBuilder.setMediaMetadata(mediaMetadataBuilder.build())
        }

        exoPlayer.setMediaItem(mediaItemBuilder.build())
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    fun isPlaying(): Boolean {
        return player?.isPlaying ?: false
    }

    fun play() {
        player?.play()
    }

    fun pause() {
        player?.pause()
    }

    fun stop() {
        player?.stop()
    }

    fun seekTo(position: Long) {
        player?.seekTo(position)
    }

    fun release() {
        playerListener?.let { listener ->
            player?.removeListener(listener)
        }
        playerListener = null
        player?.release()
        player = null
        appContext = null
    }

    fun getDuration(): Long {
        return player?.duration ?: 0L
    }

    fun getCurrentPosition(): Long {
        return player?.currentPosition ?: 0L
    }

    fun isPrepared(): Boolean {
        val playerState = player?.playbackState
        return playerState != null && playerState != ExoPlayer.STATE_IDLE && playerState != ExoPlayer.STATE_ENDED
    }

}
