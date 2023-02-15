package de.menkalian.pisces.database.jpa

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.Hibernate

/**
 * Stellt einen Datensatz zur Speicherung eines individuellen Joinsounds für einen Nutzer dar.
 *
 * @property userId Discord-ID des Nutzers. Wird als Primärschlüssel genutzt und muss daher eindeutig sein.
 * @property song Gespeicherte Song-Instanz die für diesen Nutzer verwendet werden soll.
 */
@Table(name = "joinsound")
@Entity
data class JoinSoundDto(
    @Id val userId: Long,
    @ManyToOne(fetch = FetchType.EAGER) var song: SongEntryDto
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as JoinSoundDto

        return userId == other.userId
    }

    override fun hashCode(): Int = 0

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(userId = $userId , song = $song )"
    }
}