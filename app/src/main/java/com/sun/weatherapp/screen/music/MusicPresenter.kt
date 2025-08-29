package com.sun.weatherapp.screen.music

import android.location.Location
import com.sun.weatherapp.data.model.Artist
import com.sun.weatherapp.data.model.MusicTabType
import com.sun.weatherapp.data.model.Song
import com.sun.weatherapp.data.model.WeatherResponse
import com.sun.weatherapp.data.reposiroty.LocationRepository
import com.sun.weatherapp.data.reposiroty.WeatherRepository
import com.sun.weatherapp.data.repository.MusicRepository
import com.sun.weatherapp.data.reposiroty.source.remote.OnResultListener
import com.sun.weatherapp.screen.base.BasePresenter
import com.sun.weatherapp.utils.toCelsius
import com.sun.weatherapp.utils.WeatherMusicMapper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MusicPresenter(
    private val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository,
    private val musicRepository: MusicRepository = MusicRepository()
) : BasePresenter<MusicContract.View>(), MusicContract.Presenter {
    
    private var currentTab: MusicTabType = MusicTabType.RECOMMEND
    private var currentWeatherMood: String? = null
    private var allSongsCache: List<Song> = emptyList()
    
    private var currentDisplayedSongs: List<Song> = emptyList()
    
    override fun loadMusicData(tabType: MusicTabType) {
        currentTab = tabType

        when (tabType) {
            MusicTabType.RECOMMEND -> {
                // Update header only
                loadWeatherInfo()
                // Load recommendations by mood
                getView()?.showSkeletonLoading()
                if (currentWeatherMood != null) {
                    loadRecommendSongsFromFirebase(currentWeatherMood!!)
                } else {
                    getCurrentLocationAndWeather { mood ->
                        if (mood != null) {
                            currentWeatherMood = mood
                            loadRecommendSongsFromFirebase(mood)
                        } else {
                            getView()?.hideLoading()
                            getView()?.showMessage("Không lấy được thời tiết.")
                            currentDisplayedSongs = emptyList()
                            getView()?.showRecommendSongs(emptyList())
                        }
                    }
                }
            }
            MusicTabType.ALL_SONGS -> {
                loadAllSongsFromFirebase()
            }
            MusicTabType.ARTIST -> {
                loadArtistsFromFirebase()
            }
        }
    }
    
    private fun loadAllSongsFromFirebase() {
        presenterScope.launch {
            getView()?.showSkeletonLoading()
            android.util.Log.d("MusicPresenter", "Skeleton loading shown")
            delay(500)
            
            try {

                val timeoutJob = launch {
                    delay(3000)
                    getView()?.hideLoading()
                    getView()?.showAllSongs(getAllSongs())
                }
                
                musicRepository.getAllSongs().collect { songs ->
                    timeoutJob.cancel()
                    allSongsCache = songs
                    getView()?.hideLoading()
                    
                    if (songs.isNotEmpty()) {
                        currentDisplayedSongs = songs // Track displayed songs
                        getView()?.showAllSongs(songs)
                    } else {
                        getView()?.showMessage("Không tìm thấy bài hát trong Firebase.")
                        currentDisplayedSongs = emptyList() // Track empty songs
                        getView()?.showAllSongs(emptyList())
                    }
                }
            } catch (e: Exception) {
                getView()?.hideLoading()
                getView()?.showMessage("Lỗi Firebase: ${e.message}")
                currentDisplayedSongs = emptyList() // Track empty songs
                getView()?.showAllSongs(emptyList())
            }
        }
    }
    
    private fun loadRecommendSongsFromFirebase(weatherMood: String) {
        presenterScope.launch {
            try {
                val timeoutJob = launch {
                    delay(3000)
                    getView()?.hideLoading()
                    currentDisplayedSongs = emptyList() // Track empty songs
                    getView()?.showRecommendSongs(emptyList())
                }
                
                musicRepository.getRecommendedSongs(weatherMood).collect { songs ->
                    timeoutJob.cancel() // Cancel timeout if we get data
                    getView()?.hideLoading()
                    if (songs.isNotEmpty()) {
                        currentDisplayedSongs = songs // Track displayed songs
                        getView()?.showRecommendSongs(songs)
                    } else {
                        currentDisplayedSongs = emptyList() // Track empty songs
                        getView()?.showRecommendSongs(emptyList())
                    }
                }
            } catch (e: Exception) {
                getView()?.hideLoading()
                currentDisplayedSongs = emptyList() // Track empty songs
                getView()?.showRecommendSongs(emptyList())
            }
        }
    }
    
    private fun loadArtistsFromFirebase() {
        presenterScope.launch {
            getView()?.showSkeletonLoading()
            delay(500)
            
            try {
                musicRepository.getAllSongs().collect { songs ->
                    // Group songs by artist để tạo artists list
                    val artists = songs.groupBy { it.artist }
                        .map { (artistName, artistSongs) ->
                            Artist(
                                id = artistName.replace(" ", "_").lowercase(),
                                name = artistName,
                                description = "Ca sĩ Việt Nam",
                                imageUrl = artistSongs.firstOrNull()?.imageUrl ?: "",
                                songCount = artistSongs.size
                            )
                        }
                        .sortedByDescending { it.songCount }
                    
                    getView()?.hideLoading()
                    getView()?.showArtists(artists)
                }
            } catch (e: Exception) {
                getView()?.hideLoading()
                getView()?.showMessage("Lỗi Firebase artists: ${e.message}. Dùng dữ liệu mẫu.")
                getView()?.showArtists(getMockArtists())
            }
        }
    }
    
    override fun onTabSelected(tabType: MusicTabType) {
        if (currentTab != tabType) {
            getView()?.updateSelectedTab(tabType)
            loadMusicData(tabType)
        }
    }
    
    override fun onArtistClicked(artist: Artist) {
        getView()?.navigateToArtistDetail(artist)
    }
    
    override fun onSongClicked(song: Song) {
        presenterScope.launch {
            try {
                musicRepository.incrementPlayCount(song.id)
            } catch (e: Exception) {
                // Silent fail - không ảnh hưởng UX
            }
        }

        // Get current playlist based on current tab
        val currentPlaylist = getCurrentDisplayedPlaylist()
        android.util.Log.d("MusicPresenter", "onSongClick: ${song.title}, playlist size: ${currentPlaylist.size}")

        // Navigate to playing music với current playlist
        getView()?.navigateToPlayingMusic(song, currentPlaylist)
    }
    
    private fun getCurrentDisplayedPlaylist(): List<Song> {
        if (currentDisplayedSongs.isNotEmpty()) return currentDisplayedSongs
        
        return when (currentTab) {
            MusicTabType.RECOMMEND -> {
                val source = allSongsCache
                if (source.isEmpty()) emptyList() else {
                    currentWeatherMood?.let { mood ->
                        val filtered = source.filter { it.weatherMoods.contains(mood) }
                        if (filtered.isNotEmpty()) filtered else source
                    } ?: source
                }
            }
            MusicTabType.ALL_SONGS,
            MusicTabType.ARTIST -> {
                if (allSongsCache.isNotEmpty()) allSongsCache else emptyList()
            }
        }
    }
    
    override fun loadWeatherInfo() {
        presenterScope.launch {
            try {
                getCurrentLocationAndWeather { /* mood not needed here */ }
            } catch (e: Exception) {
                getView()?.showError("Không thể tải thông tin thời tiết")
            }
        }
    }
    
    private fun getAllSongs(): List<Song> {
        return emptyList()
    }
    
    private fun getMockArtists(): List<Artist> {
        return emptyList()
    }
    

    private fun getCurrentLocationAndWeather(callback: (String?) -> Unit) {
        locationRepository.getCurrentLocation(object : OnResultListener<Location> {
            override fun onSuccess(location: Location) {
                android.util.Log.d("MusicPresenter", "Location found: ${location.latitude}, ${location.longitude}")
                
                weatherRepository.getCurrentWeather(location.latitude, location.longitude, object : OnResultListener<WeatherResponse> {
                    override fun onSuccess(weatherResponse: WeatherResponse) {
                        val condition = weatherResponse.weather.firstOrNull()?.main ?: ""
                        val temperature = weatherResponse.main.temp.toCelsius().toDouble()
                        val weatherMood = WeatherMusicMapper.mapWeatherToMood(condition, temperature)
                        
                        android.util.Log.d("MusicPresenter", "Weather: $condition, ${temperature}°C → mood: $weatherMood")
                        
                        // Update weather info on UI
                        getView()?.showWeatherInfo(
                            weatherResponse.name,
                            "${temperature.toInt()}°C"
                        )
                        
                        callback(weatherMood)
                        }

                    override fun onError(exception: Exception?) {
                        android.util.Log.e("MusicPresenter", "Weather error: ${exception?.message}")
                        callback(null)
                    }
                })
            }

            override fun onError(exception: Exception?) {
                android.util.Log.e("MusicPresenter", "Location error: ${exception?.message}")
                callback(null)
            }
        })
    }
}
