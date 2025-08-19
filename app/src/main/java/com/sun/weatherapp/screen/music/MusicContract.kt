package com.sun.weatherapp.screen.music

import com.sun.weatherapp.data.model.Artist
import com.sun.weatherapp.data.model.MusicTabType
import com.sun.weatherapp.data.model.Song
import com.sun.weatherapp.screen.base.BaseContract

interface MusicContract : BaseContract<MusicContract.View, MusicContract.Presenter> {
    
    interface View : BaseContract.View {
        fun showSongs(songs: List<Song>)
        fun showRecommendSongs(songs: List<Song>)
        fun showAllSongs(songs: List<Song>)
        fun showArtists(artists: List<Artist>)
        fun updateSelectedTab(tabType: MusicTabType)
        fun showWeatherInfo(location: String, temperature: String)
        fun navigateToArtistDetail(artist: Artist)
        fun navigateToSongDetail(song: Song)
        fun navigateToPlayingMusic(song: Song, playlist: List<Song> = emptyList())
        fun showSkeletonLoading()
        fun hideSkeletonLoading()
        fun clearAdapterData()

        // Additional utility methods
        fun showMessage(message: String)
    }
    
    interface Presenter : BaseContract.Presenter<View> {
        fun loadMusicData(tabType: MusicTabType)
        fun onTabSelected(tabType: MusicTabType)
        fun onArtistClicked(artist: Artist)
        fun onSongClicked(song: Song)
        fun loadWeatherInfo()
    }
}
