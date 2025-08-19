package com.sun.weatherapp.screen.artistdetail

import com.sun.weatherapp.data.model.Artist
import com.sun.weatherapp.data.model.Song
import com.sun.weatherapp.data.repository.MusicRepository
import com.sun.weatherapp.screen.base.BasePresenter
import kotlinx.coroutines.launch

class ArtistDetailPresenter : BasePresenter<ArtistDetailContract.View>(), ArtistDetailContract.Presenter {
    
    private val musicRepository = MusicRepository()
    private var currentArtist: Artist? = null
    private var artistSongs: List<Song> = emptyList()
    
    override fun loadArtistDetail(artist: Artist) {
        currentArtist = artist
        
        getView()?.showArtistInfo(artist)
        getView()?.showLoading()
        
        presenterScope.launch {
            try {
                musicRepository.getAllSongs().collect { allSongs ->
                    // Filter songs by artist
                    artistSongs = allSongs.filter { song -> song.artist == artist.name }
                    
                    getView()?.hideLoading()
                    
                    if (artistSongs.isNotEmpty()) {
                        getView()?.hideEmptyState()
                        getView()?.showSongs(artistSongs)
                    } else {
                        getView()?.showEmptyState()
                    }
                }
            } catch (e: Exception) {
                getView()?.hideLoading()
                getView()?.showError("Lỗi load dữ liệu: ${e.message}")
                getView()?.showEmptyState()
            }
        }
    }
    
    override fun onSongClicked(song: Song) {
        presenterScope.launch {
            try {
                // Increment play count
                musicRepository.incrementPlayCount(song.id)
            } catch (e: Exception) {
                // Silent fail - không ảnh hưởng UX
            }
        }
        
        getView()?.navigateToPlayingMusic(song, artistSongs)
    }
}
