package com.mossgreen.lava.music

import android.graphics.Bitmap

data class MusicState(
    val title: String = "No Music",
    val artist: String = "Select a song",
    val isPlaying: Boolean = false,
    val albumArt: Bitmap? = null,
    val duration: Long = 0L,
    val position: Long = 0L
)
