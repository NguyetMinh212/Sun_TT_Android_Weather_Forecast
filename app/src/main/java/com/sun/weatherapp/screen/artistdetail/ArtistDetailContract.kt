package com.sun.weatherapp.screen.artistdetail

import com.sun.weatherapp.data.model.Artist
import com.sun.weatherapp.data.model.Song
import com.sun.weatherapp.screen.base.BaseContract

interface ArtistDetailContract : BaseContract<ArtistDetailContract.View, ArtistDetailContract.Presenter> {
    
    interface View : BaseContract.View {
        fun showArtistInfo(artist: Artist)
        fun showSongs(songs: List<Song>)
        override fun showLoading()
        override fun hideLoading()
        fun showEmptyState()
        fun hideEmptyState()
        override fun showError(message: String)
        fun navigateToPlayingMusic(song: Song, playlist: List<Song>)
    }
    
    interface Presenter : BaseContract.Presenter<View> {
        fun loadArtistDetail(artist: Artist)
        fun onSongClicked(song: Song)
    }
}
