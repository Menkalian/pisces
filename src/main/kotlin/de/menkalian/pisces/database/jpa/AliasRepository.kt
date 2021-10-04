package de.menkalian.pisces.database.jpa

import org.springframework.data.repository.CrudRepository
import javax.transaction.Transactional

/**
 * JPA-Repository zum Zugriff auf [AliasDto]-Datensätze.
 * Diese Schnittstelle wird automatisch von Spring implementiert und als Bean bereitgestellt.
 */
interface AliasRepository : CrudRepository<AliasDto, Long> {

    /**
     * Löscht alle Aliase abhängig von der hinterlegten [Server-ID][AliasDto.guildId].
     *
     * @param guildId Server-ID nach der gefiltert werden soll.
     */
    @Transactional
    fun deleteAllByGuildId(guildId: Long)

    /**
     * Sucht den Datensatz mit genau dieser Kombination aus ID und Alternativname (diese Kombination ist unique).
     *
     * @param id Server-ID des Datensatzes
     * @param alias Eingetragenener Alternativname
     * @return Der Datensatz, falls dieser existiert
     */
    fun findByGuildIdAndAlias(id: Long, alias: String): AliasDto?

    /**
     * Sucht den ersten Datensatz, der eine der angegebenen [Server-IDs][AliasDto.guildId] und das [Alias][AliasDto.alias] enthält.
     * Die Ergebnisse werden nach den Server-IDs sortiert, also wird bei mehreren Ergebnissen, das mit der höchsten Server-ID zurückgegeben.
     *
     * @param ids Server-IDs nach denen gefiltert werden soll.
     * @param alias Alias, nach dem gefiltert werden soll.
     * @return Das gefundene Objekt, falls dieses existiert. Sonst `null`.
     */
    fun getFirstByGuildIdInAndAliasIsOrderByGuildIdDesc(ids: List<Long>, alias: String): AliasDto?
}