package de.menkalian.pisces.database.jpa

import org.hibernate.Hibernate
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Table(name = "settings_dto", uniqueConstraints = [
    UniqueConstraint(name = "uc_aliasdto_guildid_alias", columnNames = ["guildId", "alias"])
])
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
