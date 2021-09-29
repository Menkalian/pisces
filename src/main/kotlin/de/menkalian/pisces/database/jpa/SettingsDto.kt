package de.menkalian.pisces.database.jpa

import org.hibernate.Hibernate
import javax.persistence.Column
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
 * @property variableName Schlüssel des Einstellungsparameters
 * @property value Wert des Einstellungsparameters
 */
@Table(
    name = "SETTINGS", uniqueConstraints = [
        UniqueConstraint(name = "UNIQUE_TBL_SETTINGS_COL_GUILDID_VARIABLENAME", columnNames = ["GUILD_ID", "VARIABLE_NAME"])
    ]
)
@Entity
data class SettingsDto(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Long = -1,
    @Column(name = "GUILD_ID") val guildId: Long,
    @Column(name = "VARIABLE_NAME") val variableName: String,
    var value: String
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
        return this::class.simpleName + "(id = $id , guildId = $guildId , key = $variableName , value = $value )"
    }
}
