package com.sun.weatherapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Artist(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val songCount: Int = 0
) : Parcelable {
    
    // Firebase requires empty constructor
    constructor() : this("", "", "", "", 0)
}
