package de.menkalian.pisces.database.jpa

import org.springframework.data.repository.CrudRepository

interface SettingsRepository: CrudRepository<SettingsDto, Long> {
    fun deleteAllByGuildId(long: Long)
    fun getFirstByGuildIdInAndKeyIsOrderByGuildIdDesc(ids: List<Long>, key: String): SettingsDto?
}