package com.sun.weatherapp.screen.playingmusic

import com.sun.weatherapp.data.model.Song
import com.sun.weatherapp.screen.base.BasePresenter

class PlayingMusicPresenter : BasePresenter<PlayingMusicContract.View>(), PlayingMusicContract.Presenter {
    
    override fun loadSong(song: Song) {
        // Load the song details and update the view
    }
    
    override fun onPlayPauseClicked() {
        // Toggle play/pause state
    }
    
    override fun onPreviousClicked() {
        // Implement previous song logic
    }
    
    override fun onNextClicked() {
        // Implement next song logic
    }
    
    override fun onShuffleClicked() {
        // Toggle shuffle state
    }
    
    override fun onRepeatClicked() {
        // Toggle repeat state
    }
    
    override fun onSeekChanged(progress: Int) {
        // Update current progress based on user input
    }
}
