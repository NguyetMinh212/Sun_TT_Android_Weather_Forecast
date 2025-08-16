package com.sun.weatherapp.screen.music

import android.location.Location
import com.sun.weatherapp.data.model.Artist
import com.sun.weatherapp.data.model.MusicTabType
import com.sun.weatherapp.data.model.Song
import com.sun.weatherapp.data.model.WeatherResponse
import com.sun.weatherapp.data.reposiroty.LocationRepository
import com.sun.weatherapp.data.reposiroty.WeatherRepository
import com.sun.weatherapp.data.reposiroty.source.remote.OnResultListener
import com.sun.weatherapp.screen.base.BasePresenter
import com.sun.weatherapp.utils.toCelsius
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MusicPresenter(
    private val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository
) : BasePresenter<MusicContract.View>(), MusicContract.Presenter {

    private var currentTab = MusicTabType.RECOMMEND
    private var isInitialLoad = true
    
    override fun loadMusicData(tabType: MusicTabType) {
        currentTab = tabType
        
        presenterScope.launch {
            try {
                // Clear old data immediately
                getView()?.clearAdapterData()
                
                if (isInitialLoad) {
                    getView()?.showSkeletonLoading()
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

                if (isInitialLoad) {
                    getView()?.hideSkeletonLoading()
                }
                // Hide loading for all cases (initial load and tab switching)
                getView()?.hideLoading()
                isInitialLoad = false
                
            } catch (e: Exception) {
                if (isInitialLoad) {
                    getView()?.hideSkeletonLoading()
                }
                // Hide loading even on error
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
        getView()?.navigateToArtistDetail(artist)
    }
    
    override fun onSongClicked(song: Song) {
        getView()?.navigateToSongDetail(song)
    }
    
    override fun loadWeatherInfo() {
        presenterScope.launch {
            try {
                fetchWeatherWithCurrentLocation()
            } catch (e: Exception) {
                getView()?.showError("Không thể tải thông tin thời tiết")
            }
        }
    }

    private suspend fun fetchWeatherWithCurrentLocation() {
        delay(500)
        
        locationRepository.getCurrentLocation(object : OnResultListener<Location> {
            override fun onSuccess(location: Location) {
                fetchWeatherDataWithLocation(location.latitude, location.longitude)
            }

            override fun onError(exception: Exception?) {
                getView()?.showError(exception?.message ?: "Failed to get current location")
            }
        })
    }
    
    private fun fetchWeatherDataWithLocation(latitude: Double, longitude: Double) {
        weatherRepository.getCurrentWeather(latitude, longitude, object : OnResultListener<WeatherResponse> {
            override fun onSuccess(data: WeatherResponse) {
                val location = data.name
                val temperature = "${data.main.temp.toCelsius()}°C"
                getView()?.showWeatherInfo(location, temperature)
            }

            override fun onError(exception: Exception?) {
                getView()?.showError(exception?.message ?: "Failed to load weather data")
            }
        })
    }
    
    // Mock data methods
    private fun getRecommendSongs(): List<Song> {
        return listOf(
            Song("1", "Bad Guy", "Billie Eilish", "https://picsum.photos/220", "3:14"),
            Song("2", "Blinding Lights", "The Weeknd", "https://picsum.photos/221", "3:20"),
            Song("3", "Dance Monkey", "Tones and I", "https://picsum.photos/222", "3:29"),
            Song("r4", "Hãy trao cho anh", "Sơn Tùng M-TP", "https://picsum.photos/223", "4:01"),
            Song("r5", "Muộn rồi mà sao còn", "Sơn Tùng M-TP", "https://picsum.photos/224", "3:55"),
            Song("r6", "Nơi này có anh", "Sơn Tùng M-TP", "https://picsum.photos/225", "4:22")
        )
    }
    
    private fun getAllSongs(): List<Song> {
        return listOf(
            Song("1", "Bad Guy", "Billie Eilish", "https://picsum.photos/230", "3:14"),
            Song("2", "Blinding Lights", "The Weeknd", "https://picsum.photos/231", "3:20"),
            Song("3", "Dance Monkey", "Tones and I", "https://picsum.photos/232", "3:29"),
            Song("a2", "Bông hoa đẹp nhất", "Quân A.P", "https://picsum.photos/234", "4:12"),
            Song("a3", "Có chàng trai viết lên cây", "Phan Mạnh Quỳnh", "https://picsum.photos/235", "4:05"),
            Song("a4", "Để Mị nói cho mà nghe", "Hoàng Thùy Linh", "https://picsum.photos/236", "3:45"),
            Song("a5", "Em gái mưa", "Hương Tràm", "https://picsum.photos/237", "4:18")
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
