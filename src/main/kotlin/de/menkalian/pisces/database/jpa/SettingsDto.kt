package de.menkalian.pisces.database.jpa

import org.hibernate.Hibernate
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.UniqueConstraint

/**
 * Stellt einen Datensatz zur Speicherung einer Einstellung dar.
 * Diese Einstellung kann spezifisch für einen Server sein, aber auch allgemein.
 *
 * @property id Automatisch generierte ID des Datensatzes
 * @property guildId Discord-ID des Servers (`0`, falls die Einstellung allgemein gültig ist)
 * @property key Schlüssel des Einstellungsparameters
 * @property value Wert des Einstellungsparameters
 */
@Table(
    name = "SETTINGS", uniqueConstraints = [
        UniqueConstraint(name = "UNIQUE_TBL_SETTINGS_COL_GUILDID_KEY", columnNames = ["GUILD_ID", "KEY"])
    ]
)
@Entity
data class SettingsDto(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Long = -1,
    val guildId: Long,
    val key: String,
    val value: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as SettingsDto

        return id == other.id
    }

    override fun hashCode(): Int = 0

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id , guildId = $guildId , key = $key , value = $value )"
    }

}