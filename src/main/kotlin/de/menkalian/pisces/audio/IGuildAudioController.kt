package de.menkalian.pisces.audio

import de.menkalian.pisces.audio.data.TrackInfo
import de.menkalian.pisces.util.QueueResult

/**
 * Kontrolliert alles was mit Audio/Sound zu tun hat; jeder Server erhält eine eigene Instanz dieser Klasse.
 * Durch diese Schnittstelle findet eine Abstraktion statt.
 * Die korrekte Instanz des [IGuildAudioController] zu erhalten kann die aktive Instanz des [IAudioHandler] verwendet werden.
 */
interface IGuildAudioController {

    /**
     * Gibt die Discord ID des Channels an, mit dem der [IGuildAudioController] momentan verbunden ist.
     *
     * @return Discord ID des Channels
     */
    fun getConnectedChannel() : Long?

    /**
     * Verbindet sich zu dem Voicechannel mit der angegebenen ID.
     *
     * @param channelId Discord ID des Channels
     *
     * @return Ob die Verbindung erfolgreich war.
     *         Die Verbindung kann fehlschlagen, wenn der Bot nicht die nötigen Rechte zur Verbindung hat oder der Channel nicht existiert.
     */
    fun connect(channelId: Long): Boolean

    /**
     * Beendet die aktuelle Sprachverbindung.
     *
     * @return Ob die Trennung der Verbindung erfolgreich war.
     *         Die Trennung kann fehlschlagen, wenn keine vorige Verbindung existiert hat.
     */
    fun disconnect(): Boolean

    /**
     * Setzt den [IGuildAudioController] zurück.
     * Dies beinhaltet das Beenden der aktuellen Verbindung, das Leeren der Queue und das Zurücksetzen von [IGuildAudioController.togglePause], [IGuildAudioController.toggleRepeat] und [IGuildAudioController.togglePermanentShuffle].
     */
    fun reset()

    /**
     * Stoppt die aktuelle Wiedergabe, setzt aber weder die Queue noch die Einstellungen zurück.
     */
    fun stop()

    /**
     * Versucht unter dem angegebenen Text eine Audiodatei zu lokalisieren und der Queue hinzuzufügen.
     * Dabei wird die folgende Reihenfolge verwendet:
     *  1. Ist der Text eine URL, die auf eine abspielbare Audiodatei verweist -> Gebe diese Audiodatei wieder
     *  2. Suche auf Youtube nach dem gegebenen Text. Falls diese Suche ein Ergebnis hat -> Gebe das erste Ergebnis dieser Suche wieder.
     *  3. Das Ergebnis [EPlayTrackResult.NOT_FOUND][de.menkalian.pisces.audio.data.EPlayTrackResult.NOT_FOUND] wird zurückgegeben und die aktuelle Wiedergabe oder Queue wird **nicht verändert**
     *
     *  @param searchterm  Text, der zur Lokalisierung des Audiotracks verwendet wird
     *  @param playInstant Flag, ob die aktuelle Queue ignoriert werden soll (d.h. der gefundene Track wird sofort abgespielt, die aktuelle Wiedergabe wird **abgebrochen**)
     *  @param interruptCurrent Falls dieser Wert `true` ist, wird die Wiedergabe des aktuellen Tracks unterbrochen und stattdessen der angegebene Track gespielt.
     *                          **NACHDEM** der Track fertig gespielt hat wird die aktuelle Wiedergabe **fortgesetzt**.
     *  @param playFullPlaylist Falls der gefundene Track (bzw. die angegebene URL) teil einer Playlist ist, wird diese Playlist vollständig zur Queue hinzugefügt.
     *
     *  @return Das passende Enum-Literal von [EPlayTrackResult][de.menkalian.pisces.audio.data.EPlayTrackResult] und eine Liste mit allen Tracks, die zur aktuellen Queue hinzugefügt wurden.
     */
    fun playTrack(
        searchterm: String,
        playInstant: Boolean = false,
        interruptCurrent: Boolean = false,
        playFullPlaylist: Boolean = false
    ): QueueResult

    /**
     * Versucht unter dem angegebenen Text eine Audiodatei zu lokalisieren und Informationen dazu zurückzugeben.
     * Das Vorgehen ist hierbei prinzipiell identisch zu [IGuildAudioController.playTrack], jedoch wird die Queue **niemals** verändert.
     *
     *  @param searchterm   Text, der zur Lokalisierung des Audiotracks verwendet wird
     *  @param enableSearch Aktiviert die Youtube-Suche.
     *  @param results      Anzahl der Such-Ergebnisse, die zurückgegeben werden sollen (nur relevant, falls die Suche aktiviert ist)
     *
     *  @return Das passende Enum-Literal von [EPlayTrackResult][de.menkalian.pisces.audio.data.EPlayTrackResult] und eine Liste mit allen Tracks, die gefunden wurden.
     */
    fun lookupTracks(
        searchterm: String,
        enableSearch: Boolean = true,
        results: Int = 1
    ): QueueResult

    /**
     * Beendet die aktuelle Wiedergabe und überspringt evtl. weitere Tracks in der Queue.
     *
     * @param requeue Ob die übersprungenen/entfernten Tracks wieder an das ende der Queue eingefügt werden sollen
     * @param skipAmount Die Anzahl an Tracks, die übersprungen werden sollen. `1` bedeutet, dass nur der Track, der aktuell gespielt wird, übersprungen wird.
     *
     * @return Die Informationen der übersprungenen Tracks.
     *         Falls die Liste leer ist, konnte der Track nicht übersprungen werden.
     *         In diesem Fall spielt wahrscheinlich aktuell kein Track.
     */
    fun skipTracks(requeue: Boolean = false, skipAmount: Int = 1): List<TrackInfo>

    /**
     * Spult den aktuellen Audiotrack vorwärts oder rückwärts um die angegebene Länge.
     *
     * @param deltaMs Um wie viel die aktuelle Position des Tracks verändert werden soll.
     *                Ein positiver Wert bewirkt das Vorspulen des Tracks; ein negativer Wert spult zurück.
     *
     * @return Die Informationen des aktuellen Tracks **nachdem** die Methode ausgeführt wurde oder `null` falls aktuell kein Track spielt
     */
    fun windCurrentTrack(deltaMs: Long): TrackInfo?

    /**
     * Löscht den Track am angegebenen Index aus der Queue.
     *
     * @param index Index des Tracks, der entfernt werden soll.
     *              Ein Index `0` bedeutet, dass der Track entfernt wird, der als nächstes abgespielt worden wäre.
     *
     * @return Die Informationen des entfernten Tracks oder `null`, falls kein Track mit diesem Index existiert
     */
    fun deleteFromQueue(index: Int): TrackInfo?

    /**
     * Löscht die aktuelle Queue.
     */
    fun clearQueue()

    /**
     * Ordnet die aktuellen Einträge der Queue zufällig neu an.
     */
    fun shuffleQueue()

    /**
     * Stellt die Informationen des aktuell spielenden Tracks bereit.
     *
     * @return Die Informationen des aktuellen Tracks oder `null`, falls kein Track spielt.
     */
    fun getCurrentTrackInfo(): TrackInfo?

    /**
     * Stellt die Informationen aller Tracks in der Queue bereit.
     *
     * @return Eine unveränderliche Liste mit Informationen zu allen Tracks, die aktuell in der Queue sind.
     */
    fun getQueueInfo(): List<TrackInfo>

    /**
     * Ändert den Pausenstatus des Audioplayers (Pausiert/Nicht pausiert).
     * Die Werte werden als Boolean folgendermaßen behandelt:
     *   * `true`: pausiert
     *   * `false`: nicht pausiert
     *
     * @param changeValue Falls dieser Parameter `true` ist, wird die Pausierung des Players geändert.
     *
     * @return Der Status **nach** der Ausführung der Methode.
     */
    fun togglePause(changeValue: Boolean = true): Boolean

    /**
     * Ändert den Wiederholungsstatus.
     * Die Werte werden als Boolean folgendermaßen behandelt:
     *   * `true`: beendete Tracks werden wieder neu zur Queue hinzugefügt
     *   * `false`: beendete Tracks werden verworfen
     *
     * @param changeValue Falls dieser Parameter `true` ist, wird der Status der Wiederholung geändert.
     *
     * @return Der Status **nach** der Ausführung der Methode.
     */
    fun toggleRepeat(changeValue: Boolean = true): Boolean

    /**
     * Ändert den Zufallsmodus des Audioplayers.
     * Die Werte werden als Boolean folgendermaßen behandelt:
     *   * `true`: der nächste Track wird zufällig aus der Queue ausgewählt
     *   * `false`: der nächste Track wird von der ersten Position der Liste genommen.
     *
     * @param changeValue Falls dieser Parameter `true` ist, wird der Status des Zufallsmodus geändert.
     *
     * @return Der Status **nach** der Ausführung der Methode.
     */
    fun togglePermanentShuffle(changeValue: Boolean = true): Boolean
}