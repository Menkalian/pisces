package de.menkalian.pisces.database.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.Hibernate

/**
 * Stellt einen Datensatz zur Speicherung einer Playlist dar.
 * Diese Playlist, sowie der zugehörige Name, ist immer spezifisch für einen Discord-Server.
 *
 * @property id Automatisch generierte ID des Datensatzes
 * @property guildId Discord-ID des Servers
 * @property name Name der Playlist (eindeutig pro Server)
 * @property songs Liste der enthaltenen Songs/Lieder
 */
@Table(
    name = "PLAYLIST", uniqueConstraints = [
        UniqueConstraint(name = "UNIQUE_TBL_PLAYLIST_COL_GUILDID_NAME", columnNames = ["GUILD_ID", "NAME"])
    ]
)
@Entity
data class PlaylistDto(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) var id: Long = -1,
    @Column(name = "GUILD_ID") val guildId: Long,
    var name: String,
    @ManyToMany(fetch = FetchType.EAGER) val songs: MutableList<SongEntryDto> = mutableListOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as PlaylistDto

        return id == other.id
    }

    override fun hashCode(): Int = 0

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id , guildId = $guildId , name = $name )"
    }
}