package de.menkalian.pisces.database.jpa

import de.menkalian.pisces.util.toDurationString
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.Hibernate

/**
 * Stellt einen Datensatz zur Speicherung von Songinformationen dar.
 * Diese Informationen können sowohl für Join-Sounds, als auch für Playlists verwendet werden.
 * Die Kombination aus [url] und [name] muss eindeutig sein.
 *
 * @property id Automatisch generierte ID des Datensatzes
 * @property url Url über die der Song gefunden werden kann.
 * @property name Titel/Name des gespeicherten Song-Entries
 * @property duration Dauer des Songs. Kann verwendet werden um die Laufzeit einer Playlist zu bestimmen.
 * @property playlists [PlaylistDto]s, die diesen Song referenzieren
 * @property joinSounds [JoinSoundDto]s, die diesen Song referenzieren
 */
@Table(
    name = "SONG", uniqueConstraints = [
        UniqueConstraint(name = "UNIQUE_TBL_SONG_COL_URL_NAME", columnNames = ["URL", "NAME"])
    ]
)
@Entity
data class SongEntryDto(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) var id: Long = -1,
    val url: String,
    val name: String,
    val duration: Long,
    @ManyToMany val playlists: MutableList<PlaylistDto> = mutableListOf(),
    @OneToMany val joinSounds: MutableList<JoinSoundDto> = mutableListOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as SongEntryDto

        return id == other.id
    }

    override fun hashCode(): Int = 0

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id , url = $url , name = $name , duration = ${duration.toDurationString()} )"
    }
}