package de.menkalian.pisces.database.jpa

import org.hibernate.Hibernate
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToMany
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Table(name = "playlist", uniqueConstraints = [
    UniqueConstraint(name = "uc_playlistdto_guildid_name", columnNames = ["guildId", "name"])
])
@Entity
data class PlaylistDto(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) var id: Long = -1,
    val guildId: Long,
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