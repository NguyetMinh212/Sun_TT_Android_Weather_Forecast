package com.sun.weatherapp.screen.playingmusic

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.sun.weatherapp.data.model.Song
import com.sun.weatherapp.screen.base.BasePresenter
import com.sun.weatherapp.service.MusicPlayerService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayingMusicPresenter : BasePresenter<PlayingMusicContract.View>(), PlayingMusicContract.Presenter {
    
    private var musicPlayerService: MusicPlayerService? = null
    private var isBound = false
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicPlayerService.MusicPlayerBinder
            musicPlayerService = binder.getService()
            isBound = true
            
            // Set callback to listen for song changes
            musicPlayerService?.setOnSongChangedCallback { song ->
                // Reset progress immediately when song changes
                getView()?.updateProgress("0:00", song.duration, 0)
                updateSongInfo(song)
                updatePlaybackControls()
            }
            
            // Set callback to listen for playback state changes
            musicPlayerService?.setOnPlaybackStateChangedCallback { isPlaying ->
                if (isPlaying) {
                    getView()?.showPlayingState()
                } else {
                    getView()?.showPausedState()
                }
            }
            
            // Load pending song immediately when service is connected
            pendingSong?.let { song ->
                musicPlayerService?.loadSong(song)
                updateSongInfo(song)
                updatePlaybackControls()
                pendingSong = null
            }
            
            // Start progress updates
            startProgressUpdates()
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            musicPlayerService = null
            isBound = false
        }
    }
    
    private var pendingSong: Song? = null
    
    override fun loadSong(song: Song) {
        if (isBound) {
            getView()?.updateProgress("0:00", song.duration, 0)
            musicPlayerService?.loadSong(song)
            updateSongInfo(song)
            updatePlaybackControls()
        } else {
            pendingSong = song
            getView()?.updateProgress("0:00", song.duration, 0)
        }
    }
    
    override fun onPlayPauseClicked() {
        if (isBound) {
            musicPlayerService?.togglePlayPause()
        }
    }
    
    override fun onPreviousClicked() {
        if (isBound) {
            getView()?.updateProgress("0:00", "0:00", 0)
            getView()?.showPlayingState()
            musicPlayerService?.previousSong()
            val currentSong = musicPlayerService?.getCurrentSong()
            currentSong?.let { updateSongInfo(it) }
        }
    }
    
    override fun onNextClicked() {
        if (isBound) {
            getView()?.updateProgress("0:00", "0:00", 0)
            getView()?.showPlayingState()
            musicPlayerService?.nextSong()
            val currentSong = musicPlayerService?.getCurrentSong()
            currentSong?.let { updateSongInfo(it) }
        }
    }
    
    override fun onShuffleClicked() {
        if (isBound) {
            musicPlayerService?.toggleShuffle()
            updatePlaybackControls()
        }
    }
    
    override fun onRepeatClicked() {
        if (isBound) {
            musicPlayerService?.toggleRepeat()
            updatePlaybackControls()
        }
    }
    
    override fun onSeekChanged(progress: Int) {
        if (isBound) {
            musicPlayerService?.seekTo(progress)
            updateProgress()
        }
    }
    
    private fun updateSongInfo(song: Song) {
        getView()?.showSongInfo(song)
    }
    
    private fun updatePlaybackControls() {
        if (isBound) {
            val isPlaying = musicPlayerService?.isPlaying() ?: false
            val isShuffleEnabled = musicPlayerService?.isShuffleEnabled() ?: false
            val isRepeatEnabled = musicPlayerService?.isRepeatEnabled() ?: false
            
            getView()?.updatePlaybackControls(isPlaying, isShuffleEnabled, isRepeatEnabled)
        }
    }
    
    private fun updateProgress() {
        if (isBound) {
            val currentPosition = musicPlayerService?.getCurrentPosition() ?: 0
            val totalDuration = musicPlayerService?.getTotalDuration() ?: 0
            
            if (totalDuration > 0) {
                val currentTimeMs = (currentPosition * totalDuration / 100).toLong()
                val currentTime = formatTime(currentTimeMs / 1000)
                val totalTime = formatTime((totalDuration / 1000).toLong())
                
                getView()?.updateProgress(currentTime, totalTime, currentPosition)
            }
        }
    }
    
    private fun startProgressUpdates() {
        presenterScope.launch {
            while (isBound) {
                updateProgress()
                delay(100)
            }
        }
    }
    
    private fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return "$minutes:${String.format("%02d", remainingSeconds)}"
    }

    fun stopMusicAndService(context: Context) {
        if (isBound) {
            musicPlayerService?.stopMusic()
            // Stop the service completely
            val intent = Intent(context, MusicPlayerService::class.java)
            context.stopService(intent)
            unbindService(context)
        }
    }
    
    fun bindService(context: Context) {
        val intent = Intent(context, MusicPlayerService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    
    fun unbindService(context: Context) {
        if (isBound) {
            context.unbindService(serviceConnection)
            isBound = false
        }
    }
}
