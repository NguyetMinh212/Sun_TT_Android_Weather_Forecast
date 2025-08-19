package com.sun.weatherapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val id: String = "",
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val imageUrl: String = "",
    val audioUrl: String = "",
    val duration: String = "",
    val durationMs: Long = 0,
    val category: String = "",          // "pop", "ballad", "rock", etc.
    val weatherMoods: List<String> = emptyList(),  // ["sunny", "rainy", "cloudy", "stormy", "clear"]
    val releaseYear: Int = 0,
    val playCount: Long = 0,
    val isFavorite: Boolean = false,
    val lyrics: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable {
    
    // Firebase requires empty constructor
    constructor() : this("", "", "", "", "", "", "", 0, "", emptyList(), 0, 0, false, "", 0, 0)
    
    companion object {
        // Weather mood constants
        const val WEATHER_SUNNY = "sunny"
        const val WEATHER_RAINY = "rainy"
        const val WEATHER_CLOUDY = "cloudy"
        const val WEATHER_STORMY = "stormy"
        const val WEATHER_CLEAR = "clear"
        const val WEATHER_FOGGY = "foggy"
        const val WEATHER_WINDY = "windy"
    }
}
