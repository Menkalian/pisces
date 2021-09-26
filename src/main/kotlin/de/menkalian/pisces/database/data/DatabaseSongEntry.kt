package de.menkalian.pisces.database.data

import de.menkalian.pisces.database.jpa.SongEntryDto

/**
 * Informationen zu einem Song, die aus der Datenbank gelesen wurden.
 * Dies ist keine vollständige Repräsentation einer [TrackInfo][de.menkalian.pisces.audio.data.TrackInfo].
 *
 * @property title Titel/Name des gespeicherten Song-Entries
 * @property url Url über die der Song gefunden werden kann.
 * @property duration Dauer des Songs. Kann verwendet werden um die Laufzeit einer Playlist zu bestimmen.
 */
data class DatabaseSongEntry(val title: String, val url: String, val duration: Long) {
    constructor(songEntryDto: SongEntryDto) : this(songEntryDto.name, songEntryDto.url, songEntryDto.duration)
}
