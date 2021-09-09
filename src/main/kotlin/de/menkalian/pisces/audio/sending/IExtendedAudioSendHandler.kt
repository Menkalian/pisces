package de.menkalian.pisces.audio.sending

import de.menkalian.pisces.audio.sending.filter.IAudioInputFilter
import de.menkalian.pisces.util.ExperimentalPisces
import net.dv8tion.jda.api.audio.AudioSendHandler

/**
 * Erweiterte Schnittstelle für [AudioSendHandler].
 * Diese Schnittstelle erweitert die bestehende JDA-Schnittstelle um die folgenden Funktionen:
 *  - [[ExperimentalPisces]] Filter zur Manipulation des Audiostreams
 *
 *  @property filter Anzuwendende Filter
 */
interface IExtendedAudioSendHandler : AudioSendHandler {
    @OptIn(ExperimentalPisces::class)
    val filter: MutableList<IAudioInputFilter>
}
