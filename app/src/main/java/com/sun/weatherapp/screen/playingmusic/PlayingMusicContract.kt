package com.sun.weatherapp.screen.playingmusic

import com.sun.weatherapp.data.model.Song
import com.sun.weatherapp.screen.base.BaseContract

interface PlayingMusicContract : BaseContract<PlayingMusicContract.View, PlayingMusicContract.Presenter> {
    
    interface View : BaseContract.View {
        fun showSongInfo(song: Song)
        fun updateProgress(currentTime: String, duration: String, progress: Int)
        fun showPlayingState()
        fun showPausedState()
        fun updatePlaybackControls(isPlaying: Boolean, isShuffleEnabled: Boolean, isRepeatEnabled: Boolean)
    }
    
    interface Presenter : BaseContract.Presenter<View> {
        fun loadSong(song: Song)
        fun onPlayPauseClicked()
        fun onPreviousClicked()
        fun onNextClicked()
        fun onShuffleClicked()
        fun onRepeatClicked()
        fun onSeekChanged(progress: Int)
    }
}
