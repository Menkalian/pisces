package de.menkalian.pisces.audio.queue

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import de.menkalian.pisces.util.logger

/**
 * Eine Queue für Tracks in Form einer MutableList.
 * Die Klasse erhält Methoden zur Erweiterung der Funktionalität, sofern dies nötig ist.
 */
internal class TrackQueue : MutableList<AudioTrack> by mutableListOf() {
    /**
     * Gibt den nächsten Track aus der Queue zurück.
     *
     * @param isShuffle Ob der nächste Track zufällig gewählt werden soll oder
     */
    fun nextTrack(isShuffle: Boolean): AudioTrack? {
        if (isEmpty()) {
            logger().info("Reached end of queue $this")
            return null
        }

        val nextTrack = if (isShuffle) random() else first()
        logger().debug("Providing next track: $nextTrack.ma.")
        remove(nextTrack)
        return nextTrack
    }
}