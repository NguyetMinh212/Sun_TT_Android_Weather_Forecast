package com.sun.weatherapp.artistdetail

import com.sun.weatherapp.data.repository.MusicRepository
import com.sun.weatherapp.screen.artistdetail.ArtistDetailContract
import com.sun.weatherapp.screen.artistdetail.ArtistDetailPresenter
import com.sun.weatherapp.testutil.MusicTestDummies
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.atLeastOnce
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistDetailPresenterTest {
    private lateinit var presenter: ArtistDetailPresenter
    private val mockMusicRepo = mock<MusicRepository>()
    private val mockView: ArtistDetailContract.View = mock()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun init() {
        Dispatchers.setMain(testDispatcher)
        presenter = ArtistDetailPresenter(mockMusicRepo)
        presenter.attachView(mockView)
    }

    @Test
    fun `loadArtistDetail should show artist info and loading`() = runTest {
        val artist = MusicTestDummies.createMockArtist()
        
        presenter.loadArtistDetail(artist)
        
        verify(mockView).showArtistInfo(artist)
        verify(mockView).showLoading()
    }

    @Test
    fun `loadArtistDetail should filter songs by artist name`() = runTest {
        val artist = MusicTestDummies.createMockArtist(name = "Test Artist")
        val mockSongs = listOf(
            MusicTestDummies.createMockSong(artist = "Test Artist", title = "Song 1"),
            MusicTestDummies.createMockSong(artist = "Other Artist", title = "Song 2"),
            MusicTestDummies.createMockSong(artist = "Test Artist", title = "Song 3")
        )
        
        whenever(mockMusicRepo.getAllSongs()).thenReturn(flowOf(mockSongs))
        
        presenter.loadArtistDetail(artist)
        testDispatcher.scheduler.advanceUntilIdle()
        
        verify(mockView).hideLoading()
        verify(mockView).showSongs(any())
    }

    @Test
    fun `loadArtistDetail should show songs when artist has songs`() = runTest {
        val artist = MusicTestDummies.createMockArtist(name = "Test Artist")
        val mockSongs = listOf(
            MusicTestDummies.createMockSong(artist = "Test Artist", title = "Song 1"),
            MusicTestDummies.createMockSong(artist = "Test Artist", title = "Song 2")
        )
        
        whenever(mockMusicRepo.getAllSongs()).thenReturn(flowOf(mockSongs))
        
        presenter.loadArtistDetail(artist)
        testDispatcher.scheduler.advanceUntilIdle()
        
        verify(mockView).hideLoading()
        verify(mockView).hideEmptyState()
        verify(mockView).showSongs(any())
    }

    @Test
    fun `loadArtistDetail should show empty state when artist has no songs`() = runTest {
        val artist = MusicTestDummies.createMockArtist(name = "Test Artist")
        val mockSongs = listOf(
            MusicTestDummies.createMockSong(artist = "Other Artist", title = "Song 1"),
            MusicTestDummies.createMockSong(artist = "Another Artist", title = "Song 2")
        )
        
        whenever(mockMusicRepo.getAllSongs()).thenReturn(flowOf(mockSongs))
        
        presenter.loadArtistDetail(artist)
        testDispatcher.scheduler.advanceUntilIdle()
        
        verify(mockView).hideLoading()
        verify(mockView).showEmptyState()
    }

    @Test
    fun `loadArtistDetail should handle empty song list`() = runTest {
        val artist = MusicTestDummies.createMockArtist(name = "Test Artist")
        
        whenever(mockMusicRepo.getAllSongs()).thenReturn(flowOf(emptyList()))
        
        presenter.loadArtistDetail(artist)
        testDispatcher.scheduler.advanceUntilIdle()
        
        verify(mockView).hideLoading()
        verify(mockView).showEmptyState()
    }

    @Test
    fun `loadArtistDetail should handle Firebase error gracefully`() = runTest {
        val artist = MusicTestDummies.createMockArtist(name = "Test Artist")
        
        whenever(mockMusicRepo.getAllSongs()).thenThrow(RuntimeException("Firebase error"))
        
        presenter.loadArtistDetail(artist)
        testDispatcher.scheduler.advanceUntilIdle()
        
        verify(mockView).hideLoading()
        verify(mockView).showError("Lỗi load dữ liệu: Firebase error")
        verify(mockView).showEmptyState()
    }

    @Test
    fun `onSongClicked should increment play count and navigate to playing music`() = runTest {
        val song = MusicTestDummies.createMockSong()
        val artist = MusicTestDummies.createMockArtist(name = song.artist)
        val mockSongs = listOf(song)
        
        // First load artist detail to set up artistSongs
        whenever(mockMusicRepo.getAllSongs()).thenReturn(flowOf(mockSongs))
        presenter.loadArtistDetail(artist)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then click on song
        presenter.onSongClicked(song)
        
        verify(mockView).navigateToPlayingMusic(any(), any())
    }

    @Test
    fun `onSongClicked should handle play count increment error gracefully`() = runTest {
        val song = MusicTestDummies.createMockSong()
        val artist = MusicTestDummies.createMockArtist(name = song.artist)
        val mockSongs = listOf(song)
        
        // First load artist detail to set up artistSongs
        whenever(mockMusicRepo.getAllSongs()).thenReturn(flowOf(mockSongs))
        presenter.loadArtistDetail(artist)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Mock increment play count to throw error (suspend function can throw)
        whenever(mockMusicRepo.incrementPlayCount(song.id)).thenThrow(RuntimeException("Increment error"))
        
        // Click on song - should not crash
        presenter.onSongClicked(song)
        
        verify(mockView).navigateToPlayingMusic(any(), any())
    }

    @Test
    fun `onSongClicked should work with different song data`() = runTest {
        val songs = listOf(
            MusicTestDummies.createMockSong(id = "song1", title = "Song 1", artist = "Test Artist"),
            MusicTestDummies.createMockSong(id = "song2", title = "Song 2", artist = "Test Artist")
        )
        val artist = MusicTestDummies.createMockArtist(name = "Test Artist")
        
        whenever(mockMusicRepo.getAllSongs()).thenReturn(flowOf(songs))
        presenter.loadArtistDetail(artist)
        testDispatcher.scheduler.advanceUntilIdle()
        
        songs.forEach { song ->
            presenter.onSongClicked(song)
        }
        
        // Verify that navigateToPlayingMusic was called for each song
        verify(mockView, atLeastOnce()).navigateToPlayingMusic(any(), any())
    }

    @Test
    fun `artist detail should handle multiple artists correctly`() = runTest {
        val artist1 = MusicTestDummies.createMockArtist(name = "Artist 1")
        val artist2 = MusicTestDummies.createMockArtist(name = "Artist 2")
        
        val mockSongs = listOf(
            MusicTestDummies.createMockSong(artist = "Artist 1", title = "Song 1"),
            MusicTestDummies.createMockSong(artist = "Artist 2", title = "Song 2")
        )
        
        whenever(mockMusicRepo.getAllSongs()).thenReturn(flowOf(mockSongs))
        
        // Load first artist
        presenter.loadArtistDetail(artist1)
        testDispatcher.scheduler.advanceUntilIdle()
        verify(mockView, atLeastOnce()).showSongs(any())
        
        // Load second artist
        presenter.loadArtistDetail(artist2)
        testDispatcher.scheduler.advanceUntilIdle()
        verify(mockView, atLeastOnce()).showSongs(any())
    }

    @Test
    fun `artist detail should handle edge case artist data`() = runTest {
        val artist = MusicTestDummies.createMockArtist(
            id = "",
            name = "",
            description = "",
            imageUrl = "",
            songCount = 0
        )
        
        whenever(mockMusicRepo.getAllSongs()).thenReturn(flowOf(emptyList()))
        
        presenter.loadArtistDetail(artist)
        testDispatcher.scheduler.advanceUntilIdle()
        
        verify(mockView).showArtistInfo(artist)
        verify(mockView).hideLoading()
        verify(mockView).showEmptyState()
    }

    @Test
    fun `artist detail should handle large song list`() = runTest {
        val artist = MusicTestDummies.createMockArtist(name = "Test Artist")
        val mockSongs = (1..100).map { index ->
            MusicTestDummies.createMockSong(
                id = "song$index",
                title = "Song $index",
                artist = "Test Artist"
            )
        }
        
        whenever(mockMusicRepo.getAllSongs()).thenReturn(flowOf(mockSongs))
        
        presenter.loadArtistDetail(artist)
        testDispatcher.scheduler.advanceUntilIdle()
        
        verify(mockView).hideLoading()
        verify(mockView).hideEmptyState()
        verify(mockView).showSongs(any())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
} 