package com.sun.weatherapp.testutil

import com.sun.weatherapp.data.model.Artist
import com.sun.weatherapp.data.model.Song

object MusicTestDummies {
    
    fun createMockSong(
        id: String = "test_song_id",
        title: String = "Test Song",
        artist: String = "Test Artist",
        album: String = "Test Album",
        imageUrl: String = "https://example.com/image.jpg",
        audioUrl: String = "https://example.com/audio.mp3",
        duration: String = "3:30",
        durationMs: Long = 210000,
        category: String = "pop",
        weatherMoods: List<String> = listOf("sunny", "clear"),
        releaseYear: Int = 2023,
        playCount: Long = 0,
        isFavorite: Boolean = false,
        lyrics: String = "Test lyrics",
        createdAt: Long = System.currentTimeMillis(),
        updatedAt: Long = System.currentTimeMillis()
    ): Song {
        return Song(
            id = id,
            title = title,
            artist = artist,
            album = album,
            imageUrl = imageUrl,
            audioUrl = audioUrl,
            duration = duration,
            durationMs = durationMs,
            category = category,
            weatherMoods = weatherMoods,
            releaseYear = releaseYear,
            playCount = playCount,
            isFavorite = isFavorite,
            lyrics = lyrics,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    fun createMockArtist(
        id: String = "test_artist_id",
        name: String = "Test Artist",
        description: String = "Test Description",
        imageUrl: String = "https://example.com/artist.jpg",
        songCount: Int = 5
    ): Artist {
        return Artist(
            id = id,
            name = name,
            description = description,
            imageUrl = imageUrl,
            songCount = songCount
        )
    }
    
    fun createMockSongsList(): List<Song> {
        return listOf(
            createMockSong(
                id = "song_1",
                title = "Sunny Day",
                artist = "Artist A",
                weatherMoods = listOf("sunny", "clear"),
                playCount = 10
            ),
            createMockSong(
                id = "song_2",
                title = "Rainy Night",
                artist = "Artist B",
                weatherMoods = listOf("rainy", "cloudy"),
                playCount = 5
            ),
            createMockSong(
                id = "song_3",
                title = "Cloudy Afternoon",
                artist = "Artist A",
                weatherMoods = listOf("cloudy", "foggy"),
                playCount = 8
            ),
            createMockSong(
                id = "song_4",
                title = "Clear Sky",
                artist = "Artist C",
                weatherMoods = listOf("clear", "sunny"),
                playCount = 15
            )
        )
    }
    
    fun createMockArtistsList(): List<Artist> {
        return listOf(
            createMockArtist(
                id = "artist_a",
                name = "Artist A",
                songCount = 2
            ),
            createMockArtist(
                id = "artist_b",
                name = "Artist B",
                songCount = 1
            ),
            createMockArtist(
                id = "artist_c",
                name = "Artist C",
                songCount = 1
            )
        )
    }
    
    fun createMockSongsByArtist(artistName: String): List<Song> {
        return createMockSongsList().filter { it.artist == artistName }
    }
    
    fun createMockSongsByWeatherMood(weatherMood: String): List<Song> {
        return createMockSongsList().filter { it.weatherMoods.contains(weatherMood) }
    }
} 