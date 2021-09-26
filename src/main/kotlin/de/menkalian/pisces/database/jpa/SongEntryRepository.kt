package de.menkalian.pisces.database.jpa

import org.springframework.data.repository.CrudRepository

/**
 * JPA-Repository zum Zugriff auf [SongEntryDto]-Datensätze.
 * Diese Schnittstelle wird automatisch von Spring implementiert und als Bean bereitgestellt.
 */
interface SongEntryRepository : CrudRepository<SongEntryDto, Long> {
    /**
     * Findet das gesuchte [SongEntryDto]-Objekt anhand der [URL][SongEntryDto.url].
     *
     * @param url URL des gesuchten Objekts
     * @return Das gefundene Objekt, falls dieses existiert. Sonst `null`.
     */
    fun findByUrl(url: String): SongEntryDto?
}