package de.menkalian.pisces.database.jpa

import org.springframework.data.repository.CrudRepository

/**
 * JPA-Repository zum Zugriff auf [JoinSoundDto]-Datensätze.
 * Diese Schnittstelle wird automatisch von Spring implementiert und als Bean bereitgestellt.
 */
interface JoinSoundRepository : CrudRepository<JoinSoundDto, Long>