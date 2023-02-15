package de.menkalian.pisces.database.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.Hibernate

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
    @Id @GeneratedValue(strategy = GenerationType.AUTO) var id: Long = -1,
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
