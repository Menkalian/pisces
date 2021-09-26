package de.menkalian.pisces.database.jpa

import org.hibernate.Hibernate
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.UniqueConstraint

/**
 * Stellt einen Datensatz zur Speicherung eines alternativen Namens für ein Command dar.
 * Diese Abkürzung kann spezifisch für einen Server sein, aber auch allgemein.
 *
 * @property id Automatisch generierte ID des Datensatzes
 * @property guildId Discord-ID des Servers (`0`, falls die Namensvariante allgemein gültig ist)
 * @property alias Alias/Abkürzender Name des Commands
 * @property original Originaler/Vollständiger Name des Commands
 */
@Table(
    name = "ALIAS", uniqueConstraints = [
        UniqueConstraint(name = "UNIQUE_TBL_ALIAS_COL_GUILDID_ALIAS", columnNames = ["GUILD_ID", "ALIAS"])
    ]
)
@Entity
data class AliasDto(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) var id: Long = -1,
    val guildId: Long,
    val alias: String,
    val original: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as AliasDto

        return id == other.id
    }

    override fun hashCode(): Int = 0

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id , guildId = $guildId , alias = $alias , original = $original )"
    }
}
