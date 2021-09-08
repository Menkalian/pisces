package de.menkalian.pisces.audio.sending.filter

import de.menkalian.pisces.audio.sending.IExtendedAudioSendHandler
import de.menkalian.pisces.util.ExperimentalPisces

/**
 * Schnittstelle für eine Klasse, die in der Lage ist die erhaltenen Audiodaten zu filtern.
 * Diese Schnittstelle ist noch experimentell und sollte momentan nicht verwendet werden.
 */
@ExperimentalPisces
interface IAudioInputFilter {
    /**
     * Ob der Audiofilter aktiv ist
     */
    val active: Boolean

    /**
     * Wandelt die angegebenen Eingabebytes um.
     */
    fun processInputBytes(sendHandler: IExtendedAudioSendHandler, bytes: ByteArray): ByteArray
}