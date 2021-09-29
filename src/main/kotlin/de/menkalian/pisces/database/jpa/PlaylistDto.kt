package de.menkalian.pisces.database.jpa

import org.hibernate.Hibernate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToMany
import javax.persistence.Table
import javax.persistence.UniqueConstraint

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
    val name: String,
    @ManyToMany val songs: MutableList<SongEntryDto> = mutableListOf()
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