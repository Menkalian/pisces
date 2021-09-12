package de.menkalian.pisces.database

import de.menkalian.pisces.database.jpa.AliasDto
import de.menkalian.pisces.database.jpa.AliasRepository
import de.menkalian.pisces.util.CommonHandlerImpl
import org.springframework.stereotype.Service

@Service
class JpaDatabaseHandler(val aliasRepo: AliasRepository) : IDatabaseHandler, CommonHandlerImpl() {

    override fun addCommandShortcut(guildId: Long, alias: String, original: String) {
        val origResolved = aliasRepo.getFirstAliasDtoByGuildIdNotInAndAliasIsOrderByGuildIdDesc(listOf(0L, guildId), original)?.original ?: original
        aliasRepo.save(AliasDto(guildId = guildId, alias = alias, original = origResolved))
    }

    override fun getFormalCommandName(guildId: Long, alias: String): String {
        return aliasRepo.getFirstAliasDtoByGuildIdNotInAndAliasIsOrderByGuildIdDesc(listOf(0L, guildId), alias)?.original ?: alias
    }

    override fun getSettingsValue(guildId: Long, variable: String, default: String): String {
        TODO("Not yet implemented")
    }

    override fun setSettingsValue(guildId: Long, variable: String, value: String) {
        TODO("Not yet implemented")
    }

    override fun initialize() {
        // Clear generated
        aliasRepo.deleteAliasDtoByGuildId(0L)

        finishInitialization()
    }

    override fun deinitialize() {
        startDeinitialization()
    }
}