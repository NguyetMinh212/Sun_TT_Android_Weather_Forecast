package com.sun.weatherapp.screen.music

import com.sun.weatherapp.data.model.Artist
import com.sun.weatherapp.data.model.MusicTabType
import com.sun.weatherapp.data.model.Song
import com.sun.weatherapp.screen.base.BasePresenter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MusicPresenter : BasePresenter<MusicContract.View>(), MusicContract.Presenter {
    
    private var currentTab = MusicTabType.RECOMMEND
    private var isInitialLoad = true
    
    override fun loadMusicData(tabType: MusicTabType) {
        currentTab = tabType
        
        presenterScope.launch {
            try {
                if (isInitialLoad) {
                    getView()?.showLoading()
                }
                
                getView()?.updateSelectedTab(tabType)
                delay(500)
                when (tabType) {
                    MusicTabType.RECOMMEND -> {
                        getView()?.showSongs(getRecommendSongs())
                    }
                    MusicTabType.ARTIST -> {
                        getView()?.showArtists(getMockArtists())
                    }
                    MusicTabType.ALL_SONGS -> {
                        getView()?.showSongs(getAllSongs())
                    }
                }
                
                getView()?.hideLoading()
                isInitialLoad = false
                
            } catch (e: Exception) {
                getView()?.hideLoading()
                getView()?.showError("Không thể tải dữ liệu nhạc: ${e.message}")
                isInitialLoad = false
            }
        }
    }
    
    override fun onTabSelected(tabType: MusicTabType) {
        if (currentTab != tabType) {
            loadMusicData(tabType)
        }
    }
    
    override fun onArtistClicked(artist: Artist) {
        // TODO: Navigate to artist detail
    }
    
    override fun onSongClicked(song: Song) {
        // TODO: Play song
    }
    
    override fun loadWeatherInfo() {
        presenterScope.launch {
            try {
                getView()?.showWeatherInfo("Ha Noi, Viet Nam", "3°C")
            } catch (e: Exception) {
                getView()?.showError("Không thể tải thông tin thời tiết")
            }
        }
    }
    
    // Mock data methods
    private fun getRecommendSongs(): List<Song> {
        return listOf(
            Song("r1", "Em của ngày hôm qua", "Sơn Tùng M-TP", "https://picsum.photos/220", "3:45"),
            Song("r2", "Lạc trôi", "Sơn Tùng M-TP", "https://picsum.photos/221", "4:12"),
            Song("r3", "Chạy ngay đi", "Sơn Tùng M-TP", "https://picsum.photos/222", "3:28"),
            Song("r4", "Hãy trao cho anh", "Sơn Tùng M-TP", "https://picsum.photos/223", "4:01"),
            Song("r5", "Muộn rồi mà sao còn", "Sơn Tùng M-TP", "https://picsum.photos/224", "3:55"),
            Song("r6", "Nơi này có anh", "Sơn Tùng M-TP", "https://picsum.photos/225", "4:22")
        )
    }
    
    private fun getAllSongs(): List<Song> {
        return listOf(
            Song("a1", "Ánh sao và bầu trời", "T.R.I", "https://picsum.photos/230", "3:45"),
            Song("a2", "Bông hoa đẹp nhất", "Quân A.P", "https://picsum.photos/231", "4:12"),
            Song("a3", "Cô đơn dành cho ai", "Lee Ken, Nal", "https://picsum.photos/232", "3:28"),
            Song("a4", "Dễ đến dễ đi", "Quang Hùng MasterD", "https://picsum.photos/233", "4:01"),
            Song("a5", "Em không sai chúng ta sai", "Erik", "https://picsum.photos/234", "3:55"),
            Song("a6", "Faded", "Alan Walker", "https://picsum.photos/235", "4:22"),
            Song("a7", "Gentlemen", "Psy", "https://picsum.photos/236", "3:30"),
            Song("a8", "Hôm nay em cưới rồi", "Khổng Tú Quỳnh", "https://picsum.photos/237", "4:15")
        )
    }
    
    private fun getMockArtists(): List<Artist> {
        return listOf(
            Artist("1", "Sơn Tùng M-TP", "Ca sĩ, nhạc sĩ nổi tiếng Việt Nam", "https://picsum.photos/210", 35),
            Artist("2", "Hòa Minzy", "Ca sĩ, diễn viên đa tài", "https://picsum.photos/211", 28),
            Artist("3", "Erik", "Ca sĩ trẻ tài năng", "https://picsum.photos/212", 42),
            Artist("4", "Quang Hùng MasterD", "Ca sĩ, vũ công chuyên nghiệp", "https://picsum.photos/213", 19),
            Artist("5", "T.R.I", "Rapper, producer", "https://picsum.photos/214", 33),
            Artist("6", "Quân A.P", "Ca sĩ indie nổi tiếng", "https://picsum.photos/215", 27)
        )
    }
} 