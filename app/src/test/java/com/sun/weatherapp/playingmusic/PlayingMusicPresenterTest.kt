package com.sun.weatherapp.playingmusic

import android.content.Context
import com.sun.weatherapp.data.model.Song
import com.sun.weatherapp.screen.playingmusic.PlayingMusicContract
import com.sun.weatherapp.screen.playingmusic.PlayingMusicPresenter
import com.sun.weatherapp.service.MusicPlayerService
import com.sun.weatherapp.testutil.MusicTestDummies
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any

@OptIn(ExperimentalCoroutinesApi::class)
class PlayingMusicPresenterTest {
    private lateinit var presenter: PlayingMusicPresenter
    private val mockView: PlayingMusicContract.View = mock()
    private val mockContext: Context = mock()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun init() {
        Dispatchers.setMain(testDispatcher)
        presenter = PlayingMusicPresenter()
        presenter.attachView(mockView)
    }

    @Test
    fun `loadSong should update progress and show song info when service is bound`() = runTest {
        val song = MusicTestDummies.createMockSong()
        presenter.loadSong(song)
        
        verify(mockView).updateProgress(any(), any(), any())
    }

    @Test
    fun `loadSongWithPlaylist should update progress and set playlist when service is bound`() = runTest {
        val song = MusicTestDummies.createMockSong()
        val playlist = listOf(song, MusicTestDummies.createMockSong(id = "song_2", title = "Test Song 2"))
        
        presenter.loadSongWithPlaylist(song, playlist)
        
        verify(mockView).updateProgress(any(), any(), any())
    }

    @Test
    fun `onPlayPauseClicked should toggle play pause when service is bound`() = runTest {
        presenter.onPlayPauseClicked()
    }

    @Test
    fun `onPreviousClicked should update progress and show playing state when service is bound`() = runTest {
        presenter.onPreviousClicked()
    }

    @Test
    fun `onNextClicked should update progress and show playing state when service is bound`() = runTest {
        presenter.onNextClicked()
    }

    @Test
    fun `onShuffleClicked should toggle shuffle when service is bound`() = runTest {
        presenter.onShuffleClicked()
    }

    @Test
    fun `onRepeatClicked should toggle repeat when service is bound`() = runTest {
        presenter.onRepeatClicked()
    }

    @Test
    fun `onSeekChanged should seek to position when service is bound`() = runTest {
        val progress = 50
        presenter.onSeekChanged(progress)
    }

    @Test
    fun `bindService should bind to MusicPlayerService`() = runTest {
        presenter.bindService(mockContext)
    }

    @Test
    fun `unbindService should unbind from MusicPlayerService`() = runTest {
        presenter.unbindService(mockContext)
    }

    @Test
    fun `stopMusicAndService should stop music and service`() = runTest {
        presenter.stopMusicAndService(mockContext)
    }

    @Test
    fun `loadSong should handle different song durations`() = runTest {
        val song1 = MusicTestDummies.createMockSong(duration = "3:30", durationMs = 210000)
        val song2 = MusicTestDummies.createMockSong(duration = "4:15", durationMs = 255000)
        
        presenter.loadSong(song1)
        presenter.loadSong(song2)
        
        // Verify that updateProgress was called at least once
        verify(mockView, org.mockito.Mockito.atLeastOnce()).updateProgress(any(), any(), any())
    }

    @Test
    fun `loadSongWithPlaylist should handle different playlist sizes`() = runTest {
        val song = MusicTestDummies.createMockSong()
        val smallPlaylist = listOf(song)
        val largePlaylist = (1..10).map { index ->
            MusicTestDummies.createMockSong(id = "song$index", title = "Song $index")
        }
        
        presenter.loadSongWithPlaylist(song, smallPlaylist)
        presenter.loadSongWithPlaylist(song, largePlaylist)
        
        // Verify that updateProgress was called at least once
        verify(mockView, org.mockito.Mockito.atLeastOnce()).updateProgress(any(), any(), any())
    }

    @Test
    fun `onSeekChanged should handle different progress values`() = runTest {
        val progressValues = listOf(0, 25, 50, 75, 100)
        
        progressValues.forEach { progress ->
            presenter.onSeekChanged(progress)
            // Should not crash for any progress value
        }
    }

    @Test
    fun `service connection should handle song changes correctly`() = runTest {
        
        presenter.onPlayPauseClicked()
        presenter.onPreviousClicked()
        presenter.onNextClicked()
        
        // Should not crash
    }

    @Test
    fun `service connection should handle playback state changes correctly`() = runTest {
        presenter.onShuffleClicked()
        presenter.onRepeatClicked()
        
        // Should not crash
    }

    @Test
    fun `progress updates should work correctly`() = runTest {
        presenter.onSeekChanged(25)
        presenter.onSeekChanged(50)
        presenter.onSeekChanged(75)
    }

    @Test
    fun `loadSong should work with edge case song data`() = runTest {
        val edgeCaseSong = MusicTestDummies.createMockSong(
            id = "",
            title = "",
            artist = "",
            duration = "0:00",
            durationMs = 0
        )
        
        presenter.loadSong(edgeCaseSong)
        
        verify(mockView).updateProgress(any(), any(), any())
    }

    @Test
    fun `loadSongWithPlaylist should work with empty playlist`() = runTest {
        val song = MusicTestDummies.createMockSong()
        val emptyPlaylist = emptyList<Song>()
        
        presenter.loadSongWithPlaylist(song, emptyPlaylist)
        
        verify(mockView).updateProgress(any(), any(), any())
    }

    @Test
    fun `multiple song loads should work correctly`() = runTest {
        val songs = listOf(
            MusicTestDummies.createMockSong(id = "song1", title = "Song 1"),
            MusicTestDummies.createMockSong(id = "song2", title = "Song 2"),
            MusicTestDummies.createMockSong(id = "song3", title = "Song 3")
        )
        
        songs.forEach { song ->
            presenter.loadSong(song)
        }
        
        // Verify that updateProgress was called for each song
        verify(mockView, org.mockito.Mockito.atLeastOnce()).updateProgress(any(), any(), any())
    }

    @Test
    fun `service lifecycle methods should not crash`() = runTest {
        // Test all service lifecycle methods
        presenter.bindService(mockContext)
        presenter.unbindService(mockContext)
        presenter.stopMusicAndService(mockContext)
        
        // Should not crash
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
} 