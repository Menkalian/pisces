package de.menkalian.pisces.database.data

import de.menkalian.pisces.database.jpa.SongEntryDto

data class DatabaseSongEntry(val title: String, val url: String, val duration: Long) {
    constructor(songEntryDto: SongEntryDto) : this(songEntryDto.name, songEntryDto.url, songEntryDto.duration)
}
