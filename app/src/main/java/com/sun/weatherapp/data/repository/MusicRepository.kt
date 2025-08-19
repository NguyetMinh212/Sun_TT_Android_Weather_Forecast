package com.sun.weatherapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sun.weatherapp.data.model.Song
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MusicRepository {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val songsCollection = firestore.collection("songs")

    companion object {
        private const val FIELD_WEATHER_MOODS = "weatherMoods"
        private const val FIELD_CATEGORY = "category"
        private const val FIELD_PLAY_COUNT = "playCount"
        private const val FIELD_CREATED_AT = "createdAt"
    }
    
    /**
     * Get all songs với real-time updates
     */
    fun getAllSongs(): Flow<List<Song>> = callbackFlow {
        android.util.Log.d("MusicRepository", "Starting getAllSongs() query")
        val listener = songsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val songs = snapshot?.documents?.mapNotNull { document ->
                    try {
                        val song = document.toObject(Song::class.java)?.copy(id = document.id)
                        song
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                val sortedSongs = songs.sortedByDescending { it.playCount }
                trySend(sortedSongs)
            }
        
        awaitClose { 
            android.util.Log.d("MusicRepository", "Closing getAllSongs listener")
            listener.remove()
        }
    }
    
    /**
     * Get recommended songs based on current weather
     */
    fun getRecommendedSongs(weatherMood: String, limit: Int = 20): Flow<List<Song>> = callbackFlow {
        val listener = songsCollection
            .whereArrayContains(FIELD_WEATHER_MOODS, weatherMood)
            // Remove orderBy to avoid composite index requirement
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val songs = snapshot?.documents?.mapNotNull { document ->
                    try {
                        document.toObject(Song::class.java)?.copy(id = document.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                // Sort locally instead of in Firestore
                val sortedSongs = songs.sortedByDescending { it.playCount }
                trySend(sortedSongs)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Update play count khi user play song
     */
    suspend fun incrementPlayCount(songId: String) {
        try {
            firestore.runTransaction { transaction ->
                val songRef = songsCollection.document(songId)
                val snapshot = transaction.get(songRef)
                val currentPlayCount = snapshot.getLong(FIELD_PLAY_COUNT) ?: 0
                transaction.update(songRef, FIELD_PLAY_COUNT, currentPlayCount + 1)
            }.await()
        } catch (e: Exception) {
            // Log error but don't crash app
        }
    }
}
