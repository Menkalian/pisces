package de.menkalian.pisces.database.jpa

import org.springframework.data.repository.CrudRepository

interface AliasRepository : CrudRepository<AliasDto, Long> {
    fun deleteAliasDtoByGuildId(long: Long)
    fun getFirstAliasDtoByGuildIdNotInAndAliasIsOrderByGuildIdDesc(ids: List<Long>, alias: String): AliasDto?
}