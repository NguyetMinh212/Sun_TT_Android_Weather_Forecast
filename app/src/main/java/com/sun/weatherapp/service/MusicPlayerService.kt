package com.sun.weatherapp.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.sun.weatherapp.data.model.Song
import kotlinx.coroutines.*

class MusicPlayerService : Service() {
    
    private val binder = MusicPlayerBinder()
    private var mediaPlayer: MediaPlayer? = null
    // Remove mock playlist - will get songs from Firebase
    private var currentPlaylist: List<Song> = emptyList()
    private var currentSong: Song? = null
    private var isPlaying = false
    private var isShuffleEnabled = false
    private var isRepeatEnabled = false
    private var currentPosition = 0
    private var totalDuration = 0
    private var currentPlaylistIndex = 0
    private var onServiceReadyCallback: (() -> Unit)? = null
    private var onSongChangedCallback: ((Song) -> Unit)? = null
    private var onPlaybackStateChangedCallback: ((Boolean) -> Unit)? = null
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    inner class MusicPlayerBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }
    
    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    
    override fun onCreate() {
        super.onCreate()
        initializeMediaPlayer()
    }
    
    private fun initializeMediaPlayer() {
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setOnCompletionListener {
            onSongCompleted()
        }
        mediaPlayer?.setOnPreparedListener { mp ->
            totalDuration = mp.duration
            // Auto-play when song is loaded
            mp.start()
            isPlaying = true
            notifyPlaybackStateChanged(true)
            startProgressUpdate()
        }
        mediaPlayer?.setOnErrorListener { _, what, extra ->
            Log.e("MusicPlayerService", "MediaPlayer error: what=$what, extra=$extra")
            false
        }
    }
    
    fun setPlaylist(songs: List<Song>) {
        currentPlaylist = songs
    }
    
    fun loadSong(song: Song) {
        currentSong = song
        val index = currentPlaylist.indexOfFirst { it.id == song.id }
        currentPlaylistIndex = if (index != -1) index else 0
        
        Log.d("MusicPlayerService", "Attempting to load song: ${song.title} by ${song.artist}")
        Log.d("MusicPlayerService", "Song index in playlist: $currentPlaylistIndex")
        isPlaying = false
        currentPosition = 0
        totalDuration = 0
        
        try {
            mediaPlayer?.reset()
            // Use real audio URL from Firebase (Google Drive URLs)
            val audioUrl = if (song.audioUrl.isNotEmpty()) {
                Log.d("MusicPlayerService", "Using Firebase audioUrl: ${song.audioUrl}")
                song.audioUrl
            } else {
                Log.w("MusicPlayerService", "Empty audioUrl, using fallback for song: ${song.title}")
                // Fallback to sample for development only
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
            }
            
            mediaPlayer?.setDataSource(audioUrl)
            mediaPlayer?.prepareAsync()
            Log.d("MusicPlayerService", "Preparing audio: $audioUrl")
            
            // Notify that song changed (this will trigger UI reset immediately)
            notifySongChanged(song)
        } catch (e: Exception) {
            Log.e("MusicPlayerService", "Error loading song: ${e.message}")
        }
    }
    
    fun play() {
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
            isPlaying = true
            startProgressUpdate()
            notifyPlaybackStateChanged(true)
        }
    }
    
    fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            isPlaying = false
            notifyPlaybackStateChanged(false)
        }
    }
    
    fun togglePlayPause() {
        if (isPlaying) {
            pause()
        } else {
            play()
        }
    }
    
    fun seekTo(position: Int) {
        val seekPosition = (position * totalDuration / 100).toLong()
        mediaPlayer?.seekTo(seekPosition.toInt())
        currentPosition = position
    }
    
    fun getCurrentPosition(): Int = currentPosition
    
    fun getTotalDuration(): Int = totalDuration
    
    // Precise current position in milliseconds (avoids 1-3s jumps when deriving from percent)
    fun getCurrentPositionMs(): Int = mediaPlayer?.currentPosition ?: 0
    
    fun getCurrentPlaylist(): List<Song> = currentPlaylist
    
    fun getCurrentSong(): Song? = currentSong
    
    fun isPlaying(): Boolean = isPlaying
    
    fun toggleShuffle() {
        isShuffleEnabled = !isShuffleEnabled
    }
    
    fun isShuffleEnabled(): Boolean = isShuffleEnabled
    
    fun toggleRepeat() {
        isRepeatEnabled = !isRepeatEnabled
    }
    
    fun isRepeatEnabled(): Boolean = isRepeatEnabled
    
    fun nextSong() {
        if (currentPlaylist.isEmpty()) {
            Log.w("MusicPlayerService", "Cannot advance - playlist is empty")
            return
        }
        
        // Stop current song and reset progress immediately
        isPlaying = false
        mediaPlayer?.pause()
        currentPosition = 0

        if (isShuffleEnabled) {
            currentPlaylistIndex = (0 until currentPlaylist.size).random()
        } else {
            currentPlaylistIndex = (currentPlaylistIndex + 1) % currentPlaylist.size
        }

        val nextSong = currentPlaylist[currentPlaylistIndex]
        
        loadSong(nextSong)
    }
    
    fun previousSong() {
        if (currentPlaylist.isEmpty()) {
            return
        }
        
        isPlaying = false
        mediaPlayer?.pause()
        currentPosition = 0

        if (isShuffleEnabled) {
            currentPlaylistIndex = (0 until currentPlaylist.size).random()
        } else {
            currentPlaylistIndex = if (currentPlaylistIndex > 0) currentPlaylistIndex - 1 else currentPlaylist.size - 1
        }

        val prevSong = currentPlaylist[currentPlaylistIndex]
        loadSong(prevSong)
    }
    
    private fun onSongCompleted() {
        Log.d("MusicPlayerService", "Song completed - isRepeatEnabled: $isRepeatEnabled")

        isPlaying = false
        currentPosition = 0
        
        if (isRepeatEnabled && currentSong != null) {
            // Repeat current song
            Log.d("MusicPlayerService", "Repeating current song: ${currentSong?.title}")
            loadSong(currentSong!!)
        } else if (currentPlaylist.isNotEmpty()) {
            // Auto-advance to next song
            currentPlaylistIndex = (currentPlaylistIndex + 1) % currentPlaylist.size
            val nextSong = currentPlaylist[currentPlaylistIndex]
            loadSong(nextSong)
        } else {
            Log.w("MusicPlayerService", "Playlist is empty - cannot auto-advance")
        }
    }
    
    private fun startProgressUpdate() {
        serviceScope.launch {
            while (isPlaying) {
                delay(100)
                if (mediaPlayer?.isPlaying == true && totalDuration > 0) {
                    val currentPos = mediaPlayer?.currentPosition ?: 0
                    currentPosition = (currentPos * 100 / totalDuration).coerceAtMost(100)
                } else if (!isPlaying) {
                    break
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
        serviceScope.cancel()
    }
    
    fun stopMusic() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
        }
        isPlaying = false
    }

    fun setOnSongChangedCallback(callback: (Song) -> Unit) {
        onSongChangedCallback = callback
    }
    
    fun setOnPlaybackStateChangedCallback(callback: (Boolean) -> Unit) {
        onPlaybackStateChangedCallback = callback
    }
    
    private fun notifySongChanged(song: Song) {
        onSongChangedCallback?.invoke(song)
    }
    
    private fun notifyPlaybackStateChanged(isPlaying: Boolean) {
        onPlaybackStateChangedCallback?.invoke(isPlaying)
    }
}
