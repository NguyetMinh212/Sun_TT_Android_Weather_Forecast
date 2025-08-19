package com.sun.weatherapp.utils

import android.util.Log

object FirebaseImageHelper {
    
    private const val TAG = "FirebaseImageHelper"
    
    /**
     * Convert Google Drive sharing URL to direct download URL
     * @param driveUrl: Google Drive sharing URL
     * @return Direct download URL for Glide
     */
    fun getDirectImageUrl(driveUrl: String?): String? {
        if (driveUrl.isNullOrEmpty()) {
            return null
        }
        
        return try {
            // If it's already a direct URL, return as is
            if (driveUrl.contains("uc?export=download")) {
                return driveUrl
            }
            
            // Extract file ID from Google Drive URL
            val fileId = extractFileIdFromDriveUrl(driveUrl)
            if (fileId != null) {
                "https://drive.google.com/uc?export=download&id=$fileId"
            } else {
                Log.w(TAG, "Could not extract file ID from URL: $driveUrl")
                driveUrl
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error converting Drive URL: ${e.message}")
            driveUrl
        }
    }
    
    /**
     * Extract file ID from various Google Drive URL formats
     */
    private fun extractFileIdFromDriveUrl(driveUrl: String): String? {
        val patterns = listOf(
            // https://drive.google.com/file/d/FILE_ID/view
            Regex("""/file/d/([a-zA-Z0-9_-]+)"""),
            // https://drive.google.com/open?id=FILE_ID
            Regex("""[?&]id=([a-zA-Z0-9_-]+)"""),
            // https://drive.google.com/drive/folders/FILE_ID
            Regex("""/folders/([a-zA-Z0-9_-]+)""")
        )
        
        for (pattern in patterns) {
            val match = pattern.find(driveUrl)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        
        return null
    }
}
