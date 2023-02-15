package de.menkalian.pisces.web.data

data class AudioPlayerStateData(
    val isPaused: Boolean,
    val isRepeating: Boolean,
    val isShuffle: Boolean,
    val queueSize: Int,
    val nextTracks: List<TrackMessageData>
)
