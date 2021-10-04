package de.menkalian.pisces.command

import de.menkalian.pisces.OnConfigValueCondition
import de.menkalian.pisces.RequiresKey
import de.menkalian.pisces.database.IDatabaseHandler
import de.menkalian.pisces.util.CommonHandlerImpl
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

@Suppress("RedundantModalityModifier")
@Component
@Conditional(OnConfigValueCondition::class)
@RequiresKey(["pisces.command.Handler.DefaultCommandHandler"])
class DefaultCommandHandler(
    final override val commands: List<CommonCommandBase>,
    val databaseHandler: IDatabaseHandler
) : ICommandHandler,
    CommonHandlerImpl() {
    override val commandsByName: Map<String, CommonCommandBase> = commands.associateBy { it.name.lowercase() }

    override fun initialize() {
        commands.forEach { it.initialize() }
        finishInitialization()
    }

    override fun deinitialize() {
        startDeinitialization()
    }

    override fun getCommand(name: String, guildId: Long): ICommand? {
        val origName = databaseHandler.getFormalCommandName(guildId, name).lowercase()
        return commandsByName[origName]
    }

    private val prefixChangedListeners = mutableListOf<ICommandHandler.IGuildPrefixChangedListener>()
    override fun fireGuildPrefixChanged(guildId: Long, newPrefix: String) {
        prefixChangedListeners
            .parallelStream()
            .forEach { it.onPrefixChanged(guildId, newPrefix) }
    }

    override fun addGuildPrefixChangedListener(listener: ICommandHandler.IGuildPrefixChangedListener) {
        prefixChangedListeners.add(listener)
    }

    override fun removeGuildPrefixChangedListener(listener: ICommandHandler.IGuildPrefixChangedListener) {
        prefixChangedListeners.remove(listener)
    }
}