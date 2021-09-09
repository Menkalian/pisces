package de.menkalian.pisces.audio

import de.menkalian.pisces.IHandler

/**
 * Schnittstelle zur Bearbeitung von Audio.
 * Eine Instanz dieser Schnittstelle wird als Spring-Bean in der Applikation zur Verfügung gestellt.
 * Der AudioHandler verwaltet die aktiven [IGuildAudioController]-Instanzen und stellt diese (threadsicher) zur Verfügung
 */
interface IAudioHandler : IHandler {
    /**
     * Stellt den [IGuildAudioController] zur angegebenen ID bereit.
     * Falls zum geforderten Discord-Server kein Controller verfügbar ist, wird eine neue Instanz erzeugt.
     *
     * @param guildId Discord-ID des Servers
     *
     * @return [IGuildAudioController] des Servers
     */
    fun getGuildAudioController(guildId: Long): IGuildAudioController

    /**
     * Löscht/Entfernt den [IGuildAudioController] des angegebenen Servers.
     *
     * @param guildId Discord-ID des Servers
     *
     * @return Ob ein [IGuildAudioController] gelöscht wurde (z.B. `false` wenn kein [IGuildAudioController] für die angegebene ID existiert)
     */
    fun deleteGuildAudioController(guildId: Long): Boolean
}