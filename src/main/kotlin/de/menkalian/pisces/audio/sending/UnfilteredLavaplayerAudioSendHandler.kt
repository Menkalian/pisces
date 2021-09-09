package de.menkalian.pisces.audio.sending

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import de.menkalian.pisces.audio.sending.filter.IAudioInputFilter
import de.menkalian.pisces.util.ExperimentalPisces
import java.nio.ByteBuffer

class UnfilteredLavaplayerAudioSendHandler(val player: AudioPlayer) : IExtendedAudioSendHandler {

    /**
     *
     */
    @OptIn(ExperimentalPisces::class)
    override val filter: MutableList<IAudioInputFilter> = mutableListOf()

    private val frameProvisionMutex = Any()
    private var lastFrame: AudioFrame? = null

    override fun canProvide(): Boolean {
        synchronized(frameProvisionMutex) {
            readFrame()
            return lastFrame != null
        }
    }

    override fun provide20MsAudio(): ByteBuffer? {
        val data: ByteBuffer
        synchronized(frameProvisionMutex) {
            readFrame()
            if (lastFrame != null) {
                data = ByteBuffer.wrap(lastFrame?.data)
            } else {
                return null
            }
            clearFrame()
        }
        return data
    }

    private fun readFrame() {
        if (lastFrame == null) {
            lastFrame = player.provide()
        }
    }

    private fun clearFrame() {
        lastFrame = null
    }
}