package de.menkalian.pisces.audio.queue

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import de.menkalian.pisces.util.logger

class TrackQueue : MutableList<AudioTrack> by mutableListOf() {
    fun nextTrack(isShuffle: Boolean): AudioTrack? {
        if(isEmpty()) {
            logger().info("Reached end of queue $this")
            return null
        }

        val nextTrack = if (isShuffle) random() else first()
        logger().debug("Providing next track: $nextTrack.ma.")
        return nextTrack
    }
}