package com.sun.weatherapp.music

import android.location.Location
import com.sun.weatherapp.data.model.MusicTabType
import com.sun.weatherapp.data.model.WeatherResponse
import com.sun.weatherapp.data.reposiroty.LocationRepository
import com.sun.weatherapp.data.reposiroty.WeatherRepository
import com.sun.weatherapp.data.repository.MusicRepository
import com.sun.weatherapp.data.reposiroty.source.remote.OnResultListener
import com.sun.weatherapp.screen.music.MusicContract
import com.sun.weatherapp.screen.music.MusicPresenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.atLeastOnce
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import com.sun.weatherapp.data.model.Clouds
import com.sun.weatherapp.data.model.Coord
import com.sun.weatherapp.data.model.Main
import com.sun.weatherapp.data.model.Sys
import com.sun.weatherapp.data.model.Weather
import com.sun.weatherapp.data.model.Wind
import com.sun.weatherapp.testutil.MusicTestDummies

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class MusicPresenterTest {
    private lateinit var presenter: MusicPresenter
    private val mockLocationRepo: LocationRepository = mock()
    private val mockWeatherRepo: WeatherRepository = mock()
    private val mockMusicRepo: MusicRepository = mock()
    private val mockView: MusicContract.View = mock()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun init() {
        Dispatchers.setMain(testDispatcher)
        
        // Create presenter with mocked dependencies including MusicRepository
        presenter = MusicPresenter(mockLocationRepo, mockWeatherRepo, mockMusicRepo)
        
        presenter.attachView(mockView)
    }

    @Test
    fun `loadMusicData with RECOMMEND tab should show skeleton loading`() = runTest {
        presenter.loadMusicData(MusicTabType.RECOMMEND)
        testDispatcher.scheduler.advanceUntilIdle()
        verify(mockView).showSkeletonLoading()
    }

    @Test
    @Ignore("Android Log mocking issue in unit tests")
    @SuppressWarnings("unchecked")
    fun `loadMusicData with ALL_SONGS tab should show skeleton loading`() = runTest {
        whenever(mockMusicRepo.getAllSongs()).thenReturn(flowOf(emptyList()))
        presenter.loadMusicData(MusicTabType.ALL_SONGS)
        testDispatcher.scheduler.advanceUntilIdle()
        verify(mockView).showSkeletonLoading()
    }

    @Test
    @SuppressWarnings("unchecked")
    fun `loadMusicData with ARTIST tab should show artists`() = runTest {
        val mockSongs = listOf(
            MusicTestDummies.createMockSong(artist = "Artist 1"),
            MusicTestDummies.createMockSong(artist = "Artist 2"),
            MusicTestDummies.createMockSong(artist = "Artist 1")
        )
        whenever(mockMusicRepo.getAllSongs()).thenReturn(flowOf(mockSongs))
        
        presenter.loadMusicData(MusicTabType.ARTIST)
        testDispatcher.scheduler.advanceUntilIdle()
        verify(mockView, atLeastOnce()).showArtists(any())
    }

    @Test
    @Ignore("Android Log mocking issue in unit tests")
    @SuppressWarnings("unchecked")
    fun `onTabSelected should update selected tab and load music data`() = runTest {
        whenever(mockMusicRepo.getAllSongs()).thenReturn(flowOf(emptyList()))
        presenter.onTabSelected(MusicTabType.ALL_SONGS)
        testDispatcher.scheduler.advanceUntilIdle()
        
        verify(mockView).updateSelectedTab(MusicTabType.ALL_SONGS)
        verify(mockView).showSkeletonLoading()
    }

    @Test
    @SuppressWarnings("unchecked")
    fun `onArtistClicked should navigate to artist detail`() = runTest {
        val artist = MusicTestDummies.createMockArtist()
        presenter.onArtistClicked(artist)
        
        verify(mockView).navigateToArtistDetail(artist)
    }

    @Test
    @Ignore("Android Log mocking issue in unit tests")
    @SuppressWarnings("unchecked")
    fun `onSongClicked should navigate to playing music`() = runTest {
        val song = MusicTestDummies.createMockSong()
        presenter.onSongClicked(song)
        
        verify(mockView).navigateToPlayingMusic(eq(song), any())
    }

    @Test
    @Ignore("Android Log mocking issue in unit tests")
    @SuppressWarnings("unchecked")
    fun `loadWeatherInfo should get current location and weather`() = runTest {
        presenter.loadWeatherInfo()
        testDispatcher.scheduler.advanceUntilIdle()

        val locationCaptor = argumentCaptor<OnResultListener<Location>>()
        verify(mockLocationRepo).getCurrentLocation(locationCaptor.capture())
        val locationListener = locationCaptor.firstValue

        val fakeLocation = mock(Location::class.java)
        whenever(fakeLocation.latitude).thenReturn(10.0)
        whenever(fakeLocation.longitude).thenReturn(20.0)

        locationListener.onSuccess(fakeLocation)

        val weatherCaptor = argumentCaptor<OnResultListener<WeatherResponse>>()
        verify(mockWeatherRepo).getCurrentWeather(
            eq(10.0),
            eq(20.0),
            any()
        )
    }

    @Test
    @Ignore("Android Log mocking issue in unit tests")
    @SuppressWarnings("unchecked")
    fun `location error should show error message`() = runTest {
        presenter.loadWeatherInfo()
        testDispatcher.scheduler.advanceUntilIdle()

        val locationCaptor = argumentCaptor<OnResultListener<Location>>()
        verify(mockLocationRepo).getCurrentLocation(locationCaptor.capture())
        val locationListener = locationCaptor.firstValue

        locationListener.onError(Exception("Location failed"))

        verify(mockView).showError("Location failed")
    }

    @Test
    @Ignore("Android Log mocking issue in unit tests")
    @SuppressWarnings("unchecked")
    fun `weather success should show weather info`() = runTest {
        presenter.loadWeatherInfo()
        testDispatcher.scheduler.advanceUntilIdle()

        val locationCaptor = argumentCaptor<OnResultListener<Location>>()
        verify(mockLocationRepo).getCurrentLocation(locationCaptor.capture())
        val locationListener = locationCaptor.firstValue

        val fakeLocation = mock(Location::class.java)
        whenever(fakeLocation.latitude).thenReturn(10.0)
        whenever(fakeLocation.longitude).thenReturn(20.0)

        locationListener.onSuccess(fakeLocation)

        val weatherCaptor = argumentCaptor<OnResultListener<WeatherResponse>>()
        verify(mockWeatherRepo).getCurrentWeather(eq(10.0), eq(20.0), weatherCaptor.capture())
        val weatherListener = weatherCaptor.firstValue

        val mockWeatherResponse = WeatherResponse(
            coord = Coord(10.0, 20.0),
            weather = listOf(Weather(800, "Clear", "clear sky", "01d")),
            base = "stations",
            main = Main(25.0, 25.0, 25.0, 25.0, 1013, 50, 1013, 1013),
            visibility = 10000,
            wind = Wind(5.0, 180, 5.0),
            rain = null,
            clouds = Clouds(20),
            dt = 1234567890,
            sys = Sys(1, 1234567890, "VN", 1234567890, 1234567890),
            timezone = 25200,
            id = 123456,
            name = "Test City",
            cod = 200
        )

        weatherListener.onSuccess(mockWeatherResponse)

        verify(mockView).showWeatherInfo(any(), any())
    }

    @Test
    @Ignore("Android Log mocking issue in unit tests")
    @SuppressWarnings("unchecked")
    fun `weather error should not crash and continue gracefully`() = runTest {
        presenter.loadWeatherInfo()
        testDispatcher.scheduler.advanceUntilIdle()

        val locationCaptor = argumentCaptor<OnResultListener<Location>>()
        verify(mockLocationRepo).getCurrentLocation(locationCaptor.capture())
        val locationListener = locationCaptor.firstValue

        val fakeLocation = mock(Location::class.java)
        whenever(fakeLocation.latitude).thenReturn(10.0)
        whenever(fakeLocation.longitude).thenReturn(20.0)

        locationListener.onSuccess(fakeLocation)

        val weatherCaptor = argumentCaptor<OnResultListener<WeatherResponse>>()
        verify(mockWeatherRepo).getCurrentWeather(eq(10.0), eq(20.0), weatherCaptor.capture())
        val weatherListener = weatherCaptor.firstValue

        val exception = Exception("weather failed")
        weatherListener.onError(exception)

        // Should not crash and continue gracefully
        // No specific verification needed as it's a silent fail
    }

    @Test
    @SuppressWarnings("unchecked")
    fun `loadMusicData with ARTIST tab should show skeleton loading and load artists`() = runTest {
        whenever(mockMusicRepo.getAllSongs()).thenReturn(flowOf(MusicTestDummies.createMockSongsList()))
        presenter.loadMusicData(MusicTabType.ARTIST)
        testDispatcher.scheduler.advanceUntilIdle()
        
        verify(mockView, atLeastOnce()).showArtists(any())
        // Should also show skeleton loading for Firebase data
        verify(mockView).showSkeletonLoading()
    }

    @Test
    @SuppressWarnings("unchecked")
    fun `onArtistClicked should navigate to artist detail with correct artist data`() = runTest {
        val artist = MusicTestDummies.createMockArtist(
            id = "test_artist_1",
            name = "Test Artist 1",
            songCount = 5
        )
        
        presenter.onArtistClicked(artist)
        
        verify(mockView).navigateToArtistDetail(artist)
    }

    @Test
    @SuppressWarnings("unchecked")
    fun `onArtistClicked should handle multiple artists correctly`() = runTest {
        val artists = MusicTestDummies.createMockArtistsList()
        
        artists.forEach { artist ->
            presenter.onArtistClicked(artist)
            verify(mockView).navigateToArtistDetail(artist)
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    fun `artist navigation should work with different artist data`() = runTest {
        val artist1 = MusicTestDummies.createMockArtist(
            id = "artist_1",
            name = "Artist One",
            songCount = 3
        )
        
        val artist2 = MusicTestDummies.createMockArtist(
            id = "artist_2", 
            name = "Artist Two",
            songCount = 7
        )
        
        presenter.onArtistClicked(artist1)
        verify(mockView).navigateToArtistDetail(artist1)
        
        presenter.onArtistClicked(artist2)
        verify(mockView).navigateToArtistDetail(artist2)
    }

    @Test
    @SuppressWarnings("unchecked")
    fun `tab switching should handle artist tab correctly`() = runTest {
        // Switch to artist tab
        whenever(mockMusicRepo.getAllSongs()).thenReturn(flowOf(MusicTestDummies.createMockSongsList()))
        presenter.onTabSelected(MusicTabType.ARTIST)
        testDispatcher.scheduler.advanceUntilIdle()
        
        verify(mockView).updateSelectedTab(MusicTabType.ARTIST)
        verify(mockView, atLeastOnce()).showArtists(any())
    }

    @Test
    @SuppressWarnings("unchecked")
    fun `artist tab should show artists even when switching from other tabs`() = runTest {
        // First load recommend tab
        presenter.loadMusicData(MusicTabType.RECOMMEND)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then switch to artist tab
        whenever(mockMusicRepo.getAllSongs()).thenReturn(flowOf(MusicTestDummies.createMockSongsList()))
        presenter.onTabSelected(MusicTabType.ARTIST)
        testDispatcher.scheduler.advanceUntilIdle()
        
        verify(mockView).updateSelectedTab(MusicTabType.ARTIST)
        verify(mockView, atLeastOnce()).showArtists(any())
    }

    @Test
    @SuppressWarnings("unchecked")
    fun `artist functionality should work with empty artist list`() = runTest {
        // Test with empty artist list scenario
        whenever(mockMusicRepo.getAllSongs()).thenReturn(flowOf(emptyList()))
        presenter.loadMusicData(MusicTabType.ARTIST)
        testDispatcher.scheduler.advanceUntilIdle()
        
        verify(mockView, atLeastOnce()).showArtists(any())
    }

    @Test
    @SuppressWarnings("unchecked")
    fun `artist click should not crash with null artist data`() = runTest {
        val artist = MusicTestDummies.createMockArtist(
            id = "",
            name = "",
            songCount = 0
        )
        
        presenter.onArtistClicked(artist)
        
        verify(mockView).navigateToArtistDetail(artist)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
} 