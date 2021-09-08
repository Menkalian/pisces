package de.menkalian.pisces.audio.queue

import com.sedmelluq.discord.lavaplayer.track.AudioTrack

class TrackQueue : MutableList<AudioTrack> by mutableListOf() {
    fun nextTrack(isShuffle: Boolean) = if (isShuffle) random() else first()
}