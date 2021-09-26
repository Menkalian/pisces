package de.menkalian.pisces.database.jpa

import org.springframework.data.repository.CrudRepository

/**
 * JPA-Repository zum Zugriff auf [SettingsDto]-Datensätze.
 * Diese Schnittstelle wird automatisch von Spring implementiert und als Bean bereitgestellt.
 */
interface SettingsRepository : CrudRepository<SettingsDto, Long> {

    /**
     * Löscht alle Einstellungen abhängig von der gespeicherten [Server-ID][SettingsDto.guildId].
     *
     * @param guildId Server-ID nach der gefiltert werden soll.
     */
    fun deleteAllByGuildId(guildId: Long)

    /**
     * Sucht den ersten Datensatz, der eine der angegebenen [Server-IDs][SettingsDto.guildId] und den [Schlüssel][SettingsDto.key] enthält.
     * Die Ergebnisse werden nach den Server-IDs sortiert, also wird bei mehreren Ergebnissen, das mit der höchsten Server-ID zurückgegeben.
     *
     * @param ids Server-IDs nach denen gefiltert werden soll.
     * @param key Schlüssel, nach dem gefiltert werden soll.
     * @return Das gefundene Objekt, falls dieses existiert. Sonst `null`.
     */
    fun getFirstByGuildIdInAndKeyIsOrderByGuildIdDesc(ids: List<Long>, key: String): SettingsDto?
}