package de.menkalian.pisces.database.jpa

import org.springframework.data.repository.CrudRepository
import javax.transaction.Transactional

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
    @Transactional
    fun deleteAllByGuildId(guildId: Long)

    /**
     * Sucht den Datensatz mit genau dieser Kombination aus ID und Alternativname (diese Kombination ist unique).
     *
     * @param id Server-ID des Datensatzes
     * @param name Variablenname der Einstellung
     * @return Der Datensatz, falls dieser existiert
     */
    fun findByGuildIdAndVariableName(id: Long, name: String): SettingsDto?

    /**
     * Sucht den ersten Datensatz, der eine der angegebenen [Server-IDs][SettingsDto.guildId] und den [Schlüssel][SettingsDto.variableName] enthält.
     * Die Ergebnisse werden nach den Server-IDs sortiert, also wird bei mehreren Ergebnissen, das mit der höchsten Server-ID zurückgegeben.
     *
     * @param ids Server-IDs nach denen gefiltert werden soll.
     * @param name Variablenname, nach dem gefiltert werden soll.
     * @return Das gefundene Objekt, falls dieses existiert. Sonst `null`.
     */
    fun getFirstByGuildIdInAndVariableNameIsOrderByGuildIdDesc(ids: List<Long>, name: String): SettingsDto?
}