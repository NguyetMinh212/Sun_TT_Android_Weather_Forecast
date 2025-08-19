package com.sun.weatherapp.utils

import com.sun.weatherapp.data.model.Song

object WeatherMusicMapper {
    
    /**
     * Map weather condition từ API sang music mood
     */
    fun mapWeatherToMood(weatherCondition: String, temperature: Double): String {
        return when {
            // Nắng đẹp, nhiệt độ cao
            weatherCondition.contains("clear", ignoreCase = true) && temperature > 25 -> Song.WEATHER_SUNNY
            weatherCondition.contains("sunny", ignoreCase = true) && temperature > 25 -> Song.WEATHER_SUNNY
            
            // Mưa - tất cả loại mưa đều buồn/lãng mạn
            weatherCondition.contains("rain", ignoreCase = true) -> Song.WEATHER_RAINY
            weatherCondition.contains("drizzle", ignoreCase = true) -> Song.WEATHER_RAINY
            weatherCondition.contains("shower", ignoreCase = true) -> Song.WEATHER_RAINY
            
            // Bão tố - nhạc mạnh mẽ
            weatherCondition.contains("storm", ignoreCase = true) -> Song.WEATHER_STORMY
            weatherCondition.contains("thunder", ignoreCase = true) -> Song.WEATHER_STORMY
            
            // Có mây nhưng không mưa - nhạc nhẹ nhàng
            weatherCondition.contains("cloud", ignoreCase = true) -> Song.WEATHER_CLOUDY
            weatherCondition.contains("overcast", ignoreCase = true) -> Song.WEATHER_CLOUDY
            
            // Sương mù - nhạc mơ màng
            weatherCondition.contains("fog", ignoreCase = true) -> Song.WEATHER_FOGGY
            weatherCondition.contains("mist", ignoreCase = true) -> Song.WEATHER_FOGGY
            weatherCondition.contains("haze", ignoreCase = true) -> Song.WEATHER_FOGGY
            
            // Gió mạnh - nhạc sôi động
            weatherCondition.contains("wind", ignoreCase = true) -> Song.WEATHER_WINDY
            
            // Quang đãng, nhiệt độ vừa phải
            weatherCondition.contains("clear", ignoreCase = true) -> Song.WEATHER_CLEAR
            
            // Default fallback
            else -> Song.WEATHER_CLEAR
        }
    }
    
    /**
     * Get mood description cho UI
     */
    fun getMoodDescription(mood: String): String {
        return when (mood) {
            Song.WEATHER_SUNNY -> "Nắng đẹp - Nhạc sôi động, vui tươi"
            Song.WEATHER_RAINY -> "Mưa - Nhạc buồn, lãng mạn"
            Song.WEATHER_CLOUDY -> "Có mây - Nhạc nhẹ nhàng, thư giãn"
            Song.WEATHER_STORMY -> "Bão tố - Nhạc mạnh mẽ, kịch tính"
            Song.WEATHER_CLEAR -> "Quang đãng - Nhạc tươi sáng"
            Song.WEATHER_FOGGY -> "Sương mù - Nhạc mơ màng, trữ tình"
            Song.WEATHER_WINDY -> "Gió - Nhạc sôi động, tự do"
            else -> "Nhạc phù hợp với thời tiết"
        }
    }
    
    /**
     * Get mood emoji cho UI
     */
    fun getMoodEmoji(mood: String): String {
        return when (mood) {
            Song.WEATHER_SUNNY -> "☀️"
            Song.WEATHER_RAINY -> "🌧️"
            Song.WEATHER_CLOUDY -> "☁️"
            Song.WEATHER_STORMY -> "⛈️"
            Song.WEATHER_CLEAR -> "🌤️"
            Song.WEATHER_FOGGY -> "🌫️"
            Song.WEATHER_WINDY -> "💨"
            else -> "🎵"
        }
    }
    
    /**
     * Suggest multiple moods cho mixed weather
     */
    fun getSuggestedMoods(weatherCondition: String, temperature: Double): List<String> {
        val primaryMood = mapWeatherToMood(weatherCondition, temperature)
        val suggestions = mutableListOf(primaryMood)
        
        // Add secondary moods based on conditions
        when {
            temperature > 30 -> suggestions.add(Song.WEATHER_SUNNY)
            temperature < 15 -> suggestions.add(Song.WEATHER_RAINY)
            weatherCondition.contains("partly", ignoreCase = true) -> {
                suggestions.add(Song.WEATHER_CLOUDY)
                suggestions.add(Song.WEATHER_CLEAR)
            }
        }
        
        return suggestions.distinct()
    }
}
