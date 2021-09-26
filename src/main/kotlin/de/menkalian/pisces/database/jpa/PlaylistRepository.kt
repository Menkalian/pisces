package de.menkalian.pisces.database.jpa

import org.springframework.data.repository.CrudRepository

/**
 * JPA-Repository zum Zugriff auf [PlaylistDto]-Datensätze.
 * Diese Schnittstelle wird automatisch von Spring implementiert und als Bean bereitgestellt.
 */
interface PlaylistRepository : CrudRepository<PlaylistDto, Long> {
    /**
     * Findet das gesuchte [PlaylistDto]-Objekt anhand der [Server-ID][PlaylistDto.guildId] und des [Namens][PlaylistDto.name]
     *
     * @param guildId Server-ID nach der gefiltert werden soll.
     * @param name Name der gesuchten Playlist.
     * @return Das gefundene Objekt, falls dieses existiert. Sonst `null`
     */
    fun findByGuildIdAndName(guildId: Long, name: String): PlaylistDto?
}