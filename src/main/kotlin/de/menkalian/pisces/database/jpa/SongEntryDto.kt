package de.menkalian.pisces.database.jpa

import de.menkalian.pisces.util.toDurationString
import org.hibernate.Hibernate
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToMany
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Table(name = "song", uniqueConstraints = [
    UniqueConstraint(name = "uc_songentrydto_url_name", columnNames = ["url", "name"])
])
@Entity
data class SongEntryDto(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) var id: Long = -1,
    val url: String,
    val name: String,
    val duration: Long,
    @ManyToMany val playlists: MutableList<PlaylistDto> = mutableListOf()
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