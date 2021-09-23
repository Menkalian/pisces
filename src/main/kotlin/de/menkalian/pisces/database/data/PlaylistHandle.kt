package de.menkalian.pisces.database.data

import de.menkalian.pisces.database.IDatabaseHandler

class PlaylistHandle internal constructor(
    val databaseHandler: IDatabaseHandler,
    val name: String,
    val guildId: Long
) {

}
