package com.sun.weatherapp.screen.music

import com.sun.weatherapp.data.model.Artist
import com.sun.weatherapp.data.model.MusicTabType
import com.sun.weatherapp.data.model.Song
import com.sun.weatherapp.screen.base.BaseContract

interface MusicContract : BaseContract<MusicContract.View, MusicContract.Presenter> {
    
    interface View : BaseContract.View {
        fun showSongs(songs: List<Song>)
        fun showArtists(artists: List<Artist>)
        fun updateSelectedTab(tabType: MusicTabType)
        fun showWeatherInfo(location: String, temperature: String)
        fun navigateToArtistDetail(artist: Artist)
        fun navigateToSongDetail(song: Song)
    }
    
    interface Presenter : BaseContract.Presenter<View> {
        fun loadMusicData(tabType: MusicTabType)
        fun onTabSelected(tabType: MusicTabType)
        fun onArtistClicked(artist: Artist)
        fun onSongClicked(song: Song)
        fun loadWeatherInfo()
    }
} 