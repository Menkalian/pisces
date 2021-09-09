package de.menkalian.pisces.audio.data

/**
 * Aktueller Status des Tracks.
 * Entspricht den Werten von [com.sedmelluq.discord.lavaplayer.track.AudioTrackState], außer [UNKNOWN].
 *
 * @property UNKNOWN Wurde hinzugefügt um `null`-Werte behandeln zu können.
 */
enum class AudioTrackState {
    INACTIVE, LOADING, PLAYING, SEEKING, STOPPING, FINISHED, UNKNOWN
}

