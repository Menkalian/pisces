package de.menkalian.pisces.database.jpa

import org.springframework.data.repository.CrudRepository

interface PlaylistRepository : CrudRepository<PlaylistDto, Long> {
    fun findByGuildIdAndName(guildId: Long, name: String) : PlaylistDto?
}