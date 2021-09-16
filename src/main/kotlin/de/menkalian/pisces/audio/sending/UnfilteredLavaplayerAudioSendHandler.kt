package de.menkalian.pisces.audio.sending

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import de.menkalian.pisces.audio.sending.filter.IAudioInputFilter
import de.menkalian.pisces.util.ExperimentalPisces
import java.nio.ByteBuffer

/**
 * Implementierung der [IExtendedAudioSendHandler]-Schnittstelle.
 * Dieser SendHandler erhält die Audiodaten von Lavaplayer und leitet diese ohne weitere Verarbeitung an Discord/JDA weiter.
 * Die [IAudioInputFilter] werden in dieser Klasse **nicht** verwendet.
 */
internal class UnfilteredLavaplayerAudioSendHandler(val player: AudioPlayer) : IExtendedAudioSendHandler {

    /**
     * Definition zur Erfüllung der Schnittstelle.
     * [@Unused]
     */
    @OptIn(ExperimentalPisces::class)
    override val filter: MutableList<IAudioInputFilter> = mutableListOf()

    /**
     * Mutex zur Absicherung des Multithread-Zugriffs.
     */
    private val frameProvisionMutex = Any()

    /**
     * Caching eines [AudioFrame]s (da für diese einfache Verarbeitung nicht mehrere [AudioFrame]s gespeichert werden müssen)
     */
    private var lastFrame: AudioFrame? = null

    /**
     * Prüft, ob ein [AudioFrame] zum Senden zur Verfügung steht.
     */
    override fun canProvide(): Boolean {
        synchronized(frameProvisionMutex) {
            readFrame()
            return lastFrame != null
        }
    }

    /**
     * Stellt das nächste [AudioFrame] an Discord bereit.
     */
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

    /**
     * Liest das nächste [AudioFrame] von [player] aus und speichert es im Cache.
     */
    private fun readFrame() {
        if (lastFrame == null) {
            lastFrame = player.provide()
        }
    }

    /**
     * Löscht das gespeicherte [AudioFrame].
     */
    private fun clearFrame() {
        lastFrame = null
    }
}