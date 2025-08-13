package com.sun.weatherapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Artist(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val songCount: Int = 0
) : Parcelable

@Parcelize
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val imageUrl: String,
    val duration: String,
    val isPlaying: Boolean = false
) : Parcelable

enum class MusicTabType {
    RECOMMEND,
    ARTIST,
    ALL_SONGS
} 