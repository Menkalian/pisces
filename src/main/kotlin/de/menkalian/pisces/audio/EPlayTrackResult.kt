package de.menkalian.pisces.audio

/**
 * Das Ergebnis des Ladevorgangs für einen Track.
 * Wird in Form eines [QueueResult][de.menkalian.pisces.util.QueueResult] gemeinsam mit der Liste der geladenen [TrackInfos][TrackInfo] zurückgegeben.
 *
 * @property TRACK_URL           Ein **einzelner** Track wurde unter der angegebenen URL gefunden und erfolgreich geladen.
 * @property TRACK_SEARCH        Ein **einzelner** Track wurde ausgewählt und erfolgreich geladen, nachdem der angegebene Text auf Youtube gesucht wurde.
 * @property PLAYLIST            **Mehrere** Tracks wurden in einer Playlist gefunden und erfolgreich geladen.
 * @property TRACK_FROM_PLAYLIST Ein **einzelner** Track wurde unter der angegebenen URL gefunden und erfolgreich geladen. Der Track ist Teil einer Playlist, aber nur dieser eine wurde geladen.
 * @property NOT_FOUND           Der angegebene Text ist keine URL für einen Track und konnte auch in der Youtube-Suche kein Ergebnis produzieren.
 * @property ERROR               Beim Laden des Tracks ist ein Fehler aufgetreten.
 */
enum class EPlayTrackResult {
    TRACK_URL,
    TRACK_SEARCH,
    PLAYLIST,
    TRACK_FROM_PLAYLIST,
    NOT_FOUND,
    ERROR
}