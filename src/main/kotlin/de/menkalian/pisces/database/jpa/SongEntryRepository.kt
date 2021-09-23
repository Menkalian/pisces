package de.menkalian.pisces.database.jpa

import org.springframework.data.repository.CrudRepository

interface SongEntryRepository : CrudRepository<SongEntryDto, Long> {
    fun findByUrl(url: String): SongEntryDto?
}