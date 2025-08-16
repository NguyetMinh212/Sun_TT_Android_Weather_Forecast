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
    
    private val mockPlaylist = listOf(
        Song("1", "Bad Guy", "Billie Eilish", "https://example.com/badguy.jpg", "0:03"),
        Song("2", "Blinding Lights", "The Weeknd", "https://example.com/blindinglights.jpg", "0:06"),
        Song("3", "Dance Monkey", "Tones and I", "https://example.com/dancemonkey.jpg", "0:09")
    )
    
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
            startProgressUpdate()
            
            // Notify UI that playback state changed to playing
            notifyPlaybackStateChanged(true)
            
            Log.d("MusicPlayerService", "Song prepared and started playing: duration=${mp.duration}ms")
        }
        mediaPlayer?.setOnErrorListener { _, what, extra ->
            Log.e("MusicPlayerService", "MediaPlayer error: what=$what, extra=$extra")
            false
        }
    }
    
    fun loadSong(song: Song) {
        currentSong = song
        val index = mockPlaylist.indexOfFirst { it.id == song.id }
        currentPlaylistIndex = if (index != -1) index else 0
        
        Log.d("MusicPlayerService", "Loading song: ${song.title}")
        
        // Reset progress and stop current playback immediately
        isPlaying = false
        currentPosition = 0
        totalDuration = 0
        
        try {
            mediaPlayer?.reset()

            val audioUrl = when (song.id) {
                "1" -> "https://samplelib.com/lib/preview/mp3/sample-3s.mp3"
                "2" -> "https://samplelib.com/lib/preview/mp3/sample-6s.mp3"
                "3" -> "https://samplelib.com/lib/preview/mp3/sample-9s.mp3"
                else -> "https://samplelib.com/lib/preview/mp3/sample-3s.mp3"
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
    
    fun isPlaying(): Boolean = isPlaying
    
    fun getCurrentSong(): Song? = currentSong
    
    fun toggleShuffle() {
        isShuffleEnabled = !isShuffleEnabled
    }
    
    fun isShuffleEnabled(): Boolean = isShuffleEnabled
    
    fun toggleRepeat() {
        isRepeatEnabled = !isRepeatEnabled
    }
    
    fun isRepeatEnabled(): Boolean = isRepeatEnabled
    
    fun nextSong() {
        Log.d("MusicPlayerService", "Next song - current index: $currentPlaylistIndex")
        
        // Stop current song and reset progress immediately
        isPlaying = false
        mediaPlayer?.pause()
        currentPosition = 0
        
        if (isShuffleEnabled) {
            currentPlaylistIndex = (0 until mockPlaylist.size).random()
        } else {
            currentPlaylistIndex = (currentPlaylistIndex + 1) % mockPlaylist.size
        }
        
        Log.d("MusicPlayerService", "Moving to song index: $currentPlaylistIndex, title: ${mockPlaylist[currentPlaylistIndex].title}")
        loadSong(mockPlaylist[currentPlaylistIndex])
    }
    
    fun previousSong() {
        Log.d("MusicPlayerService", "Previous song - current index: $currentPlaylistIndex")
        
        isPlaying = false
        mediaPlayer?.pause()
        currentPosition = 0
        
        if (isShuffleEnabled) {
            currentPlaylistIndex = (0 until mockPlaylist.size).random()
        } else {
            currentPlaylistIndex = if (currentPlaylistIndex > 0) currentPlaylistIndex - 1 else mockPlaylist.size - 1
        }
        
        Log.d("MusicPlayerService", "Moving to song index: $currentPlaylistIndex, title: ${mockPlaylist[currentPlaylistIndex].title}")
        loadSong(mockPlaylist[currentPlaylistIndex])
    }
    
    private fun onSongCompleted() {
        Log.d("MusicPlayerService", "Song completed - isRepeatEnabled: $isRepeatEnabled")
        
        isPlaying = false
        currentPosition = 0
        
        if (isRepeatEnabled) {
            // Repeat current song
            Log.d("MusicPlayerService", "Repeating current song: ${currentSong?.title}")
            loadSong(currentSong ?: mockPlaylist[0])
        } else {
            // Auto-advance to next song
            Log.d("MusicPlayerService", "Auto-advancing to next song")
            nextSong()
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
