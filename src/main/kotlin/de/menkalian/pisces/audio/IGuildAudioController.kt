package de.menkalian.pisces.audio

interface IGuildAudioController {
    enum class EPlayTrackResult {
        TRACK,
        PLAYLIST,
        TRACK_FROM_PLAYLIST,
        NOT_FOUND,
        ERROR
    }

    fun reset()
    fun stop()

    fun playTrack(
        searchterm: String,
        playInstant: Boolean = false,
        playFullPlaylist: Boolean = false
    ): EPlayTrackResult

    fun skipCurrentTrack(requeue: Boolean = false, skipAmount: Int = 1): TrackInfo?
    fun changeTrackPosition(delta: Long)

    fun deleteFromQueue(index: Int)
    fun clearQueue()
    fun shuffleQueue()

    fun getCurrentTrackInfo(): TrackInfo
    fun getQueueInfo(): List<TrackInfo>

    fun togglePause(readOnly: Boolean = false): Boolean
    fun toggleRepeat(readOnly: Boolean = false): Boolean
    fun togglePermanentShuffle(readOnly: Boolean = false): Boolean
}