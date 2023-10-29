package com.stevenjamesmead.cis4034madhorseyappviewmodel

import androidx.compose.ui.graphics.ImageBitmap

data class HorseImage(
    val filename: String,
    val description: String,
    val image: ImageBitmap,
    var liked: Boolean = false
)