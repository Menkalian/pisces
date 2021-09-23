package de.menkalian.pisces.database.jpa

import org.springframework.data.repository.CrudRepository

interface AliasRepository : CrudRepository<AliasDto, Long> {
    fun deleteAllByGuildId(long: Long)
    fun getFirstByGuildIdInAndAliasIsOrderByGuildIdDesc(ids: List<Long>, alias: String): AliasDto?
}