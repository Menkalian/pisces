package de.menkalian.pisces.web.data

data class PreloadedTrackData(
    val preloadUuid: String,
    val displayName: String,
    val preloadUrl: String,
    val hasError: Boolean
)